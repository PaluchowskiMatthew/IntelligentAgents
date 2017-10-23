package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Arrays;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;


public class MarkovDecissionProcessModel {

	private static HashMap<State, City>bestAction = new HashMap<State, City>();
	private static HashMap<State, Double>V = new HashMap<State, Double>();
	private static HashMap<State, Double>Vprev = new HashMap<State, Double>();
	private static HashMap<State, HashMap<City, Double>>Q = new HashMap<State, HashMap<City, Double>>();

	private static List<State> allStates = new ArrayList<State>();

	private static void generateAllStates(Topology topology) {
		List<City> cities = topology.cities();
		int allStatesCount = 0;
		
		for (City currCity : cities) {
			for(City destCity: cities) {
				if(currCity.id != destCity.id) {
					allStates.add(new State(currCity, destCity));
					allStatesCount++;
				}	
			}
			allStates.add(new State(currCity, null));
			allStatesCount++;
		}
		System.out.println("All states Ccount: " + allStatesCount);
	}

	private static void initializeMDP(){
		for(State currState: allStates) {
			bestAction.put(currState, null);
			V.put(currState, -100.0);
			Vprev.put(currState, -100.0);
		}
	}

	public static void learnOfflinePolicy(Topology topology, TaskDistribution td, Agent agent, Double discount) {
			
		generateAllStates(topology);
		initializeMDP();		
		
		Double changeOfV = 1000000.1;
		int numberOfIterations = 0;
		while(changeOfV > 1E-10) {
			for(State state: allStates) {
				List<City> actions = getPossibleAcctions(state);
				for(City action: actions) {
					double currentQ = 0.0;
					
					long reward = calculateReward(state, action, agent.vehicles().get(0), td);
					
					double transition = calculateTransitionProbability(state, action, td);
					
					currentQ = reward + discount * transition;
					
					HashMap<City, Double> innerQ = new HashMap<City, Double>();
					innerQ.put(action, currentQ);
					Q.put(state, innerQ);
				}
				double newVprev = V.get(state).doubleValue();
				Vprev.put(state, newVprev);
				
				HashMap<City, Double> QsForActions = Q.get(state);
				Double tmpBestQ = -100.0;
				City tmpBestAction = null;
				for(City qs: QsForActions.keySet()) {
					Double tmpQ = QsForActions.get(qs).doubleValue();
					if(tmpQ > tmpBestQ) {
						tmpBestQ = tmpQ;
						tmpBestAction = qs;
					}
				}
				V.put(state, tmpBestQ);
				bestAction.put(state, tmpBestAction);
			}
			changeOfV = changeOfV();
			numberOfIterations++;
		}
		System.out.println("changeOfV " + changeOfV);
		System.out.println("numberOfIterations " + numberOfIterations);
		System.out.println(Arrays.asList(bestAction));


	}
	
	public static City getBestAction(State s){
		return bestAction.get(s);
	}

	
	private static double calculateTransitionProbability(State state, City action, TaskDistribution td) {
		double t = 0.0;
		for(State statePrime: allStates) {
			City statePrimeCurrentCity = statePrime.getCurrentCity();
			City statePrimeDeliveryCity = statePrime.getDeliveryCity();
			
			if(statePrimeCurrentCity.equals(action)) {
				t += (td.probability(statePrimeCurrentCity, statePrimeDeliveryCity) * V.get(statePrime).doubleValue());
			}
		}
		return t;
	}

	private static List<City> getPossibleAcctions(State state) {
		City currentCity = state.getCurrentCity();
		City deliveryCity = state.getDeliveryCity(); // can be added twice if neighbor is delivery city and null?
		
		List<City> possibleActions = new ArrayList();
		possibleActions.addAll(currentCity.neighbors());
		
		if(deliveryCity != null && !possibleActions.contains(deliveryCity)) {
			possibleActions.add(deliveryCity);
		}
		
		return possibleActions;
	}

	private static double changeOfV() {
		double changeOfV = 0.0;
		
		for(State s : V.keySet()) {
			double diff =  V.get(s).doubleValue() - Vprev.get(s).doubleValue();
			changeOfV += diff;
		}
		
		return changeOfV;
	}

	private static long calculateReward(State s, City action, Vehicle v, TaskDistribution td) {
		long reward = 0l;

		City currentCity = s.getCurrentCity();
		City deliveryCity = s.getDeliveryCity();
				
		long taskPenalty = (long) (v.costPerKm() * currentCity.distanceTo(action));

		long taskReward = ((deliveryCity != null) && (deliveryCity.equals(action))) ? td.reward(currentCity, action) : 0l;

		reward = taskReward - taskPenalty;

		return reward;
	}

}
