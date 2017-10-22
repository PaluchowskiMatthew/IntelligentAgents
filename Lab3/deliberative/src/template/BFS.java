package template;

import java.util.ArrayList;
import java.util.List;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import template.State;

public class BFS {
	static List<State> Q = new ArrayList<State>();
	static List<State> C = new ArrayList<State>();
	static int agentCapacity;

	public static Plan createPlan(Vehicle vehicle, TaskSet tasks, int capacity) {
		agentCapacity = capacity;
		State finalState = BFSAlgorithm(vehicle, tasks);
		Plan plan = finalState.plan;
		return plan;
	}

	public static State BFSAlgorithm(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);
		
		State initialState = new State(vehicle.getCurrentCity(), vehicle.getCurrentTasks(), tasks, plan); // n
		Q.add(initialState);

		State finalState = null;
		do {
			List<State> S = null;
			if (Q.isEmpty()) {
				System.out.println("BFS Error. Final State not found.");
				break;
			}
			 State n = Q.remove(0);
			 if(n.isFinalState()) {
				 finalState = n;
				 break;
			 }
			 if(!C.contains(n)) {
				 C.add(n);
				 S = getSuccessors(n);
			 }
			 Q.addAll(S);
			 
		} while (true);

		return finalState;
	}

	private static List<State> getSuccessors(State state) {
		List<State> succesors = new ArrayList<State>();
		TaskSet vehicleTasks = state.getVehicleTasks();
		TaskSet topologyTasks = state.getTopologyTasks();
		City current = state.getCurrentCity();
		Plan plan = state.getPlan();
		int weightInTheTrunk = vehicleTasks.weightSum();

		// Successor pickup state
		for (Task task : topologyTasks) {
			City taskCity = task.pickupCity;
			int taskWeight = task.weight;
			if ((weightInTheTrunk + taskWeight) < agentCapacity) {
				TaskSet newVehicleTasks = vehicleTasks.clone();
				newVehicleTasks.add(task);
				TaskSet newTopologyTasks = topologyTasks.clone();
				newTopologyTasks.remove(task);
				Plan newplan = plan;
				newplan.appendMove(taskCity);
				newplan.appendPickup(task);
				State nextState = new State(taskCity, newVehicleTasks, newTopologyTasks, newplan);
				succesors.add(nextState);
			}
		}

		// Successor deliver state
		for (Task task : vehicleTasks) {
			City taskCity = task.deliveryCity;
			TaskSet newVehicleTasks = vehicleTasks.clone();
			newVehicleTasks.remove(task);
			Plan newplan = plan;
			newplan.appendMove(taskCity);
			newplan.appendDelivery(task);
			State nextState = new State(taskCity, newVehicleTasks, topologyTasks, newplan);
			succesors.add(nextState);
		}
		return succesors;
	}

}
