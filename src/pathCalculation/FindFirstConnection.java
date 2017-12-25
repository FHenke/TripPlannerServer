package pathCalculation;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.utilities.GoogleMaps;
import database.ClosestAirports;
import utilities.Connection;
import utilities.Request;

public class FindFirstConnection {
	
	protected static final Logger logger = LogManager.getLogger(ClosestAirports.class);
	public static final int DISTANCE = 1;
	public static final int DURATION = 2;
	private int valueOfInterest = DISTANCE;

	public FindFirstConnection(){
		
	}
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request, LinkedBlockingQueue<Connection> connection){
		try {
			api.SkyscannerCache skyCache = new api.SkyscannerCache();
			api.EStream eStream = new api.EStream();
			Connection headConnection;
			String transportation = GoogleMaps.DRIVING;
		
			if(request.getTransportation()[3])
				transportation = GoogleMaps.WALKING;
			if(request.getTransportation()[2])
				transportation = GoogleMaps.BICYCLING;
			if(request.getTransportation()[1])
				transportation = GoogleMaps.TRANSIT;
			if(request.getTransportation()[0])
				transportation = GoogleMaps.DRIVING;
			
			
			//get closest airport from origin
			ClosestAirports closeOriginAirports = new ClosestAirports();
			LinkedBlockingQueue<Connection> originAirportBeeline = closeOriginAirports.createAirportsBeeline(request.getOrigin(), 3, -1);
			closeOriginAirports.setAirportOtherDistance(transportation);
			Connection[] originToAirportList = closeOriginAirports.getListOrderedByDistance();
			
			//get closest airport from destination
			ClosestAirports closeDestinationAirports = new ClosestAirports();
			LinkedBlockingQueue<Connection> destinationAirportBeeline = closeDestinationAirports.createAirportsBeeline(request.getDestination(), 3, 1);
			closeDestinationAirports.setAirportOtherDistance(transportation);
			Connection[] airportToDestinationList = closeDestinationAirports.getListOrderedByDistance();

			
			
			for(Connection a : originToAirportList){
				System.out.println(a.getOrigin().getName() + " - " + a.getDestination().getName() + " : " + a.getDistance() / 1000);
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
						//LinkedBlockingQueue<Connection> result = skyCache.getAllConnections(originToAirportList[i1].getDestination().getIata(), airportToDestinationList[c].getOrigin().getIata(), request.getDepartureDateString(), null);
						LinkedBlockingQueue<Connection> result = eStream.getCheapestConnection(originToAirportList[i1].getDestination().getIata(), airportToDestinationList[c].getOrigin().getIata(), request.getDepartureDateString(), null);
						// if a flight connection was found take this connection
						if(!result.isEmpty()){
							headConnection = new Connection(originToAirportList[i1].getOrigin(), airportToDestinationList[c].getDestination());
							
							headConnection.getSubConnections().addAll(Arrays.asList(originToAirportList));
							headConnection.getSubConnections().addAll(Arrays.asList(airportToDestinationList));
							
							headConnection.getSubConnections().add(originToAirportList[i1]);
							for(utilities.Connection con : result){
								headConnection.getSubConnections().add(con);
							}
							headConnection.getSubConnections().add(airportToDestinationList[c]);
							
							//remove all connections to and from airports that are not used
							for(Connection con : originToAirportList){
								if(con.getId() != originToAirportList[i1].getId()){
									headConnection.getSubConnections().add(new Connection(con.getId(), Connection.REMOVE));
								}
							}
							for(Connection con : airportToDestinationList){
								if(con.getId() != airportToDestinationList[c].getId()){
									headConnection.getSubConnections().add(new Connection(con.getId(), Connection.REMOVE));
								}
							}
							
							
							//some more information for head connection
							headConnection.setSummary("Car, Plain, Car");
							
							connection.add(headConnection);
							return connection;
						}
					}
					if(lastChanged == 2){
						//System.out.println(originToAirportList[c].getDestination().getIata() + " - " + airportToDestinationList[i2].getOrigin().getIata() + " - " + request.getDepartureDateString().getTimeInMillis());
						//LinkedBlockingQueue<Connection> result = skyCache.getAllConnections(originToAirportList[c].getDestination().getIata(), airportToDestinationList[i2].getOrigin().getIata(), request.getDepartureDateString(), null);
						LinkedBlockingQueue<Connection> result = eStream.getAllConnections(originToAirportList[c].getDestination().getIata(), airportToDestinationList[i2].getOrigin().getIata(), request.getDepartureDateString(), null);
						// if a flight connection was found take this connection
						if(!result.isEmpty()){
							headConnection = new Connection(originToAirportList[c].getOrigin(), airportToDestinationList[i2].getDestination());
							
							headConnection.getSubConnections().addAll(Arrays.asList(originToAirportList));
							headConnection.getSubConnections().addAll(Arrays.asList(airportToDestinationList));
							
							headConnection.getSubConnections().add(originToAirportList[c]);
							for(utilities.Connection con : result){
								headConnection.getSubConnections().add(con);
							}
							headConnection.getSubConnections().add(airportToDestinationList[i2]);

							//remove all connections to and from airports that are not used
							for(Connection con : originToAirportList){
								if(con.getId() != originToAirportList[c].getId()){
									headConnection.getSubConnections().add(new Connection(con.getId(), Connection.REMOVE));
								}
							}
							for(Connection con : airportToDestinationList){
								if(con.getId() != airportToDestinationList[i2].getId()){
									headConnection.getSubConnections().add(new Connection(con.getId(), Connection.REMOVE));
								}
							}
							
							//some more information for head connection
							headConnection.setSummary("Car, Plane, Car");
							
							connection.add(headConnection);
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

