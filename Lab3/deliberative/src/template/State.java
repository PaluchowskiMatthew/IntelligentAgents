package template;

import java.util.ArrayList;
import java.util.List;

import logist.simulation.Vehicle;
import logist.plan.Action;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import logist.plan.Plan;


public class State {	
	City currentCity; // current city where vehicle is located
	TaskSet vehicleTasks; // tasks picked up by vehicle
	TaskSet topologyTasks; // tasks awaiting to be delivered
	Plan plan;
	
	public State(City currentCity, TaskSet vehicleTasks, TaskSet topologyTasks, Plan plan) {
		this.currentCity = currentCity;
		this.vehicleTasks = vehicleTasks;
		this.topologyTasks = topologyTasks;
		this.plan = plan;
	}

	public City getCurrentCity() {
		return currentCity;
	}

	public void setCurrentCity(City currentCity) {
		this.currentCity = currentCity;
	}

	public TaskSet getVehicleTasks() {
		return vehicleTasks;
	}

	public void setVehicleTasks(TaskSet vehicleTasks) {
		this.vehicleTasks = vehicleTasks;
	}

	public TaskSet getTopologyTasks() {
		return topologyTasks;
	}

	public void setTopologyTasks(TaskSet topologyTasks) {
		this.topologyTasks = topologyTasks;
	}
	
	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}
	
	public Boolean isFinalState() {
		return vehicleTasks.isEmpty() && topologyTasks.isEmpty();
	}
	
}
