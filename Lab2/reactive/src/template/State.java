package template;

import logist.topology.Topology.City;

public class State {
	
	private City currentCity;	
	private City deliveryCity;
	
	
	public State(City currentCity, City deliveryCity) {
		super();
		this.currentCity = currentCity;
		this.deliveryCity = deliveryCity;
	}


	public City getCurrentCity() {
		return currentCity;
	}


	public void setCurrentCity(City currentCity) {
		this.currentCity = currentCity;
	}


	public City getDeliveryCity() {
		return deliveryCity;
	}


	public void setDeliveryCity(City deliveryCity) {
		this.deliveryCity = deliveryCity;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentCity == null) ? 0 : currentCity.hashCode());
		result = prime * result + ((deliveryCity == null) ? 0 : deliveryCity.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (currentCity == null) {
			if (other.currentCity != null)
				return false;
		} else if (!currentCity.equals(other.currentCity))
			return false;
		if (deliveryCity == null) {
			if (other.deliveryCity != null)
				return false;
		} else if (!deliveryCity.equals(other.deliveryCity))
			return false;
		return true;
	} 
	
	
	
}
