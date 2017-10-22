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

	public static Plan createPlan(Vehicle vehicle, TaskSet tasks) {
		agentCapacity = vehicle.capacity();

		State finalState = BFSAlgorithm(vehicle, tasks);
		Plan plan = finalState.plan;
		System.out.println(plan);
		return plan;
	}

	public static State BFSAlgorithm(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);
		
		State initialState = new State(vehicle.getCurrentCity(), vehicle.getCurrentCity(), vehicle.getCurrentTasks(), tasks, plan); // n
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
//			 if(!C.contains(n)) {
//				 C.add(n);
//				 S = getSuccessors(n);
//			 }
			 S = getSuccessors(n);
//			 System.out.println(S.size());
			 Q.addAll(S);
//			 Q=S;
		} while (true);

		return finalState;
	}

	private static List<State> getSuccessors(State state) {
		List<State> successors = new ArrayList<State>();
		TaskSet vehicleTasks = state.getVehicleTasks();
		TaskSet topologyTasks = state.getTopologyTasks();
//		City current = state.getCurrentCity();
//		Plan plan = state.getPlan();
		int weightInTheTrunk = vehicleTasks.weightSum();

		// Successor pickup state
		for (Task task : topologyTasks) {
			City taskpCity = task.pickupCity;
			int taskWeight = task.weight;
			State nextState = state.copyState();
			
			
			if ((weightInTheTrunk + taskWeight) < agentCapacity) {
				TaskSet newVehicleTasks = nextState.getVehicleTasks(); //vehicleTasks.clone();
				newVehicleTasks.add(task);
				nextState.setVehicleTasks(newVehicleTasks);
				
				TaskSet newTopologyTasks = nextState.getTopologyTasks(); //topologyTasks.clone();
				newTopologyTasks.remove(task);
				nextState.setTopologyTasks(newTopologyTasks);
				
//								Plan newplan = plan;
				for (City city : nextState.getCurrentCity().pathTo(taskpCity))
					nextState.plan.appendMove(city);

				nextState.plan.appendPickup(task);
				nextState.currentCity = taskpCity;
				successors.add(nextState);
			}
		}
		
		// Successor deliver state
		for (Task task : vehicleTasks) {
			City taskdCity = task.deliveryCity;
			State nextState = state.copyState();
			
			TaskSet newVehicleTasks = nextState.getVehicleTasks();//vehicleTasks.clone();
			newVehicleTasks.remove(task);
			nextState.setVehicleTasks(newVehicleTasks);
			
//			Plan newplan = plan;
//			newplan.appendMove(taskCity);
			for (City city : nextState.getCurrentCity().pathTo(taskdCity))
				nextState.plan.appendMove(city);

			nextState.plan.appendDelivery(task);
			nextState.currentCity = taskdCity;
//			State nextState = new State(taskCity, newVehicleTasks, topologyTasks, newplan);
			successors.add(nextState);
		}
		
		

		return successors;
	}

}
