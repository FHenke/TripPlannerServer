package pathCalculation.recursiveBreadthFirst;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.ConnectedAirports;
import database.ConnectedHotspots;
import database.Query;
import pathCalculation.breadthFirstSearch.ControlObject;
import utilities.Connection;
import utilities.Place;

public class FindHotspotsToDestination {

	protected static final Logger logger = LogManager.getLogger(FindHotspotsToDestination.class);
	
	/**
	 * Searches all hotspots that are connected (over other airports) to the destination airport
	 * @param controlObject
	 * @param destinationAirport
	 * @return
	 * @throws SQLException
	 */
	public static ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> findConnectionToDestinationAirport(ControlObject controlObject, Place destinationAirport) throws SQLException{
		boolean hotspotReached = false;
		ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> destinationHashMap = new ConcurrentHashMap<String, LinkedBlockingQueue<Connection>>();
		ConcurrentHashMap<String, Place> currentDestinationHashSet = new ConcurrentHashMap<String, Place>();
		currentDestinationHashSet.put(destinationAirport.getIata(), destinationAirport);
		
		//if destination airport is a hotspot already
		if(Query.isAirportHotspot(destinationAirport.getIata()))
			return destinationHashMap;
		
		for(int level = 0; !hotspotReached && level < 5; level++){
			ConcurrentHashMap<String, Place> newDestinationHashSet = new ConcurrentHashMap<String, Place>();
			
			boolean useHotspotSearch = true;
			findNextConnectionforEachConnection(controlObject, useHotspotSearch, currentDestinationHashSet, newDestinationHashSet, destinationHashMap);
			//if no connection from a hotspot was found search connection to an other airport (Breadth First Search)
			if(newDestinationHashSet.isEmpty()){
				useHotspotSearch = false;
				findNextConnectionforEachConnection(controlObject, useHotspotSearch, currentDestinationHashSet, newDestinationHashSet, destinationHashMap);
			}else{
				hotspotReached = true;
			}
			currentDestinationHashSet = newDestinationHashSet;
		}
		
		if(hotspotReached){
			controlObject.setDestinationAirportsMap(destinationHashMap);
			return destinationHashMap;
		}
		else
			return null;
	}
	
	
	private static void addAllConnectionsToHashMap(LinkedBlockingQueue<Connection> connectionList, ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> destinationHashMap, ConcurrentHashMap<String, Place> newDestinationHashSet){
		connectionList.parallelStream().forEach(newConnection -> {
			destinationHashMap.putIfAbsent(newConnection.getOrigin().getIata(), new LinkedBlockingQueue<Connection>());
			destinationHashMap.get(newConnection.getOrigin().getIata()).add(newConnection);
			newDestinationHashSet.put(newConnection.getOrigin().getIata(), newConnection.getOrigin());
		});
	}
	
	private static void findNextConnectionforEachConnection(ControlObject controlObject, boolean useHotspotSearch, ConcurrentHashMap<String, Place> currentDestinationHashSet, ConcurrentHashMap<String, Place> newDestinationHashSet, ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> destinationHashMap){
		currentDestinationHashSet.entrySet().parallelStream().forEach(destination -> {
			try {
				if(!destinationHashMap.containsKey(destination)){
					LinkedBlockingQueue<Connection> newConnectionList;
					if(useHotspotSearch)
						newConnectionList = ConnectedHotspots.getAllInboundConnectionsWithinFiveDays(destination.getValue(), controlObject.getRequest().getDepartureDateString());
					else
						newConnectionList = ConnectedAirports.getAllInboundConnectionsWithinFiveDays(destination.getValue(), controlObject.getRequest().getDepartureDateString());
					
					addAllConnectionsToHashMap(newConnectionList, destinationHashMap, newDestinationHashSet);
				}
			} catch (SQLException e) {
				logger.warn("Inbound hotspot connections for connections to destination cant be determined." + e);
			}
		});
	}
}
