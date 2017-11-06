package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class CSP {
	List<Vehicle> vehicles;
	TaskSet deliveryTasks;
	
	// If there are multiple equally good assignments, 
	// it chooses one randomly. Then with probability p it returns A, 
	// with probability 1 − p it returns the current assignment Aold.
	float probability;
	int iterations;
	

	public CSP(List<Vehicle> vehicles, TaskSet deliveryTasks, float p, int iterations) {
		this.vehicles = vehicles;
		this.deliveryTasks = deliveryTasks;
		this.probability = p;
		this.iterations = iterations;
	}

	public CSPSolution createCentralizedPlan() {
		CSPSolution A = selectInitialSolution();
		int iteration = 1;
		do {
			CSPSolution Aold = new CSPSolution(A);
			List<CSPSolution> N = chooseNeighbours(Aold);
			A = localChoice(N);
			iteration += 1;
		}
		while (iteration < iterations);
		return A;
	}
	
	CSPSolution selectInitialSolution() {
		// TODO
		return null;
	}
	
	CSPSolution localChoice(List<CSPSolution> N) {
		// TODO
		return null;
	}
	
	int calculateTotalCost(CSPSolution A) {
		// TODO
		return 0;
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
			for(int tIdx1=1; tIdx1 <= length-1; tIdx1++) {
				for(int tIdx2=tIdx1+1; tIdx2 <= length; tIdx2++) {
					CSPSolution A = changeTaskOrder(Aold, vi, tIdx1, tIdx2);
					if (A != null) {
						N.add(A);
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
			if(tPre1 == null) {
				A1.setNextTask(vi, t2);
			}
			else {
				A1.setNextTask(tPre1, t2);
			}
			
			A1.setNextTask(t2, t1);
			A1.setNextTask(t1, tPost2);
		} else {
			if(tPre1 == null) {
				A1.setNextTask(vi, t2);
			}
			else {
				A1.setNextTask(tPre1, t2);
			}
			A1.setNextTask(tPre2, t1);
			A1.setNextTask(t2, tPost1);
			A1.setNextTask(t1, tPost2);
		}
		updateTime(A1, vi);
		
		for(int tit1 = 1; tit1 < (nbTasks - tIdx1 + 1); tit1 ++) {
			for(int tit2 = 1; tit2 < (nbTasks - tIdx2 + 1); tit2 ++) {
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
