package pathCalculation;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.utilities.GoogleMaps;
import database.ClosestAirports;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class EStreamingCache {
	
	protected static final Logger logger = LogManager.getLogger(ClosestAirports.class);
	public static final int DISTANCE = 1;
	public static final int DURATION = 2;
	private int valueOfInterest = DISTANCE;

	public EStreamingCache(){
		
	}
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request, LinkedBlockingQueue<Connection> connection){
		try {
			api.EStream eStream = new api.EStream();
			Connection headConnection;
			
			//get closest airport from origin
			ClosestAirports closeOriginAirports = new ClosestAirports();
			Place originAirport = closeOriginAirports.getClosestBeelineAirport(request.getOrigin());
			
			//get closest airport from destination
			ClosestAirports closeDestinationAirports = new ClosestAirports();
			Place destinationAirport = closeDestinationAirports.getClosestBeelineAirport(request.getDestination());
			
			LinkedBlockingQueue<Connection> result = eStream.getAllConnections(originAirport.getIata(), destinationAirport.getIata(), request.getDepartureDateString(), null);
			// if a flight connection was found take this connection
			if(!result.isEmpty()){
				headConnection = new Connection(originAirport, destinationAirport);
				
				for(utilities.Connection con : result){
					headConnection.getSubConnections().add(con);
				}
				
				//some more information for head connection
				headConnection.setSummary("Plain");
				
				connection.add(headConnection);
				return connection;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}

