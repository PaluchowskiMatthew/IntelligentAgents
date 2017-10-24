package template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import template.State;

public class AStar {
	List<State> Q = new ArrayList<State>();
	List<State> C = new ArrayList<State>();
	int agentCapacity;
	int costPerKm;

	public Plan createPlan(Vehicle vehicle, TaskSet tasks) {
		agentCapacity = vehicle.capacity();
		costPerKm = vehicle.costPerKm();

		State finalState = AStarAlgorithm(vehicle, tasks);
		Plan plan = finalState.plan;
		System.out.println(plan);
		return plan;
	}

	public State AStarAlgorithm(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		State initialState = new State(vehicle.getCurrentCity(), vehicle.getCurrentCity(),
				vehicle.getCurrentTasks(), tasks, plan); // n
		Q.add(initialState);

		State finalState = null;
		do {
			List<State> S = null;
			if (Q.isEmpty()) {
				System.out.println("AStar Error. Final State not found.");
				break;
			}
			State n = Q.remove(0);
			if (n.isFinalState()) {
				finalState = n;
				break;
			}
			// TODO: this check with distance is no longer applicable. TODO
			if (!C.contains(n) || C.get(C.indexOf(n)).plan.totalDistance() > n.plan.totalDistance()) {
				C.add(n);
				StatesRewards statesRewards = getSuccessors(n);
//				S = statesRewards.getNextStates();
				S = sortSuccessors(n, statesRewards);
				Q.addAll(S);
			}
		} while (true);

		return finalState;
	}

	private List<State> sortSuccessors(State currentState, StatesRewards statesRewards) {
		List<Long> newRewards = new ArrayList<Long>();
		List<State> sortedSuccessors = new ArrayList<State>();
		
		List<State> nextStates = statesRewards.getNextStates();
		List<Long> rewards = statesRewards.getRewards();
		
		City currentCity = currentState.getCurrentCity();
		for (State nextState : nextStates) {
			int index = nextStates.indexOf(nextState);
			City nextCity = nextState.getCurrentCity();
			
			long newReward = rewards.get(index);
			newRewards.add(newReward);
		}
		ArrayList<Long> newRewardsStore = new ArrayList<Long>(newRewards);
		Collections.sort(newRewards, Collections.reverseOrder());
		for (int n = 0; n < newRewards.size(); n++) {
			int index = newRewardsStore.indexOf(newRewards.get(n));
			sortedSuccessors.add(n, nextStates.get(index));
		}
		return sortedSuccessors;
	}

	List<Task> pickupsInCity(City c, State s) {
		List<Task> pickups = new ArrayList<Task>();
		for (Task t : s.getTopologyTasks()) {
			if (t.pickupCity == c) {
				pickups.add(t);
			}
		}
		return pickups;
	}

	List<Task> deliveriesForCity(City c, State s) {
		List<Task> deliveries = new ArrayList<Task>();
		for (Task t : s.getVehicleTasks()) {
			if (t.deliveryCity == c) {
				deliveries.add(t);
			}
		}
		return deliveries;
	}

	public class StatesRewards {

		private List<State> nextStates;
		private List<Long> rewards;

		public StatesRewards(List<State> states, List<Long> rewards) {
			// TODO Auto-generated constructor stub
			this.nextStates = states;
			this.rewards = rewards;
		}

		public List<State> getNextStates() {
			return nextStates;
		}

		public void setNextStates(List<State> nextStates) {
			this.nextStates = nextStates;
		}

		public List<Long> getRewards() {
			return rewards;
		}

		public void setRewards(List<Long> rewards) {
			this.rewards = rewards;
		}

	}

	private StatesRewards getSuccessors(State state) {
		List<State> nextStates = new ArrayList<State>();
		List<Long> rewards = new ArrayList<Long>();
		TaskSet vehicleTasks = state.getVehicleTasks();
		TaskSet topologyTasks = state.getTopologyTasks();


		// Successor pickup state
		for (Task task : topologyTasks) {
			State nextState = state.copyState();
			Long nextReward = 0l;
			City previousCity = state.getCurrentCity();
			
			// if we start in some city we want to pick up tasks straight away
			for (Task pickupOnAWay : pickupsInCity(previousCity, state)) {
				if (((nextState.vehicleTasks.weightSum() + task.weight
						+ pickupOnAWay.weight) < agentCapacity) && pickupOnAWay != task) {
					nextState.plan.appendPickup(pickupOnAWay);
					nextState.vehicleTasks.add(pickupOnAWay);
					nextState.topologyTasks.remove(pickupOnAWay);
					nextReward += pickupOnAWay.reward * (100);
				}
			}

			for (City city : state.getCurrentCity().pathTo(task.pickupCity)) {
				nextState.plan.appendMove(city);
				nextReward -= (long) (costPerKm * previousCity.distanceTo(city));
				previousCity = city;

				// IF there is a task to deliver on a way of task pickup then please do deliver
				for (Task deliveryOnAWay : deliveriesForCity(city, state)) {
					nextState.plan.appendDelivery(deliveryOnAWay);
					nextState.vehicleTasks.remove(deliveryOnAWay);
				}
				
				for (Task pickupOnAWay : pickupsInCity(city, state)) {
					if (((nextState.vehicleTasks.weightSum() + task.weight
							+ pickupOnAWay.weight) < agentCapacity) && pickupOnAWay != task) {
						nextState.plan.appendPickup(pickupOnAWay);
						nextState.vehicleTasks.add(pickupOnAWay);
						nextState.topologyTasks.remove(pickupOnAWay);
						nextReward += pickupOnAWay.reward * 100;
					}
				}
				

			}
			nextState.currentCity = task.pickupCity;
			nextState.plan.appendPickup(task);
			nextState.topologyTasks.remove(task);
			nextState.vehicleTasks.add(task);
			nextReward += task.reward * (900 / 4);

			if (((nextState.vehicleTasks.weightSum() + task.weight) < agentCapacity)) {
				nextStates.add(nextState);
				rewards.add(nextReward);
			}
		}

		// Successor deliver state
		for (Task task : vehicleTasks) {
			State nextState = state.copyState();
			City previousCity = state.getCurrentCity();
			Long nextReward = 0l;

			// just go to delivery city, one step at a time.
			for (City city : state.getCurrentCity().pathTo(task.deliveryCity)) {
				nextState.plan.appendMove(city);
				nextReward -= (long) (costPerKm * previousCity.distanceTo(city));
				previousCity = city;

				// IF there is a task to deliver on a way of task pickup then please do deliver
				if (city != task.deliveryCity) {
					for (Task deliveryOnAWay : deliveriesForCity(city, state)) {
						nextState.plan.appendDelivery(deliveryOnAWay);
						nextState.vehicleTasks.remove(deliveryOnAWay);
					}
				}
				for (Task pickupOnAWay : pickupsInCity(city, state)) {
					if (((nextState.vehicleTasks.weightSum() + pickupOnAWay.weight) < agentCapacity)) {
						nextState.plan.appendPickup(pickupOnAWay);
						nextState.vehicleTasks.add(pickupOnAWay);
						nextState.topologyTasks.remove(pickupOnAWay);
						nextReward += pickupOnAWay.reward * 100;
					}
				}
			}
			nextState.currentCity = task.deliveryCity;
			nextState.plan.appendDelivery(task);
			nextState.vehicleTasks.remove(task);
			rewards.add(nextReward);
			nextStates.add(nextState);
		}
		StatesRewards statesRewards = new StatesRewards(nextStates, rewards);// new StatesRewards(nextStates,rewards);
		return statesRewards;
	}

}
