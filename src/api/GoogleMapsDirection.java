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
import utilities.CarrierList;
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
		LinkedBlockingQueue<Connection> connections = null;
		
		Place originObject = new Place(origin);
		Place destinationObject = new Place(destination);
		
		/*String url = "https://maps.googleapis.com/maps/api/directions/xml?origin=Göttingen&destination=Berlin&key=AIzaSyDhieKypOeAVC9O1rD2y7SoSEgESt0S8ao";
		Element rootFromAutosuggestXML = getInput(url);
		XMLUtilities.writeXmlToFile(rootFromAutosuggestXML, "testGoogle.xml");*/	
		
		//outbound connection
		connections = getConnection(originObject, destinationObject, outboundDate, true, "", "", "de", false);
		//inbound connection
		connections.addAll(getConnection(destinationObject, originObject, inboundDate, true, "", "", "de", false));
		
		return connections;
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
	public LinkedBlockingQueue<Connection> getConnection(Place origin, Place destination, GregorianCalendar date, boolean isDepartureDate, String transportation, String avoid, String language, boolean alternatives) throws ClientProtocolException, IOException, IllegalStateException, JDOMException{
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		String url = api.utilities.GoogleMaps.createDirectionURL(GoogleMaps.PlaceToGoogleMapsString(origin), GoogleMaps.PlaceToGoogleMapsString(destination), date, isDepartureDate, transportation, avoid, language, alternatives);
		Element rootFromConnectionsXML = getInput(url);
		
		
		// status is OK if there is a result, if a place cant be found it is ZERO_RESULT
		if(rootFromConnectionsXML.getChildText("status").equals("OK")){
			for(Element routeOption : rootFromConnectionsXML.getDescendants(Filters.element("leg"))){
				Connection headConnection = null;
				Connection connection = null;
				
				// tries to parse the coordinates of the returning xml file from start and endlocation into double
				// if it is successful write it to the place objects otherwise set the coordinates from the places to MAX_VALUE. That represents null
				// because in that case can't be ensured that the coordinates really represents the place
				try{
					double startLatitude = Double.parseDouble(routeOption.getChild("start_location").getChildText("lat"));
					double startLongitude = Double.parseDouble(routeOption.getChild("start_location").getChildText("lng"));
					double endLatitude = Double.parseDouble(routeOption.getChild("end_location").getChildText("lat"));
					double endLongitude = Double.parseDouble(routeOption.getChild("end_location").getChildText("lng"));
					String startName = routeOption.getChildText("start_address");
					String endName = routeOption.getChildText("end_address");
					
					origin.setLatitude(startLatitude);
					origin.setLongitude(startLongitude);
					if(origin.getType() != Place.AIRPORT){
						origin.setName(startName);
					}
					destination.setLatitude(endLatitude);
					destination.setLongitude(endLongitude);
					if(destination.getType() != Place.AIRPORT){
						destination.setName(endName);
					}
					
				}catch(NullPointerException | NumberFormatException e){
					origin.setLatitude(Double.MAX_VALUE);
					origin.setLongitude(Double.MAX_VALUE);
					destination.setLatitude(Double.MAX_VALUE);
					destination.setLongitude(Double.MAX_VALUE);
					
					logger.warn("The Coordinates of one or more Places can't be parsed to Double.");
				}
				
				connection = new Connection(origin, destination);
				
				//Duration
				try{
					connection.setDuration(new Duration ((Long.parseLong(routeOption.getChild("duration_in_traffic").getChildText("value"))) * 1000L));
				}catch(NullPointerException | NumberFormatException e){
					try{
						connection.setDuration(new Duration ((Long.parseLong(routeOption.getChild("duration").getChildText("value"))) * 1000L));
					}catch(NullPointerException | NumberFormatException ex){
						logger.warn("The duration of one connection can't be parsed to long. " + ex);
					}
				}
				
				//start/end time/date
				try{
					//departure time
					//Sets the departure time in local time and not UTC
					GregorianCalendar departureTime = new GregorianCalendar(TimeZone.getTimeZone(routeOption.getChild("departure_time").getChildText("time_zone")));
					//GregorianCalendar departureTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
					departureTime.setTimeInMillis(Integer.parseInt(routeOption.getChild("departure_time").getChildText("value")) * 1000L);
					connection.setDepartureDate(departureTime);
					
					//arrival time
					//Sets the arrival time in local time and not UTC
					GregorianCalendar arrivalTime = new GregorianCalendar(TimeZone.getTimeZone(routeOption.getChild("arrival_time").getChildText("time_zone")));
					//GregorianCalendar arrivalTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
					arrivalTime.setTimeInMillis(Integer.parseInt(routeOption.getChild("arrival_time").getChildText("value")) * 1000L);
					connection.setArrivalDate(arrivalTime);
					
				}catch(NumberFormatException e){
					logger.warn("The departure and arrival time of one connection can't be set." + e);
				}catch(NullPointerException e){
					try{
						GregorianCalendar departureTime = new GregorianCalendar();
						if(isDepartureDate)
							departureTime.setTimeInMillis(date.getTimeInMillis());
						else
							departureTime.setTimeInMillis(date.getTimeInMillis() - connection.getDuration().getMillis());
						// TODO: Correct Time Zone 
						departureTime.setTimeZone(TimeZone.getTimeZone(GoogleMapsTimeZone.getTimeZoneInfo(departureTime, connection.getOrigin()).getTimeZoneId()));
						connection.setDepartureDate(departureTime);
						
						GregorianCalendar arrivalTime = new GregorianCalendar();
						if(isDepartureDate)
							arrivalTime.setTimeInMillis(date.getTimeInMillis() + connection.getDuration().getMillis());
						else
							arrivalTime.setTimeInMillis(date.getTimeInMillis());
						arrivalTime.setTimeZone(TimeZone.getTimeZone(GoogleMapsTimeZone.getTimeZoneInfo(arrivalTime, connection.getDestination()).getTimeZoneId()));
						
						connection.setArrivalDate(arrivalTime);
					}catch(NullPointerException ex){
						if(date != null)
							logger.warn("The departure and arrival time of one connections can't be set." + ex);
					}
				}
				
				
				//Distance
				try{
					connection.setDistance(Integer.parseInt(routeOption.getChild("distance").getChildText("value")));
				}catch(NullPointerException | NumberFormatException e){
					logger.warn("The distance of one connection can't be parsed to integer.");
				}
				
				//overview polyline
				try{
					connection.setPolyline(routeOption.getParentElement().getChild("overview_polyline").getChildText("points"));
				}catch(NullPointerException | NumberFormatException e){
					logger.warn("The overview polyline of one connection can't be read.");
				}
				
				//summary
				try{
					connection.setSummary(routeOption.getParentElement().getChildText("summary"));
				}catch(NullPointerException | NumberFormatException e){
					logger.warn("The summary of one connection can't be read.");
				}
				
				//fare
				try{
					connection.addPrice(Double.parseDouble(routeOption.getParentElement().getChild("fare").getChildText("value")));
				}catch(NullPointerException | NumberFormatException e){
					//usually fare is not available
				}
				
				//travel mode
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
				
				
				
				//subconnection
				for(Element subconnectionXML : routeOption.getDescendants(Filters.element("step"))){
					
						Connection subconnection = null;
						Place startLocation = null;
						Place endLocation = null;
						
						// tries to get coordinates and name from start and end location and writes them to a place object to generate an connection object afterwards
						// if it is not successful write only the coordinates to the place objects
						// transit mode has coordinates and name, the other modes only have coordinates
						try{
							double startLatitude = Double.parseDouble(subconnectionXML.getChild("transit_details").getChild("departure_stop").getChild("location").getChildText("lat"));
							double startLongitude = Double.parseDouble(subconnectionXML.getChild("transit_details").getChild("departure_stop").getChild("location").getChildText("lng"));
							double endLatitude = Double.parseDouble(subconnectionXML.getChild("transit_details").getChild("arrival_stop").getChild("location").getChildText("lat"));
							double endLongitude = Double.parseDouble(subconnectionXML.getChild("transit_details").getChild("arrival_stop").getChild("location").getChildText("lng"));
							
							String startName = subconnectionXML.getChild("transit_details").getChild("departure_stop").getChildText("name");
							String endName = subconnectionXML.getChild("transit_details").getChild("arrival_stop").getChildText("name");
							
							startLocation = new Place(startName, startLongitude, startLatitude);
							endLocation = new Place(endName, endLongitude, endLatitude);
							
						}catch(NullPointerException | NumberFormatException e){
							double startLatitude = Double.parseDouble(subconnectionXML.getChild("start_location").getChildText("lat"));
							double startLongitude = Double.parseDouble(subconnectionXML.getChild("start_location").getChildText("lng"));
							double endLatitude = Double.parseDouble(subconnectionXML.getChild("end_location").getChildText("lat"));
							double endLongitude = Double.parseDouble(subconnectionXML.getChild("end_location").getChildText("lng"));
							
							startLocation = new Place(startLongitude, startLatitude);
							endLocation = new Place(endLongitude, endLatitude);
						}
						
						subconnection = new Connection(startLocation, endLocation);
						
						//Duration
						try{
							subconnection.setDuration(new Duration ((Long.parseLong(subconnectionXML.getChild("duration").getChildText("value"))) * 1000L));
						}catch(NullPointerException | NumberFormatException e){
							logger.warn("The duration of one subconnection can't be parsed to long." + e);
						}
						
						//Distance
						try{
							subconnection.setDistance(Integer.parseInt(subconnectionXML.getChild("distance").getChildText("value")));
						}catch(NullPointerException | NumberFormatException e){
							logger.warn("The distance of one subconnection can't be parsed to integer." + e);
						}
						
						//Start/end time/date
						try{
							//departure time
							GregorianCalendar departureTime = new GregorianCalendar(TimeZone.getTimeZone(routeOption.getChild("departure_time").getChildText("time_zone")));
							departureTime.setTimeInMillis(Integer.parseInt(routeOption.getChild("departure_time").getChildText("value")) * 1000L);
							subconnection.setDepartureDate(departureTime);
							
							//arrival time
							GregorianCalendar arrivalTime = new GregorianCalendar(TimeZone.getTimeZone(routeOption.getChild("arrival_time").getChildText("time_zone")));
							arrivalTime.setTimeInMillis(Integer.parseInt(routeOption.getChild("arrival_time").getChildText("value")) * 1000L);
							subconnection.setArrivalDate(arrivalTime);
							
						}catch(NumberFormatException e){
							logger.warn("The departure and arrival time of one connection can't be set." + e);
						}catch(NullPointerException e){
							//No departure or arrivelTime available, everything ok
						}
						
						//quotedateTime
						//quoteDateTime is still of type Date therefore first a GregorianCalendar is generated and then a Date, if it changes at any time it can be simple changed here
						subconnection.setQuoteDateTime((new GregorianCalendar(TimeZone.getTimeZone("UTC"))).getTime());
						
						//travelMode
						switch (subconnectionXML.getChildText("travel_mode")){
							case "TRANSIT":
								subconnection.setType(Connection.PUBLIC_TRANSPORT);
								break;
							case "DRIVING":
								subconnection.setType(Connection.CAR);
								break;
							case "WALKING":
								subconnection.setType(Connection.WALK);
								break;
							case "BICYCLING":
								subconnection.setType(Connection.BICYCLE);
								break;				
						}
						
						//agency
						try{
							subconnection.setCarrier(new CarrierList(subconnectionXML.getChild("transit_details").getChild("line").getChild("agency").getChildText("name"), subconnectionXML.getChild("transit_details").getChild("line").getChild("agency").getChildText("url")));
						}catch(NullPointerException e){
							//do nothing, no agency avaiable (agency is available only for transit)
						}
							
							
						//polyline
						//detailed polyline is not necessary but needs hige transmission rate, therefor deactivate it
						try{
							subconnection.setPolyline(subconnectionXML.getChild("polyline").getChildText("points"));
						}catch(NullPointerException e){
							//do nothing, no polyline available (should be available usually)
						}/**/
						
						//html instruction
						try{
							subconnection.setHtmlInstructions(subconnectionXML.getChildText("html_instructions"));	
						}catch(NullPointerException e){
							//do nothing, no HTMLInstructions available (should be available usually)
						}

						//add the subconnection to the connection
						connection.getSubConnections().add(subconnection);
						//connection.addSubconnection(subconnection);
				}
				
				
				
				headConnection = new Connection(connection.getType(), connection.getOrigin(), connection.getDestination(), connection.getPrice(), connection.getDuration(),
						connection.getDepartureDate(), connection.getArrivalDate());
				headConnection.setSummary(connection.getSummary());
				headConnection.setDuration(connection.getDuration());
				headConnection.setDistance(connection.getDistance());
				headConnection.simpleAddSubconnection(connection);
				
				connectionList.add(headConnection);
				
			}
		}
		//To write the returned XML file into a file
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
