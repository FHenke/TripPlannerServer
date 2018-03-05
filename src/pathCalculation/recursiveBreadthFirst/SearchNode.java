package pathCalculation.recursiveBreadthFirst;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import api.GoogleMapsTimeZone;
import database.ConnectedAirports;
import database.ConnectedHotspots;
import utilities.Connection;
import utilities.Place;
import utilities.Request;
import utilities.TimeFunctions;

public class SearchNode implements Runnable{

	protected static final Logger logger = LogManager.getLogger(SearchNode.class);
	
	public static final int CHEAPEST_CONNECTION = 1;
	public static final int Best_CONNECTIONS = 2;
	public static final int ALL_CONNECTIONS = 3;
	
	ControlObject controlObject;
	Connection connection;
	
	public SearchNode(ControlObject controlObject, Connection connection){
		this.controlObject = controlObject;
		this.connection = connection;
	}
	
	@Override
	public void run() {
		// TODO: verbindung zum flughafen muss irgendwann hinzugefügt werden
		
		if(shouldExploit(connection)){
			System.out.println(connection.getDestination().getIata()  + ": " + connection.getVirtualPrice(controlObject.getRequest().getPriceForHoure()));
			controlObject.addUsedConnection(connection.clone());
			try {
				LinkedBlockingQueue<Connection> outboundConnectionList = getAllOutboundConnections(connection, controlObject.getRequest());
				
				//get connections to add
				
				//add connections to previous connection
				
				//call this method for each new connection
				
				
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println(e);
			}
		}
	}
	
	private boolean shouldExploit(Connection connection){
		// TODO: has to be extended
		if(connection.getSubConnections().size() < 5)
			return true;
		return false;
	}
	
	private LinkedBlockingQueue<Connection> getAllOutboundConnections(Connection connection, Request request) throws SQLException{
		//Add one houre (transit time) to arrival time (for the next departure time)
		GregorianCalendar timeIncludingMinimumTransit;
		if(connection.getArrivalDate() == null)
			timeIncludingMinimumTransit = request.getDepartureDateString();
		else
			timeIncludingMinimumTransit = TimeFunctions.cloneAndAddHoures(connection.getArrivalDate(), 1);
		return ConnectedAirports.getAllOutboundConnectionsWithinOneDay(connection.getDestination(), timeIncludingMinimumTransit);			
	}
	
	private LinkedBlockingQueue<Connection> getConnectionsToAdd(int method, LinkedBlockingQueue<Connection> outboundConnectionList){
		LinkedBlockingQueue<Connection> connectionsToAdd = new LinkedBlockingQueue<Connection>();
		//put all already visited airports for this connection in a hash map
		ConcurrentHashMap<String, Boolean> visitedAirportsMap = hashmapOfVisitedAirports(connection);
		
		if(method == ALL_CONNECTIONS){
			connectionsToAdd = removeCircles(outboundConnectionList, visitedAirportsMap);
		}
		
		if(method == CHEAPEST_CONNECTION){
			ConcurrentHashMap<String, Connection> connectionsToAddMap = findCheapestConnections(outboundConnectionList, visitedAirportsMap);
			connectionsToAdd.addAll(new ArrayList<Connection>(connectionsToAddMap.values()));			
		}
		
		return connectionsToAdd;
	}
	
	private LinkedBlockingQueue<Connection> removeCircles(LinkedBlockingQueue<Connection> connectionList, ConcurrentHashMap<String, Boolean> visitedAirports){
		LinkedBlockingQueue<Connection> newConnectionList = new LinkedBlockingQueue<Connection>();
		connectionList.parallelStream().forEach(connection -> {
			if(visitedAirports.containsKey(connection.getDestination().getIata()))
				newConnectionList.add(connection);
		});
		return newConnectionList;
	}
	
	/**
	 * choose for each connection the best flight
	 * @param outboundConnections
	 * @param departureTime
	 * @param visitedAirportsMap
	 * @return
	 */
	private ConcurrentHashMap<String, Connection> findCheapestConnections(LinkedBlockingQueue<Connection> outboundConnections, ConcurrentHashMap<String, Boolean> visitedAirportsMap){
		ConcurrentHashMap<String, Connection> connectedAirportsMap = new ConcurrentHashMap<String, Connection>();
		for(Connection nextSubConnection : outboundConnections){
			//true if no flight to the departure airport is already in the list from this airport
			// or if the flight is ceaper than the current one
			if(isCheapestConnection(connectedAirportsMap, nextSubConnection, visitedAirportsMap)){
				connectedAirportsMap.put(nextSubConnection.getDestination().getIata(), nextSubConnection);
			}
		}
		return connectedAirportsMap;
	}
	
	private boolean isCheapestConnection(ConcurrentHashMap<String, Connection> connectedAirportsMap, Connection connection, ConcurrentHashMap<String, Boolean> usedAirportsMap){
		//Airport was visited to a previous point in time (cycle prevention)
		if(usedAirportsMap.containsKey(connection.getDestination().getIata()))
			return false;
		//airport is not connected at this time
		if(!connectedAirportsMap.containsKey(connection.getDestination().getIata()))
			return true;
		//test if the new connection is better than the previoust choosen one
		if(connection.getPrice() < connectedAirportsMap.get(connection.getDestination().getIata()).getPrice())
			return true;
		return false;
	}
	
	
	/**
	 * 
	 * @param controlObject
	 * @param connection
	 * @param destination
	 * @param method 1: all outbound connections, 2: hotspots only, 3: destination hash data only
	 * @return
	 */
	/*
	public LinkedBlockingQueue<Connection> getNextConnections(ControlObject controlObject, Connection connection, int method){
		
		ConcurrentHashMap<String, Boolean> usedAirportsMap = new ConcurrentHashMap<String, Boolean>();
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		LinkedBlockingQueue<Connection> outboundConnections = new LinkedBlockingQueue<Connection>();
		Place node = connection.getDestination();
		
		try {
			


			
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
	
	*/
	/**
	 * adds the sub connection to the Connection if there was no better flight to the smae destination airport earlier and add the full connection to the result set
	 * @param connection
	 * @param nextSubConnection
	 * @param controlObject
	 * @param method
	 * @param connectionList
	 */
	/*private void addSubconnectionToConnection(Connection connection, Connection nextSubConnection, ControlObject controlObject, int method, LinkedBlockingQueue<Connection> connectionList){
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
	
	/**
	 * put all already visited airports for this connection in a hash map
	 * @connection
	 * @return
	 */
	private ConcurrentHashMap<String, Boolean> hashmapOfVisitedAirports(Connection connection){
		ConcurrentHashMap<String, Boolean> visitedAirportsMap = new ConcurrentHashMap<String, Boolean>();
		for(Connection subConnection : connection.getSubConnections()){
			if(subConnection.getDestination().getType() == Place.AIRPORT)
				visitedAirportsMap.put(subConnection.getDestination().getIata(), true);
		}
		return visitedAirportsMap;
	}


	
}
