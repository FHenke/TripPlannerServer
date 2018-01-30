package pathCalculation.breadthFirstSearch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import api.GoogleMapsTimeZone;
import database.Querry;
import database.QueryAllConnectionsFromAirport;
import sockets.LineCoordinatesOnly;
import utilities.Connection;
import utilities.Place;
import utilities.TimeFunctions;

public class SearchNode{
	
	protected static final Logger logger = LogManager.getLogger(SearchNode.class);
	
	public SearchNode(){
		
	}	
	
	
	public LinkedBlockingQueue<Connection> getNextConnections(ControlObject controlObject, Connection connection, Place destination){
		
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		Place node = connection.getDestination();
		try {
			GregorianCalendar timeIncludingMinimumTransit = (GregorianCalendar) connection.getArrivalDate().clone();
			timeIncludingMinimumTransit.add(GregorianCalendar.HOUR_OF_DAY, 1);
			LinkedBlockingQueue<Connection> connectedAirports = QueryAllConnectionsFromAirport.getAllOutboundConnectionsWithinOneDay(node, timeIncludingMinimumTransit);
			//TODO: parallelisieren
			for(Connection nextSubConnection : connectedAirports){
				String departureAirportIATA = nextSubConnection.getDestination().getIata();

				//if next destination is not on list already
				if(controlObject.isDepartureAirportIATA(departureAirportIATA) || controlObject.addAirportToTree(departureAirportIATA)){
					Connection newConnection = connection.clone();
					
					Connection newNextSubConnection = nextSubConnection.clone();
					newConnection.addSubconnection(newNextSubConnection);
					
					
					//if departure place is found
					if(departureAirportIATA.equals(destination.getIata())){
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
							GregorianCalendar departureTimeFromAirport = GoogleMapsTimeZone.getUTCTime(TimeFunctions.cloneAndAddHoures(newNextSubConnection.getArrivalDate(), 1), newNextSubConnection.getDestination());
							LinkedBlockingQueue<Connection> connectionFromAirport = googleDirection.getConnection(newNextSubConnection.getDestination(), controlObject.getRequest().getDestination(), departureTimeFromAirport, true, controlObject.getRequest().getBestTransportation(), "", "", false);
							newConnection.addSubconnection(connectionFromAirport.peek());
						} catch (IllegalStateException | IOException | JDOMException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						newConnection.setRecursiveAction(Connection.ADD);
						controlObject.setConnectionIsFound();
						System.out.println("-> Connection Found");
						
					} 
					
					connectionList.add(newConnection);
					
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return connectionList;
		
	}
	
}
