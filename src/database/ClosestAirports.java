package database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	private LinkedList<ClosestAirportListElement> airportList = new LinkedList<ClosestAirportListElement>();
	private int ordered = 0;
	
	public ClosestAirports(Connection conn){
		this.conn = conn;
	}
	
	
	public int createAirportsBeeline(Place place, int limit){
		
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
				ClosestAirportListElement airportElement = new ClosestAirportListElement();
				Place airport = new Place(querryResult.getDouble("lng"), querryResult.getDouble("lat"));
				airport.setIata(querryResult.getString("iata"));
				airport.setName(querryResult.getString("name"));
				airport.setType(Place.AIRPORT);
				airportElement.setAirport(airport);
				airportElement.setBeeline(querryResult.getInt("distance"));
				airportElement.setPlace(place);
				airportList.add(airportElement);
			}
		} catch (SQLException e) {
			System.out.println("Querry Fails");
			e.printStackTrace();
			return -1;
		}
		
		ordered = ORDERED_BY_BEELINE;
		
		return 1;
	}
	
	
	/**
	 * 
	 * @param direction	1 for the direction to th Airport | -1 for the direction from the Airport
	 * @return 1 for success | -1 unsuccessful (maybe list is empty, call createAirportsBeeline() before this function, maybe for one airport cant be find a distance or duration and it was not possible to remove it from the list)
	// duration and distance is not matched to the right airports 
	public int setAirportDrivingDistance(int direction, String transportation){
		if(airportList.isEmpty()){
			return -1;
		}
		GoogleMapsDistance distance = new GoogleMapsDistance();
		LinkedList<ClosestAirportListElement> removeFromList = new LinkedList<ClosestAirportListElement>();
		LinkedList<Place> placeList = new LinkedList<Place>();
		
		LinkedList<Place> airportPlaceList = new LinkedList<Place>();
		for(ClosestAirportListElement airport : airportList){
			airportPlaceList.add(airport.getAirport());
		}
		
		placeList.add(airportList.get(0).getPlace());
		
		LinkedBlockingQueue<utilities.Connection> connection = null;
		try {
			if(direction > 0){
				connection = distance.getConnection(placeList, airportPlaceList, null, true, transportation, "", "de");
			}
			if(direction < 0){
				connection = distance.getConnection(airportPlaceList, placeList, null, true, transportation, "", "de");
			}
		} catch (IllegalStateException | IOException | JDOMException e) {
			logger.error("Cant calculate the distance and duration \n" + e);
				return -1;
		}
		
		for(int i = 0; i < airportList.size(); i++){
			try {
				utilities.Connection connectionElement = connection.poll();
				airportList.get(i).setDistance(connectionElement.getDistance());
				airportList.get(i).setDuration(new Duration(connectionElement.getDuration()));
			} catch (IllegalStateException e) {
				logger.error("Problem by calculating the distance or duration of one Element: " + airportList.get(i).getAirport().getName() + "\n" + e);
				removeFromList.add(airportList.get(i));					
			} catch (NoSuchElementException | NullPointerException e){
				removeFromList.add(airportList.get(i));
			}
		}
		
		for(ClosestAirportListElement airportToRemove : removeFromList){
			if(airportList.remove(airportToRemove) == false){
				logger.error("It was not be possible to remove the element for which it was noch be possible to calculate the distance or duration. Element: " + airportToRemove.getAirport().getName() + "\n");
				return -1;
			}
		}
		
		ordered = ORDERED_BY_DISTANCE;
		
		return 1;
	}*/
	
	
	/**
	 * 
	 * @param direction	-1 for the direction to the Airport | 1 for the direction from the Airport
	 * @return 1 for success | -1 unsuccessful (maybe list is empty, call createAirportsBeeline() before this function, maybe for one airport cant be find a distance or duration and it was not possible to remove it from the list)
	 */
	public int setAirportOtherDistance(int direction, String transportation){
		if(airportList.isEmpty()){
			return -1;
		}
		GoogleMapsDirection distance = new GoogleMapsDirection();
		LinkedList<ClosestAirportListElement> removeFromList = new LinkedList<ClosestAirportListElement>();
		for(ClosestAirportListElement airport : airportList){
			try {
				utilities.Connection connection = null;
				if(direction < 0){
					connection = distance.getConnection(airport.getPlace(), airport.getAirport(), null, true, transportation, "", "de", false).element();
				}
				if(direction > 0){
					connection = distance.getConnection(airport.getAirport(), airport.getPlace(), null, true, transportation, "", "de", false).element();
				}
				airport.setConnection(connection);
			} catch (IllegalStateException | IOException | JDOMException e) {
				logger.error("Problem by calculating the distance or duration of one Element: " + airport.getAirport().getName() + "\n" + e);
				removeFromList.add(airport);					
			} catch (NoSuchElementException e){
				removeFromList.add(airport);
			}
		}
		
		for(ClosestAirportListElement airportToRemove : removeFromList){
			if(airportList.remove(airportToRemove) == false){
				logger.error("It was not be possible to remove the element for which it was noch be possible to calculate the distance or duration. Element: " + airportToRemove.getAirport().getName() + "\n");
				return -1;
			}
		}
		
		ordered = ORDERED_BY_DISTANCE;
		
		return 1;
	}
	
	
	public int orderListByDistance(){
		airportList.sort(new CompareClosestAirportsForDistance());
		ordered = ORDERED_BY_DISTANCE;
		return 1;
	}
	
	public int orderListByDuration(){
		airportList.sort(new CompareClosestAirportsForDuration());
		ordered = ORDERED_BY_DURATION;
		return 1;
	}
	
	public LinkedList<ClosestAirportListElement> getAirportList(){
		return airportList;
	}
	
	public int getOrder(){
		return ordered;
	}
	
	
}
