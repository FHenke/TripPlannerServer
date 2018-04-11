package pathCalculation.recursiveBreadthFirst;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import api.GoogleMapsTimeZone;
import utilities.Connection;
import utilities.TimeFunctions;

public class TerminationCriteria {
	
	protected static final Logger logger = LogManager.getLogger(TerminationCriteria.class);

	
	public TerminationCriteria(){
		
	}
	
	
	public static boolean shouldExploit(Connection connection, ControlObject controlObject){
		boolean reachedDestinationInThisStep = false;
		
		//if this airport was already reached by a better (cheaper at a earlier point of time) connection terminate
		if(connection.getSubConnections().size() > 1 && !controlObject.addConnectionToAirportInfo(connection)){
			controlObject.addUnusedConnection(connection);
			controlObject.increaseCounter(ControlObject.BETTER_CONNECTION_TO_AIRPORT, connection);
			return false;
		}
		
		//If connections has already x steps terminate
		if(amountOfSubConnections(connection) >= 8){
			controlObject.addUnusedConnection(connection);
			controlObject.increaseCounter(ControlObject.TO_MANY_STEPS, connection);
			return false;
		}
		
		//If connection is already worse than the x best connections terminate this connection
		if(controlObject.isVirtualPriceToHigth(connection)){
			controlObject.addUnusedConnection(connection);
			controlObject.increaseCounter(ControlObject.CONNECTION_IS_WORSE, connection);
			return false;
		}
		
		// If the airport is one of the destination airports do following
		if(controlObject.isDestinationAirport(connection.getDestination().getIata())){
			Connection newConnection = addConnectionFromAirport(connection.clone(), controlObject);
			controlObject.addUsedConnection(newConnection.clone());
			controlObject.increaseCounter(ControlObject.CONNECTION_FOUND, connection);
			reachedDestinationInThisStep = true;
		}
		
		//if last airport was destination terminate connection
		//last and not current airport because if the current airport is one of the destinations the algorithm should have the possibility to go to a even better destination airport
		if(connection.isDestinationReached()){
			controlObject.addUnusedConnection(connection);
			controlObject.increaseCounter(ControlObject.LAST_CONNECTION_DESTINATION, connection);
			return false;
		}
		
		if(reachedDestinationInThisStep){
			connection.setDestinationReached(true);
		}
		
		//if the distance to the destination is to high terminate
		if(connection.getSubConnections().size() > 1 && isDistanceToDestinationToHigh3(controlObject, connection)){
			controlObject.addUnusedConnection(connection);
			controlObject.increaseCounter(ControlObject.TO_FAR_FROM_DESTINATION, connection);
			return false;
		}
		
		return true;
	}
	
	/**
	 * discards as least flights as possible
	 * @param connection
	 * @param controlObject
	 * @return
	 */
	/*public static boolean shouldExploitMin(Connection connection, ControlObject controlObject){
		//connection to destination is found
		if(controlObject.isDestinationAirport(connection.getDestination().getIata())){
			connection = addConnectionFromAirport(connection, controlObject);
			controlObject.addUsedConnection(connection.clone());
			return false;
		}
		if(connection.getSubConnections().size() >= 4){
			controlObject.addUnusedConnection(connection);
			return false;
		}
		return true;
	}*/
	
	private static boolean isDistanceToDestinationToHigh1(ControlObject controlObject, Connection connection){
		try{
			AirportInfo airportinfo = controlObject.getAirportinfo(connection.getDestination().getIata());		
			int fullDistance = controlObject.getBeelineDistance();
			int distanceToDestination = airportinfo.getDistanceToDestination();
			int hops = amountOfSubConnections(connection);
			double percentage = (100 * (fullDistance - distanceToDestination)) / fullDistance;
			
			if(percentage < Math.pow(hops, 2) - 20)
				return true;
		
			return false;
		}catch(NullPointerException e){
			return true;
		}
	}
	
	private static boolean isDistanceToDestinationToHigh2(ControlObject controlObject, Connection connection){
		try{
			AirportInfo airportinfo = controlObject.getAirportinfo(connection.getDestination().getIata());		
			int fullDistance = controlObject.getBeelineDistance();
			int distanceSolved = fullDistance - airportinfo.getDistanceToDestination();
			int hops = amountOfSubConnections(connection);
			
			if(distanceSolved < 100 * Math.pow(hops, 2) - 2000)
				return true;
		
			return false;
		}catch(NullPointerException e){
			System.out.println("Error");
			return true;
		}
	}
	
	private static boolean isDistanceToDestinationToHigh3(ControlObject controlObject, Connection connection){
		try{
			AirportInfo airportinfo = controlObject.getAirportinfo(connection.getDestination().getIata());		
			int fullDistance = controlObject.getBeelineDistance();
			int distanceSolved = fullDistance - airportinfo.getDistanceToDestination();
			double duration = (double) connection.getDuration().getMillis() / 1000.0 / 60.0 / 60.0;
			double factor = controlObject.getFactorForMaxDistance();
			
			//System.out.println(fullDistance + "km " + duration + "h " + connection.getDestination().getName() + ": " + distanceSolved + " < " + (factor * Math.pow(duration, 2) - fullDistance));
			
			if(distanceSolved < factor * Math.pow(duration, 2) - fullDistance){
				//System.out.println("T: " + distanceSolved + " < " + (factor * Math.pow(duration, 2) - fullDistance));
				return true;
			}
		
			//System.out.println("F: " + distanceSolved + " < " + (factor * Math.pow(duration, 2) - fullDistance));
			return false;
		}catch(NullPointerException e){
			System.out.println("Error");
			return true;
		}
	}
	
	/**
	 * counts the amount of plane hops (including subconnections of flights)
	 * @param connection
	 * @return
	 */
	private static int amountOfSubConnections(Connection connection){
		return connection.getSubConnections().size();
	}
	
	
	private static Connection addConnectionFromAirport(Connection connection, ControlObject controlObject){
		controlObject.incrementGoogleApiCounter();
		// first remove the rough connection from origin to origin airport and than add the new one (with origin and departure time and polyline)
		api.GoogleMapsDirection googleDirection = new api.GoogleMapsDirection();
		//Add connection from origin to origin airport
		try {
			//GregorianCalendar departureTimeOnAirport = GoogleMapsTimeZone.getUTCTime(TimeFunctions.cloneAndAddHoures(connection.getSubConnections().peek().getDepartureDate(), 1), connection.getDestination());
			GregorianCalendar departureTimeOnAirport = GoogleMapsTimeZone.getUTCTime(TimeFunctions.cloneAndAddHoures(connection.getArrivalDate(), 1), connection.getDestination());
			LinkedBlockingQueue<Connection> connectionFromAirport = googleDirection.getConnection(connection.getDestination(), controlObject.getRequest().getDestination(), departureTimeOnAirport, true, controlObject.getRequest().getBestTransportation(), "", "", false);
			connection.addSubconnection(connectionFromAirport.peek());
		} catch (IllegalStateException | IOException | JDOMException e) {
			logger.warn("Connection from destination Airport to destination can't be added. (destination airport: " + connection.getDestination().getIata() + ")\n" + e);
		}		
		return connection;
	}
	

}
