/**
 * 
 */
package api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import utilities.Connection;
import utilities.XMLUtilities;

/**
 * @author Florian
 *
 */
public class GoogleMapsDirection implements API {
	
	public GoogleMapsDirection(){
		
	}
	
	public LinkedBlockingQueue<Connection> getAllConnections(String origin, String destination, Date outboundDate, Date inboundDate) throws ClientProtocolException, IOException, JDOMException{
		String url = "https://maps.googleapis.com/maps/api/directions/xml?origin=Göttingen&destination=Berlin&key=AIzaSyDhieKypOeAVC9O1rD2y7SoSEgESt0S8ao";
		url = "https://maps.googleapis.com/maps/api/directions/xml?origin=" + origin + "&destination=" + destination + "&mode=transit" + "&key=AIzaSyDhieKypOeAVC9O1rD2y7SoSEgESt0S8ao";
		InputStream test = getInput(url);
		
		SAXBuilder builder = new SAXBuilder();
		Document responseXML = builder.build(test);
		Element rootFromAutosuggestXML = responseXML.getRootElement();
		
		XMLUtilities.writeXmlToFile(rootFromAutosuggestXML, "testGoogle.xml");
		return null;
	}
	
	
	/**
	 * executes the Querry to Skyscanner API
	 * @param url URL for the Querry
	 * @return InputStream of the XML code that API responses
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	private static InputStream getInput(String url) throws ClientProtocolException, IOException {

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);

		// add request header
		request.addHeader("Accept", "application/xml");

		HttpResponse response = client.execute(request);
		
		return response.getEntity().getContent();
	}
}
