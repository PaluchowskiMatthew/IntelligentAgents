package template;

import java.util.ArrayList;
import java.util.List;

import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

// our action is just a city class as in agent can go to any City

public class MarkovDecissionProcessModel {

	
	public static void learnOfflinePolicy(Topology topology, TaskDistribution td, Agent agent, Double discount) {
		
		/*
		 *  for s in all states:
		 *  		actions = getPossibleActions(s);
		 *  		for a in all actions:
		 *  			reward = calculateReward(s, a)
		 *  			
		 *  			transistion = calculateTransition(state
		 */

	}

	
	public List<City> getPossibleActions(State s){
		List<City> possibleActions = new ArrayList<City>();
		
		Vehicle stateVehicle = s.getCurrentVehicle();
		Task stateTask = s.getCurrentTask();
		
		if(stateTask != null) {
			possibleActions.add(stateTask.deliveryCity);
		}
		
		List<City> neighbors = stateVehicle.getCurrentCity().neighbors();
		possibleActions.addAll(neighbors);

		return possibleActions;
	}
	
	
	public long calculateReward(State s, City action) {
		long reward = 0l;

		Vehicle stateVehicle = s.getCurrentVehicle();
		City stateVehicleCurrentCity = stateVehicle.getCurrentCity();

		Task stateTask = s.getCurrentTask();

		long taskPenalty = (long) (stateVehicle.costPerKm()
				* stateVehicleCurrentCity.distanceTo(action));

		long taskReward = (stateTask != null) ? stateTask.reward : 0l;

		reward = taskReward - taskPenalty;

		return reward;
	}
	
	/*
	 * public long calculateReward(State s, City action) {
		long reward = 0l;

		Vehicle stateVehicle = s.getCurrentVehicle();
		City stateVehicleCurrentCity = stateVehicle.getCurrentCity();

		Task stateTask = s.getCurrentTask();

		long taskPenalty = (long) (stateVehicle.costPerKm()
				* stateVehicleCurrentCity.distanceTo(action));

		long taskReward = (stateTask != null) ? stateTask.reward : 0l;

		reward = taskReward - taskPenalty;

		return reward;
	}
	 */

}
