/**
 * 
 */
package api.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.updateTables.UpdateContinents;
import utilities.Place;

/**
 * @author Florian
 * Utilities for the GoogleMaps APIs
 */
public class GoogleMaps {
	
	public static final String DRIVING = "driving";
	public static final String WALKING = "walking";
	public static final String BICYCLING = "bicycling";
	public static final String TRANSIT = "transit";
	public static final String AIRPLANE = "airplane";
	
	public static final String TOLLS = "tolls";
	public static final String HIGHWAYS = "highways";
	public static final String FERRIES = "ferries";
	public static final String INDOOR = "indoor";
	
	public static final int GOOGLE_MAPS_DISTANCE_API = 1;
	public static final int GOOGLE_MAPS_DIRECTION_API = 2;	
	private static final String DIRECTION_API_KEY = "AIzaSyDhieKypOeAVC9O1rD2y7SoSEgESt0S8ao";
	private static final String DISTANCE_API_KEY = "AIzaSyDhieKypOeAVC9O1rD2y7SoSEgESt0S8ao";
	private static final String URL = "https://maps.googleapis.com/maps/api/";
	
	protected static final Logger logger = LogManager.getLogger(UpdateContinents.class);

	
	
	public static String PlaceToGoogleMapsString(LinkedList<Place> placeList){
		String placeString = "";
		for(Place place:placeList){
			try {
				placeString = concatStringWithDelimiter(placeString, PlaceToGoogleMapsString(place), URLEncoder.encode("|", "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.warn("Problem by encoding pipe symbol - Place: " + place.getId() + " can't be considert! | " + e.toString());
			}
		}
		
		return placeString;
	}
	
	/**
	 * Generates a String from a place Object that is readable as a Place by the GoogleMaps API
	 * @param place Place object
	 * @return String that is readable by the GoogleMaps APi as a place | null if it was not possible to create such a String from the given data
	 */
	public static String PlaceToGoogleMapsString(Place placeObject){
		String placeString = "";
		//if there are coordinates always use them and nothing else
		if(placeObject.hasCoordinates()){
			placeString = placeObject.getLatitude() + "," + placeObject.getLongitude();
		}else{
			if(placeObject.hasHousenumber() && placeObject.hasStreet()){
				placeString = placeObject.getHousenumber();
			}
			if(placeObject.hasStreet()){
				placeString = concatStringWithDelimiter(placeString, placeObject.getStreet(), "+");
			}else{
				//If there is no street try if there is a name and use it instead of the street and house number
				if(placeObject.hasName()){
					placeString = concatStringWithDelimiter(placeString, placeObject.getName(), "+");
				}
			}
			if(placeObject.hasCity()){
				placeString = concatStringWithDelimiter(placeString, placeObject.getCity(), "+");
			}
			if(placeObject.hasCountry()){
				placeString = concatStringWithDelimiter(placeString, placeObject.getCountry(), "+");
			}
		}
		
		if(placeString.equals(""))
			return null;
		else
			return placeString.replace(' ', '+');
	}
	
	
	/**
	 * Concats two strings with a delimiter between them
	 * @param string1 first String
	 * @param string2 second String
	 * @param delim delimiter
	 * @return concat both strings with the delimiter between, if string 1 was empty return string 2
	 */
	private static String concatStringWithDelimiter(String string1, String string2, String delim){
		//if The String is not empty add a delimiter between the both strings
		if(!string1.equals("")){
			return string1 + delim + string2;
		}
		return string2;
	}
	
	
	public static String createDirectionURL(String origin, String destination, GregorianCalendar date, boolean isDepartureDate, String transportation, String avoid, String language, boolean alternative){
		if(!transportation.equals(DRIVING) && !transportation.equals(WALKING) && !transportation.equals(BICYCLING) && !transportation.equals(TRANSIT) && !transportation.equals("")){
			throw new IllegalArgumentException("Transportation has not a valid content");
		}
		
		String url = URL + "directions/xml?origin=" + origin + "&destination=" + destination + "&alternatives=" + alternative + "&key=" + DIRECTION_API_KEY;
		url += (!transportation.equals(null)) ? "&mode=" + transportation : "";
		url += (!avoid.equals(null)) ? "&avoid=" + avoid : "";
		url += (!language.equals(null)) ? "&language=" + language : "";
		//url += (!language.equals(null)) ? "&language=" + language : "";
		if(date != null){
			url += ((isDepartureDate) ? "&departure_time=" : "&arrival_time=") + (date.getTimeInMillis() / 1000);
		}
		
		return url;
	}
	
	public static String createDistanceURL(String origin, String destination, GregorianCalendar date, boolean isDepartureDate, String transportation, String avoid, String language){
		if(!transportation.equals(DRIVING) && !transportation.equals(WALKING) && !transportation.equals(BICYCLING) && !transportation.equals(TRANSIT) && !transportation.equals("")){
			throw new IllegalArgumentException("Transportation has not a valid content");
		}
		
		String url = URL + "distancematrix/xml?origins=" + origin + "&destinations=" + destination + "&key=" + DISTANCE_API_KEY;
		url += (!transportation.equals("")) ? "&mode=" + transportation : "";
		url += (!avoid.equals("")) ? "&avoid=" + avoid : "";
		url += (!language.equals("")) ? "&language=" + language : "";
		if(date != null){
			url += ((isDepartureDate) ? "&departure_time=" : "&arrival_time=") + (date.getTimeInMillis() / 1000);
		}
				
		return url;
	}
	
	
	
	
	
	
}
