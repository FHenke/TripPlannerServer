package pathCalculation;

import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import database.ClosestAirports;
import database.ConnectedAirports;
import database.QueryConnection;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class RequestDatabase {

	public RequestDatabase(){
		
	}
	
	public LinkedBlockingQueue<Connection> getConnectionListWithDate(Request request) throws SQLException{
		
		LinkedBlockingQueue<Connection> connectionList;
		
		//get closest airport from origin
		ClosestAirports closeOriginAirports = new ClosestAirports();
		Place originAirport = closeOriginAirports.getClosestBeelineAirport(request.getOrigin());
		
		ClosestAirports closeDestinationAirports = new ClosestAirports();
		Place destinationAirport = closeDestinationAirports.getClosestBeelineAirport(request.getDestination());
		
		
		
		
		connectionList = QueryConnection.getAllConnections(originAirport, destinationAirport, request.getDepartureDateString());
		connectionList = setAddAction(connectionList);
		connectionList = addSubConnections(connectionList);
		return connectionList;
	}
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request) throws SQLException{
		
		LinkedBlockingQueue<Connection> connectionList;
		
		//get closest airport from origin
		ClosestAirports closeOriginAirports = new ClosestAirports();
		Place originAirport = closeOriginAirports.getClosestBeelineAirport(request.getOrigin());
		
		ClosestAirports closeDestinationAirports = new ClosestAirports();
		Place destinationAirport = closeDestinationAirports.getClosestBeelineAirport(request.getDestination());
		
		
		
		
		connectionList = QueryConnection.getAllConnections(originAirport, destinationAirport);
		connectionList = setAddAction(connectionList);
		connectionList = addSubConnections(connectionList);
		return connectionList;
	}
	
	private LinkedBlockingQueue<Connection> setAddAction(LinkedBlockingQueue<Connection> connectionList){
		connectionList.parallelStream().forEach(connection -> {
			connection.setRecursiveAction(Connection.ADD);
		});
		return connectionList;
	}
	
	private LinkedBlockingQueue<Connection> addSubConnections(LinkedBlockingQueue<Connection> connectionList){
		database.utilities.SQLUtilities.addSubConnections(connectionList);
		return connectionList;
	}
}
