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
	private Request request = null;
	private Place originAirport = null;
	private Place destinationAirport = null;
	
	public ControlObject(){
		
	}
	
	public ControlObject(Request request, Place originAirport, Place destinationAirport){
		this.request = request;
		this.originAirport = originAirport;
		this.destinationAirport = destinationAirport;
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
	
	/**
	 * @return the originAirport
	 */
	public Place getOriginAirport() {
		return originAirport;
	}

	/**
	 * @return the destinationAirport
	 */
	public Place getDestinationAirport() {
		return destinationAirport;
	}

	/**
	 * @param originAirport the originAirport to set
	 */
	public void setOriginAirport(Place originAirport) {
		this.originAirport = originAirport;
	}

	/**
	 * @param destinationAirport the destinationAirport to set
	 */
	public void setDestinationAirport(Place destinationAirport) {
		this.destinationAirport = destinationAirport;
	}

	public boolean isAirportInTree(String iata){
		if(airportsInTree.get(iata) == null)
			return false;
		else
			return true;
	}
	
	public boolean isDestinationAirport(String iata){
		if(destinationAirport.getIata().equals(iata)){
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
		this.setConnectionIsFound();
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
	
	public boolean isPathToDestinationAirportKnown(String iata){
		if(destinationAirportsMap.containsKey(iata) || destinationAirport.getIata().equals(iata))
			return true;
		else
			return false;
	}
	
	public LinkedBlockingQueue<Connection> getFlightsFromOneDestinationAirport(String iata){
		return destinationAirportsMap.get(iata);
	}

	
}
