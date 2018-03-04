package pathCalculation.recursiveBreadthFirst;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import database.ClosestAirports;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class RecursiveBreadthSearch {

	protected static final Logger logger = LogManager.getLogger(RecursiveBreadthSearch.class);
	
	public RecursiveBreadthSearch(){
		
	}
	
	@SuppressWarnings("null")
	public LinkedBlockingQueue<Connection> getHotspotPath(Request request) throws Exception{
		
		long startTime = System.nanoTime();
		Connection[] originAirports = null;
		Connection[] destinationAirports = null;
		ControlObject controlObject;
		
		try {
			Connection[][] airports = calculateClosestAirportConnections(request);
			originAirports = airports[0];
			destinationAirports = airports[1];
		} catch (InterruptedException | ExecutionException e) {
			logger.error("It was not possible to get the closest Airports for origin or destination place!\n" + e);
			throw new Exception("Not possible to calculate path because of previous Exceptions");
		}
		
		controlObject = generateControlObject(request, originAirports, destinationAirports);
		
		//für jede connection rufe die rekursion auf
		Arrays.stream(originAirports).parallel().forEach(connection -> {
			Thread thread = new Thread(new SearchNode(controlObject, connection));
			thread.start();
			controlObject.getThreadList().add(thread);
		});
		
		waitForJoin(controlObject.getThreadList());
		
		long elapsedTime = System.nanoTime() - startTime;
		System.out.println((double) elapsedTime / 1000000000.0 + " time needed for Recursive search");
		System.out.println(controlObject.getUsedConnectionSet().size() + " connections found.");
		return controlObject.getUsedConnectionQueue();
		//return controlObject.getUnusedConnectionList();
	}
	
	private void waitForJoin(LinkedBlockingQueue<Thread> threadList){
		Thread thread;
		while((thread = threadList.poll()) != null){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private Connection[][] calculateClosestAirportConnections(Request request) throws InterruptedException, ExecutionException{
		ExecutorService executor = Executors.newFixedThreadPool(2);
		ClosestAirports closestAirports = new ClosestAirports();
		Connection[][] airports = new Connection[2][];

		//get 10 closest airports to origin
		Callable<Connection[]> findOriginAirports = () -> {
			boolean placeIsOrigin = true;
			return closestAirports.getFastestAirports(request, request.getOrigin(), placeIsOrigin, request.getAmountOfOriginAirports()); 
		};
		Future<Connection[]> futureOriginAirports = executor.submit(findOriginAirports);
		
		//get 10 closest airports to destination
		Callable<Connection[]> findDestinationAirports = () -> {
			boolean placeIsOrigin = false;
			return closestAirports.getFastestAirports(request, request.getDestination(), placeIsOrigin, request.getAmountOfDestinationAirports());
		};
		Future<Connection[]> futureDestinationAirports = executor.submit(findDestinationAirports);
		
		//get the results
		airports[0] = futureOriginAirports.get();
		airports[1] = futureDestinationAirports.get();
		return airports;
	}
	
	private ControlObject generateControlObject(Request request, Connection[] originAirports, Connection destinationAirports[]){
		ConcurrentHashMap<String, Place> originAirportsMap = new ConcurrentHashMap<String, Place>();
		ConcurrentHashMap<String, Place> destinationAirportsMap = new ConcurrentHashMap<String, Place>();
		
		// generate Hashmaps for origin and destination airports needed for controlObject
		for(Connection connection : originAirports){
			originAirportsMap.put(connection.getDestination().getIata(), connection.getDestination());
		}
		for(Connection connection : destinationAirports){
			destinationAirportsMap.put(connection.getOrigin().getIata(), connection.getOrigin());
		}
		
		return new ControlObject(request, originAirportsMap, destinationAirportsMap);
	}
	
	/**
	 * 
	 * @param connectionList
	 * @param controlObject
	 * @param maxLevel
	 * @return 1 if a connection was found, 0 if no connection was found
	 */
	
	/*
	private LinkedBlockingQueue<Connection> searchNextConnection(LinkedBlockingQueue<Connection> connectionList, ControlObject controlObject, int maxLevel){
		//Iterative search of next hotspot or destination
		for(int level = 0; !controlObject.isConnectionFound() && level < maxLevel; level++){
			System.out.println(level);
			LinkedBlockingQueue<Connection> newConnectionList = new LinkedBlockingQueue<Connection>();
			
			//add next sub connections to connection
			generateNextSubConnection(connectionList, newConnectionList, controlObject, 2);
			//if no connection to hotspots was found search for connection to any airport
			if(newConnectionList.isEmpty()){
				generateNextSubConnection(connectionList, newConnectionList, controlObject, 1);
			}
			//if still no connection was found return null
			if(newConnectionList.isEmpty()){
				return null;
			}
			connectionList = newConnectionList;
		}
		
		// in the case that no connection was found
		if(!controlObject.isConnectionFound())
			return null;
						
		return connectionList;
	}
	*/
	/**
	 * for each connection add next sub connections to connection
	 * @param oldConnectionList
	 * @param newConnectionList
	 * @param controlObject
	 * @param method 1: all outbound connections, 2: hotspots only, 3: destination hash data only
	 */
	/*
	private void generateNextSubConnection(LinkedBlockingQueue<Connection> oldConnectionList, LinkedBlockingQueue<Connection> newConnectionList, ControlObject controlObject, int method){
		oldConnectionList.parallelStream().forEach(connection -> {
			//??????? wofür ist dieses if ????????
			if(connection.getAction().equals(Connection.ADD) || connection.getAction().equals(Connection.UNUSED)){
				SearchNode searchNode = new SearchNode();
				newConnectionList.addAll(searchNode.getNextConnections(controlObject, connection.clone(), method));	
			}
		});
	}
	*/
	
	/**
	 * Generates the headConnection and
	 * set a temporal connection to the start airport.
	 * This tmp connection is needed by the SearchNode class to calculate the following flights.
	 * Only the destination and arrivalDate parameters are required.
	 * @param request
	 * @param headConnection
	 * @param originAirport
	 */
	private Connection generateHeadConnection(Request request, Place originAirport){
		Connection headConnection = new Connection(request.getOrigin(), originAirport);
		headConnection.setArrivalDate(request.getDepartureDateString());
		Connection tmpStartConnection = new Connection(request.getOrigin(), originAirport);
		tmpStartConnection.setArrivalDate(request.getDepartureDateString());
		headConnection.getSubConnections().add(tmpStartConnection);
		return headConnection;
	}	
}
