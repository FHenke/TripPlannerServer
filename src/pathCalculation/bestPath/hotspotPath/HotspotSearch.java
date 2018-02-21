package pathCalculation.bestPath.hotspotPath;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.ClosestAirports;
import database.ConnectedAirports;
import database.ConnectedHotspots;
import database.Query;
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
		ClosestAirports closestAirports = new ClosestAirports();
		Place originAirport = closestAirports.getClosestBeelineAirport(request.getOrigin());
		Place destinationAirport = closestAirports.getClosestBeelineAirport(request.getDestination());
		ControlObject controlObject = new ControlObject(request, originAirport, destinationAirport);
		
		Connection headConnection = new Connection(request.getOrigin(), originAirport);
		headConnection.setArrivalDate(request.getDepartureDateString());
		
		//TODO: in own method
		// set a temporal connection to the start airport. This connection is needed by the SearchNode class to calculate the following flights.  Only the destination and arrivalDate parameters are required.
		Connection tmpStartConnection = new Connection(request.getOrigin(), originAirport);
		tmpStartConnection.setArrivalDate(request.getDepartureDateString());
		headConnection.getSubConnections().add(tmpStartConnection);
		
		try {
			connectionList.add(headConnection);
			
			//find all inbound connections to destination to exploit the destination airports
			if(findConnectionToDestinationAirport(controlObject, destinationAirport) == null)
				return null;

			System.out.println(controlObject.getDestinationAirportsMap().keySet());
			
			//TODO: own method
			//Iterative search of next hotspot or destination
			for(int level = 0; !controlObject.isConnectionFound() && level < 10; level++){
				LinkedBlockingQueue<Connection> tmpConnectionList = new LinkedBlockingQueue<Connection>();
				System.out.println(level);
				connectionList.parallelStream().forEach(connection -> {
					//??????? wofür ist dieses if ????????
					if(connection.getAction().equals(Connection.ADD) || connection.getAction().equals(Connection.UNUSED)){
						SearchNode searchNode = new SearchNode();
						tmpConnectionList.addAll(searchNode.getNextConnections(controlObject, connection.clone(), 2));	
					}
				});
				//if no connection to hotspots was found search for connection to any airport
				if(tmpConnectionList.isEmpty()){
					connectionList.parallelStream().forEach(connection -> {
						//??????? wofür ist dieses if ????????
						if(connection.getAction().equals(Connection.ADD) || connection.getAction().equals(Connection.UNUSED)){
							SearchNode searchNode = new SearchNode();
							tmpConnectionList.addAll(searchNode.getNextConnections(controlObject, connection.clone(), 1));	
						}
					});
				}
				//if still no connection was found return null
				if(tmpConnectionList.isEmpty()){
					return null;
				}
				
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
		System.out.println(controlObject.getConnectionList().size() + " connections found.");
		return connectionList;
		//return controlObject.getConnectionList();
	}
	
	/*private LinkedBlockingQueue<Connection> findConnectionToDestinationAirport(ControlObject controlObject, Connection connection) throws SQLException{
		boolean hotspotFound = false;
		LinkedBlockingQueue<Connection> finalConnection = new LinkedBlockingQueue<Connection>();
		finalConnection.add(connection);
		ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> destinationAirportsMap = new ConcurrentHashMap<String, LinkedBlockingQueue<Connection>>();
		
		while(!hotspotFound){
			LinkedBlockingQueue<Connection> tmpFinalConnectionList = ConnectedHotspots.getAllInboundConnectionsWithinFiveDays(controlObject.getOriginAirport(), controlObject.getDestinationAirport(), controlObject.getRequest().getDepartureDateString());
			
			if(tmpFinalConnectionList.isEmpty()){
				finalConnectionList = ConnectedAirports.getAllInboundConnectionsWithinFiveDays(controlObject.getDestinationAirport(), controlObject.getRequest().getDepartureDateString());
			}
		}
		
		
		//#################
		
		//connectionList = tmpConnection = aktuell betrachtete Connection
		
		//TODO: own method
		//Iterative search of next hotspot or destination
		for(int level = 0; hotspotFound == false && level < 5; level++){
			LinkedBlockingQueue<Connection> tmpConnectionList = new LinkedBlockingQueue<Connection>();
			System.out.println("find connection to destination: " + level);
			finalConnection.parallelStream().forEach(finalElement -> {
				
				LinkedBlockingQueue<Connection> connectedAirports = ConnectedHotspots.getAllInboundConnectionsWithinFiveDays(tmpConnection.getOrigin(), controlObject.getRequest().getDepartureDateString()));
			
				//add connection to hashmap and tmpConnectionList if the origin airport is not in the hashmap already
				if(!destinationAirportsMap.putIfAbsent(finalElement.getOrigin().getIata())){
					
					// add new subconnection to old one
					tmpConnectionList.add(finalElement)
					// add the connection to hashmap
					destinationAirportsMap.put(finalElement.getOrigin().getIata(), value)
					
				}
				
				tmpConnectionList.addAll(ConnectedHotspots.getAllInboundConnectionsWithinFiveDays(tmpConnection.getOrigin(), controlObject.getRequest().getDepartureDateString()));
			});
			//if no connection to hotspots was found search for connection to any airport
			if(tmpConnectionList.isEmpty()){
				connectionList.parallelStream().forEach(tmpConnection -> {
					//add connection to hashmap and tmpConnectionList if the origin airport is not in the hashmap already
					tmpConnectionList.addAll(ConnectedAirports.getAllInboundConnectionsWithinFiveDays(tmpConnection.getOrigin(), controlObject.getRequest().getDepartureDateString()));
				});
			}else{
				hotspotFound = true;
			}
			//if still no connection was found return null
			if(tmpConnectionList.isEmpty()){
				return null;
			}
			
			//remove that shit
			tmpConnectionList.parallelStream().forEach(tmpConnection -> {
				if(destinationAirportsMap.putIfAbsent(tmpConnection.getOrigin().getIata(), new LinkedBlockingQueue<Connection>()) == null)
					
				destinationAirportsMap.get(tmpConnection.getOrigin().getIata()).add(tmpConnection);
			}
			connectionList = tmpConnectionList;
		}

		
		
		//###############
		
		
		
		//remove that shit too
		destinationAirportsMap.put(controlObject.getDestinationAirport().getIata(), new LinkedBlockingQueue<Connection>());
		finalConnectionList.parallelStream().forEach(finalConnection -> {
			// because of parallel purpose it has to try to insert an empty list before adding a connection afterwards
			
			destinationAirportsMap.putIfAbsent(finalConnection.getOrigin().getIata(), new LinkedBlockingQueue<Connection>());
			destinationAirportsMap.get(finalConnection.getOrigin().getIata()).add(finalConnection);
		});
		controlObject.setDestinationAirportsMap(destinationAirportsMap);
		
		return finalConnectionList;
		
		return null;
	}*/
	
	public ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> findConnectionToDestinationAirport(ControlObject controlObject, Place destinationAirport) throws SQLException{
		boolean hotspotReached = false;
		ConcurrentHashMap<String, LinkedBlockingQueue<Connection>> destinationHashMap = new ConcurrentHashMap<String, LinkedBlockingQueue<Connection>>();
		ConcurrentHashMap<String, Place> currentDestinationHashSet = new ConcurrentHashMap<String, Place>();
		currentDestinationHashSet.put(destinationAirport.getIata(), destinationAirport);
		
		//if destination airport is a hotspot already
		if(Query.isAirportHotspot(destinationAirport.getIata()))
			return destinationHashMap;
		
		for(int level = 0; !hotspotReached && level < 5; level++){
			System.out.println("sub level: " + level);
			ConcurrentHashMap<String, Place> newDestinationHashSet = new ConcurrentHashMap<String, Place>();
			
			currentDestinationHashSet.entrySet().parallelStream().forEach(destination -> {
				try {
					if(!destinationHashMap.containsKey(destination)){
						LinkedBlockingQueue<Connection> newConnectionList = ConnectedHotspots.getAllInboundConnectionsWithinFiveDays(destination.getValue(), controlObject.getRequest().getDepartureDateString());
						newConnectionList.parallelStream().forEach(newConnection -> {
							destinationHashMap.putIfAbsent(newConnection.getOrigin().getIata(), new LinkedBlockingQueue<Connection>());
							destinationHashMap.get(newConnection.getOrigin().getIata()).add(newConnection);
							newDestinationHashSet.put(newConnection.getOrigin().getIata(), newConnection.getOrigin());
						});
					}
				} catch (SQLException e) {
					logger.warn("Inbound hotspot connections for connections to destination cant be determined." + e);
				}
			});
			//if no connection from a hotspot was found search connection to an other airport (Breadth First Search)
			
			if(newDestinationHashSet.isEmpty()){
				currentDestinationHashSet.entrySet().parallelStream().forEach(destination -> {
					try {
						if(!destinationHashMap.containsKey(destination)){
							System.out.println("no hotspot reached");
							LinkedBlockingQueue<Connection> newConnectionList = ConnectedAirports.getAllInboundConnectionsWithinFiveDays(destination.getValue(), controlObject.getRequest().getDepartureDateString());
							newConnectionList.parallelStream().forEach(newConnection -> {
								destinationHashMap.putIfAbsent(newConnection.getOrigin().getIata(), new LinkedBlockingQueue<Connection>());
								destinationHashMap.get(newConnection.getOrigin().getIata()).add(newConnection);
								newDestinationHashSet.put(newConnection.getOrigin().getIata(), newConnection.getOrigin());
							});
						}
					} catch (SQLException e) {
						logger.warn("Inbound connections for connections to destination cant be determined." + e);
					}
				});
			}else{
				System.out.println("hotspot reached = true");
				hotspotReached = true;
			}
			
			currentDestinationHashSet = newDestinationHashSet;
			System.out.println(currentDestinationHashSet.keySet());
		}
		
		if(hotspotReached){
			controlObject.setDestinationAirportsMap(destinationHashMap);
			return destinationHashMap;
		}
		else
			return null;
	}
	
	
	/*private LinkedBlockingQueue<Connection> backwardsSearch(Connection connection, ControlObject controlObject) throws SQLException{
		LinkedBlockingQueue<Connection> tmpFinalConnectionList = ConnectedAirports.getAllInboundConnectionsWithinFiveDays(connection.getOrigin(), controlObject.getRequest().getDepartureDateString());
		
		
		return tmpFinalConnectionList;
	}*/
}
