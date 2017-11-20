package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private double costOfAgent;
//	private Vehicle vehicle;
//	private City currentCity;

//	private List<Task> tasksWon; // Not needed. agent.getTasks() is the same.
	private List<Task> allTasks;
	private HashMap<Task, Integer> taskWinner = new HashMap<Task, Integer>();
	private HashMap<Task, Long[]> taskBids = new HashMap<Task, Long[]>();
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.costOfAgent = Double.MIN_VALUE;
//		this.vehicle = agent.vehicles().get(0);
//		this.currentCity = vehicle.homeCity();

		long seed = -9019554669489983951L * distribution.hashCode() * agent.id();
		this.random = new Random(seed);
		
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) { 
		// NOT NEEDED - its for dummy agent
//		if (winner == agent.id()) {
//			currentCity = previous.deliveryCity;
//		}
		if(previous != null) {
			allTasks.add(previous);
			taskWinner.put(previous, winner);
			taskBids.put(previous, bids);
		}
	}
	
	@Override
	public Long askPrice(Task task) {

//		if (vehicle.capacity() < task.weight)
//			return null;
//
//		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
//		long distanceSum = distanceTask
//				+ currentCity.distanceUnitsTo(task.pickupCity);
//		double marginalCost = Measures.unitsToKM(distanceSum
//				* vehicle.costPerKm());
		TaskSet tempTasks = agent.getTasks();
		List<Vehicle> agentVehicles = agent.vehicles();
		tempTasks.add(task);
		
		CSP csp = new CSP(agentVehicles, tempTasks, 0.95, 1000);
		CSPSolution bestSolution = csp.calculateCSP();
		double solutionCost = csp.calculateTotalCost(bestSolution);
		double marginalCost = solutionCost - costOfAgent;
	
		double ratio = 1.0; // + (random.nextDouble() * 0.05 * task.id);
		double bid = ratio * marginalCost;

		return (long) Math.round(bid);
	}
	
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        List<Plan> plans = centralizedPlan(vehicles, tasks);
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in "+duration+" milliseconds.");
        
        return plans;
    }
    
    private List<Plan> centralizedPlan(List<Vehicle> vehicles, TaskSet tasks) {
    	
    		CSP csp = new CSP(vehicles, tasks, 0.95, 1000);
    		List <Plan> plans = csp.createCentralizedPlan();
    		return plans;
    }

//	@Override
//	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
//		
////		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
//
//		Plan planVehicle1 = naivePlan(vehicle, tasks);
//
//		List<Plan> plans = new ArrayList<Plan>();
//		plans.add(planVehicle1);
//		while (plans.size() < vehicles.size())
//			plans.add(Plan.EMPTY);
//
//		return plans;
//	}

	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}
	
	
}
