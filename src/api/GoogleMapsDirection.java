/**
 * 
 */
package api;

import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.joda.time.Duration;

import api.utilities.GoogleMaps;
import database.updateTables.UpdateContinents;
import utilities.Connection;
import utilities.Place;
import utilities.XMLUtilities;

/**
 * @author Florian
 *
 */
public class GoogleMapsDirection implements API {
	
	protected static final Logger logger = LogManager.getLogger(GoogleMapsDirection.class);
	
	/**
	 * Empty Constructor
	 */
	public GoogleMapsDirection(){
		
	}
	
	public LinkedBlockingQueue<Connection> getAllConnections(String origin, String destination, GregorianCalendar outboundDate, GregorianCalendar inboundDate) throws ClientProtocolException, IOException, JDOMException{
		String url = "https://maps.googleapis.com/maps/api/directions/xml?origin=Göttingen&destination=Berlin&key=AIzaSyDhieKypOeAVC9O1rD2y7SoSEgESt0S8ao";
		url = "https://maps.googleapis.com/maps/api/directions/xml?origin=" + origin + "&destination=" + destination + "&mode=transit" + "&departure_time=1502546005&key=AIzaSyDhieKypOeAVC9O1rD2y7SoSEgESt0S8ao";

		Element rootFromAutosuggestXML = getInput(url);
		
		XMLUtilities.writeXmlToFile(rootFromAutosuggestXML, "testGoogle.xml");
		return null;
	}
	
	
	/**
	 * 
	 * @param originList list of Places of origins.
	 * @param destinationList List of places for departure.
	 * @param date date and time of travel. Can be null.
	 * @param isDepartureDate is the date the departure or arrival date.
	 * @param transportation kind of Transportation that should be used. Use class constants from GoogleMaps. Can be "".
	 * @param avoid kind of transportation that should be avoid. Use class constants from GoogleMaps. Can be "".
	 * @param language return language (like "de", "fr", "en", ...). Can be "".
	 * @return List of available connections.
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws IllegalStateException
	 * @throws JDOMException
	 */
	public LinkedBlockingQueue<Connection> getConnection(Place origin, Place destination, GregorianCalendar date, boolean isDepartureDate, String transportation, String avoid, String language) throws ClientProtocolException, IOException, IllegalStateException, JDOMException{
		//ToDo:insert body
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		Element rootFromConnectionsXML = getInput(api.utilities.GoogleMaps.createDirectionURL(GoogleMaps.PlaceToGoogleMapsString(origin), GoogleMaps.PlaceToGoogleMapsString(destination), date, isDepartureDate, transportation, avoid, language));
		
		for(Element routeOption : rootFromConnectionsXML.getDescendants(Filters.element("leg"))){
			
			Connection connection = null;
			
			// tries to parse the coordinates of the returning xml file from start and endlocation into double
			// if it is successful write it to the place objects otherwise set the coordinates from the places to MAX_VALUE. That represents null
			// because in that case can't be ensured that the coordinates really represents the place
			try{
				double startLattitude = Double.parseDouble(routeOption.getChild("start_location").getChildText("lat"));
				double startLongitude = Double.parseDouble(routeOption.getChild("start_location").getChildText("lng"));
				double endLattitude = Double.parseDouble(routeOption.getChild("end_location").getChildText("lat"));
				double endLongitude = Double.parseDouble(routeOption.getChild("end_location").getChildText("lng"));
				
				origin.setLatitude(startLattitude);
				origin.setLongitude(startLongitude);
				destination.setLatitude(endLattitude);
				destination.setLongitude(endLongitude);
				
			}catch(NullPointerException | NumberFormatException e){
				origin.setLatitude(Double.MAX_VALUE);
				origin.setLongitude(Double.MAX_VALUE);
				destination.setLatitude(Double.MAX_VALUE);
				destination.setLongitude(Double.MAX_VALUE);
				
				logger.warn("The Coordinates of one or more Places can't be parsed to Double.");
			}
			
			connection = new Connection(origin, destination);
			
			// ToDo: insert start/end time/date into connection
			try{
				//departure time
				//GregorianCalendar departureTime = new GregorianCalendar(TimeZone.getTimeZone(routeOption.getChild("departure_time").getChildText("time_zone")));
				//GregorianCalendar departureTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
				GregorianCalendar departureTime = new GregorianCalendar();
				departureTime.setTimeInMillis(Integer.parseInt(routeOption.getChild("departure_time").getChildText("value")) * 1000L);
				connection.setDepartureDate(departureTime);
				
				//arrival time
				GregorianCalendar arrivalTime = new GregorianCalendar(TimeZone.getTimeZone(routeOption.getChild("arrival_time").getChildText("time_zone")));
				arrivalTime.setTimeInMillis(Integer.parseInt(routeOption.getChild("arrival_time").getChildText("value")) * 1000L);
				connection.setArrivalDate(arrivalTime);
				
			}catch(NumberFormatException e){
				logger.warn("The departure and arrival time of one connections can't be set." + e);
			}catch(NullPointerException e){
				//No departure or arrivelTime available, everything ok
			}
			
			//Duration
			try{
				connection.setDuration(new Duration ((Long.parseLong(routeOption.getChild("duration").getChildText("value"))) * 1000L));
			}catch(NullPointerException | NumberFormatException e){
				logger.warn("The duration of one connections can't be parsed to long.");
			}
			
			//Distance
			try{
				connection.setDistance(Integer.parseInt(routeOption.getChild("distance").getChildText("value")));
			}catch(NullPointerException | NumberFormatException e){
				logger.warn("The distance of one connections can't be parsed to integer.");
			}
			
			//connection.setType();
			switch (transportation){
				case GoogleMaps.TRANSIT:
					connection.setType(Connection.PUBLIC_TRANSPORT);
					break;
				case GoogleMaps.DRIVING:
					connection.setType(Connection.CAR);
					break;
				case GoogleMaps.WALKING:
					connection.setType(Connection.WALK);
					break;
				case GoogleMaps.BICYCLING:
					connection.setType(Connection.BICYCLE);
					break;				
			}
			
			// ToDo: insert subconnection

			/*
			
			for(Element connectionXML : routeOption.getDescendants(Filters.element("step"))){
				// status is OK if there is a result, if a place cant be found it is ZERO_RESULT
				if(connectionXML.getChildText("status").equals("OK")){
					
					
					connection.setDuration(new Duration ((Long.parseLong(routeOption.getChild("duration").getChildText("value"))) * 1000));
					connection.setDistance(Integer.parseInt(routeOption.getChild("distance").getChildText("value")));
					
					connectionList.add(connection);
				}
			}*/
			connectionList.add(connection);
		}
		
		//To write the retunrned XML file into a file
		XMLUtilities.writeXmlToFile(rootFromConnectionsXML, "testGoogle.xml");
		
		return connectionList;
	}
	
	
	/**
	 * executes the query to Skyscanner API and returns the root element of the xml response
	 * @param url URL for the query
	 * @return root element of the XML response from the query
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws JDOMException 
	 * @throws IllegalStateException 
	 */
	private static Element getInput(String url) throws ClientProtocolException, IOException, IllegalStateException, JDOMException {

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);

		HttpResponse response = client.execute(request);
		
		SAXBuilder builder = new SAXBuilder();
		Document responseXML = builder.build(response.getEntity().getContent());
		Element rootElement = responseXML.getRootElement();
		
		return rootElement;
	}
}
