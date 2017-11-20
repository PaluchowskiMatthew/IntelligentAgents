package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class CSPSolution {
	// Do we actually need this split into tasks for vehicle and for action?
		HashMap<Vehicle, Task> TasksForVehicleAction = new HashMap<Vehicle, Task>();
		HashMap<Task, Task> TasksForTask = new HashMap<Task, Task>();
		HashMap<Task, Integer> time = new HashMap<Task, Integer>();
		HashMap<Task, Integer> timeInTrunk = new HashMap<Task, Integer>();
		HashMap<Task, Vehicle> vehicle = new HashMap<Task, Vehicle>();

		
		public CSPSolution(List<Vehicle> vehicles, TaskSet deliveryTasks) {
			// initialize HashMaps
			for(Vehicle v: vehicles) {
				TasksForVehicleAction.put(v, null);
			}
			for(Task task: deliveryTasks) {
					TasksForTask.put(task, null);
					time.put(task, 1);
					timeInTrunk.put(task, 1);
					vehicle.put(task, null);
			}
		}
		
		// Copy constructor
		public CSPSolution(CSPSolution differentSolution) {
			this.TasksForVehicleAction = new HashMap<Vehicle, Task>(differentSolution.TasksForVehicleAction);
			this.TasksForTask = new HashMap<Task, Task>(differentSolution.TasksForTask);
			this.time = new HashMap<Task, Integer>(differentSolution.time);
			this.vehicle = new HashMap<Task, Vehicle>(differentSolution.vehicle);
			this.timeInTrunk = new HashMap<Task, Integer>(differentSolution.timeInTrunk);
		}
		
		public List<Vehicle> getInvolvedVehicles(){
			List<Vehicle> vehiclesInvolved = new ArrayList<Vehicle>();
			
			for (Vehicle keyVehicle: TasksForVehicleAction.keySet()) {
				Task t = TasksForVehicleAction.get(keyVehicle);
				if(t != null && !vehiclesInvolved.contains(keyVehicle)) {
					vehiclesInvolved.add(keyVehicle);
				}
			}
			return vehiclesInvolved;
		}
		
		public List<Task> getAllTasks(){
			List<Task> Tasks = new ArrayList<Task>();
			
			for (Task key: vehicle.keySet()) {
				if(key != null) {
					Tasks.add(key);
				}
			}
			
			return Tasks;
		}
		
		public Task getNextTask(Vehicle v) {
			return TasksForVehicleAction.get(v);
		}
		
		public void setNextTask(Vehicle v, Task Task) {
			TasksForVehicleAction.put(v, Task);
		}
		
		public Task	getNextTask(Task Task) {
			return TasksForTask.get(Task);
		}
		
		public void setNextTask(Task key, Task Task) {
			TasksForTask.put(key, Task);
		}
		
		public int	getTime(Task Task) {
			return time.get(Task);
		}
		
		public void setTime(Task key, int t) {
			time.put(key, t);
		}
		
		public Vehicle	getVehicle(Task Task) {
			return vehicle.get(Task);
		}
		
		public void setVehicle(Task key, Vehicle v) {
			vehicle.put(key, v);
		}

		
		public int	getTimeInTrunk(Task Task) {
			return timeInTrunk.get(Task);
		}
		
		public void setTimeInTrunk(Task key, int t) {
			timeInTrunk.put(key, t);
		}
				
}
