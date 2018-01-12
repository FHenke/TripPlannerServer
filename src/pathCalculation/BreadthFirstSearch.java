package pathCalculation;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.jdom2.JDOMException;

import database.ClosestAirports;
import pathCalculation.breadthFirstSearch.ControlObject;
import pathCalculation.breadthFirstSearch.SearchNode;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class BreadthFirstSearch {

	
	public BreadthFirstSearch(){
		
	}
	
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request){
		
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		ControlObject controlObject = new ControlObject();
		ClosestAirports closestAirports = new ClosestAirports();
		Place originAirport = closestAirports.getClosestBeelineAirport(request.getOrigin());
		Place destinationAirport = closestAirports.getClosestBeelineAirport(request.getDestination());
		long startTime = System.nanoTime();
		
		controlObject.setDepartureAirport(destinationAirport);
		controlObject.setRequest(request);
		Connection headConnection = new Connection(request.getOrigin(), originAirport);
		headConnection.setArrivalDate(request.getDepartureDateString());
		// set a temporal connection to the startairport. This connection is needed by the SearchNode class to calculate the following flights.  Only the destination and arrivalDate parameters are required.
		Connection tmpStartConnection = new Connection(request.getOrigin(), originAirport);
		tmpStartConnection.setArrivalDate(request.getDepartureDateString());
		headConnection.getSubConnections().add(tmpStartConnection);
		
		//api.GoogleMapsDirection googleDirection = new api.GoogleMapsDirection();
		try {
			//Connection startConnection = new Connection(request.getOrigin(), origin);
			//LinkedBlockingQueue<Connection> connectionToAirport = googleDirection.getConnection(request.getOrigin(), originAirport, request.getDepartureDateString(), true, request.getBestTransportation(), "", "", false);
			//startConnection.addSubconnection(connectionToAirport.peek());
			//connectionList.add(startConnection);
			connectionList.add(headConnection);
			
			int level = 0;
			while(!controlObject.isConnectionFound() && level < 10){
				LinkedBlockingQueue<Connection> tmpConnectionList = new LinkedBlockingQueue<Connection>();
				System.out.println(level++);
				connectionList.parallelStream().forEach(connection -> {
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
			
			//LinkedBlockingQueue<Connection> connectionFromAirport = googleDirection.getConnection(destinationAirport, request.getDestination(), controlObject.getConnectionList().peek().getArrivalDate(), true, request.getBestTransportation(), "", "", false);
			//controlObject.getConnectionList().peek().addSubconnection(connectionFromAirport.peek());
			
		
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		long elapsedTime = System.nanoTime() - startTime;
		System.out.println((double) elapsedTime / 1000000000.0);
		return connectionList;
		//return controlObject.getConnectionList();
	}
}
