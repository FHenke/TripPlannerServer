package pathCalculation.recursiveBreadthFirst;

import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;

import database.Query;
import utilities.Connection;
import utilities.Place;

public class AirportInfo {

	
	private String iata;
	private int distanceToDestination = Integer.MAX_VALUE;
	private ConcurrentSkipListMap<GregorianCalendar, Double> connectionToAirport = new ConcurrentSkipListMap<GregorianCalendar, Double>();
	private ReentrantLock lock1 = new ReentrantLock();
	
	public AirportInfo(String iata, Place destination){
		this.iata = iata;
		this.distanceToDestination = Query.getDistanceBeetweenAirportAndPlace(iata, destination);
	}
	
	public void setDistanceToDestination(int distanceToDestination){
		this.distanceToDestination = distanceToDestination;
	}
	
	public int getDistanceToDestination(){
		return distanceToDestination;
	}
	
	public void addConnectionToAirport(GregorianCalendar departureTime, double price){
		connectionToAirport.put(departureTime, price);
	}
	
	/**
	 * Cecks if there is no cheaper connection to this airport before the departure time of this connection.
	 * In this case the flight price and departure date are added to the Map and true is returned otherwise false is returned.
	 * @param departureTime departure time on airport
	 * @param price price to this connection
	 * @return true if there is no cheaper price arriving this airport earlyer than the departure time and false otherwise
	 */
	public boolean addIfNoBetterConnectionIsAvailable(GregorianCalendar departureTime, double price){
		lock1.lock();
		if(connectionToAirport.lowerEntry(departureTime).getValue() > price){
			connectionToAirport.put(departureTime, price);
			lock1.unlock();
			return true;
		}
		lock1.unlock();
		return false;
	}
	

	
	
	
	
	
}
