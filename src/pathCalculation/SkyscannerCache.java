package pathCalculation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.naming.spi.DirStateFactory.Result;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import api.utilities.GoogleMaps;
import database.ClosestAirports;
import database.DatabaseConnection;
import database.utilities.ClosestAirportListElement;
import utilities.Connection;
import utilities.Request;

public class SkyscannerCache {
	
	protected static final Logger logger = LogManager.getLogger(ClosestAirports.class);
	public static final int DISTANCE = 1;
	public static final int DURATION = 2;
	private int valueOfInterest = DISTANCE;

	public SkyscannerCache(){
		
	}
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request){
		try {
			DatabaseConnection databaseConnection = new DatabaseConnection();
			api.SkyscannerCache skyCache = new api.SkyscannerCache();
			//get closest airport from origin
			ClosestAirports closeOriginAirports = new ClosestAirports(databaseConnection.getConnection());
			closeOriginAirports.createAirportsBeeline(request.getOrigin(), 10);
			closeOriginAirports.setAirportOtherDistance(-1, GoogleMaps.DRIVING);
			LinkedList<ClosestAirportListElement> originToAirportList = closeOriginAirports.getAirportList();
			
			//get closest airport from destination
			ClosestAirports closeDestinationAirports = new ClosestAirports(databaseConnection.getConnection());
			closeDestinationAirports.createAirportsBeeline(request.getDestination(), 10);
			closeDestinationAirports.setAirportOtherDistance(1, GoogleMaps.DRIVING);
			LinkedList<ClosestAirportListElement> airportToDestinationList = closeDestinationAirports.getAirportList();			
			
			
			//get flight between both airports
			int i1 = 0;
			int i2 = 0;
			int lastChanged = 1;
			//boolean success = false;
			while(i1 < originToAirportList.size() || i2 < airportToDestinationList.size() ){
				int counter = 0;
				
				if(lastChanged == 1)
					counter = i2;
				if(lastChanged == 2)
					counter = i1;
			
				for(int c = 0; c <= counter; c++){
					if(lastChanged == 1){
						System.out.println(originToAirportList.get(i1).getAirport().getIata() + " - " + airportToDestinationList.get(c).getAirport().getIata() + " - " + request.getDepartureDateString().getTimeInMillis());
						LinkedBlockingQueue<Connection> result = skyCache.getAllConnections(originToAirportList.get(i1).getAirport().getIata(), airportToDestinationList.get(c).getAirport().getIata(), request.getDepartureDateString(), null);
						if(!result.isEmpty()){
							
							LinkedBlockingQueue<Connection> connection = new LinkedBlockingQueue<Connection>();
							connection.add(originToAirportList.get(i1).getConnection());
							connection.add(result.element());
							connection.add(airportToDestinationList.get(c).getConnection());
							return connection;
						}
					}
					if(lastChanged == 2){
						System.out.println(originToAirportList.get(c).getAirport().getIata() + " - " + airportToDestinationList.get(i2).getAirport().getIata() + " - " + request.getDepartureDateString().getTimeInMillis());
						LinkedBlockingQueue<Connection> result = skyCache.getAllConnections(originToAirportList.get(c).getAirport().getIata(), airportToDestinationList.get(i2).getAirport().getIata(), request.getDepartureDateString(), null);
						if(!result.isEmpty()){
							LinkedBlockingQueue<Connection> connection = new LinkedBlockingQueue<Connection>();
							connection.add(originToAirportList.get(c).getConnection());
							connection.add(result.element());
							connection.add(airportToDestinationList.get(i2).getConnection());
							return connection;
						}
					}
				}
				
				
				if(i1 == originToAirportList.size() - 1){
					if(i2 == airportToDestinationList.size() - 1){
						return null;
					}
					else{
						i2++;
						lastChanged = 2;
					}
				}else{
					if(i2 == airportToDestinationList.size() - 1){
						i1++;
						lastChanged = 1;
					}else{
						int diff1 = getValue(originToAirportList.get(i1 + 1)) - getValue(originToAirportList.get(0));
						int diff2 = getValue(airportToDestinationList.get(i2 + 1)) - getValue(airportToDestinationList.get(0));
						if(diff1 <= diff2){
							i1++;
							lastChanged = 1;
						}
						if(diff1 > diff2){
							i2++;
							lastChanged = 2;
						}
					}
				}
			}
			
		
		
		
		
		} catch (SQLException e) {
			logger.error("It was not possible to generate a connecthionwith the Database \n" + e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return null;
	}
	
	private int getValue(ClosestAirportListElement airport){
		if(valueOfInterest == 1){
			return airport.getConnection().getDistance();
		}
		if(valueOfInterest == 2){
			return (int) airport.getConnection().getDuration().getMillis();
		}
		return -1;
	}
}
