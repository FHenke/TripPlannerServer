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
		Place origin = closestAirports.getClosestBeelineAirport(request.getOrigin());
		Place destination = closestAirports.getClosestBeelineAirport(request.getDestination());
		long startTime = System.nanoTime();

		
		api.GoogleMapsDirection googleDirection = new api.GoogleMapsDirection();
		try {
			//Connection startConnection = new Connection(request.getOrigin(), origin);
			LinkedBlockingQueue<Connection> connectionToAirport = googleDirection.getConnection(request.getOrigin(), origin, request.getDepartureDate(), request.isDepartureTime(), request.getBestTransportation(), "", "", false);
			//startConnection.addSubconnection(connectionToAirport.peek());
			//connectionList.add(startConnection);
			connectionList.add(connectionToAirport.peek());
			
			int level = 0;
			while(!controlObject.isConnectionFound()){
				LinkedBlockingQueue<Connection> tmpConnectionList = new LinkedBlockingQueue<Connection>();
				System.out.println(level++);
				connectionList.parallelStream().forEach(connection -> {
					if(connection.getAction().equals(Connection.ADD) || connection.getAction().equals(Connection.UNUSED)){
						System.out.println(connection.getOrigin().getName() + " - " + connection.getDestination().getName());
						SearchNode searchNode = new SearchNode();
						tmpConnectionList.addAll(searchNode.getNextConnections(controlObject, connection.clone(), destination));
					}
				});
				connectionList = tmpConnectionList;
			}
			
			LinkedBlockingQueue<Connection> connectionFromAirport = googleDirection.getConnection(destination, request.getDestination(), controlObject.getConnectionList().peek().getArrivalDate(), true, request.getBestTransportation(), "", "", false);
			controlObject.getConnectionList().peek().addSubconnection(connectionFromAirport.peek());
			
		
		} catch (IllegalStateException | IOException | JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		long elapsedTime = System.nanoTime() - startTime;
		System.out.println((double) elapsedTime / 1000000000.0);
		return connectionList;
		//return controlObject.getConnectionList();
	}
}
