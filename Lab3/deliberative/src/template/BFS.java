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
	List<State> Q = new ArrayList<State>();
	List<State> C = new ArrayList<State>();
	int agentCapacity;

	public Plan createPlan(Vehicle vehicle, TaskSet tasks) {
		agentCapacity = vehicle.capacity();

		State finalState = BFSAlgorithm(vehicle, tasks);
		Plan plan = finalState.plan;
		System.out.println(plan);
		return plan;
	}

	public State BFSAlgorithm(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		State initialState = new State(vehicle.getCurrentCity(), vehicle.getCurrentCity(),
				vehicle.getCurrentTasks(), tasks, plan); // n
		Q.add(initialState);

		State finalState = null;
		do {
			List<State> S = null;
			if (Q.isEmpty()) {
				System.out.println("BFS Error. Final State not found.");
				break;
			}
			State n = Q.remove(0);
			if (n.isFinalState()) {
				finalState = n;
				break;
			}
			if (!C.contains(n)) {
				C.add(n);
				S = getSuccessors(n);
			}

			Q.addAll(S);
			// Q=S;
		} while (true);

		return finalState;
	}

	private List<Task> pickupsInCity(City c, State s) {
		List<Task> pickups = new ArrayList<Task>();
		for (Task t : s.getTopologyTasks()) {
			if (t.pickupCity == c) {
				pickups.add(t);
			}
		}
		return pickups;
	}

	private List<Task> deliveriesForCity(City c, State s) {
		List<Task> deliveries = new ArrayList<Task>();
		for (Task t : s.getVehicleTasks()) {
			if (t.deliveryCity == c) {
				deliveries.add(t);
			}
		}
		return deliveries;
	}

	private List<State> getSuccessors(State state) {
		List<State> nextStates = new ArrayList<State>();

		TaskSet vehicleTasks = state.getVehicleTasks();
		TaskSet topologyTasks = state.getTopologyTasks();

		List<Task> newVehicleTasks = new ArrayList<Task>(vehicleTasks);
		// Successor pickup state
		for (Task task : topologyTasks) {
			State nextState = state.copyState();

			for (City city : state.getCurrentCity().pathTo(task.pickupCity)) {
				nextState.plan.appendMove(city);

				// IF there is a task to deliver on a way of task pickup then please do deliver
				for (Task deliveryOnAWay : deliveriesForCity(city, state)) {
					nextState.plan.appendDelivery(deliveryOnAWay);
					nextState.vehicleTasks.remove(deliveryOnAWay);
					newVehicleTasks.remove(deliveryOnAWay);
				}
				if (city != task.pickupCity) {
					for (Task pickupOnAWay : pickupsInCity(city, state)) {
						if (((nextState.vehicleTasks.weightSum() + task.weight
								+ pickupOnAWay.weight) < agentCapacity)) {
							nextState.plan.appendPickup(pickupOnAWay);
							nextState.vehicleTasks.add(pickupOnAWay);
							nextState.topologyTasks.remove(pickupOnAWay);
						}
					}
				}

			}
			nextState.currentCity = task.pickupCity;
			nextState.plan.appendPickup(task);
			nextState.topologyTasks.remove(task);
			nextState.vehicleTasks.add(task);

			if (((nextState.vehicleTasks.weightSum() + task.weight) < agentCapacity)) {
				nextStates.add(nextState);
			}
		}

		// Successor deliver state
		for (Task task : newVehicleTasks) {
			State nextState = state.copyState();

			// just go to delivery city, one step at a time.
			for (City city : state.getCurrentCity().pathTo(task.deliveryCity)) {
				nextState.plan.appendMove(city);

				// IF there is a task to deliver on a way of task pickup then please do deliver
				if (city != task.deliveryCity) {
					for (Task deliveryOnAWay : deliveriesForCity(city, state)) {
						nextState.plan.appendDelivery(deliveryOnAWay);
						nextState.vehicleTasks.remove(deliveryOnAWay);
						newVehicleTasks.remove(deliveryOnAWay);
					}
				}
				for (Task pickupOnAWay : pickupsInCity(city, state)) {
					if (((nextState.vehicleTasks.weightSum() + pickupOnAWay.weight) < agentCapacity)) {
						nextState.plan.appendPickup(pickupOnAWay);
						nextState.vehicleTasks.add(pickupOnAWay);
						nextState.topologyTasks.remove(pickupOnAWay);
					}
				}

			}
			nextState.currentCity = task.deliveryCity;
			nextState.plan.appendDelivery(task);
			nextState.vehicleTasks.remove(task);
			nextStates.add(nextState);

		}
		return nextStates;
	}

}
