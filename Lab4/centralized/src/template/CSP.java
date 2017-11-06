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
		for(Vehicle vehicle: vehicles) {
			if(!involvedVehicles.contains(vehicle)) {
				plans.add(Plan.EMPTY);
			}
			else {
				City current = vehicle.getCurrentCity();
				Plan plan = new Plan(current);
				CSPTask firstTask = bestSolution.getNextTask(vehicle);
				Task task = firstTask.task;
				
				City startingPoint = current;
				for (City city : startingPoint.pathTo(task.pickupCity)) {
	                plan.appendMove(city);
	                current = city;
	            }
				plan.appendPickup(firstTask.task);
				
				CSPTask currentTask = firstTask;

				while(currentTask != null) {
					
					List<Task> shortestDeliveries = shortestDeliveryPath(bestSolution, currentTask);
					for(Task toDeliver: shortestDeliveries) {
						startingPoint = current;
						for (City city : startingPoint.pathTo(toDeliver.deliveryCity)) {
			                plan.appendMove(city);
			                current = city;
			            }
						plan.appendDelivery(task);
					}
					
					CSPTask nextTask = bestSolution.getNextTask(currentTask);
					if(nextTask != null) {
						startingPoint = current;
						for (City city : startingPoint.pathTo(nextTask.task.pickupCity)) {
			                plan.appendMove(city);
			                current = city;
			            }
						plan.appendPickup(nextTask.task);
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
//		int iteration = 1;
//		do {
//			CSPSolution Aold = new CSPSolution(A);
//			List<CSPSolution> N = chooseNeighbours(Aold);
//			A = localChoice(N);
//			iteration += 1;
//		} while (iteration < iterations);
		System.out.println("debug");
		return A;
	}

	CSPSolution selectInitialSolution() {
		CSPSolution initialSolution = new CSPSolution(vehicles, deliveryTasks);

		int currentVehicleIndex = 0;
		Vehicle currentVehicle = vehicles.get(currentVehicleIndex);
		int timePoint = 0;
		int timeInTrunk = 1;
		CSPTask previousVehicleTask = null;
		for (Task task : deliveryTasks) {
			int vehicleWeight = weight(initialSolution, currentVehicle, timePoint);
			int currentTaskWeight = task.weight;

			while (currentVehicle.capacity() < vehicleWeight + currentTaskWeight) {
				// get nextVehicle
				previousVehicleTask = null;
				timeInTrunk = 1;
				currentVehicleIndex += 1;
				if (currentVehicleIndex >= vehicles.size()) {
					return null; // no solution
				}
				currentVehicle = vehicles.get(currentVehicleIndex);
				vehicleWeight = weight(initialSolution, currentVehicle, timePoint);
				currentTaskWeight = task.weight;
			}
			;
			CSPTask cspTask = new CSPTask(task, timeInTrunk);
			initialSolution.setTime(cspTask, timePoint);
			initialSolution.setVehicle(cspTask, currentVehicle);
			if (previousVehicleTask == null) {
				initialSolution.setNextTask(currentVehicle, cspTask);
			} else {
				initialSolution.setNextTask(previousVehicleTask, cspTask);
			}
			previousVehicleTask = cspTask;

			timePoint += 1;
//			timeInTrunk += 1;
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
			City homeCity = vehicle.homeCity();
			City nextCity = A.getNextTask(vehicle).task.pickupCity;
			totalCost += homeCity.distanceTo(nextCity) * vehicle.costPerKm();
		}
		for (CSPTask task : A.getAllCSPTasks()) {
			totalCost += distanceNextStep(A, task) * A.getVehicle(task).costPerKm();
		}
		return totalCost;
	}

	double distanceNextStep(CSPSolution A, CSPTask ti) {
		List<City> deliveryCities= getDeliveryCities(A, ti);
		CSPTask tj = A.getNextTask(ti);
		City start = ti.task.pickupCity;
		City end = tj.task.pickupCity;
		
	    Collection<List<City>> deliveryPermutations = Collections2.permutations(deliveryCities);
	    
	    double shortestDelivery = Double.MAX_VALUE;
	    for (List<City> deliveries : deliveryPermutations) {
	        double distance = start.distanceTo(deliveries.get(0));
	    		for(int i=0; i < deliveries.size()-1; i++) {
	    			City city1 = deliveries.get(i);
	    			City city2 = deliveries.get(i+1);
	    			distance += city1.distanceTo(city2);
	    		}
	    		distance += deliveries.get(deliveries.size()-1).distanceTo(end);
	    		
	    		if(distance < shortestDelivery) {
	    			shortestDelivery = distance;
	    		}
	    }

		return shortestDelivery;
	}
	
	List<Task> shortestDeliveryPath(CSPSolution A, CSPTask ti) {
		List<Task> deliveryTasks= getDeliveryTasks(A, ti);
		
		if(deliveryTasks.size() < 2) {
			return deliveryTasks;
		}
		
		CSPTask tj = A.getNextTask(ti);
		City start = ti.task.pickupCity;
		City end = tj.task.pickupCity;
		
	    Collection<List<Task>> deliveryPermutations = Collections2.permutations(deliveryTasks);
	    
	    double shortestDelivery = Double.MAX_VALUE;
	    List<Task> shortestTaskPath = null;
	    for (List<Task> tasks : deliveryPermutations) {
	        double distance = start.distanceTo(tasks.get(0).deliveryCity);
	    		for(int i=0; i < tasks.size()-1; i++) {
	    			Task task1 = tasks.get(i);
	    			Task task2 = tasks.get(i+1);
	    			distance += task1.deliveryCity.distanceTo(task2.deliveryCity);
	    		}
	    		distance += tasks.get(tasks.size()-1).deliveryCity.distanceTo(end);
	    		
	    		if(distance < shortestDelivery) {
	    			shortestDelivery = distance;
	    			shortestTaskPath = tasks;
	    		}
	    }

		return shortestTaskPath;
	}

	List<City> getDeliveryCities(CSPSolution A, CSPTask task) {
		List<City> deliveries = new ArrayList<City>();
		int taskTime = A.getTime(task);
		Vehicle v = A.getVehicle(task);
		CSPTask current = A.getNextTask(v);
		for (int time = 1; time < taskTime; time++) {
			if (current.timeInTrunk - (taskTime - time) == 1) {
				deliveries.add(current.task.deliveryCity);
			}
		}
		return deliveries;
	}
	
	List<Task> getDeliveryTasks(CSPSolution A, CSPTask task) {
		List<Task> deliveries = new ArrayList<Task>();
		int taskTime = A.getTime(task);
		Vehicle v = A.getVehicle(task);
		CSPTask current = A.getNextTask(v);
		for (int time = 0; time < taskTime+1; time++) {
			if (current.timeInTrunk - (taskTime - time) == 1) {
				deliveries.add(current.task);
			}
		}
		return deliveries;
	}

	List<CSPSolution> chooseNeighbours(CSPSolution Aold) {
		List<CSPSolution> N = new ArrayList<CSPSolution>();
		CSPTask vehicleTask = null;
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
			CSPTask t = Aold.getNextTask(vi);
			// if (load(t) <= capacity(vj)) I dont think its needed
			CSPSolution A = changeVehicle(Aold, vi, vj);
			if (A != null) {
				N.add(A);
			}
		}

		// Applying the changing task order operator
		// compute the number of tasks of vehicle
		int length = 0;
		CSPTask t = Aold.getNextTask(vi);
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
		CSPSolution A1 = A;
		CSPTask t = A.getNextTask(v1);
		int v1TimeInTrunk = t.timeInTrunk; // Added
		CSPTask tForV1 = A.getNextTask(t);
		tForV1.timeInTrunk = v1TimeInTrunk; // Added

		A1.setNextTask(v1, tForV1);
		A1.setNextTask(t, A1.getNextTask(v2));
		t.timeInTrunk = 1;
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
		CSPSolution A1 = A;
		CSPTask t1 = A1.getNextTask(vi);
		int count = 1;
		CSPTask tPre1 = null;
		while (count < tIdx1) {
			tPre1 = t1;
			t1 = A1.getNextTask(t1);
			count += 1;
		}

		CSPTask tPost1 = A1.getNextTask(t1);
		CSPTask tPre2 = t1;
		CSPTask t2 = A1.getNextTask(tPre2);
		// imho cspTask.time should be updated here as well
		count += 1;
		while (count < tIdx2) {
			tPre2 = t2;
			t2 = A1.getNextTask(t2);
			count += 1;
		}
		CSPTask tPost2 = A1.getNextTask(t2);
		// imho cspTask.time should be updated here as well
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

				t1.timeInTrunk = tit1;
				t2.timeInTrunk = tit2;
				if (checkIfPossibeSolution(A1, vi)) {
					N.add(A1);
				}
			}
		}
		return N;
	}

	int weight(CSPSolution A, Vehicle v, int timePoint) {
		int weight = 0;
		CSPTask current = A.getNextTask(v);
		for (int i = 1; i < timePoint + 1; i++) {
			if (current.timeInTrunk > timePoint - i) {
				weight += current.task.weight;
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
		CSPTask taskI = A.getNextTask(v);
		if (taskI != null) {
			A.setTime(taskI, 1);
			CSPTask taskJ = null;
			do {
				taskJ = A.getNextTask(taskI);
				if (taskJ != null) {
					int newTime = A.getTime(taskI) + 1;
					A.setTime(taskJ, newTime);
					taskI = taskJ;
				}
			} while (taskJ != null);
		}
	}

	int amountOfTasks(CSPSolution A, Vehicle v) {
		int amountOfTasks = 0;
		CSPTask current = A.getNextTask(v);
		while (current != null) {
			amountOfTasks += 1;
			current = A.getNextTask(current);
		}
		return amountOfTasks;
	}

}
