/**
 * 
 */
package api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Time;
import java.util.Date;
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

import utilities.Connection;
import utilities.XMLUtilities;
import utilities.Place;

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
	
	public GoogleMapsDistance(){
		
	}
	
	public LinkedBlockingQueue<Connection> getAllConnections(String origin, String destination, Date outboundDate, Date InboundDate) throws ClientProtocolException, IOException, JDOMException{
		
		//ToDo: function calls for getting the lists
		
		LinkedBlockingQueue<Connection> connections = null;
		//connections.addAll(c);
		
		
		String urlOutbound = "https://maps.googleapis.com/maps/api/distancematrix/xml?origins=" + origin + "&destinations=" + destination + "&mode=transit" + "&key=AIzaSyA2wxUVkdyzbBdcOdtIItCnco2vyJoVMVE";
		String urlInbound;
		//url = "https://maps.googleapis.com/maps/api/distancematrix/xml?units=imperial&origins=40.6655101,-73.89188969999998&destinations=40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.659569%2C-73.933783%7C40.729029%2C-73.851524%7C40.6860072%2C-73.6334271%7C40.598566%2C-73.7527626%7C40.659569%2C-73.933783%7C40.729029%2C-73.851524%7C40.6860072%2C-73.6334271%7C40.598566%2C-73.7527626&key=AIzaSyA2wxUVkdyzbBdcOdtIItCnco2vyJoVMVE";

		Element rootFromAutosuggestXML = getInput(urlOutbound);
		
		XMLUtilities.writeXmlToFile(rootFromAutosuggestXML, "testGoogle.xml");
		return connections;
	}
	
	public LinkedBlockingQueue<Connection> getConnection(LinkedList<Place> originList, LinkedList<Place> destinationList, Date date, boolean isDepartureDate, String transportation, String avoid, String language) throws ClientProtocolException, IOException, IllegalStateException, JDOMException{
		//ToDo:insert body
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		int destinationIndex = 0;
		
		Element rootFromConnectionsXML = getInput(api.utilities.GoogleMaps.createDistanceURL(GoogleMaps.PlaceToGoogleMapsString(originList), GoogleMaps.PlaceToGoogleMapsString(destinationList), date, isDepartureDate, transportation, avoid, language));
		
		
		for(Element originXML : rootFromConnectionsXML.getDescendants(Filters.element("row"))){
			//gets and removes the first element of the list of origins
			Place origin = originList.pollFirst();
			destinationIndex = 0;
			for(Element connectionXML : originXML.getDescendants(Filters.element("element"))){
				// status is OK if there is a result, if a place cant be nfound it is ZERO_RESULT
				if(connectionXML.getChildText("status").equals("OK")){
					Connection connection;
					
					Place destination = destinationList.get(destinationIndex);
					
					connection = new Connection(origin, destination);
					connection.setDuration(new Time(Long.parseLong(connectionXML.getChild("duration").getChildText("value"))));
					connection.setDistance(Integer.parseInt(connectionXML.getChild("distance").getChildText("value")));
					
					connectionList.add(connection);
				}
				destinationIndex++;
			}
			
		}
		
		
		
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
		


		// add request header
		//request.addHeader("Accept", "application/xml");

		HttpResponse response = client.execute(request);
		
		SAXBuilder builder = new SAXBuilder();
		Document responseXML = builder.build(response.getEntity().getContent());
		Element rootElement = responseXML.getRootElement();
		
		return rootElement;
	}
}
