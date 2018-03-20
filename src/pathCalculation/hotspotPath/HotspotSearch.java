package pathCalculation.hotspotPath;

import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.ClosestAirports;
import pathCalculation.breadthFirstSearch.ControlObject;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class HotspotSearch {

	protected static final Logger logger = LogManager.getLogger(HotspotSearch.class);
	
	public HotspotSearch(){
		
	}
	
	
	public LinkedBlockingQueue<Connection> getHotspotPath(Request request) throws IllegalStateException{
		
		long startTime = System.nanoTime();
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		ClosestAirports closestAirports = new ClosestAirports();
		Place originAirport = closestAirports.getClosestBeelineAirport(request.getOrigin());
		Place destinationAirport = closestAirports.getClosestBeelineAirport(request.getDestination());
		ControlObject controlObject = new ControlObject(request, originAirport, destinationAirport);
		Connection headConnection = generateHeadConnection(request, originAirport);		
		
		try {
			connectionList.add(headConnection);
			
			//find all inbound connections to destination to exploit the destination airports
			if(FindHotspotsToDestination.findConnectionToDestinationAirport(controlObject, destinationAirport) == null)
				return null;

			//itterative search of next subconnection for each connection
			int maxLevel = 10;
			if((connectionList = searchNextConnection(connectionList, controlObject, maxLevel)) == null)
				return null;
			
		} catch (IllegalStateException | SQLException e) {
			logger.warn("Connectionf for hotspot search can't be calculated." + e);
			throw new IllegalStateException("Connectionf for hotspot search can't be calculated." + e);
		}
		
		
		long elapsedTime = System.nanoTime() - startTime;
		System.out.println((double) elapsedTime / 1000000000.0 + " time needed for hotspot search");
		System.out.println(controlObject.getConnectionList().size() + " hotspot connections found.");
		//return connectionList;
		LinkedBlockingQueue<Connection> conList = controlObject.getConnectionList();
		conList.parallelStream().forEach(con -> {
				con.setSubConnections(database.utilities.SQLUtilities.addSubConnections(con.getSubConnections()));
		});
		return connectionList;
		//return controlObject.getConnectionList();
	}
	
	/**
	 * 
	 * @param connectionList
	 * @param controlObject
	 * @param maxLevel
	 * @return 1 if a connection was found, 0 if no connection was found
	 */
	private LinkedBlockingQueue<Connection> searchNextConnection(LinkedBlockingQueue<Connection> connectionList, ControlObject controlObject, int maxLevel){
		//Iterative search of next hotspot or destination
		for(int level = 0; !controlObject.isConnectionFound() && level < maxLevel; level++){
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
	
	/**
	 * for each connection add next sub connections to connection
	 * @param oldConnectionList
	 * @param newConnectionList
	 * @param controlObject
	 * @param method 1: all outbound connections, 2: hotspots only, 3: destination hash data only
	 */
	private void generateNextSubConnection(LinkedBlockingQueue<Connection> oldConnectionList, LinkedBlockingQueue<Connection> newConnectionList, ControlObject controlObject, int method){
		oldConnectionList.parallelStream().forEach(connection -> {
			//??????? wofür ist dieses if ????????
			if(connection.getAction().equals(Connection.ADD) || connection.getAction().equals(Connection.UNUSED)){
				SearchNode searchNode = new SearchNode();
				newConnectionList.addAll(searchNode.getNextConnections(controlObject, connection.clone(), method));	
			}
		});
	}
	
	
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
