package pathCalculation.bestPath.hotspotPath;

import java.io.IOException;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import api.GoogleMapsTimeZone;
import database.ConnectedAirports;
import database.ConnectedHotspots;
import pathCalculation.breadthFirstSearch.ControlObject;
import utilities.Connection;
import utilities.Place;
import utilities.TimeFunctions;

public class SearchNode {

	protected static final Logger logger = LogManager.getLogger(SearchNode.class);
	
	public SearchNode(){
		
	}
	
	/**
	 * 
	 * @param controlObject
	 * @param connection
	 * @param destination
	 * @param method 1: all outbound connections, 2: hotspots only, 3: destination hash data only
	 * @return
	 */
	public LinkedBlockingQueue<Connection> getNextConnections(ControlObject controlObject, Connection connection, int method){
		
		ConcurrentHashMap<String, Boolean> usedAirportsMap = new ConcurrentHashMap<String, Boolean>();
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		LinkedBlockingQueue<Connection> outboundConnections = new LinkedBlockingQueue<Connection>();
		Place node = connection.getDestination();
		
		try {
			
			//Add one houre (transit time) to arrival time (for the next departure time)
			GregorianCalendar timeIncludingMinimumTransit = TimeFunctions.cloneAndAddHoures(connection.getArrivalDate(), 1);
			
			outboundConnections = getAllOutboundConnections(method, node, controlObject, timeIncludingMinimumTransit);			
			
			//put all already visited airports for this connection in a hash map
			usedAirportsMap = hashmapOfVisitedAirports(connection);
			
			//choose for each connection the best flight
			ConcurrentHashMap<String, Connection> connectedAirportsMap = findBestConnection(outboundConnections, timeIncludingMinimumTransit, usedAirportsMap);

			//add for each connection the best flight to the previous connection and add the full connection to the result set
			connectedAirportsMap.entrySet().parallelStream().forEach(nextSubConnection -> {
				addSubconnectionToConnection(connection, nextSubConnection.getValue(), controlObject, method, connectionList);	
			});
				
		} catch (SQLException e) {
			logger.warn("Next nodes for hotspot search can't be calculated." + e);
			throw new IllegalStateException("Next nodes for hotspot search can't be calculated." + e);
		}
		
		return connectionList;
	}
	
	
	/**
	 * adds the sub connection to the Connection if there was no better flight to the smae destination airport earlier and add the full connection to the result set
	 * @param connection
	 * @param nextSubConnection
	 * @param controlObject
	 * @param method
	 * @param connectionList
	 */
	private void addSubconnectionToConnection(Connection connection, Connection nextSubConnection, ControlObject controlObject, int method, LinkedBlockingQueue<Connection> connectionList){
		//add sub connection to old connection
		Connection newConnection = connection.clone();
		Connection newNextSubConnection = nextSubConnection.clone();
		newConnection.addSubconnection(newNextSubConnection);
		
		//if departure place is found
		if(controlObject.isPathToDestinationAirportKnown(newNextSubConnection.getDestination().getIata()) && method != 3){
			connectionList.addAll(addFinalConnectionToDestinationAirport(newConnection, controlObject));
		}else{
			connectionList.add(newConnection);
			if(method == 3 && controlObject.isDestinationAirport(newConnection.getDestination().getIata())){
				addToFromAirport(newConnection, controlObject);
			}
		}
	}
	
	
	private LinkedBlockingQueue<Connection> addFinalConnectionToDestinationAirport(Connection newConnection, ControlObject controlObject){
		LinkedBlockingQueue<Connection> connectionsToDestinationAirport = new LinkedBlockingQueue<Connection>();
		connectionsToDestinationAirport.add(newConnection);
		
		//if destination of connection is already the destination airport
		if(controlObject.isDestinationAirport(newConnection.getDestination().getIata())){
			addToFromAirport(newConnection, controlObject);		
			return connectionsToDestinationAirport;
		}
		
		for(int level = 0; !controlObject.isConnectionFound() && level < 5; level++){
			LinkedBlockingQueue<Connection> tmpConnectionsToDestinationAirport = new LinkedBlockingQueue<Connection>();
			connectionsToDestinationAirport.parallelStream().forEach(connection -> {
				tmpConnectionsToDestinationAirport.addAll(getNextConnections(controlObject, connection, 3));
			});
			connectionsToDestinationAirport = tmpConnectionsToDestinationAirport;
		}
		return connectionsToDestinationAirport;
	}
	
