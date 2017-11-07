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

	public List<Plan> createCentralizedPlan() {
		List<Plan> plans = new ArrayList<Plan>();

		CSPSolution bestSolution = calculateCSP();

		List<Vehicle> involvedVehicles = bestSolution.getInvolvedVehicles();
		for (Vehicle vehicle : vehicles) {
			List<Task> tasksCarriedByVehicle = new ArrayList<Task>();
			if (!involvedVehicles.contains(vehicle)) {
				plans.add(Plan.EMPTY);
			} else {
				City current = vehicle.getCurrentCity();
				Plan plan = new Plan(current);
				Task firstTask = bestSolution.getNextTask(vehicle);
				Task task = firstTask;

				City startingPoint = current;
				for (City city : startingPoint.pathTo(task.pickupCity)) {
					plan.appendMove(city);
					current = city;
				}
				plan.appendPickup(firstTask);
				tasksCarriedByVehicle.add(firstTask);

				Task currentTask = firstTask;

				while (currentTask != null) {

					List<Task> shortestDeliveries = shortestDeliveryPath(bestSolution, currentTask, tasksCarriedByVehicle);
					for (Task toDeliver : shortestDeliveries) {
						startingPoint = current;
						for (City city : startingPoint.pathTo(toDeliver.deliveryCity)) {
							plan.appendMove(city);
							current = city;
						}
						plan.appendDelivery(toDeliver);
						tasksCarriedByVehicle.remove(toDeliver);
					}

					Task nextTask = bestSolution.getNextTask(currentTask);
					if (nextTask != null) {
						startingPoint = current;
						for (City city : startingPoint.pathTo(nextTask.pickupCity)) {
							plan.appendMove(city);
							current = city;
						}
						plan.appendPickup(nextTask);
						tasksCarriedByVehicle.add(nextTask);
					}
					currentTask = nextTask;
				}
				plans.add(plan);
			}
		}
		return plans;
	}

	public CSPSolution calculateCSP() {
		CSPSolution A = selectInitialSolution();
		 int iteration = 1;
		 do {
		 CSPSolution Aold = new CSPSolution(A);
		 List<CSPSolution> N = chooseNeighbours(Aold);
		 A = localChoice(N);
		 iteration += 1;
		 } while (iteration < iterations);
		System.out.println("debug");
		return A;
	}

	CSPSolution selectInitialSolution() {
		CSPSolution initialSolution = new CSPSolution(vehicles, deliveryTasks);

		int currentVehicleIndex = 0;
		Vehicle currentVehicle = vehicles.get(currentVehicleIndex);
		int timePoint = 1;
		Task previousVehicleTask = null;
		for (Task task : deliveryTasks) {
			int vehicleWeight = weight(initialSolution, currentVehicle, timePoint);
			int currentTaskWeight = task.weight;

			while (currentVehicle.capacity() < vehicleWeight + currentTaskWeight) {
				// get nextVehicle
				previousVehicleTask = null;
				currentVehicleIndex += 1;
				if (currentVehicleIndex >= vehicles.size()) {
					return null; // no solution
				}
				currentVehicle = vehicles.get(currentVehicleIndex);
				vehicleWeight = weight(initialSolution, currentVehicle, timePoint);
				currentTaskWeight = task.weight;
			}
			
			initialSolution.setTime(task, timePoint);
			initialSolution.setVehicle(task, currentVehicle);
			if (previousVehicleTask == null) {
				initialSolution.setNextTask(currentVehicle, task);
			} else {
				initialSolution.setNextTask(previousVehicleTask, task);
			}
			previousVehicleTask = task;

			timePoint += 1;
			// timeInTrunk += 1;
		}
		return initialSolution;
	}

	CSPSolution localChoice(List<CSPSolution> N) {
		if (N.isEmpty()) {
			return null;
		}
		int length = N.size();
		double minCost = calculateTotalCost(N.get(0));
		CSPSolution minCostSol = N.get(0);
		for (int i = 1; i < length; i++) {
			double currentCost = calculateTotalCost(N.get(i));
			if (currentCost < minCost) {
				minCost = currentCost;
				minCostSol = N.get(i);
			}
		}
		
		return minCostSol;
	}

	double calculateTotalCost(CSPSolution A) {
		double totalCost = 0;
		for (Vehicle vehicle : A.getInvolvedVehicles()) {
			Task nextTask = A.getNextTask(vehicle);
			if(nextTask != null) {
				City homeCity = vehicle.homeCity();
				City nextCity = A.getNextTask(vehicle).pickupCity;
				totalCost += homeCity.distanceTo(nextCity) * vehicle.costPerKm();
			}
		}
		for (Task task : A.getAllTasks()) {
			totalCost += distanceNextStep(A, task) * A.getVehicle(task).costPerKm();
		}
		return totalCost;
	}

	double distanceNextStep(CSPSolution A, Task ti) {
		List<City> deliveryCities = getDeliveryCities(A, ti);
		Task tj = A.getNextTask(ti);
		City start = ti.pickupCity;
		
		
		if(deliveryCities.isEmpty()) {
			City end = tj.pickupCity;
			return start.distanceTo(end);
		} 
		Collection<List<City>> deliveryPermutations = Collections2.permutations(deliveryCities);

		double shortestDelivery = Double.MAX_VALUE;
		for (List<City> deliveries : deliveryPermutations) {
			double distance = start.distanceTo(deliveries.get(0));
			for (int i = 0; i < deliveries.size() - 1; i++) {
				City city1 = deliveries.get(i);
				City city2 = deliveries.get(i + 1);
				distance += city1.distanceTo(city2);
			}
			
			if((tj != null)) {
				City end = tj.pickupCity;
				distance += deliveries.get(deliveries.size() - 1).distanceTo(end);
			}
			
			
			if (distance < shortestDelivery) {
				shortestDelivery = distance;
			}
		}

		return shortestDelivery;
	}

	List<Task> shortestDeliveryPath(CSPSolution A, Task ti, List<Task> tasksCarried) {
		List<Task> deliveryTasks = getDeliveryTasks(A, ti, tasksCarried);
		Task tj = A.getNextTask(ti);
		City start = ti.pickupCity;
		
		if(deliveryTasks.isEmpty()) {
			return deliveryTasks;
		} 

		Collection<List<Task>> deliveryPermutations = Collections2.permutations(deliveryTasks);

		double shortestDelivery = Double.MAX_VALUE;
		List<Task> shortestTaskPath = null;
		for (List<Task> tasks : deliveryPermutations) {
			double distance = start.distanceTo(tasks.get(0).deliveryCity);
			for (int i = 0; i < tasks.size() - 1; i++) {
				Task task1 = tasks.get(i);
				Task task2 = tasks.get(i + 1);
				distance += task1.deliveryCity.distanceTo(task2.deliveryCity);
			}
			if((tj != null)) {
				City end = tj.pickupCity;
				distance += tasks.get(tasks.size() - 1).deliveryCity.distanceTo(end);
			}

			if (distance < shortestDelivery) {
				shortestDelivery = distance;
				shortestTaskPath = tasks;
			}
		}

		return shortestTaskPath;
	}

	List<City> getDeliveryCities(CSPSolution A, Task task) {
		List<City> deliveries = new ArrayList<City>();
		int taskTime = A.getTime(task);
		Vehicle v = A.getVehicle(task);
		Task current = A.getNextTask(v);
		for (int time = 1; time < taskTime+1; time++) {
			int tit = A.getTimeInTrunk(current);
			if (tit - (taskTime - time) == 1) {
				deliveries.add(current.deliveryCity);
				current = A.getNextTask(current);
			}
		}
		return deliveries;
	}

	List<Task> getDeliveryTasks(CSPSolution A, Task task, List<Task> tasksCarried) {
		List<Task> deliveries = new ArrayList<Task>();
		int taskTime = A.getTime(task);
		Vehicle v = A.getVehicle(task);
		Task current = A.getNextTask(v);
		for (int time = 1; time < taskTime + 1; time++) {
			int tit = A.getTimeInTrunk(current);
			if ((tit - (taskTime - time) == 1) ) {//&& tasksCarried.contains(current)) {
				deliveries.add(current);
				current = A.getNextTask(current);
			}
		}

		return deliveries;
	}

	List<CSPSolution> chooseNeighbours(CSPSolution Aold) {
		List<CSPSolution> N = new ArrayList<CSPSolution>();
		Task vehicleTask = null;
		Vehicle vi = null;
		do {
			Random randomizer = new Random();
			vi = vehicles.get(randomizer.nextInt(vehicles.size()));
			vehicleTask = Aold.getNextTask(vi);
		} while (vehicleTask == null);

		// Applying the changing vehicle operator
		for (Vehicle vj : vehicles) {
			if (vi == vj) {
				continue;
			}
//			Task t = Aold.getNextTask(vi);
			// if (load(t) <= capacity(vj)) I dont think its needed
			CSPSolution A = changeVehicle(Aold, vi, vj);
			if (A != null) {
				N.add(A);
			}
		}

		// Applying the changing task order operator
		// compute the number of tasks of vehicle
		int length = 0;
		Task t = Aold.getNextTask(vi);
		length += 1;
		if (t != null) {
			do {
				t = Aold.getNextTask(t);
				length += 1;
			} while (t != null);
		}
		if (length >= 2) {
			for (int tIdx1 = 1; tIdx1 <= length - 1; tIdx1++) {
				for (int tIdx2 = tIdx1 + 1; tIdx2 <= length; tIdx2++) {
					List<CSPSolution> A = changeTaskOrder(Aold, vi, tIdx1, tIdx2);
					if (!A.isEmpty()) {
						N.addAll(A);
					}
				}
			}
		}

		return N;
	}

	CSPSolution changeVehicle(CSPSolution A, Vehicle v1, Vehicle v2) {
		CSPSolution A1 = new CSPSolution(A);
		Task t = A1.getNextTask(v1);
		int v1TimeInTrunk = A1.getTimeInTrunk(t); // Added
		Task tForV1 = A1.getNextTask(t);
		A1.setTimeInTrunk(tForV1, v1TimeInTrunk); // Added

		A1.setNextTask(v1, tForV1);
		A1.setNextTask(t, A1.getNextTask(v2));
		A1.setTimeInTrunk(t, 1);
		A1.setNextTask(v2, t);
		updateTime(A1, v1);
		updateTime(A1, v2);
		A1.setVehicle(t, v2);
		if (checkIfPossibeSolution(A1, v1) && checkIfPossibeSolution(A1, v2)) {
			return A1;
		} else {
			return null;
		}
	}

	List<CSPSolution> changeTaskOrder(CSPSolution A, Vehicle vi, int tIdx1, int tIdx2) {
		List<CSPSolution> N = new ArrayList<CSPSolution>();
		int nbTasks = amountOfTasks(A, vi);
		CSPSolution A1 = new CSPSolution(A);
		Task t1 = A1.getNextTask(vi);
		int count = 1;
		Task tPre1 = null;
		while (count < tIdx1) {
			tPre1 = t1;
			t1 = A1.getNextTask(t1);
			count += 1;
		}

		Task tPost1 = A1.getNextTask(t1);
		Task tPre2 = t1;
		Task t2 = A1.getNextTask(tPre2);
		// imho Task.time should be updated here as well
		count += 1;
		while (count < tIdx2) {
			tPre2 = t2;
			t2 = A1.getNextTask(t2);
			count += 1;
		}
		Task tPost2 = A1.getNextTask(t2);
		// imho Task.time should be updated here as well
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

		for (int tit1 = 1; tit1 < (nbTasks - tIdx1 + 1); tit1++) {
			for (int tit2 = 1; tit2 < (nbTasks - tIdx2 + 1); tit2++) {
				CSPSolution A2 = new CSPSolution(A1);
				A2.setTimeInTrunk(t1, tit1);
				A2.setTimeInTrunk(t2, tit2);
				if (checkIfPossibeSolution(A2, vi)) {
					N.add(A2);
				}
			}
		}
		return N;
	}

	int weight(CSPSolution A, Vehicle v, int timePoint) {
		int weight = 0;
		Task current = A.getNextTask(v);
		for (int i = 1; i < timePoint + 1; i++) {
			if ((current != null) && (A.getTimeInTrunk(current) > timePoint - i) ) {
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
