package br.ufrj.dcc.world;

import java.util.Random;

import br.ufrj.dcc.ant.Ant;

public class World {

	private WorldMap worldMap;
	private Pheromone pheromone;
	private Ant[] ants;
	private Random rand;
	private int rankSize = 10;
	private Ant bestSoFar;
	
	public World(WorldMap worldMap) {
		this.worldMap = worldMap;
		ants = new Ant[worldMap.getSize()];
		this.rand = new Random();

		for (int i = 0; i < ants.length; i++) {
			ants[i] = new Ant(i);
		}
	}

	public World(WorldMap worldMap, long seed) {
		this.worldMap = worldMap;
		ants = new Ant[worldMap.getSize()];
		this.rand = new Random(seed);

		for (int i = 0; i < ants.length; i++) {
			ants[i] = new Ant(i);
		}
	}
	
	public WorldMap getWorldMap() {
		return this.worldMap;
	}

	public void createPheromoneMatrix(double initialValue) {

		if (this.worldMap.getSize() == 0 || this.worldMap == null) {
			throw new IllegalArgumentException("WorldMap has not been initiated.");
		}
		this.pheromone = new Pheromone(initialValue, this.worldMap.getSize());
	}

	public long[] greedySearch(WorldMap worldMap) {
		int[] indexes = new int[worldMap.getSize()];
		long[] results = new long[worldMap.getSize()];
		int i = 1;
		indexes[0] = 1;

		for (int k = 0; k < worldMap.getSize() - 1; k++) {
			results[k] = Long.MAX_VALUE;

			for (int j = 1; j <= worldMap.getSize(); j++) {
				long value = worldMap.getDistance(i, j);

				if (value == 0) {
					continue;
				}

				if (results[k] > value && !containsValue(indexes, j)) {
					results[k] = value;
					indexes[k + 1] = j;
				}
			}
			i = indexes[k + 1];

			if (k == worldMap.getSize() - 2) {
				results[k + 1] = worldMap.getDistance(i, 1);
			}
		}
		return results;
	}

	public long greedyPathSize() {
		int[] indexes = new int[this.worldMap.getSize()];
		long[] results = new long[this.worldMap.getSize()];
		long result = 0;
		int i = 1;
		indexes[0] = 1;

		for (int k = 0; k < this.worldMap.getSize() - 1; k++) {
			results[k] = Long.MAX_VALUE;

			for (int j = 1; j <= this.worldMap.getSize(); j++) {
				long value = this.worldMap.getDistance(i, j);

				if (value == 0) {
					continue;
				}

				if (results[k] > value && !containsValue(indexes, j)) {
					results[k] = value;
					indexes[k + 1] = j;
				}
			}
			result += results[k];
			i = indexes[k + 1];

			if (k == this.worldMap.getSize() - 2) {
				results[k + 1] = this.worldMap.getDistance(i, 1);
				result += this.worldMap.getDistance(i, 1);
			}
		}
		return result;
	}

	public boolean containsValue(int[] array, int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				return true;
			}
		}
		return false;
	}

	public void run() {
		tourConstruction();
		updatePheromone();
	}

	private void updatePheromone() {
		// TODO Auto-generated method stub
	}

	private void tourConstruction() {
		for (int i = 0; i < worldMap.getSize(); i++) {
			for (int j = 0; j < ants.length; j++) {
				long distances[] = worldMap.getDistances(ants[j].getCurrentNode());
				double pheromones[] = pheromone.getPheromones(j);
				double sample = rand.nextDouble();
				long v = ants[j].move(distances, pheromones, sample);		
				if (v < 0){
					System.out.println("Deu merda");
				}
			}
		}
		
		for (int j = 0; j < ants.length; j++) {
			int homeNode = ants[j].getHomeNode();
			int currentNode = ants[j].getCurrentNode();
			long distance = worldMap.getDistance(currentNode, homeNode);
			ants[j].moveHomeNode(distance);			
		}
		
		for (int i = 0; i < ants.length; i++) {
			if (bestSoFar == null || bestSoFar.getPathLength() > ants[i].getPathLength()){
				bestSoFar = ants[i];
			}
		}
		
	}
}
