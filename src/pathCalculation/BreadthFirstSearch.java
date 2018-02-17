package pathCalculation;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import database.ClosestAirports;
import database.QueryAllConnectionsFromAirport;
import pathCalculation.breadthFirstSearch.ControlObject;
import pathCalculation.breadthFirstSearch.SearchNode;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class BreadthFirstSearch {

	protected static final Logger logger = LogManager.getLogger(BreadthFirstSearch.class);
	
	public BreadthFirstSearch(){
		
	}
	
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request) throws IllegalStateException{
		
		long startTime = System.nanoTime();
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		ControlObject controlObject = new ControlObject();
		ClosestAirports closestAirports = new ClosestAirports();
		Place originAirport = closestAirports.getClosestBeelineAirport(request.getOrigin());
		Place destinationAirport = closestAirports.getClosestBeelineAirport(request.getDestination());
		
		controlObject.setDepartureAirport(destinationAirport);
		controlObject.setRequest(request);
		
		Connection headConnection = new Connection(request.getOrigin(), originAirport);
		headConnection.setArrivalDate(request.getDepartureDateString());
		// set a temporal connection to the startairport. This connection is needed by the SearchNode class to calculate the following flights.  Only the destination and arrivalDate parameters are required.
		Connection tmpStartConnection = new Connection(request.getOrigin(), originAirport);
		tmpStartConnection.setArrivalDate(request.getDepartureDateString());
		headConnection.getSubConnections().add(tmpStartConnection);
		
		try {
			connectionList.add(headConnection);
			
			int level = 0;
			while(!controlObject.isConnectionFound() && level < 10){
				LinkedBlockingQueue<Connection> tmpConnectionList = new LinkedBlockingQueue<Connection>();
				System.out.println(level++);
				connectionList.parallelStream().forEach(connection -> {
					if(connection.getAction().equals(Connection.ADD) || connection.getAction().equals(Connection.UNUSED)){
						SearchNode searchNode = new SearchNode();
						tmpConnectionList.addAll(searchNode.getNextConnections(controlObject, connection.clone(), destinationAirport));
					}
				});
				connectionList = tmpConnectionList;
			}
			
			// in the case that no connection was found
			if(!controlObject.isConnectionFound())
				return null;
						
		
		} catch (IllegalStateException e) {
			logger.warn("Connectionf for Breadth first Search can't be calculated." + e);
			throw new IllegalStateException("Connectionf for Breadth first Search can't be calculated." + e);
		}
		
		
		long elapsedTime = System.nanoTime() - startTime;
		System.out.println((double) elapsedTime / 1000000000.0);
		return connectionList;
	}
}
