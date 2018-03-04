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
	
	/**
	 * Wait until all Threads are finished
	 * @param threadList
	 */
	private void waitForJoin(LinkedBlockingQueue<Thread> threadList){
		Thread thread;
		while((thread = threadList.poll()) != null){
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.warn("It as not possible to join all threads. Maybe the algorithm was not doen before terminating.\n" + e);
			}
		}
	}
	
	/**
	 * Calculates wich are the closest airports to the origin and destination point (using fastest route)
	 * and returns an array of the connections to this airports
	 * @param request
	 * @return an array with connection arrays:
	 * 	airports[0] includes an array of connections to the origin airports and
	 * 	airports[1] includes an array of connections from the destination airports
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
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
	
	/**
	 * Generates HashMaps for the origin and destination airports and generates the ControlObject.
	 * @param request
	 * @param originAirports Array with the connections to the origin airports.
	 * @param destinationAirports Array with the connections from the destination airports.
	 * @return
	 */
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
}
