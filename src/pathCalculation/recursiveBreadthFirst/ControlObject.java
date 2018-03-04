package pathCalculation.recursiveBreadthFirst;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class ControlObject {

	//caches all connection for an airport
	private Request request = null;
	LinkedBlockingQueue<Thread> threadList = new LinkedBlockingQueue<Thread>();
	private ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> airportsInTree = new ConcurrentHashMap<String, LinkedBlockingQueue<Connection>>();
	private LinkedBlockingQueue<Connection> unusedConnectionList = new LinkedBlockingQueue<Connection>();
	private ConcurrentSkipListSet<Connection> usedConnectionSet = null;
	private boolean connectionFound = false;
	private ReentrantLock lock2 = new ReentrantLock();
	private ReentrantLock lock3 = new ReentrantLock();
	ConcurrentHashMap<String, Place> originAirports = null;
	ConcurrentHashMap<String, Place> destinationAirports = null;
	
	
	public ControlObject(Request request, ConcurrentHashMap<String, Place> originAirports, ConcurrentHashMap<String, Place> destinationAirports){
		this.request = request;
		this.originAirports = originAirports;
		this.destinationAirports = destinationAirports;
		// generates the list for the used connections and sets the price for an hour value to the Comparator object
		usedConnectionSet = new ConcurrentSkipListSet<Connection>(new ConnectionComparator(request.getPriceForHoure()));
	}
	
	/**
	 * 
	 * @param iata iata code of the airport that should be added
	 * @return true if the airport was not in the hash map before | false if the airport was in the hash map already.
	 */
	public boolean addAirportToTree(String iata, LinkedBlockingQueue<Connection> connectionList){
		if(airportsInTree.put(iata, connectionList) == null)
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
	
	/* -------------------- used and unused connection Lists -----------------------------*/
	
	public LinkedBlockingQueue<Connection> getUnusedConnectionList(){
		return unusedConnectionList;
	}
	
	public ConcurrentSkipListSet<Connection> getUsedConnectionSet(){
		return usedConnectionSet;
	}
	
	public LinkedBlockingQueue<Connection> getUsedConnectionQueue(){
		LinkedBlockingQueue<Connection> connectionQueue= new LinkedBlockingQueue<Connection>();
		connectionQueue.addAll(usedConnectionSet);
		return connectionQueue;
	}
	
	/**
	 * if there is an element smaller than con or the list is not full already, add con to the set and remove the lowest element from the list and add it to unused elements
	 * Otherwise add the connection to the unused list
	 * sets the connection as ADD if it will be added
	 * @param con Connection that will be added
	 */
	public void addUsedConnection(Connection con){
		lock2.lock();
		if(usedConnectionSet.size() < request.getAmountOfConnectionsToShow() ||  usedConnectionSet.ceiling(con) != null){
			con.setRecursiveAction(Connection.ADD);
			usedConnectionSet.add(con.clone());
			lock2.unlock();
			lock3.lock();
			if(usedConnectionSet.size() > request.getAmountOfConnectionsToShow()){
				Connection newUnusedConnection = usedConnectionSet.pollLast();
				lock3.unlock();
				if(newUnusedConnection != null){
					addUnusedConnection(newUnusedConnection);
				}
			}else{
				lock3.unlock();
			}
		}else{
			lock2.unlock();
			addUnusedConnection(con);
		}
	}
	
	
	public void addUnusedConnection(Connection con){
		con.setRecursiveAction(Connection.UNUSED);
		unusedConnectionList.add(con);
	}
	
	/* ---------------- Thread counter ------------------- */
	
	public LinkedBlockingQueue<Thread> getThreadList(){
		return threadList;
	}

	public Thread getFirstThread(){
		return threadList.poll();
	}
	
	public void addThread(Thread thread){
		threadList.add(thread);
	}
	
	public boolean hasRunningThreads(){
		if(threadList.isEmpty())
			return false;
		else
			return true;
	}
	
	public int threadsRunning(){
		return threadList.size();
	}
	
	/* ----------- Origin and destination airorts ----------------------------*/
	
	public boolean isOriginAirport(String iata){
		return originAirports.containsKey(iata);
	}
	
	public boolean isDestinationAirport(String iata){
		return destinationAirports.containsKey(iata);
	}
}