/**
 * 
 */
package api;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.joda.time.Duration;

import utilities.Connection;
import utilities.XMLUtilities;
import utilities.Place;
import utilities.TimeFunctions;
import api.utilities.GoogleMaps;

/**
 * @author Florian
 *
 */
public class GoogleMapsDistance implements API {
	
	public static final String DRIVING = api.utilities.GoogleMaps.DRIVING;
	public static final String WALKING = api.utilities.GoogleMaps.WALKING;
	public static final String BICYCLING = api.utilities.GoogleMaps.BICYCLING;
	public static final String TRANSIT = api.utilities.GoogleMaps.TRANSIT;
	public static final String AIRPLANE = api.utilities.GoogleMaps.AIRPLANE;
	
	/**
	 * Empty Constructor
	 */
	public GoogleMapsDistance(){
		
	}
	

	public LinkedBlockingQueue<Connection> getAllConnections(String origin, String destination, GregorianCalendar outboundDate, GregorianCalendar inboundDate) throws ClientProtocolException, IOException, JDOMException{
		
		LinkedList<Place> originlist = new LinkedList<Place>();
		originlist.add(new Place(origin));
		LinkedList<Place> destinationlist = new LinkedList<Place>();
		destinationlist.add(new Place(destination));		
		
		LinkedBlockingQueue<Connection> connections = null;
		
		/* To manuelly set Values in the URL for test purposes
		String urlOutbound = "https://maps.googleapis.com/maps/api/distancematrix/xml?origins=" + origin + "&destinations=" + destination + "&mode=transit&departure_time=1502546005" + "&key=AIzaSyA2wxUVkdyzbBdcOdtIItCnco2vyJoVMVE";
		Element rootFromAutosuggestXML = getInput(urlOutbound);
		XMLUtilities.writeXmlToFile(rootFromAutosuggestXML, "testGoogle.xml");
		 */		
		
		//outbound connection
		connections = getConnection(originlist, destinationlist, outboundDate, true, "", "", "de");
		//inbound connection
		connections.addAll(getConnection(destinationlist, originlist, inboundDate, true, "", "", "de"));
		
		return connections;
	}
	
	public LinkedBlockingQueue<Connection> getConnection(Place origin, Place destination, GregorianCalendar date, boolean isDepartureDate, String transportation, String avoid, String language) throws ClientProtocolException, IOException, IllegalStateException, JDOMException{
		LinkedList<Place> originlist = new LinkedList<Place>();
		originlist.add(origin);
		LinkedList<Place> destinationlist = new LinkedList<Place>();
		destinationlist.add(destination);
		
		return getConnection(originlist, destinationlist, date, isDepartureDate, transportation, avoid, language);
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
	public LinkedBlockingQueue<Connection> getConnection(LinkedList<Place> originList, LinkedList<Place> destinationList, GregorianCalendar date, boolean isDepartureDate, String transportation, String avoid, String language) throws ClientProtocolException, IOException, IllegalStateException, JDOMException{
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		//TODO: Remove
		date = TimeFunctions.cloneAndAddHoures(date, 14*24);

		int originIndex = 0;
		int destinationIndex = 0;
		
		if(avoid == null)
			avoid = "";
		if(language == null)
			language = "";
		if(transportation == null)
			transportation = "";
		
		Element rootFromConnectionsXML = getInput(api.utilities.GoogleMaps.createDistanceURL(GoogleMaps.PlaceToGoogleMapsString(originList), GoogleMaps.PlaceToGoogleMapsString(destinationList), date, isDepartureDate, transportation, avoid, language));
		
		
		for(Element originXML : rootFromConnectionsXML.getDescendants(Filters.element("row"))){
			Place origin = originList.get(originIndex);
			
			if(!origin.hasCoordinates()){
				origin = GoogleMapsGeocoding.addCoordinatesToPlace(origin);
			}
			
			destinationIndex = 0;
			for(Element connectionXML : originXML.getDescendants(Filters.element("element"))){
				// status is OK if there is a result, if a place cant be found it is ZERO_RESULT
				if(connectionXML.getChildText("status").equals("OK")){
					Connection connection;
					
					Place destination = destinationList.get(destinationIndex);
					
					if(!destination.hasCoordinates()){
						destination = GoogleMapsGeocoding.addCoordinatesToPlace(destination);
					}
					
					connection = new Connection(origin, destination);
					connection.setDuration(new Duration ((Long.parseLong(connectionXML.getChild("duration").getChildText("value"))) * 1000));
					connection.setDistance(Integer.parseInt(connectionXML.getChild("distance").getChildText("value")));
					
					connectionList.add(connection);
				}
				destinationIndex++;
			}
			originIndex++;
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
