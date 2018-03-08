package api;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import api.utilities.TimeZoneInfo;
import utilities.Place;

public class GoogleMapsTimeZone {

	protected static final Logger logger = LogManager.getLogger(GoogleMapsTimeZone.class);
	
	private static final String API_KEY = "AIzaSyDhieKypOeAVC9O1rD2y7SoSEgESt0S8ao";
	
	
	public GoogleMapsTimeZone(){
		
	}
	
	//localtime has to be a JS Date object and location has to be a LatLng object
    //returns a JS Date object
    public static GregorianCalendar getUTCTime(GregorianCalendar localTime, Place place) throws IllegalStateException {
    	TimeZoneInfo timeZoneInfo;
        try {
			timeZoneInfo = getTimeZoneInfo(localTime, place);
			//rawOffset = time difference to utc, dstOffset = day light saving time difference
	        //calculates the utc time by subtract the time difference to utc and to day light saving time
	        GregorianCalendar utcTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	        //GregorianCalendar utcTime = new GregorianCalendar(TimeZone.getTimeZone(timeZoneInfo.getTimeZoneId()));
	        utcTime.setTimeInMillis(((localTime.getTimeInMillis() / 1000) - timeZoneInfo.getDstOffset() - timeZoneInfo.getRawOffset()) * 1000);
	        return utcTime;
		} catch (IllegalStateException | IOException | JDOMException e) {
			logger.error("UTC Time can't be calculated. " + e.toString());
			throw new IllegalStateException("UTC Time can't be calculated. " + e.toString());
		}
    }

    //localtime has to be a JS date object and location has to be a LatLng object
    public static GregorianCalendar getLocalTime(GregorianCalendar utcTime, Place place) throws IllegalStateException{
        TimeZoneInfo timeZoneInfo;
		try {
			timeZoneInfo = getTimeZoneInfo(utcTime, place);
			//rawOffset = time difference to utc, dstOffset = day light saving time difference
	        //calculates the local time by adding the time difference to utc and to day light saving time
	        GregorianCalendar localTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	        //GregorianCalendar localTime = new GregorianCalendar(TimeZone.getTimeZone(timeZoneInfo.getTimeZoneId()));
	        localTime.setTimeInMillis(((utcTime.getTimeInMillis() / 1000) + timeZoneInfo.getDstOffset() + timeZoneInfo.getRawOffset()) * 1000);
	        return localTime;
		} catch (IllegalStateException | IOException | JDOMException e) {
			logger.error("Local Time can't be calculated. " + e.toString());
			throw new IllegalStateException("Local Time can't be calculated. " + e.toString());
		}
    }

    //Gets information about the TimeZone of the given location at the given date
    public static TimeZoneInfo getTimeZoneInfo(GregorianCalendar date, Place place) throws ClientProtocolException, IllegalStateException, IOException, JDOMException{
    	String url = "https://maps.googleapis.com/maps/api/timezone/json?location=" + place.getLatitude() + "," + place.getLongitude() +"&timestamp=" + (date.getTimeInMillis() / 1000) + "&key=" + API_KEY;
    	return sockets.JsonConverter.jsonToTimeZoneInfo(getInput(url));
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
	private static String getInput(String url) throws ClientProtocolException, IOException, IllegalStateException, JDOMException {

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);

		HttpResponse response = client.execute(request);

		HttpEntity entity = response.getEntity();
		return EntityUtils.toString(entity, "UTF-8");
	}
}
