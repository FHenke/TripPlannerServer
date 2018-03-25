package pathCalculation;

import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import database.ClosestAirports;
import database.ConnectedAirports;
import database.ConnectedHotspots;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class OutboundConnections {

	public OutboundConnections(){
		
	}
	
	public LinkedBlockingQueue<Connection> getConnectionListWithDate(Request request, LinkedBlockingQueue<Connection> connection) throws SQLException{
		
		LinkedBlockingQueue<Connection> connectionList;
		
		//get closest airport from origin
		ClosestAirports closeOriginAirports = new ClosestAirports();
		Place originAirport = closeOriginAirports.getClosestBeelineAirport(request.getOrigin());
		
		//get closest airport from destination
		ClosestAirports closeDestinationAirports = new ClosestAirports();
		Place destinationAirport = closeDestinationAirports.getClosestBeelineAirport(request.getDestination());
		
		
		connectionList = ConnectedAirports.getAllOutboundConnectionsWithinOneDay(originAirport, request.getDepartureDateString());
		connectionList.addAll(ConnectedAirports.getAllInboundConnectionsWithinFiveDays(destinationAirport, request.getDepartureDateString()));
		connectionList = setAddAction(connectionList);
		
		return connectionList;
	}
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request, LinkedBlockingQueue<Connection> connection) throws SQLException{
		
		LinkedBlockingQueue<Connection> connectionList;
		
		//get closest airport from origin
		ClosestAirports closeOriginAirports = new ClosestAirports();
		Place originAirport = closeOriginAirports.getClosestBeelineAirport(request.getOrigin());
		
		//get closest airport from destination
		ClosestAirports closeDestinationAirports = new ClosestAirports();
		Place destinationAirport = closeDestinationAirports.getClosestBeelineAirport(request.getDestination());
		
		
		connectionList = ConnectedAirports.getAllOutboundConnections(originAirport);
		connectionList.addAll(ConnectedAirports.getAllInboundConnections(destinationAirport));
		connectionList = setAddAction(connectionList);
		
		return connectionList;
	}
	
	public LinkedBlockingQueue<Connection> setAddAction(LinkedBlockingQueue<Connection> connectionList){
		connectionList.parallelStream().forEach(connection -> {
			connection.setRecursiveAction(Connection.ADD);
		});
		return connectionList;
	}
	
}
