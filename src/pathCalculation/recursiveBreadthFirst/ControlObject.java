package pathCalculation.recursiveBreadthFirst;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import database.Query;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class ControlObject {
	
	public static final int TO_MANY_STEPS = 0;
	public static final int LAST_CONNECTION_DESTINATION = 1;
	public static final int TO_FAR_FROM_DESTINATION = 2;
	public static final int CONNECTION_IS_WORSE = 3;
	public static final int BETTER_CONNECTION_TO_AIRPORT = 4;
	public static final int CONNECTION_FOUND = 5;

	//caches all connection for an airport
	private Request request = null;
	private LinkedBlockingQueue<Thread> threadList = new LinkedBlockingQueue<Thread>();
	private ConcurrentHashMap<String, AirportInfo> airportsMap = new ConcurrentHashMap<String, AirportInfo>();
	private LinkedBlockingQueue<Connection> unusedConnectionList = new LinkedBlockingQueue<Connection>();
	private ConcurrentSkipListSet<Connection> usedConnectionSet = null;
	private boolean connectionFound = false;
	//private ReentrantLock lock1 = new ReentrantLock();
	private ReentrantLock lock2 = new ReentrantLock();
	private ReentrantLock lock3 = new ReentrantLock();
	private ConcurrentHashMap<String, Place> originAirports = null;
	private ConcurrentHashMap<String, Place> destinationAirports = null;
	private int beelineDistance = Integer.MAX_VALUE;
	private double factorForMaxDistance = Double.MAX_VALUE;
	private AtomicInteger[][] terminationReasons = new AtomicInteger[6][20];
	private AtomicInteger GoogleApiCallCounter = new AtomicInteger(0);
	
	
	public ControlObject(Request request, ConcurrentHashMap<String, Place> originAirports, ConcurrentHashMap<String, Place> destinationAirports){
		this.request = request;
		this.originAirports = originAirports;
		this.destinationAirports = destinationAirports;
		// generates the list for the used connections and sets the price for an hour value to the Comparator object
		usedConnectionSet = new ConcurrentSkipListSet<Connection>(new ConnectionComparator(request.getPriceForHoure()));
		this.beelineDistance = Query.getDistanceBetweenPlaces(request.getOrigin(), request.getDestination());
		this.factorForMaxDistance = (double) (2*request.getAvgSpeed()*request.getAvgSpeed()) / (double) beelineDistance;
		for(int i = 0; i < terminationReasons.length; i++){
			for(int j = 0; j < terminationReasons[i].length; j++){
				terminationReasons[i][j] = new AtomicInteger(0);
				//System.out.println(i + ":" + j + " = " + terminationReasons[i][j].get());
			}
		}
		System.out.println(request.getAvgSpeed());
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
	
	public int getBeelineDistance(){
		return beelineDistance;
	}
	
	public double getFactorForMaxDistance(){
		return factorForMaxDistance;
	}
	
	public void incrementGoogleApiCounter(){
		GoogleApiCallCounter.getAndIncrement();
	}
	
	public int getGoogleApiCount(){
		return GoogleApiCallCounter.get();
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
		//con.setRecursiveAction(Connection.UNUSED);
		//unusedConnectionList.add(con);
	}
	
	public void setUsedConnectionList(LinkedBlockingQueue<Connection> connectionList){
		connectionList.parallelStream().forEach(connection -> {
			usedConnectionSet.add(connection);
		});
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
	
	/* ----------- get minimum values for used connections list ------------ */
	
	public boolean isVirtualPriceToHigth(Connection connection){
		if(!isUsedConnectionSetFull()){
			return false;
		}
		if(connection.getVirtualPrice(request.getPriceForHoure()) < getMinimumVirtualPrice()) 
			return false;
		return true;
	}
	
	public double getMinimumVirtualPrice(){
		return usedConnectionSet.last().getVirtualPrice(request.getPriceForHoure());
	}
	
	public boolean isUsedConnectionSetFull(){
		if(usedConnectionSet.size() >= request.getAmountOfConnectionsToShow())
			return true;
		return false;
	}
	
	/* ----------- airportMap methods ------------ */
	
	/**
	 * Adds the airport to the airportMap if it is not in the map already
	 * @param iata iata code of the airport that should be added
	 * @return true if the airport was not in the hash map before | false if the airport was in the hash map already.
	 */
	public void addAirportToMap(Connection connection){
		String iataCode = connection.getDestination().getIata();
		if(!airportsMap.containsKey(iataCode)){
			AirportInfo airportInfo = new AirportInfo(iataCode, request.getDestination());
			airportsMap.putIfAbsent(iataCode, airportInfo);
		}
	}
	
	/**
	 * 
	 * @param connection Connection to the airport
	 * @return
	 */
	public boolean addConnectionToAirportInfo(Connection connection){
		boolean result = false;
		addAirportToMap(connection);
		AirportInfo airportInfo = airportsMap.get(connection.getDestination().getIata());
		result = airportInfo.addIfNoBetterConnectionIsAvailable(connection.getArrivalDate(), connection.getVirtualPrice(request.getPriceForHoure()));
		return result;
	}
	
	public AirportInfo getAirportinfo(String iata){
		return airportsMap.get(iata);
	}
	
	public int countVisitedAirports(){
		return airportsMap.size();
	}
	
	/* ----------- Termination Reasons Array ------------ */
	
	public void increaseCounter(int counterNumber, Connection connection){
		increaseCounter(counterNumber, connection.getSubConnections().size() - 1);
	}
	
	public void increaseCounter(int counterNumber, int step){
		terminationReasons[counterNumber][step].incrementAndGet();
	}
	
	public String terminationReasonsToString(){
		String delimiter = "\t";
		return "--------------------------------------------------------------\n"
				+ "Step: \t\t\t\t\t\t\t1\t2\t3\t4\t5\t6\t7\t8\t9\t10\t11\t12\t13\t14\t15\t16\t17\t18\t19\t20\tTotal\n"
				+ "It exists a better connection to the same airport: \t" + stringForCounter(ControlObject.BETTER_CONNECTION_TO_AIRPORT, delimiter) + "\n"
				+ "To many steps: \t\t\t\t\t\t" + stringForCounter(ControlObject.TO_MANY_STEPS, delimiter) + "\n"
				+ "Last connection was destination: \t\t\t" + stringForCounter(ControlObject.LAST_CONNECTION_DESTINATION, delimiter) + "\n"
				+ "Connection is worse than the x best: \t\t\t" + stringForCounter(ControlObject.CONNECTION_IS_WORSE, delimiter) + "\n"
				+ "Airport is to far from Destination: \t\t\t" + stringForCounter(ControlObject.TO_FAR_FROM_DESTINATION, delimiter) + "\n"
				+ "Connections found: \t\t\t\t\t" + stringForCounter(ControlObject.CONNECTION_FOUND, delimiter) + "\n"
				+ "--------------------------------------------------------------";
	}
	
	private String stringForCounter(int reason, String delimiter){
		String result = "" + terminationReasons[reason][0].get();
		int total = 0;
		for(int i = 1; i < terminationReasons[reason].length; i++){
			total += terminationReasons[reason][i].get();
			result += delimiter + terminationReasons[reason][i].get();
		}
		result += delimiter + total;
		return result;
	}
	
	
}
