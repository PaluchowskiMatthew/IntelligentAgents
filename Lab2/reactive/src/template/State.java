package template;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

public class State {

	// 
	
	
	private Vehicle currentVehicle;	
	private Task currentTask;
	private boolean isTaskAvailable;
	
	public State(Vehicle currentVehicle, Task currentTask, boolean isTaskAvailable) {
		this.currentVehicle = currentVehicle;
		this.currentTask = currentTask;
		this.isTaskAvailable = isTaskAvailable;
	}

	public Vehicle getCurrentVehicle() {
		return currentVehicle;
	}

	public void setCurrentVehicle(Vehicle currentVehicle) {
		this.currentVehicle = currentVehicle;
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}

	public boolean isTaskAvailable() {
		return isTaskAvailable;
	}

	public void setTaskAvailable(boolean isTaskAvailable) {
		this.isTaskAvailable = isTaskAvailable;
	}
	
}
