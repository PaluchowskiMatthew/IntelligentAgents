package template;

import java.util.HashMap;
import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class CSPSolution {
	// Do we actually need this split into tasks for vehicle and for action?
		HashMap<Vehicle, CSPTask> cspTasksForVehicleAction = new HashMap<Vehicle, CSPTask>();
		HashMap<CSPTask, CSPTask> cspTasksForCSPTask = new HashMap<CSPTask, CSPTask>();
		HashMap<CSPTask, Integer> time = new HashMap<CSPTask, Integer>();
		HashMap<CSPTask, Vehicle> vehicle = new HashMap<CSPTask, Vehicle>();

		
		public CSPSolution(List<Vehicle> vehicles, TaskSet deliveryTasks) {
			// initialize HashMaps
			for(Vehicle v: vehicles) {
				cspTasksForVehicleAction.put(v, null);
			}
			for(Task task: deliveryTasks) {
				for(int i=0; i< deliveryTasks.size()+1; i++) {
					CSPTask cspTask = new CSPTask(task, i);
					cspTasksForCSPTask.put(cspTask, null);
					time.put(cspTask, 0);
				}
			}
		}
		
		// Copy constructor
		public CSPSolution(CSPSolution differentSolution) {
			this.cspTasksForVehicleAction = differentSolution.cspTasksForVehicleAction;
			this.cspTasksForCSPTask = differentSolution.cspTasksForCSPTask;
			this.time = differentSolution.time;
			this.vehicle = differentSolution.vehicle;

		}
		
		CSPTask	getNextTask(Vehicle v) {
			return cspTasksForVehicleAction.get(v);
		}
		
		void setNextTask(Vehicle v, CSPTask cspTask) {
			cspTasksForVehicleAction.put(v, cspTask);
		}
		
		CSPTask	getNextTask(CSPTask cspTask) {
			return cspTasksForCSPTask.get(cspTask);
		}
		
		void setNextTask(CSPTask key, CSPTask cspTask) {
			cspTasksForCSPTask.put(key, cspTask);
		}
		
		int	getTime(CSPTask cspTask) {
			return time.get(cspTask);
		}
		
		void setTime(CSPTask key, Integer t) {
			time.put(key, t);
		}
		
		Vehicle	getVehicle(CSPTask cspTask) {
			return vehicle.get(cspTask);
		}
		
		void setVehicle(CSPTask key, Vehicle v) {
			vehicle.put(key, v);
		}
		
}
