package pathCalculation.breadthFirstSearch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import utilities.Connection;

public class ControlObject {

	private ConcurrentHashMap<String, Boolean> airportsInTree = new ConcurrentHashMap<String, Boolean>();
	private LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
	private AtomicInteger threads = new AtomicInteger(0);
	private AtomicInteger threadsRunning = new AtomicInteger(0);
	private AtomicInteger level = new AtomicInteger(0);
	private boolean connectionFound = false;
	
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
	

	
}
