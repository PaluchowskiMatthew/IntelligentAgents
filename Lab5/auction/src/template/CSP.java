package template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Collections2;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class CSP {
	List<Vehicle> vehicles;
	TaskSet deliveryTasks;

	// If there are multiple equally good assignments,
	// it chooses one randomly. Then with probability p it returns A,
	// with probability 1 − p it returns the current assignment Aold.
	double probability;
	int iterations;

	public CSP(List<Vehicle> vehicles, TaskSet deliveryTasks, double p, int iterations) {
		this.vehicles = vehicles;
		this.deliveryTasks = deliveryTasks;
		this.probability = p;
		this.iterations = iterations;
	}

	public CSPSolution calculateCSP() {
		CSPSolution A = selectInitialSolution();
		int iteration = 1;
		boolean iterate = true;
		
		CSPSolution bestFoundSolution = null;
		double bestFoundSolutionCost = Double.MAX_VALUE;
		int iterationsAfterBestSolutionFound = 0;
		
		do {
			CSPSolution Aold = new CSPSolution(A);
			List<CSPSolution> N = chooseNeighbours(Aold);
			A = localChoice(N, A);
			
			double minCost = calculateTotalCost(A);
			if(minCost < bestFoundSolutionCost) {
				bestFoundSolutionCost = minCost;
				bestFoundSolution = A;
				iterationsAfterBestSolutionFound = 0;
			}
			
			if(iterationsAfterBestSolutionFound >= 150) {
				iterate = false;
			}
			System.out.println("Iteration: " + iteration);
			System.out.println("Current solution cost: " + minCost);
			System.out.println("Iteration after best: " + iterationsAfterBestSolutionFound);
			iterationsAfterBestSolutionFound += 1;
			iteration += 1;
		} while ( (iteration < iterations) && iterate );
//		double minCost = calculateTotalCost(bestFoundSolution);
//		System.out.println("Solution total cost: " + minCost);
		
		return bestFoundSolution;
	}

	public List<Plan> createCentralizedPlan() {
		List<Plan> plans = new ArrayList<Plan>();
		CSPSolution bestSolution = calculateCSP();

		// Create Plan for every vehicle with tasks assigned
		List<Vehicle> involvedVehicles = bestSolution.getInvolvedVehicles();
		for (Vehicle vehicle : vehicles) {
			if (!involvedVehicles.contains(vehicle)) {
				plans.add(Plan.EMPTY);
			} else {
				City current = vehicle.getCurrentCity();
				Plan plan = new Plan(current);
				Task currentTask = bestSolution.getNextTask(vehicle);

				while (currentTask != null) {

					// PICKUP
					for (City city : current.pathTo(currentTask.pickupCity)) {
						plan.appendMove(city);
					}
					current = currentTask.pickupCity;
					plan.appendPickup(currentTask);

					// DELIVER
					List<Task> shortestDeliveries = shortestDeliveryPath(bestSolution, currentTask);
					for (Task toDeliver : shortestDeliveries) {
						for (City city : current.pathTo(toDeliver.deliveryCity)) {
							plan.appendMove(city);
						}
						current = toDeliver.deliveryCity;
						plan.appendDelivery(toDeliver);
					}
					// NEXTSTEP
					currentTask = bestSolution.getNextTask(currentTask);
				}
				plans.add(plan);
				
				double costOfPlan = plan.totalDistance() * vehicle.costPerKm();
				System.out.println("Cost of Plan for vehicle " + vehicle.id() + ": " + costOfPlan);
			}
			
		}
		return plans;
	}

	CSPSolution selectInitialSolution() {
		CSPSolution initialSolution = new CSPSolution(vehicles, deliveryTasks);
		int currentVehicleIndex = 0;
		List<Task> previousTasks = new ArrayList<Task>();
		for (Vehicle vehicle : vehicles) {
			previousTasks.add(null);
		}

		Vehicle currentVehicle = vehicles.get(currentVehicleIndex);
		Task previousTask = previousTasks.get(currentVehicleIndex);

		for (Task task : deliveryTasks) {
			initialSolution.setVehicle(task, currentVehicle);
			if (previousTask == null) {
				initialSolution.setNextTask(currentVehicle, task);
			} else {
				initialSolution.setNextTask(previousTask, task);
			}
			previousTasks.set(currentVehicleIndex, task);
			updateTime(initialSolution, currentVehicle);
			currentVehicleIndex += 1;
			if (currentVehicleIndex >= vehicles.size()) {
				currentVehicleIndex = 0;
			}
			currentVehicle = vehicles.get(currentVehicleIndex);
			previousTask = previousTasks.get(currentVehicleIndex);

		}
		return initialSolution;
	}

	CSPSolution localChoice(List<CSPSolution> N, CSPSolution Aold) {
		if (N.isEmpty()) {
			return null;
		}
		int length = N.size();
		double minCost = calculateTotalCost(Aold);
		List<CSPSolution> minCostSol = new ArrayList<CSPSolution>();
		minCostSol.add(N.get(0));
		for (int i = 0; i < length; i++) {
			double currentCost = calculateTotalCost(N.get(i));
			if (currentCost < minCost) {
				minCost = currentCost;
				minCostSol.clear();
				minCostSol.add(N.get(i));
			} else if (currentCost == minCost) {
				minCostSol.add(N.get(i));
			}
		}
		CSPSolution localChoice = new CSPSolution(Aold);
		Random random = new Random();
		if (probability > random.nextDouble()) {
			localChoice = minCostSol.get(random.nextInt(minCostSol.size()));
		} else {
			localChoice = N.get(random.nextInt(N.size()));
		}

		return localChoice;
	}

	double calculateTotalCost(CSPSolution A) {
		double totalCost = 0;
		for (Vehicle vehicle : A.getInvolvedVehicles()) {
			Task nextTask = A.getNextTask(vehicle);
			if (nextTask != null) {
				City homeCity = vehicle.homeCity();
				City nextCity = nextTask.pickupCity;
				totalCost += homeCity.distanceTo(nextCity) * vehicle.costPerKm();
			}
		}
		for (Task task : A.getAllTasks()) {
			totalCost += distanceSequence(A, shortestDeliveryPath(A, task), task) * A.getVehicle(task).costPerKm();
		}
		return totalCost;
	}

	double distanceSequence(CSPSolution A, List<Task> deliveries, Task ti) {
		Task tj = A.getNextTask(ti);
		City start = ti.pickupCity;

		if (deliveries.isEmpty()) {
			if (tj == null) {
				System.out.println("Debug");
			}
			City end = tj.pickupCity;
			return start.distanceTo(end);
		}

		double distance = start.distanceTo(deliveries.get(0).deliveryCity);

		for (int i = 0; i < deliveries.size() - 1; i++) {
			City city1 = deliveries.get(i).deliveryCity;
			City city2 = deliveries.get(i + 1).deliveryCity;
			distance += city1.distanceTo(city2);
		}
		if ((tj != null)) {
			City end = tj.pickupCity;
			distance += deliveries.get(deliveries.size() - 1).deliveryCity.distanceTo(end);
		}
		return distance;
	}

	List<Task> shortestDeliveryPath(CSPSolution A, Task ti) {
		List<Task> deliveryTasks = getDeliveryTasks(A, ti);

		if (deliveryTasks.isEmpty()) {
			return deliveryTasks;
		}

		Collection<List<Task>> deliveryPermutations = Collections2.permutations(deliveryTasks);

		double shortestDelivery = Double.MAX_VALUE;
		List<Task> shortestTaskPath = null;

		for (List<Task> tasks : deliveryPermutations) {
			double distance = distanceSequence(A, tasks, ti);

			if (distance < shortestDelivery) {
				shortestDelivery = distance;
				shortestTaskPath = tasks;
			}
		}
		return shortestTaskPath;
	}

	List<Task> getDeliveryTasks(CSPSolution A, Task task) {
		List<Task> deliveries = new ArrayList<Task>();
		int taskTime = A.getTime(task);
		Vehicle v = A.getVehicle(task);
		Task current = A.getNextTask(v);
		for (int time = 1; time < taskTime + 1; time++) {
			int tit = A.getTimeInTrunk(current);
			if ((tit - (taskTime - time) == 1)) {
				deliveries.add(current);
			}
			current = A.getNextTask(current);
		}

		return deliveries;
	}

	CSPSolution updateTrunkTimes(CSPSolution A, Task task) {
		CSPSolution A1 = new CSPSolution(A);
		int taskTime = A.getTime(task);
		Vehicle v = A.getVehicle(task);
		Task current = A.getNextTask(v);
		for (int time = 1; time < taskTime + 1; time++) {
			int tit = A.getTimeInTrunk(current);
			if ((tit - (taskTime - time) >= 1)) {
				A1.setTimeInTrunk(current, tit - 1);
			}
			current = A.getNextTask(current);
		}
		return A1;
	}

	List<CSPSolution> chooseNeighbours(CSPSolution Aold) {
		List<CSPSolution> N = new ArrayList<CSPSolution>();
		List<Vehicle> involvedVehicles = Aold.getInvolvedVehicles();

		Random randomizer = new Random();
		int intrandomizer = randomizer.nextInt(involvedVehicles.size());
		Vehicle vi = involvedVehicles.get(intrandomizer);

		// Applying the changing vehicle operator
		for (Vehicle vj : vehicles) {
			if (vi == vj) {
				continue;
			}
			CSPSolution A = changeVehicle(Aold, vi, vj);
			if (A != null) {
				N.add(A);
			}
		}

		// Applying the changing task order operator
		int solutionCount = 0;
		int length = amountOfTasks(Aold, vi);
		if (length >= 2) {
			do {
				Random r1 = new Random();
				Random r2 = new Random();
				int tIdx1 = r1.nextInt(length - 1) + 1;
				int tIdx2 = r2.nextInt(length - tIdx1) + tIdx1 + 1;
				List<CSPSolution> A = changeTaskOrder(Aold, vi, tIdx1, tIdx2);
				if (!A.isEmpty()) {
					N.addAll(A);
					solutionCount += 1;
				}
			} while (solutionCount < 3);
		}
		return N;
	}

	CSPSolution changeVehicle(CSPSolution A, Vehicle v1, Vehicle v2) {
		CSPSolution A1 = new CSPSolution(A);
		int length = amountOfTasks(A, v1);
		Random r1 = new Random();
		int tIdx1 = r1.nextInt(length) + 1;

		// SELECT TASK
		Task t1 = A1.getNextTask(v1);
		Task tPre1 = null;
		int count = 1;
		while (count < tIdx1) {
			tPre1 = t1;
			t1 = A1.getNextTask(t1);
			count += 1;
		}
		Task tPost1 = A1.getNextTask(t1);

		// UPDATE TimeInTrunk
		A1 = updateTrunkTimes(A1, t1);
		// EJECT TASK
		if (tPre1 == null) {
			A1.setNextTask(v1, tPost1);
		} else {
			A1.setNextTask(tPre1, tPost1);
		}
		// INSERT TASK
		A1.setNextTask(t1, A1.getNextTask(v2));
		A1.setNextTask(v2, t1);

		A1.setTimeInTrunk(t1, 1);
		A1.setVehicle(t1, v2);

		updateTime(A1, v1);
		updateTime(A1, v2);

		if (checkIfPossibeSolution(A1, v1) && checkIfPossibeSolution(A1, v2)) {
			return A1;
		} else {
			return null;
		}
	}

	List<CSPSolution> changeTaskOrder(CSPSolution A, Vehicle vi, int tIdx1, int tIdx2) {
		List<CSPSolution> N = new ArrayList<CSPSolution>();
		CSPSolution A1 = new CSPSolution(A);

		int nbTasks = amountOfTasks(A, vi);
		Task t1 = A1.getNextTask(vi);
		Task tPre1 = null;
		int count = 1;
		while (count < tIdx1) {
			tPre1 = t1;
			t1 = A1.getNextTask(t1);
			count += 1;
		}

		Task tPost1 = A1.getNextTask(t1);
		Task tPre2 = t1;
		Task t2 = A1.getNextTask(tPre2);

		count += 1;
		while (count < tIdx2) {
			tPre2 = t2;
			t2 = A1.getNextTask(t2);
			count += 1;
		}
		Task tPost2 = A1.getNextTask(t2);

		if (tPost1 == t2) {
			if (tPre1 == null) {
				A1.setNextTask(vi, t2);
			} else {
				A1.setNextTask(tPre1, t2);
			}

			A1.setNextTask(t2, t1);
			A1.setNextTask(t1, tPost2);
		} else {
			if (tPre1 == null) {
				A1.setNextTask(vi, t2);
			} else {

				A1.setNextTask(tPre1, t2);
			}
			A1.setNextTask(tPre2, t1);
			A1.setNextTask(t2, tPost1);
			A1.setNextTask(t1, tPost2);
		}
		updateTime(A1, vi);

		int solutionCount = 0;
		do {
			Random r1 = new Random();
			Random r2 = new Random();
			int tit2 = r1.nextInt(nbTasks - tIdx1 + 1) + 1;
			int tit1 = r2.nextInt(nbTasks - tIdx2 + 1) + 1;
			CSPSolution A2 = new CSPSolution(A1);
			A2.setTimeInTrunk(t1, tit1);
			A2.setTimeInTrunk(t2, tit2);
			if (checkIfPossibeSolution(A2, vi)) {
				N.add(A2);
				solutionCount += 1;
			}
		} while (solutionCount < 2);
		return N;
	}

	int weight(CSPSolution A, Vehicle v, int timePoint) {
		int weight = 0;
		Task current = A.getNextTask(v);
		for (int i = 1; i < timePoint + 1; i++) {
			if ((current != null) && (A.getTimeInTrunk(current) > timePoint - i)) {
				weight += current.weight;
				current = A.getNextTask(current);
			}
		}
		return weight;
	}

	boolean checkIfPossibeSolution(CSPSolution A1, Vehicle v) {
		int amountOfTasks = amountOfTasks(A1, v);
		for (int i = 1; i < amountOfTasks + 1; i++) {
			if (weight(A1, v, i) > v.capacity()) {
				return false;
			}
		}
		return true;
	}

	void updateTime(CSPSolution A, Vehicle v) {
		Task taskI = A.getNextTask(v);
		if (taskI != null) {
			A.setTime(taskI, 1);
			Task taskJ = A.getNextTask(taskI);
			do {
				if (taskJ != null) {
					int newTime = A.getTime(taskI) + 1;
					A.setTime(taskJ, newTime);
					taskI = taskJ;
					taskJ = A.getNextTask(taskI);
				}
			} while (taskJ != null);
		}
	}

	int amountOfTasks(CSPSolution A, Vehicle v) {
		int amountOfTasks = 0;
		Task current = A.getNextTask(v);
		while (current != null) {
			amountOfTasks += 1;
			current = A.getNextTask(current);
		}
		return amountOfTasks;
	}

}
