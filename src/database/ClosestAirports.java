package database;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import api.GoogleMapsDirection;
import api.GoogleMapsDistance;
import database.utilities.CompareClosestAirportsForDistance;
import database.utilities.CompareClosestAirportsForDuration;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class ClosestAirports {
	
	protected static final Logger logger = LogManager.getLogger(ClosestAirports.class);
	
	public static final int ORDERED_NONE = 0;
	public static final int ORDERED_BY_BEELINE = 1;
	public static final int ORDERED_BY_DISTANCE = 2;
	public static final int ORDERED_BY_DURATION = 3;

	private java.sql.Connection conn = null;
	private LinkedBlockingQueue<Connection> airportList = new LinkedBlockingQueue<Connection>();
	private int ordered = 0;
	
	public ClosestAirports(java.sql.Connection conn){
		this.conn = conn;
	}
	
	public ClosestAirports(){
		this.conn = DatabaseConnection.getConnection();
	}
	
	/**
	 * 
	 * @param request
	 * @param place
	 * @param placeIsOrigin
	 * @param amountOfAirportsToReturn int value between 1 and 25, if the value is < 1 it will be changed to 1 if it is > 25 it will be changed to 25
	 * @return
	 * @throws SQLException 
	 * @throws JDOMException 
	 * @throws IOException 
	 * @throws IllegalStateException 
	 * @throws ClientProtocolException 
	 */
	public Connection[] getFastestAirports(Request request, Place place, boolean placeIsOrigin, int amountOfAirportsToReturn) throws SQLException, ClientProtocolException, IllegalStateException, IOException, JDOMException{
		//Because Google Maps distance can not handle more than 25 places
		amountOfAirportsToReturn = correctAirportAmountValue(amountOfAirportsToReturn);
		place = addCoordinatesToPlace(place);
		LinkedList<Place> closestBeelineAirports = QueryClosestAirports.getClosestAirports(place, amountOfAirportsToReturn);
		LinkedBlockingQueue<Connection> connections = getConnectionsBetweenPlaces(closestBeelineAirports, place, placeIsOrigin, request);

		return OrderListByDistance(connections);
	}
	
	
	private int correctAirportAmountValue(int amountOfAirportsToReturn){
		if(amountOfAirportsToReturn < 1)
			return 1;
		if(amountOfAirportsToReturn > 25)
			return 25;
		return amountOfAirportsToReturn;
	}
	
	private Place addCoordinatesToPlace(Place place){
		if(place.getLatitude() == Double.MAX_VALUE || place.getLongitude() == Double.MAX_VALUE){
			try {
				place = api.GoogleMapsGeocoding.addCoordinatesToPlace(place);
			} catch (NumberFormatException | NullPointerException | IllegalStateException | IOException | JDOMException e) {
				logger.error("Problem by adding coordinates to a place: " + place.getName() + "\n " + e);
			}
		}
		return place;
	}
	
	private LinkedBlockingQueue<Connection> getConnectionsBetweenPlaces(LinkedList<Place> closestBeelineAirports, Place place, boolean placeIsOrigin, Request request) throws ClientProtocolException, IllegalStateException, IOException, JDOMException{
		GoogleMapsDistance googleDistance = new GoogleMapsDistance();
		LinkedList<Place> originPlaces = null;
		LinkedList<Place> destinationPlaces = null;
		if(placeIsOrigin){
			originPlaces.add(place);
			destinationPlaces = closestBeelineAirports;
		}else{
			originPlaces = closestBeelineAirports;
			destinationPlaces.add(place);
		}
		LinkedBlockingQueue<Connection> connections = googleDistance.getConnection(originPlaces, destinationPlaces, request.getDepartureDateString(), request.isDepartureTime(), request.getBestTransportation(), null, "en");
		return connections;
	}
	
	/**
	 * Orders the connection list by the distance to the airport
	 * @return
	 */
	private Connection[] OrderListByDistance(LinkedBlockingQueue<Connection> connections){
		Connection[] connectionArray = connections.toArray(new Connection[connections.size()]);
		Arrays.sort(connectionArray, new CompareClosestAirportsForDistance());
		return connectionArray;
	}
	
	/**
	 * orders the connection list by the duration to the airport
	 * @return
	 */
	private Connection[] OrderListByDuration(LinkedBlockingQueue<Connection> connections){
		Connection[] connectionArray = connections.toArray(new Connection[connections.size()]);
		Arrays.sort(connectionArray, new CompareClosestAirportsForDuration());
		return connectionArray;
	}
	
	
	/**
	 * Returns the closest airport to a given place using beeline distance
	 * @param place the place to which the closest airport should be find
	 * @return closest airport to the given place
	 */
	public Place getClosestBeelineAirport(Place place){
		Place airport = null;
		try {
			airport = QueryClosestAirports.getClosestAirports(place, 1).peekFirst();
		} catch (SQLException e) {
			logger.error("Cannt find closest airport to this place: " + place.getName() + "\n " + e);
		}
		
		return airport;
	}
	
	
	// ##########################################################################################
	// #################################### DEPRECIATED #########################################
	// ##########################################################################################
	
	
	/**
	 * creates a list with the "limit"s closest airport around the given place
	 * @param place The center place of the search.
	 * @param limit the how many closest airports should be returned (beeline).
	 * @param direction is the place the origin or destination (depending on tis parameter the place will be set as origin or destination in the Connection object.)
	 * 			< 0 for the direction to the Airport | else for the direction from the Airport
	 * @return A List of the closest airport (beeline), null if its failed
	 */
	public LinkedBlockingQueue<utilities.Connection> createAirportsBeeline(Place place, int limit, int direction){
		
		place = addCoordinatesToPlace(place);		
		
		String querryString = "SELECT ST_Distance_sphere(airports.location, ST_GeomFromText('POINT("
				+ place.getLongitude()
				+ " "
				+ place.getLatitude()
				+ ")',-1)) AS distance, airports.iata_code as iata, airports.name AS name, ST_X(airports.location) AS lng, ST_Y(airports.location) AS lat FROM airports ORDER BY distance";
		if(limit > 0){
			querryString += " LIMIT " + limit;
		}
		querryString += ";";
		
		System.out.println(querryString);
		

		
		try {
			ResultSet querryResult = conn.createStatement().executeQuery(querryString);
			while(querryResult.next()){
				utilities.Connection airportElement;
				Place airport = new Place(querryResult.getDouble("lng"), querryResult.getDouble("lat"));
				airport.setIata(querryResult.getString("iata"));
				airport.setName(querryResult.getString("name") + " Airport");
				airport.setType(Place.AIRPORT);
				if(direction < 0)
					airportElement = new utilities.Connection(place, airport);
				else
					airportElement = new utilities.Connection(airport, place);
				airportElement.setBeeline(querryResult.getInt("distance"));
				airportList.add(airportElement);
			}
		} catch (SQLException e) {
			System.out.println("Querry Fails");
			e.printStackTrace();
			return null;
		}
		
		ordered = ORDERED_BY_BEELINE;
		
		return airportList;
	}
	
	/**
	 * 
	 * @param transportation	means of transportation according to GoogleMaps class
	 * @return 1 for success | -1 unsuccessful (maybe list is empty, call createAirportsBeeline() before this function, maybe for one airport cant be find a distance or duration and it was not possible to remove it from the list)
	 */
	public int setAirportOtherDistance(String transportation){
		if(airportList.isEmpty()){
			return -1;
		}
		GoogleMapsDirection distance = new GoogleMapsDirection();
		LinkedBlockingQueue<utilities.Connection> newConnectionList = new LinkedBlockingQueue<utilities.Connection>();
		for(utilities.Connection airport : airportList){
			try {
				utilities.Connection connection = null;
				connection = distance.getConnection(airport.getOrigin(), airport.getDestination(), null, true, transportation, "", "de", false).element();
				connection.setBeeline(airport.getBeeline());
				newConnectionList.add(connection);
			} catch (IllegalStateException | IOException | JDOMException e) {
				logger.error("Problem by calculating the distance or duration of one Element: " + airport.getOrigin().getName() + " / " + airport.getDestination().getName() + "\n" + e);
			} catch (NoSuchElementException e){
				logger.error("It was not be possible to remove the element for which it was noch be possible to calculate the distance or duration. Element: " + airport.getOrigin().getName() + " / " + airport.getDestination().getName() + "\n");
			}
		}
		
		airportList = newConnectionList;
		return 1;
	}
	
	/**
	 * 
	 * @return
	 */
	public Connection[] getListOrderedByDistance(){
		Connection[] connectionArray = airportList.toArray(new Connection[airportList.size()]);
		Arrays.sort(connectionArray, new CompareClosestAirportsForDistance());
		return connectionArray;
	}
	
	/**
	 * 
	 * @return
	 */
	public Connection[] getListOrderedByDuration(){
		Connection[] connectionArray = airportList.toArray(new Connection[airportList.size()]);
		Arrays.sort(connectionArray, new CompareClosestAirportsForDuration());
		return connectionArray;
	}
	
	public LinkedBlockingQueue<utilities.Connection> getAirportList(){
		return airportList;
	}
	
	public int getOrder(){
		return ordered;
	}
	
	
}
