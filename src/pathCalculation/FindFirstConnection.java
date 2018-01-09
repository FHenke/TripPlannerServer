package pathCalculation;

import java.io.IOException;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import database.ClosestAirports;
import utilities.Connection;
import utilities.Place;
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
			api.EStream eStream = new api.EStream();
			Connection headConnection;			
			
			//get closest airports from origin
			ClosestAirports closeOriginAirports = new ClosestAirports();
			Connection[] originToAirportList = closeOriginAirports.getClosestAirports(request, request.getOrigin(), true, 3);
			
			//get closest airports from destination
			ClosestAirports closeDestinationAirports = new ClosestAirports();
			Connection[] airportToDestinationList = closeDestinationAirports.getClosestAirports(request, request.getDestination(), false, 3);
			
			
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
						LinkedBlockingQueue<Connection> result = eStream.getCheapestConnection(originToAirportList[i1].getDestination().getIata(), airportToDestinationList[c].getOrigin().getIata(), request.getDepartureDateString(), null);
						// if a flight connection was found take this connection
						if(!result.isEmpty()){
							//create new head connection
							headConnection = new Connection(originToAirportList[i1].getOrigin(), airportToDestinationList[c].getDestination());
							headConnection.setAction(Connection.ADD);
							
							//add all unused connections (the distance calculated to the different airports)
							headConnection.getSubConnections().addAll(Arrays.asList(originToAirportList));
							headConnection.getSubConnections().addAll(Arrays.asList(airportToDestinationList));
							
							//add the connection to the first airport
							Connection connectionToAirport = connectAirportWithPlace(request, originToAirportList[i1].getDestination(), result.peek().getDepartureDate(), true);
							headConnection.addSubconnection(connectionToAirport);
							
							//add the flights to the connection
							for(utilities.Connection con : result){
								con.setRecursiveAction(Connection.ADD);
								headConnection.getSubConnections().add(con);
							}
							
							//add the connection from the destination airport to the destination
							Connection connectionFromAirport = connectAirportWithPlace(request, airportToDestinationList[c].getOrigin(), result.peek().getArrivalDate(), false);
							headConnection.addSubconnection(connectionFromAirport);
				
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
							
							//headConnection.getSubConnections().add(originToAirportList[c]);
							originToAirportList[c].setRecursiveAction(Connection.ADD);
							for(utilities.Connection con : result){
								con.setRecursiveAction(Connection.ADD);
								headConnection.getSubConnections().add(con);
							}
							//headConnection.getSubConnections().add(airportToDestinationList[i2]);
							airportToDestinationList[i2].setRecursiveAction(Connection.ADD);
							headConnection.setAction(Connection.ADD);
							
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
	
	/**
	 * returns the way how to come to the origin arport and how to come from the destination airport to the destination
	 * @param request
	 * @param otherPlace
	 * @param date
	 * @param isWayToOriginAirport distinguishs if the way from the origin to the origin airport or the way from the destination airport to the destination shpuld be returned
	 * @return
	 * @throws ClientProtocolException
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws JDOMException
	 */
	private Connection connectAirportWithPlace(Request request, Place otherPlace, GregorianCalendar date, boolean isWayToOriginAirport) throws ClientProtocolException, IllegalStateException, IOException, JDOMException{
		GregorianCalendar newTime = (GregorianCalendar) date.clone();
		api.GoogleMapsDirection googleDirection = new api.GoogleMapsDirection();
		Place originPlace;
		Place destinationPlace;
		
		if(isWayToOriginAirport){
			//the minimum transfer time on the origin airport
			newTime.add(GregorianCalendar.HOUR_OF_DAY, -1);
			originPlace = request.getOrigin();
			destinationPlace = otherPlace;
		}else{
			//the minimum transfer time on the destination airport 
			newTime.add(GregorianCalendar.HOUR_OF_DAY, 1);
			originPlace = otherPlace;
			destinationPlace = request.getDestination();
		}
		
		Connection connection = googleDirection.getConnection(originPlace, destinationPlace, newTime, !isWayToOriginAirport, request.getBestTransportation(), "", "en", false).peek();
		connection.setRecursiveAction(Connection.ADD);
		return connection;
	}
}

