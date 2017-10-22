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
	State parentState = null; // backtrack the states
	
	City currentCity; // current city where vehicle is located
	TaskSet vehicleTasks; // tasks picked up by vehicle
	TaskSet topologyTasks; // tasks awaiting to be delivered
	
	
	public State(State parentState, City currentCity, TaskSet vehicleTasks, TaskSet topologyTasks) {
		this.parentState = parentState;
		this.currentCity = currentCity;
		this.vehicleTasks = vehicleTasks;
		this.topologyTasks = topologyTasks;
	}

	public State getParentState() {
		return parentState;
	}

	public void setParentState(State parentState) {
		this.parentState = parentState;
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
	
	
}
