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
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request) throws SQLException{
		
		LinkedBlockingQueue<Connection> connection;
		
		//get closest airport from origin
		ClosestAirports closeOriginAirports = new ClosestAirports();
		Place originAirport = closeOriginAirports.getClosestBeelineAirport(request.getOrigin());
		
		ClosestAirports closeDestinationAirports = new ClosestAirports();
		Place destinationAirport = closeDestinationAirports.getClosestBeelineAirport(request.getDestination());
		
		
		
		
		connection = QueryConnection.getAllOutboundConnections(originAirport, destinationAirport);
		connection.parallelStream().forEach(con -> {
			con.setAction(Connection.ADD);
		});
		return connection;
	}
}
