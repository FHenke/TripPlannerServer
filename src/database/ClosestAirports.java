package database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.joda.time.Duration;

import api.GoogleMapsDirection;
import api.GoogleMapsDistance;
import api.utilities.GoogleMaps;
import database.updateTables.UpdateContinents;
import database.utilities.ClosestAirportListElement;
import database.utilities.CompareClosestAirportsForDistance;
import database.utilities.CompareClosestAirportsForDuration;
import utilities.Place;

public class ClosestAirports {
	
	protected static final Logger logger = LogManager.getLogger(ClosestAirports.class);
	
	public static final int ORDERED_NONE = 0;
	public static final int ORDERED_BY_BEELINE = 1;
	public static final int ORDERED_BY_DISTANCE = 2;
	public static final int ORDERED_BY_DURATION = 3;

	private Connection conn = null;
	private LinkedBlockingQueue<utilities.Connection> airportList = new LinkedBlockingQueue<utilities.Connection>();
	private int ordered = 0;
	
	public ClosestAirports(Connection conn){
		this.conn = conn;
	}
	
	public ClosestAirports(){
		this.conn = DatabaseConnection.getConnection();
	}
	
	/**
	 * Returns the closest airport to a given place using beeline distance
	 * @param place the place to which the closest airport should be find
	 * @return closest airport to the given place
	 */
	public Place getClosestBeelineAirport(Place place){
		Place airport;
		
		if(place.getLatitude() == Double.MAX_VALUE || place.getLongitude() == Double.MAX_VALUE){
			try {
				place = api.GoogleMapsGeocoding.addCoordinatesToPlace(place);
			} catch (NumberFormatException | NullPointerException | IllegalStateException | IOException | JDOMException e) {
				logger.error("Problem by adding coordinates to a place: " + place.getName() + "\n " + e);
			}
		}		
		
		String querryString = "SELECT ST_Distance_sphere(airports.location, ST_GeomFromText('POINT("
				+ place.getLongitude()
				+ " "
				+ place.getLatitude()
				+ ")',-1)) AS distance, airports.iata_code as iata, airports.name AS name, ST_X(airports.location) AS lng, ST_Y(airports.location) AS lat FROM airports ORDER BY distance LIMIT 1;";
		
		System.out.println(querryString);
		
		try {
			ResultSet querryResult = conn.createStatement().executeQuery(querryString);
			querryResult.next();
			airport = new Place(querryResult.getDouble("lng"), querryResult.getDouble("lat"));
			airport.setIata(querryResult.getString("iata"));
			airport.setName(querryResult.getString("name") + " Airport");
			airport.setType(Place.AIRPORT);
		} catch (SQLException e) {
			logger.error("Problem by getting data from querry result: " + place.getName() + "\n " + e);
			return null;
		}
		
		return airport;
	}
	
	
	/**
	 * creates a list with the "limit"s closest airport around the given place
	 * @param place The center place of the search.
	 * @param limit the how many closest airports should be returned (beeline).
	 * @param direction is the place the origin or destination (depending on tis parameter the place will be set as origin or destination in the Connection object.)
	 * 			< 0 for the direction to the Airport | else for the direction from the Airport
	 * @return A List of the closest airport (beeline), null if its failed
	 */
	public LinkedBlockingQueue<utilities.Connection> createAirportsBeeline(Place place, int limit, int direction){
		
		if(place.getLatitude() == Double.MAX_VALUE || place.getLongitude() == Double.MAX_VALUE){
			try {
				place = api.GoogleMapsGeocoding.addCoordinatesToPlace(place);
			} catch (NumberFormatException | NullPointerException | IllegalStateException | IOException | JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		
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
	public utilities.Connection[] getListOrderedByDistance(){
		utilities.Connection[] connectionArray = airportList.toArray(new utilities.Connection[airportList.size()]);
		Arrays.sort(connectionArray, new CompareClosestAirportsForDistance());
		return connectionArray;
	}
	
	/**
	 * 
	 * @return
	 */
	public utilities.Connection[] getListOrderedByDuration(){
		utilities.Connection[] connectionArray = airportList.toArray(new utilities.Connection[airportList.size()]);
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
