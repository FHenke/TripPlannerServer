package pathCalculation.bestPath.hotspotPath;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.ClosestAirports;
import database.ConnectedHotspots;
import pathCalculation.breadthFirstSearch.ControlObject;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class HotspotSearch {

	protected static final Logger logger = LogManager.getLogger(HotspotSearch.class);
	
	public HotspotSearch(){
		
	}
	
	
	public LinkedBlockingQueue<Connection> gethotspotPath(Request request) throws IllegalStateException{
		
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
		
		//TODO: in own method
		// set a temporal connection to the start airport. This connection is needed by the SearchNode class to calculate the following flights.  Only the destination and arrivalDate parameters are required.
		Connection tmpStartConnection = new Connection(request.getOrigin(), originAirport);
		tmpStartConnection.setArrivalDate(request.getDepartureDateString());
		headConnection.getSubConnections().add(tmpStartConnection);
		
		try {
			connectionList.add(headConnection);
			
			
			//TODO: in own Method
			//find all inboundconnections to destination to exploid the destination airports to find
			ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> destinationAirportsMap = new ConcurrentHashMap<String, LinkedBlockingQueue<Connection>>();
			LinkedBlockingQueue<Connection> finalConnectionList = ConnectedHotspots.getAllInboundConnectionsWithinFiveDays(originAirport, destinationAirport, request.getDepartureDateString());
			destinationAirportsMap.put(destinationAirport.getIata(), new LinkedBlockingQueue<Connection>());
			finalConnectionList.parallelStream().forEach(finalConnection -> {
				// because of parallel purpose it has to try to insert an empty list before adding a connection afterwards
				
				destinationAirportsMap.putIfAbsent(finalConnection.getOrigin().getIata(), new LinkedBlockingQueue<Connection>());
				destinationAirportsMap.get(finalConnection.getOrigin().getIata()).add(finalConnection);
			});
			controlObject.setDestinationAirportsMap(destinationAirportsMap);
			
			
			
			//itterative search of next hotspot or destination
			for(int level = 0; !controlObject.isConnectionFound() && level < 10; level++){
				LinkedBlockingQueue<Connection> tmpConnectionList = new LinkedBlockingQueue<Connection>();
				System.out.println(level);
				connectionList.parallelStream().forEach(connection -> {
					//??????? wofür ist dieses if ????????
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
						
		} catch (IllegalStateException | SQLException e) {
			logger.warn("Connectionf for hotspot search can't be calculated." + e);
			throw new IllegalStateException("Connectionf for hotspot search can't be calculated." + e);
		}
		
		
		long elapsedTime = System.nanoTime() - startTime;
		System.out.println((double) elapsedTime / 1000000000.0);
		//return connectionList;
		return controlObject.getConnectionList();
	}
}
