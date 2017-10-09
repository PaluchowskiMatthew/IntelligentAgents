package template;

import java.util.List;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;

	public void learn() {
		// idk yet
	}

	public boolean decideWhatToDo() {
		return false;
	}

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;

		List<Vehicle> vehicles = agent.vehicles();
		City home_cit = vehicles.get(0).homeCity();
		City curr_cit = vehicles.get(0).getCurrentCity();

		System.out.println(agent.id());
		System.out.println(agent.vehicles());
		System.out.println(agent.getTasks());

		System.out.println(topology.cities());
		System.out.println(topology.size());
		System.out.println(topology.randomCity(new Random()));
		System.out.println(topology.contains(home_cit));

		System.out.println(td.probability(home_cit, curr_cit));
		System.out.println(td.reward(home_cit, curr_cit));
		System.out.println(td.weight(home_cit, curr_cit));

		System.out.println("discount: " + discount);

		MarkovDecissionProcessModel.learnOfflinePolicy(topology, td, agent, discount);

		System.out.println("END SETUP");
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		/*
		 * 1. The vehicle starts from its home city and can move freely through the
		 * network. 2. When the vehicle arrives in a city, it finds out whether or not a
		 * task is available in that city. 3. If a task is available, the agent can
		 * decide to pick up the task and deliver it, or it can refuse the task and move
		 * to another city (without load). 4. If no task is available or if the task was
		 * refused, the vehicle moves along a route to a neighboring city. 5. The
		 * vehicle can transport only one task at a time. 6. The agent receives a reward
		 * for each task that it delivers and pays a cost for each kilometer that it
		 * travels. Since the agent tries to maximize its profit, it will always deliver
		 * a task on the shortest path. 7.
		 * Ataskwhichwasrefuseddisappearsimmediately.Thenexttimetheagentmoves to that
		 * city a new task is generated according to the probability distribution.
		 */

		// if (availableTask != null) {
		//
		// System.out.println(availableTask.deliveryCity);
		// System.out.println(availableTask.id);
		// System.out.println(availableTask.pickupCity);
		// System.out.println(availableTask.reward);
		// System.out.println(availableTask.weight); // kg
		//
		// System.out.println(vehicle.id());
		// System.out.println(vehicle.name());
		// System.out.println(vehicle.capacity());
		// System.out.println(vehicle.homeCity());
		// System.out.println(vehicle.speed());
		// System.out.println(vehicle.costPerKm());
		// System.out.println(vehicle.getCurrentCity());
		// System.out.println(vehicle.getCurrentTasks());
		// System.out.println(vehicle.getReward());
		// System.out.println(vehicle.getDistanceUnits());
		//
		//
		// boolean pick = decideWhatToDo();
		// }

		

		
		// Reactive AGENT
		City currentCity = vehicle.getCurrentCity();
		City deliveryCity = (availableTask == null) ? null : availableTask.deliveryCity;
		State currentState = new State(currentCity, deliveryCity);

		City bestAction = MarkovDecissionProcessModel.getBestAction(currentState);
		
		if (bestAction != null) {

			if (availableTask != null && bestAction.equals(availableTask.deliveryCity)) {
				action = new Pickup(availableTask);
			} else {
				action = new Move(bestAction);
			}
		} else {
			action = new Move(currentCity.randomNeighbor(random));
		}
		// END REACTIVE AGENT
		
		// RANDOM AGENT
//		if (availableTask == null || random.nextDouble() > 0.5) {
//			City currentCity = vehicle.getCurrentCity();
//			action = new Move(currentCity.randomNeighbor(random));
//		} else {
//			action = new Pickup(availableTask);
//		}
		//END RANDOM AGENT
		

		if (numActions >= 1) {
			System.out.println("The total profit after " + numActions + " actions is " + myAgent.getTotalProfit()
					+ " (average profit: " + (myAgent.getTotalProfit() / (double) numActions) + ")");
		}
		numActions++;

		return action;
	}
}
