package pathCalculation;

import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import database.ClosestAirports;
import database.QueryAllConnectionsFromAirport;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class OutboundConnections {

	public OutboundConnections(){
		
	}
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request, LinkedBlockingQueue<Connection> connection) throws SQLException{
		
		//get closest airport from origin
		ClosestAirports closeOriginAirports = new ClosestAirports();
		Place originAirport = closeOriginAirports.getClosestBeelineAirport(request.getOrigin());
		
		connection = QueryAllConnectionsFromAirport.getAllOutboundConnections(originAirport);
		return connection;
	}
	
}
