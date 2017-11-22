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
	private int biddingRound;
//	private Vehicle vehicle;
//	private City currentCity;

//	private List<Task> tasksWon; // Not needed. agent.getTasks() is the same.
	private List<Task> allTasks;
	private List<Task> opponentsTasks;
	private List<CustomVehicle> opponentsVehicles;
	private HashMap<Task, Integer> taskWinner = new HashMap<Task, Integer>();
	private HashMap<Task, Long[]> taskBids = new HashMap<Task, Long[]>();
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.costOfAgent = Double.MIN_VALUE;
		this.biddingRound = 0;

		long seed = -9019554669489983951L * distribution.hashCode() * agent.id();
		this.random = new Random(seed);
		
		this.allTasks = new ArrayList<Task>();
		this.opponentsTasks = new ArrayList<Task>();
		this.opponentsVehicles = new ArrayList<CustomVehicle>(); // naively assume same vehicles as ours
		for(Vehicle v: agent.vehicles()) {
			CustomVehicle custVehicle = new CustomVehicle(v);
			opponentsVehicles.add(custVehicle);
		}
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) { 
		long ourBid = bids[agent.id()];
		long opponentBid = bids[1-agent.id()]; // works because id's range from 0 to 1.
		// NOT NEEDED - its for dummy agent
		if (winner != agent.id()) {
//			currentCity = previous.deliveryCity;
			opponentsTasks.add(previous);
		}
		if(previous != null) {
			allTasks.add(previous);
			taskWinner.put(previous, winner);
			taskBids.put(previous, bids);
		}
		
		
		if(biddingRound == 1) {
			infereOpponentsInitialCity(previous, opponentBid);
		}
	}
	
	public void infereOpponentsInitialCity(Task task, Long opponentBid) {
		List<City> ourCities = new ArrayList<City>();
		int lowestOpponentsCost = Integer.MAX_VALUE;
		for(Vehicle v: agent.vehicles()) {
			ourCities.add(v.homeCity());
			int vehicleCost = v.costPerKm();
			if(vehicleCost < lowestOpponentsCost) {
				lowestOpponentsCost = vehicleCost;
			}
		}
		
		double lowestCost = Double.MAX_VALUE;
		City opponentsInitialCity = null;
		
		for(City city: topology.cities()) {
			if(!ourCities.contains(city)) {
				double distance = city.distanceTo(task.pickupCity) + task.pickupCity.distanceTo(task.deliveryCity);
				double cost = Math.abs(opponentBid - (distance * lowestOpponentsCost));
				
				if(cost < lowestCost) {
					lowestCost = cost;
					opponentsInitialCity = city;
				}
			}
		}
//		assigns opponentsInitialCity to first vehicle of opponent and then randomly assigns some unassigned cities to rest of vehicles
		ourCities.add(opponentsInitialCity);
		for(CustomVehicle v: opponentsVehicles) {
			Random random = new Random();
			City randomCity;
			do {
				int randomNum = random.nextInt(topology.cities().size());
				randomCity = topology.cities().get(randomNum);
			} while (ourCities.contains(randomCity));
			ourCities.add(randomCity);
			v.setInitCity(randomCity);
		}
		opponentsVehicles.get(0).setInitCity(opponentsInitialCity);
	}
	
	@Override
	public Long askPrice(Task task) {
		double marginalCost = agentsMarginalCost(task);
	
		double ratio = 1.0; // + (random.nextDouble() * 0.05 * task.id);
		double bid = ratio * marginalCost;
		
		// Keep track how many bidding rounds we performed
		biddingRound += 1;
		
		return (long) Math.round(bid);
	}
	
	public double agentsMarginalCost(Task task) {
		TaskSet previousTasks = agent.getTasks();
		List<Vehicle> agentVehicles = agent.vehicles();
		List<CustomVehicle>customVehicles = new ArrayList<CustomVehicle>(); 
		for(Vehicle v: agentVehicles) {
			CustomVehicle custVehicle = new CustomVehicle(v);
			customVehicles.add(custVehicle);
		}
		previousTasks.add(task);
		
		CSP csp = new CSP(customVehicles, previousTasks, 0.95, 1000);
		CSPSolution bestSolution = csp.calculateCSP();
		
		double solutionCost = csp.calculateTotalCost(bestSolution);
		double agentsMarginalCost = solutionCost - costOfAgent;
		
		return agentsMarginalCost;
	}
	
	public double opponentsMarginalCost(Task task) {
		// Just a trick to convert List into Array into TaskSet (we need taskset in CSP).
		Task[] opponentsTasksArray = new Task[opponentsTasks.size()];
		opponentsTasks.toArray(opponentsTasksArray); // fill the array
		TaskSet previousOponentsTasks = TaskSet.create(opponentsTasksArray);
		previousOponentsTasks.add(task);
		
		
		CSP csp = new CSP(opponentsVehicles, previousOponentsTasks, 0.95, 1000);
		CSPSolution bestOpponentSolution = csp.calculateCSP();
		
		double solutionCost = csp.calculateTotalCost(bestOpponentSolution);
		double opponentsmMarginalCost = solutionCost - costOfAgent;
		
		return opponentsmMarginalCost;
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
    		List<CustomVehicle>customVehicles = new ArrayList<CustomVehicle>(); 
    		for(Vehicle v: vehicles) {
    			CustomVehicle custVehicle = new CustomVehicle(v);
    			customVehicles.add(custVehicle);
    		}
    		CSP csp = new CSP(customVehicles, tasks, 0.95, 1000);
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
