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
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request, LinkedBlockingQueue<Connection> connection){
		try {
			DatabaseConnection databaseConnection = new DatabaseConnection();
			api.SkyscannerCache skyCache = new api.SkyscannerCache();
			api.EStream eStream = new api.EStream();
			
			//get closest airport from origin
			ClosestAirports closeOriginAirports = new ClosestAirports(databaseConnection.getConnection());
			LinkedBlockingQueue<Connection> originAirportBeeline = closeOriginAirports.createAirportsBeeline(request.getOrigin(), 1, -1);
			closeOriginAirports.setAirportOtherDistance(GoogleMaps.DRIVING);
			Connection[] originToAirportList = closeOriginAirports.orderListByDistance();
			
			//get closest airport from destination
			ClosestAirports closeDestinationAirports = new ClosestAirports(databaseConnection.getConnection());
			LinkedBlockingQueue<Connection> destinationAirportBeeline = closeDestinationAirports.createAirportsBeeline(request.getDestination(), 3, 1);
			closeDestinationAirports.setAirportOtherDistance(GoogleMaps.DRIVING);
			Connection[] airportToDestinationList = closeDestinationAirports.orderListByDistance();
			
			for(Connection a : airportToDestinationList){
				System.out.println(a.getOrigin().getName() + " : " + a.getDistance() / 1000);
			}
			
			
			//get flight between both airports
			int i1 = 0;
			int i2 = 0;
			int lastChanged = 1;
			//boolean success = false;
			while(i1 < originToAirportList.length || i2 < airportToDestinationList.length ){
				int counter = 0;
				
				if(lastChanged == 1)
					counter = i2;
				if(lastChanged == 2)
					counter = i1;
			
				for(int c = 0; c <= counter; c++){
					if(lastChanged == 1){
						//System.out.println(originToAirportList[i1].getDestination().getIata() + " - " + airportToDestinationList[c].getOrigin().getIata() + " - " + request.getDepartureDateString().getTimeInMillis());
						//LinkedBlockingQueue<Connection> result = skyCache.getAllConnections(originToAirportList[i1].getDestination().getIata(), airportToDestinationList[c].getOrigin().getIata(), request.getDepartureDateString(), null);
						LinkedBlockingQueue<Connection> result = eStream.getAllConnections(originToAirportList[i1].getDestination().getIata(), airportToDestinationList[c].getOrigin().getIata(), request.getDepartureDateString(), null);
						// if a flight connection was found take this connection
						if(!result.isEmpty()){
							//LinkedBlockingQueue<Connection> connection = new LinkedBlockingQueue<Connection>();
							connection.add(originToAirportList[i1]);
							for(utilities.Connection con : result){
								connection.add(con);
							}
							connection.add(airportToDestinationList[c]);
							return connection;
						}
					}
					if(lastChanged == 2){
						//System.out.println(originToAirportList[c].getDestination().getIata() + " - " + airportToDestinationList[i2].getOrigin().getIata() + " - " + request.getDepartureDateString().getTimeInMillis());
						//LinkedBlockingQueue<Connection> result = skyCache.getAllConnections(originToAirportList[c].getDestination().getIata(), airportToDestinationList[i2].getOrigin().getIata(), request.getDepartureDateString(), null);
						LinkedBlockingQueue<Connection> result = eStream.getAllConnections(originToAirportList[c].getDestination().getIata(), airportToDestinationList[i2].getOrigin().getIata(), request.getDepartureDateString(), null);
						// if a flight connection was found take this connection
						if(!result.isEmpty()){
							//LinkedBlockingQueue<Connection> connection = new LinkedBlockingQueue<Connection>();
							connection.add(originToAirportList[c]);
							for(utilities.Connection con : result){
								connection.add(con);
							}
							connection.add(airportToDestinationList[i2]);
							return connection;
						}
					}
				}
				
				
				if(i1 == originToAirportList.length - 1){
					if(i2 == airportToDestinationList.length - 1){
						return null;
					}
					else{
						i2++;
						lastChanged = 2;
					}
				}else{
					if(i2 == airportToDestinationList.length - 1){
						i1++;
						lastChanged = 1;
					}else{
						int diff1 = getValue(originToAirportList[i1 + 1]) - getValue(originToAirportList[0]);
						int diff2 = getValue(airportToDestinationList[i2 + 1]) - getValue(airportToDestinationList[0]);
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
			logger.error("It was not possible to generate a connecthion with the Database \n" + e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private int getValue(Connection connection){
		if(valueOfInterest == 1){
			return connection.getDistance();
		}
		if(valueOfInterest == 2){
			return (int) connection.getDuration().getMillis();
		}
		return -1;
	}
}