	private void addToFromAirport(Connection newConnection, ControlObject controlObject){
		api.GoogleMapsDirection googleDirection = new api.GoogleMapsDirection();
		newConnection.getSubConnections().poll();
		//Add connection from origin to origin airport
		try {
			GregorianCalendar arrivalTimeToAirport = GoogleMapsTimeZone.getUTCTime(TimeFunctions.cloneAndAddHoures(newConnection.getSubConnections().peek().getDepartureDate(), -1), newConnection.getSubConnections().peek().getOrigin());
			LinkedBlockingQueue<Connection> connectionToAirport = googleDirection.getConnection(controlObject.getRequest().getOrigin(), newConnection.getSubConnections().peek().getOrigin(), arrivalTimeToAirport, false, controlObject.getRequest().getBestTransportation(), "", "", false);
			newConnection.addHeadOnSubconnection(connectionToAirport.peek());
		} catch (IllegalStateException | IOException | JDOMException e) {
			logger.warn("Connection from origin to origin airport cant be added. (origin: " + newConnection.getOrigin().getIata() + ")" + e);
		}
		
		//Add connection from destination airport to destination
		try {
			GregorianCalendar departureTimeFromAirport = GoogleMapsTimeZone.getUTCTime(TimeFunctions.cloneAndAddHoures(newConnection.getArrivalDate(), 1), newConnection.getDestination());
			LinkedBlockingQueue<Connection> connectionFromAirport = googleDirection.getConnection(newConnection.getDestination(), controlObject.getRequest().getDestination(), departureTimeFromAirport, true, controlObject.getRequest().getBestTransportation(), "", "", false);
			newConnection.addSubconnection(connectionFromAirport.peek());
		} catch (IllegalStateException | IOException | JDOMException e) {
			logger.warn("Connection from origin to origin airport cant be added. (origin: " + newConnection.getDestination().getIata() + ")" + e);
		}
		
		newConnection.setRecursiveAction(Connection.ADD);
		controlObject.addConnection(newConnection);
		System.out.println("-> Connection Found");
	}
	
	
	private boolean shouldConnectionAdded(ConcurrentHashMap<String, Connection> connectedAirportsMap, String departureAirportIATA, Connection nextSubConnection, GregorianCalendar earliestDepartureTime, ConcurrentHashMap<String, Boolean> usedAirportsMap){
		//Airport was visited to a previous point in time (cycle prevention)
		if(usedAirportsMap.containsKey(departureAirportIATA))
			return false;
		//airport is not connected at this time
		if(!connectedAirportsMap.containsKey(departureAirportIATA))
			return true;
		//test if the new connection is better than the previoust choosen one
		if(nextSubConnection.getDepartureDate().before(connectedAirportsMap.get(departureAirportIATA).getDepartureDate()) && nextSubConnection.getDepartureDate().after(earliestDepartureTime))
			return true;
		return false;
	}
	
	/**
	 * 
	 * @param method 1: all outbound connections, 2: hotspots only, 3: destination hash data only
	 * @param origin
	 * @param controlObject
	 * @param departureTime
	 * @return
	 * @throws SQLException
	 */
	private LinkedBlockingQueue<Connection> getAllOutboundConnections(int method, Place origin, ControlObject controlObject, GregorianCalendar departureTime) throws SQLException{
		if(method == 1)
			return ConnectedAirports.getAllOutboundConnectionsWithinOneDay(origin, departureTime);
		if(method == 2)
			return ConnectedHotspots.getAllOutboundConnectionsWithinOneDay(origin, controlObject.getDestinationAirport(), departureTime);
		if(method == 3)
			return controlObject.getFlightsFromOneDestinationAirport(origin.getIata());
		return null;
	}
	
	/**
	 * put all already visited airports for this connection in a hash map
	 * @connection
	 * @return
	 */
	private ConcurrentHashMap<String, Boolean> hashmapOfVisitedAirports(Connection connection){
		ConcurrentHashMap<String, Boolean> visitedAirportsMap = new ConcurrentHashMap<String, Boolean>();
		for(Connection subConnection : connection.getSubConnections()){
			if(subConnection.getOrigin().getType() == Place.AIRPORT)
				visitedAirportsMap.put(subConnection.getOrigin().getIata(), true);
			if(subConnection.getDestination().getType() == Place.AIRPORT)
				visitedAirportsMap.put(subConnection.getDestination().getIata(), true);
		}
		return visitedAirportsMap;
	}
	
	/**
	 * choose for each connection the best flight
	 * @param outboundConnections
	 * @param departureTime
	 * @param visitedAirportsMap
	 * @return
	 */
	private ConcurrentHashMap<String, Connection> findBestConnection(LinkedBlockingQueue<Connection> outboundConnections, GregorianCalendar departureTime, ConcurrentHashMap<String, Boolean> visitedAirportsMap){
		ConcurrentHashMap<String, Connection> connectedAirportsMap = new ConcurrentHashMap<String, Connection>();
		for(Connection nextSubConnection : outboundConnections){
			String departureAirportIATA = nextSubConnection.getDestination().getIata();
			//true if no flight to the departure airport is already in the list from this airport
			// or if the departure date is earlier than the current one but still after the minimum transit time
			if(shouldConnectionAdded(connectedAirportsMap, departureAirportIATA, nextSubConnection, departureTime, visitedAirportsMap)){
				connectedAirportsMap.put(departureAirportIATA, nextSubConnection);
			}
		}
		return connectedAirportsMap;
	}
	
}
