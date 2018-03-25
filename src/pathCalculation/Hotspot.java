package pathCalculation;

import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import database.ClosestAirports;
import database.ConnectedHotspots;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class Hotspot {

	public LinkedBlockingQueue<Connection> getConnectionList(Request request) throws SQLException{
		
		LinkedBlockingQueue<Connection> connectionList;
		
		//get closest airport from origin
		ClosestAirports closeOriginAirports = new ClosestAirports();
		Place originAirport = closeOriginAirports.getClosestBeelineAirport(request.getOrigin());
		
		ClosestAirports closeDestinationAirports = new ClosestAirports();
		Place destinationAirport = closeDestinationAirports.getClosestBeelineAirport(request.getDestination());
		
		
		connectionList = ConnectedHotspots.getAllOutboundConnectionsWithinOneDay(originAirport, destinationAirport, request.getDepartureDateString());
		connectionList.addAll(ConnectedHotspots.getAllInboundConnectionsWithinFiveDays(destinationAirport, request.getDepartureDateString()));
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
