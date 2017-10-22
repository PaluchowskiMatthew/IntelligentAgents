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
		State finalState = findFinalState(vehicle, tasks);
		Plan plan = getPlanForFinalState(finalState);
		return plan;
	}

	public static State findFinalState(Vehicle vehicle, TaskSet tasks) {
		State initialState = new State(null, vehicle.getCurrentCity(), vehicle.getCurrentTasks(), tasks); // n
		Q.add(initialState);

		State finalState = null;
		do {
			if (Q.isEmpty()) {
				System.out.println("BFS Error. Final State not found.");
				break;
			}
			// State n = Q.

		} while (true);

		return initialState;
	}

	private static List<State> Succesor(State state) {
		List<State> succesors = new ArrayList<State>();
		TaskSet vehicleTasks = state.getVehicleTasks();
		TaskSet topologyTasks = state.getTopologyTasks();
		City current = state.getCurrentCity();
		State parentState = state.getParentState();
		int weightInTheTrunk = vehicleTasks.weightSum();

		// Succesor pickup state
		for (Task task : topologyTasks) {
			City taskCity = task.pickupCity;
			int taskWeight = task.weight;
			if ((weightInTheTrunk + taskWeight) < agentCapacity) {
				TaskSet newVehicleTasks = vehicleTasks.clone();
				newVehicleTasks.add(task);
				
				TaskSet newTopologyTasks = topologyTasks.clone();
				newTopologyTasks.remove(task);
				
				State nextState = new State(state, taskCity, newVehicleTasks, newTopologyTasks);
				succesors.add(nextState);
			}
		}

		// Succesor deliver state
		for (Task task : vehicleTasks) {
			City taskCity = task.deliveryCity;
			TaskSet newVehicleTasks = vehicleTasks.clone();
			newVehicleTasks.remove(task);
			State nextState = new State(state, taskCity, newVehicleTasks, topologyTasks);
			succesors.add(nextState);
		}
		return succesors;
	}

	public static Plan getPlanForFinalState(State finalState) {
		// Plan plan = new Plan(null, null);
		return null;
	}

}
