package database.utilities;

import org.joda.time.Duration;

import utilities.Connection;
import utilities.Place;

public class ClosestAirportListElement{
	
	private Place place;
	private Place airport;
	private int beeline;
	private int distance;
	private Duration duration;
	private Connection connection;
	
	/**
	 * @return the place
	 */
	public Place getPlace() {
		return place;
	}
	/**
	 * @return the airport
	 */
	public Place getAirport() {
		return airport;
	}
	/**
	 * @return the beeline
	 */
	public int getBeeline() {
		return beeline;
	}
	/**
	 * @return the distance
	 */
	public int getDistance() {
		return distance;
	}
	/**
	 * @return the duration
	 */
	public Duration getDuration() {
		return duration;
	}
	
	
	
	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}
	/**
	 * @param connection the connection to set
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	/**
	 * @param place the place to set
	 */
	public void setPlace(Place place) {
		this.place = place;
	}
	/**
	 * @param airport the airport to set
	 */
	public void setAirport(Place airport) {
		this.airport = airport;
	}
	/**
	 * @param beeline the beeline to set
	 */
	public void setBeeline(int beeline) {
		this.beeline = beeline;
	}
	/**
	 * @param distance the distance to set
	 */
	public void setDistance(int distance) {
		this.distance = distance;
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(Duration duration) {
		this.duration = duration;
	}
	
	
	
	

}

