package pathCalculation.breadthFirstSearch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class ControlObject {

	private ConcurrentHashMap<String, Boolean> airportsInTree = new ConcurrentHashMap<String, Boolean>();
	private LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
	private ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> destinationAirportsMap = new ConcurrentHashMap<String, LinkedBlockingQueue<Connection>>();
	private AtomicInteger threadsRunning = new AtomicInteger(0);
	private boolean connectionFound = false;
	private Place departureAirport = null;
	private Request request = null;
	
	public ControlObject(){
		
	}
	
	/**
	 * 
	 * @param iata iata code of the airport that should be added
	 * @return true if the airport was not in the hash map before | false if the airport was in the hash map already.
	 */
	public boolean addAirportToTree(String iata){
		if(airportsInTree.put(iata, true) == null)
			return true;
		else
			return false;
	}
	
	public boolean isAirportInTree(String iata){
		if(airportsInTree.get(iata) == null)
			return false;
		else
			return true;
	}
	
	public boolean isDepartureAirportIATA(String iata){
		if(departureAirport.getIata().equals(iata)){
			return true;
		}else{
			return false;
		}
	}
	
	public void incrementRunningThreads(){
		threadsRunning.incrementAndGet();
	}
	
	public void decrementRunningThreads(){
		threadsRunning.decrementAndGet();
	}
	
	public int threadsRunning(){
		return threadsRunning.get();
	}
	
	public boolean hasRunningThreads(){
		if(threadsRunning.get() > 0)
			return true;
		else
			return false;
	}
	
	public void setConnectionIsFound(){
		connectionFound = true;
	}
	
	public boolean isConnectionFound(){
		return connectionFound;
	}
	
	
	public LinkedBlockingQueue<Connection> getConnectionList(){
		return connectionList;
	}
	
	
	public void addConnection(Connection con){
		connectionList.add(con);
	}

	/**
	 * @return the departureAirport
	 */
	public Place getDepartureAirport() {
		return departureAirport;
	}

	/**
	 * @param departureAirport the departureAirport to set
	 */
	public void setDepartureAirport(Place departureAirport) {
		this.departureAirport = departureAirport;
	}

	/**
	 * @return the request
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(Request request) {
		this.request = request;
	}
	
	public void setDestinationAirportsMap(ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> destinationAirportsMap){
		this.destinationAirportsMap = destinationAirportsMap;
	}
	
	public ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> getDestinationAirportsMap(){
		return destinationAirportsMap;
	}
	
	public boolean isDestinationAirport(String iata){
		
		if(destinationAirportsMap.containsKey(iata))
			return true;
		else
			return false;
	}
	
	

	
}
