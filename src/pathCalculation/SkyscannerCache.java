package pathCalculation;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.naming.spi.DirStateFactory.Result;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import api.utilities.GoogleMaps;
import database.ClosestAirports;
import database.utilities.ClosestAirportListElement;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class SkyscannerCache {
	
	protected static final Logger logger = LogManager.getLogger(ClosestAirports.class);
	public static final int DISTANCE = 1;
	public static final int DURATION = 2;
	private int valueOfInterest = DISTANCE;

	public SkyscannerCache(){
		
	}
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request, LinkedBlockingQueue<Connection> connection){
		try {
			api.SkyscannerCache skyCache = new api.SkyscannerCache();
			Connection headConnection;
			
			//get closest airport from origin
			ClosestAirports closeOriginAirports = new ClosestAirports();
			Place originAirport = closeOriginAirports.getClosestBeelineAirport(request.getOrigin());
			
			//get closest airport from destination
			ClosestAirports closeDestinationAirports = new ClosestAirports();
			Place destinationAirport = closeDestinationAirports.getClosestBeelineAirport(request.getDestination());
			
			LinkedBlockingQueue<Connection> result = skyCache.getAllConnections(originAirport.getIata(), destinationAirport.getIata(), request.getDepartureDateString(), null);
			// if a flight connection was found take this connection
			if(!result.isEmpty()){
				headConnection = new Connection(originAirport, destinationAirport);
				
				for(utilities.Connection con : result){
					headConnection.getSubConnections().add(con);
				}
				
				//some more information for head connection
				headConnection.setSummary("Plain");
				headConnection.setRecursiveAction(Connection.ADD);
				
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

