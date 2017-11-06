/**
 * 
 */
package api;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.TimeZone;
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

import api.utilities.GoogleMaps;
import utilities.CarrierList;
import utilities.Connection;
import utilities.Place;
import utilities.XMLUtilities;

/**
 * @author Florian
 *
 */
public class GoogleMapsGeocoding {
	
	
	public static Place addCoordinatesToPlace(Place place) throws NullPointerException, NumberFormatException, ClientProtocolException, IOException, IllegalStateException, JDOMException{
		//ToDo:insert body
		String address = api.utilities.GoogleMaps.PlaceToGoogleMapsString(place);
		
		String url = "https://maps.googleapis.com/maps/api/geocode/xml?address=" + address + "&key=AIzaSyDhieKypOeAVC9O1rD2y7SoSEgESt0S8ao";
		
		Element rootFromGeocodingXML = getInput(url);
		
		if(rootFromGeocodingXML.getChildText("status").equals("OK")){
			place.setLatitude(Double.parseDouble(rootFromGeocodingXML.getChild("result").getChild("geometry").getChild("location").getChildText("lat")));
			place.setLongitude(Double.parseDouble(rootFromGeocodingXML.getChild("result").getChild("geometry").getChild("location").getChildText("lng")));
		}

		XMLUtilities.writeXmlToFile(rootFromGeocodingXML, "GoogleGeocoding.xml");
		
		return place;
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
