package br.dcc.ufrj.mmas.world;

import br.dcc.ufrj.antvrp.ant.Ant;
import br.dcc.ufrj.antvrp.util.Tour;
import br.dcc.ufrj.antvrp.world.Customer;
import br.dcc.ufrj.antvrp.world.World;
import br.dcc.ufrj.mmas.ant.MMASAnt;

public class MMASWorld extends World {

	private static double RO = 0.02;
	private double maxLimit = 0;
	private double minLimit = 0;
	private Tour iterationBest = null;
	private Tour iterationWorst = null;
	private boolean bestTourUpdateEnabled = false;
	private int count = 0;

	public void createWorld(String path) throws Exception {
		super.createWorld(path);
	}

	public MMASWorld() {
		super();
	}

	public MMASWorld(long seed) {
		super(seed);
	}

	@Override
	public void createAnts(int total) {
		this.ants = new Ant[total];
		for (int i = 0; i < total; i++) {
			this.ants[i] = new MMASAnt(i, this.getFirstDepot(), this.getCapacity(), this.getDimension());
		}
		this.setAntAmount(total);
	}

	@Override
	protected void tourConstruction() throws Exception {
		Customer currCustomer = null;
		Customer nextCustomer = null;
		this.iterationBest = null;
		this.iterationWorst = null;

		for (Ant ant : ants) {
			ant.resetTour();

			do {
				currCustomer = ant.getTour().getCurrentCustomer();
				nextCustomer = ant.chooseNextMove(currCustomer, this.getSampleDouble());
				if (nextCustomer != null) {
					nextCustomer = this.getCustomer(nextCustomer.getId());
					ant.walk(nextCustomer);
				}
			} while (nextCustomer != null);

			ant.walk(ant.getFirstCustomer());

			if (this.getWorstTour() == null || this.getWorstTour().getDistance() < ant.getTour().getDistance()) {
				this.setWorstTour(ant.getTour().clone());
			}
			
			if (this.getIterationWorst() == null || this.getIterationWorst().getDistance() < ant.getTour().getDistance()) {
				this.setIterationWorst(ant.getTour().clone());
			}

			ant.getTour().opt2();

			if (this.getBestTour() == null || this.getBestTour().getDistance() > ant.getTour().getDistance()) {
				this.setBestTour(ant.getTour().clone());
				this.maxLimit = 1 / (RO * this.getBestTour().getDistance());
				this.minLimit = this.maxLimit / 20;
			}

			if (this.iterationBest == null || this.iterationBest.getDistance() > ant.getTour().getDistance()) {
				this.setIterationBest(ant.getTour().clone());
			}
		}
	}

	@Override
	protected void pheromoneUpdate() {
		double pheromone = 0;

		for (Customer i : this.customers) {
			for (Customer j : i.getListCandidates()) {
				pheromone = Math.max(j.getPheromone() * (1 - RO), minLimit);
				j.setPheromone(pheromone);
			}
		}

		if (count % 5 == 0) {
			this.pheromoneUpdateBestTour(this.getIterationBest());
		} else {
			this.pheromoneUpdateBestTour(this.getBestTour());
		}
	}

	private void pheromoneUpdateBestTour(Tour tour) {
		Customer a = null;
		Customer b = null;
		for (int i = 0; i < tour.getSize(); i++) {
			b = tour.getCustomers()[i];
			if (a != null) {
				if (a.getId() != b.getId()) {
					this.addPheromone(a, b, 1 / tour.getDistance());
				}
			}
			a = b;
		}
	}
	
	public void pheromoneUpdateWorstTour(Tour tour) {
		Customer a = null;
		Customer b = null;
		double pheromone = 0;
		for (int i = 0; i < tour.getSize(); i++) {
			b = tour.getCustomers()[i];
			if (a != null) {
				if (a.getId() != b.getId()) {
					pheromone = Math.max(a.getPheromone() * (1 - RO), minLimit);
					a.getNeighbor(b.getId()).setPheromone(pheromone);
					b.getNeighbor(a.getId()).setPheromone(pheromone);
				}
			}
			a = b;
		}
	}


	@Override
	protected void addPheromone(Customer a, Customer b, double pheromone) {
		Customer na = a.getNeighbor(b.getId());
		na.setPheromone(Math.min(pheromone + na.getPheromone(), this.maxLimit));

		Customer nb = b.getNeighbor(a.getId());
		nb.setPheromone(Math.min(pheromone + nb.getPheromone(), this.maxLimit));
	}

	@Override
	public Tour getInitialTour() {
		int dimension = this.getDimension();
		Customer depot = this.depots[0];
		Customer customer = this.depots[0];
		Customer candidate = null;
		Tour tour = new Tour(customer, dimension);
		int currentCapacity = 0;
		int i = 0;

		do {
			candidate = customer.getListCandidates().get(i);
			candidate = this.getCustomer(candidate.getId());

			if (currentCapacity >= this.getCapacity()) {
				tour.add(depot);
				currentCapacity = 0;
				customer = depot;
			}
			if (!tour.contains(candidate)) {
				tour.add(candidate);
				customer = candidate;
				currentCapacity++;
				i = 0;
			} else {
				i++;
			}

		} while (i < customer.getListCandidates().size());

		tour.add(depot);
		tour.recalcDistance();
		return tour;
	}

	protected void computeHeuristics() {
		double heuristic = 0;

		for (Customer i : this.customers) {
			for (Customer j : i.getListCandidates()) {
				heuristic = 1 / i.getDistance(j);
				j.setHeuristic(heuristic);
			}
		}
	}

	public double getTourLength(String string) {
		double result = 0;
		String[] customers = string.replaceAll(" ", "").split(",");

		for (int i = 0; i < customers.length - 1; i++) {
			int a = Integer.parseInt(customers[i]);
			int b = Integer.parseInt(customers[i + 1]);
			result += this.getCustomer(a).getDistance(b);
		}

		return result;
	}

	@Override
	protected double getIntialValue(int antsAmount) {
		this.maxLimit = 1 / (RO * this.getInitialTour().getDistance());
		this.minLimit = this.maxLimit / 2;
		return this.maxLimit;
	}

	public boolean isBestTourEnabled() {
		return bestTourUpdateEnabled;
	}

	public void setBestTourEnabled(boolean bestTourEnabled) {
		this.bestTourUpdateEnabled = bestTourEnabled;
	}

	public Tour getIterationBest() {
		return iterationBest;
	}

	public void setIterationBest(Tour iterationBest) {
		this.iterationBest = iterationBest;
	}

	public void setIterationWorst(Tour iterationWorst) {
		this.iterationWorst = iterationWorst;
	}

	public Tour getIterationWorst() {
		return iterationWorst;
	}
}
