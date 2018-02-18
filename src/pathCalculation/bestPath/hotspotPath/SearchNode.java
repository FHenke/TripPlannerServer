package pathCalculation.bestPath.hotspotPath;

import java.io.IOException;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import api.GoogleMapsTimeZone;
import database.ConnectedHotspots;
import pathCalculation.breadthFirstSearch.ControlObject;
import utilities.Connection;
import utilities.Place;
import utilities.TimeFunctions;

public class SearchNode {

	protected static final Logger logger = LogManager.getLogger(SearchNode.class);
	
	public SearchNode(){
		
	}
	
	public LinkedBlockingQueue<Connection> getNextConnections(ControlObject controlObject, Connection connection, Place destination){
		
		ConcurrentHashMap<String, Connection> connectedAirportsMap = new ConcurrentHashMap<String, Connection>();
		ConcurrentHashMap<String, Boolean> usedAirportsMap = new ConcurrentHashMap<String, Boolean>();
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		Place node = connection.getDestination();
		try {
			
			//Add one houre (transit time) to arrival time (for the next departure time)
			GregorianCalendar timeIncludingMinimumTransit = TimeFunctions.cloneAndAddHoures(connection.getArrivalDate(), 1);
			
			LinkedBlockingQueue<Connection> outboundConnections = ConnectedHotspots.getAllOutboundConnectionsWithinOneDay(node, destination, timeIncludingMinimumTransit);
			
			//TODO: own method
			for(Connection subConnection : connection.getSubConnections()){
				if(subConnection.getOrigin().getType() == Place.AIRPORT)
					usedAirportsMap.put(subConnection.getOrigin().getIata(), true);
				if(subConnection.getDestination().getType() == Place.AIRPORT)
					usedAirportsMap.put(subConnection.getDestination().getIata(), true);
			}
			
			//choose for each connection the best flight
			for(Connection nextSubConnection : outboundConnections){
				String departureAirportIATA = nextSubConnection.getDestination().getIata();
				
				//true if no flight to the departure airport is already in the list from this airport
				// or if the departure date is earlier than the current one but still after the minimum transit time
				if(shouldConnectionAdded(connectedAirportsMap, departureAirportIATA, nextSubConnection, timeIncludingMinimumTransit, usedAirportsMap)){
					connectedAirportsMap.put(departureAirportIATA, nextSubConnection);
				}
			}
			
			//add for each connection the best flight to the previous connection and add the full connection to the result set
			connectedAirportsMap.entrySet().parallelStream().forEach(nextSubConnection -> {
				
				//add subconnection to old connection
				Connection newConnection = connection.clone();
				Connection newNextSubConnection = nextSubConnection.getValue().clone();
				newConnection.addSubconnection(newNextSubConnection);
				
				//if departure place is found
				if(controlObject.isDestinationAirport(newNextSubConnection.getDestination().getIata())){
					if(addFinalConnectionToDestinationAirport(newConnection, controlObject))
						addToFromAirport(newConnection, controlObject, newNextSubConnection);
				}
				
				connectionList.add(newConnection);

			});
			
		} catch (SQLException e) {
			logger.warn("Next nodes for hotspot search can't be calculated." + e);
			throw new IllegalStateException("Next nodes for hotspot search can't be calculated." + e);
		}
		
		return connectionList;
		
	}
	
	
	private boolean addFinalConnectionToDestinationAirport(Connection newConnection, ControlObject controlObject){
		LinkedBlockingQueue<Connection> ConnectionsToDestinationAirport = controlObject.getDestinationAirportsMap().get(newConnection.getDestination().getIata());
		Connection bestConnectionToDestination = null;
		if(!ConnectionsToDestinationAirport.isEmpty()){
			for(Connection connection : ConnectionsToDestinationAirport){
				//test if the new connection is better than the previoust choosen one
				if(connection.getDepartureDate().after(TimeFunctions.cloneAndAddHoures(newConnection.getArrivalDate(), 1)) && connection.getDepartureDate().before(TimeFunctions.cloneAndAddHoures(newConnection.getArrivalDate(), 25))){
					if(bestConnectionToDestination == null || connection.getDepartureDate().before(bestConnectionToDestination.getDepartureDate()))
						bestConnectionToDestination = connection;
				}
			}
			if(bestConnectionToDestination != null){
				newConnection.addSubconnection(bestConnectionToDestination.clone());
				return true;
			}
		}
		return false;
	}
	
	private void addToFromAirport(Connection newConnection, ControlObject controlObject, Connection newNextSubConnection){
		api.GoogleMapsDirection googleDirection = new api.GoogleMapsDirection();
		newConnection.getSubConnections().poll();
		//Add connection from origin to origin airport
		try {
			GregorianCalendar arrivalTimeToAirport = GoogleMapsTimeZone.getUTCTime(TimeFunctions.cloneAndAddHoures(newConnection.getSubConnections().peek().getDepartureDate(), -1), newConnection.getSubConnections().peek().getOrigin());
			LinkedBlockingQueue<Connection> connectionToAirport = googleDirection.getConnection(controlObject.getRequest().getOrigin(), newConnection.getSubConnections().peek().getOrigin(), arrivalTimeToAirport, false, controlObject.getRequest().getBestTransportation(), "", "", false);
			newConnection.addHeadOnSubconnection(connectionToAirport.peek());
		} catch (IllegalStateException | IOException | JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Add connection from destination airport to destination
		try {
			GregorianCalendar departureTimeFromAirport = GoogleMapsTimeZone.getUTCTime(TimeFunctions.cloneAndAddHoures(newConnection.getArrivalDate(), 1), newConnection.getDestination());
			LinkedBlockingQueue<Connection> connectionFromAirport = googleDirection.getConnection(newConnection.getDestination(), controlObject.getRequest().getDestination(), departureTimeFromAirport, true, controlObject.getRequest().getBestTransportation(), "", "", false);
			newConnection.addSubconnection(connectionFromAirport.peek());
		} catch (IllegalStateException | IOException | JDOMException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
			e.printStackTrace();
		}
		
		newConnection.setRecursiveAction(Connection.ADD);
		controlObject.setConnectionIsFound();
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
	
}
