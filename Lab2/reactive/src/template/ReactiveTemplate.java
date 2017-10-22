

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

		// Reactive AGENT
		City currentCity = vehicle.getCurrentCity();
		City deliveryCity = (availableTask == null) ? null : availableTask.deliveryCity;
		State currentState = new State(currentCity, deliveryCity);

		City bestAction = MarkovDecissionProcessModel.getBestAction(currentState);
		


		if (availableTask != null && bestAction.equals(availableTask.deliveryCity)) {
			action = new Pickup(availableTask);
		} else {
			action = new Move(bestAction);
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