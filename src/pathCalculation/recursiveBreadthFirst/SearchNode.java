package pathCalculation.recursiveBreadthFirst;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
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
		
		if(TerminationCriteria.shouldExploit(connection, controlObject)){
			try {
				//get all outbound connections for this airport
				LinkedBlockingQueue<Connection> outboundConnectionList = getAllOutboundConnections(connection, controlObject.getRequest());
				
				//get connections to add
				outboundConnectionList = getConnectionsToAdd(controlObject.getRequest().getExploitMethod() ,outboundConnectionList);
				
				//add connections to previous connection
				outboundConnectionList = addOldConnection(connection, outboundConnectionList);
				
				//recursive call of this method
				exploitNewConnections(outboundConnectionList);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println(e);
			}
		}
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
		
		if(method == Best_CONNECTIONS){
			connectionsToAdd = findBestConnections(outboundConnectionList, visitedAirportsMap);
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
	
	/**
	 * 
	 * @param outboundConnections needs to be ordered by date and time (earliest at first, latest at last)
	 * @param visitedAirportsMap
	 * @return
	 */
	private LinkedBlockingQueue<Connection> findBestConnections(LinkedBlockingQueue<Connection> outboundConnections, ConcurrentHashMap<String, Boolean> visitedAirportsMap){
		LinkedBlockingQueue<Connection> bestConnectionList = new LinkedBlockingQueue<Connection>();
		HashMap<String, Double> bestPriceToAirport = new HashMap<String, Double>();
		for(Connection connection : outboundConnections){
			String destinationIata = connection.getDestination().getIata();
			//if the connections does not contain this airport already
			if(!visitedAirportsMap.containsKey(destinationIata)){
				//if no connection to this airport is added already or the earlier connections are more expensice add current connection
				if(!bestPriceToAirport.containsKey(destinationIata) || bestPriceToAirport.get(destinationIata) > connection.getPrice()){
					bestPriceToAirport.put(destinationIata, connection.getPrice());
					bestConnectionList.add(connection);
				}
			}
		}
		return bestConnectionList;
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
	
	private LinkedBlockingQueue<Connection> addOldConnection(Connection connection, LinkedBlockingQueue<Connection> connectionList){
		LinkedBlockingQueue<Connection> newConnectionList = new LinkedBlockingQueue<Connection>();
		
		//add for each connection the best flight to the previous connection and add the full connection to the result set
		connectionList.parallelStream().forEach(nextSubConnection -> {
			Connection newConnection = connection.clone();
			newConnection.addSubconnection(nextSubConnection);
			if(newConnection.getSubConnections().size() == 2)
				newConnection = addConnectionToAirport(newConnection);
			newConnectionList.add(newConnection);
		});
		
		return newConnectionList;
	}
	
	private Connection addConnectionToAirport(Connection connection){
		// first remove the rough connection from origin to origin airport and than add the new one (with origin and departure time and polyline)
		api.GoogleMapsDirection googleDirection = new api.GoogleMapsDirection();
		connection.getSubConnections().poll();
		//Add connection from origin to origin airport
		try {
			GregorianCalendar arrivalTimeToAirport = GoogleMapsTimeZone.getUTCTime(TimeFunctions.cloneAndAddHoures(connection.getSubConnections().peek().getDepartureDate(), -1), connection.getSubConnections().peek().getOrigin());
			LinkedBlockingQueue<Connection> connectionToAirport = googleDirection.getConnection(controlObject.getRequest().getOrigin(), connection.getSubConnections().peek().getOrigin(), arrivalTimeToAirport, false, controlObject.getRequest().getBestTransportation(), "", "", false);
			connection.addHeadOnSubconnection(connectionToAirport.peek());
		} catch (IllegalStateException | IOException | JDOMException e) {
			logger.warn("Connection from origin to origin airport cant be added. (origin: " + connection.getOrigin().getIata() + ")" + e);
		}		
		return connection;
	}
	
	private void exploitNewConnections(LinkedBlockingQueue<Connection> connectionList){
		//recursion for each connection of the List
		connectionList.parallelStream().forEach(connection -> {
			Thread thread = new Thread(new SearchNode(controlObject, connection));
			thread.start();
			controlObject.getThreadList().add(thread);
		});
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
