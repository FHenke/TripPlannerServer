/**
 * 
 */
package api;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

import api.utilities.SkyscannerURL;
import database.DatabaseConnection;
import database.Query;
import database.updateTables.UpdateDatabase;
import utilities.*;

/**
 * @author Florian
 *
 */
public class SkyscannerCache implements API {
	
	protected static final Logger logger = LogManager.getLogger(SkyscannerCache.class);

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final String INBOUND = "InboundLeg";
	private static final String OUTBOUND = "OutboundLeg";
	
	private SkyscannerURL url;
	private String language;
	private String currency;
	private String region;
	
	/**
	 * empty constructor 
	 */
	public SkyscannerCache() {
		this.url = new SkyscannerURL("DE", "EUR", "de-DE");
		this.language = "DE";
		this.currency = "EUR";
		this.region = "de-DE";
	}
	
	/**
	 * 
	 * @param country
	 * @param currency
	 * @param language
	 */
	public SkyscannerCache(String country, String currency, String language) {
		url = new SkyscannerURL(country, currency, language);
	}

	/**
	 * 
	 */
	public LinkedBlockingQueue<Connection> getAllConnections(String origin, String destination, GregorianCalendar outboundDate, GregorianCalendar inboundDate) throws IOException, JDOMException, Exception{
		
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		//Dateformat.format() can't take null, therefore the distinction for inboundDate
		url.setRoute(origin, destination, DATE_FORMAT.format(outboundDate.getTimeInMillis()), (inboundDate == null) ? null : DATE_FORMAT.format(inboundDate.getTimeInMillis()));
		
		SAXBuilder builder = new SAXBuilder();
		Document connectionXML = builder.build(getInput(url.getQuotesURL()));
		Element rootFromConnectionXML = connectionXML.getRootElement();
		
		for(Element con:rootFromConnectionXML.getDescendants(Filters.element("QuoteDto"))){
			Connection connectionObject = this.createConnectionListFromXML(rootFromConnectionXML, con, OUTBOUND);
			//for return connection
			if(inboundDate != null){
				LinkedBlockingQueue<Connection> returnConnection = new LinkedBlockingQueue<Connection>();
				returnConnection.add((this.createConnectionListFromXML(rootFromConnectionXML, con, INBOUND)));
				connectionObject.setReturnConnection(returnConnection);
			}
			
			connectionList.add(connectionObject);
		}
		
		//ToDo: entfernen for release 
		XMLUtilities.writeXmlToFile(rootFromConnectionXML, "test.xml");
		return connectionList;
	}
	
	/**
	 * 
	 * @param input
	 * @return
	 * @throws ClientProtocolException
	 * @throws JDOMException
	 * @throws IOException
	 */
	public LinkedList<Place> getAutosuggest(String input) throws ClientProtocolException, JDOMException, IOException{
		LinkedList<Place> placeList = new LinkedList<Place>();
				
		SAXBuilder builder = new SAXBuilder();
		Document responseXML = builder.build(getInput(url.getAutosuggestURL(input)));
		Element rootFromAutosuggestXML = responseXML.getRootElement();

		for(Element place:rootFromAutosuggestXML.getDescendants(Filters.element("PlaceDto"))){
			Place placeObject = new Place();
			placeObject.setName(place.getChildText("PlaceName"));
			placeObject.setCountry(place.getChildText("CountryName"));
			
			//TODO: insert type, longitude, latitude, continent and city from database
			
			placeList.add(placeObject);
		}
		XMLUtilities.writeXmlToFile(rootFromAutosuggestXML, "test.xml");
		
		return placeList;
	}
	
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws JDOMException 
	 * @throws ClientProtocolException 
	 * @throws ParseException 
	 */
	public LinkedBlockingQueue<Connection> getFlightMap(GregorianCalendar date) throws IOException, JDOMException{
		
		LinkedBlockingQueue<Connection> flightMap = new LinkedBlockingQueue<Connection>();
		
		SAXBuilder builder = new SAXBuilder();
		Document responseXML = builder.build(getInput(url.getAllPlacesURL()));
		Element rootFromAirports = responseXML.getRootElement();
		
		//take one after the other element from the list of all airports
		
		Iterable<Element> itr = rootFromAirports.getDescendants(Filters.element("Airport"));
		Stream<Element> parStream = StreamSupport.stream(itr.spliterator(), true);
		
		AtomicInteger counter = new AtomicInteger();
		AtomicInteger process = new AtomicInteger();
		long streamSize = parStream.count();
		
		parStream = StreamSupport.stream(itr.spliterator(), true);
		parStream.forEach((airport) -> {
			
			if((int) (counter.getAndIncrement() * 100 / streamSize) > process.get()){
				process.set((int) (counter.get() * 100 / streamSize));
				UpdateDatabase.setStatus("collecting: " + process.get() + "%");
				System.out.println("collecting: " + process.get() + "%");
			}

			SkyscannerURL parallelUrl = new SkyscannerURL(language, currency, region);
			//for(Element airport:rootFromAirports.getDescendants(Filters.element("Airport"))){
			
			String originID = airport.getAttributeValue("Id");
			try{
					Connection connection = null;

					// get XML document for all flights available from the origin airport to any place and at the specified time or at any time if date is null
					if(date != null){
						parallelUrl.setRoute(originID, "anywhere", DATE_FORMAT.format(date.getTimeInMillis()), null);
					} else{
						parallelUrl.setRoute(originID, "anywhere", "anytime", null);
					}
					SAXBuilder parallelBuilder = new SAXBuilder();
					Document connectionlist = parallelBuilder.build(getInput(parallelUrl.getQuotesURL()));
					Element rootFromConnectionlist = connectionlist.getRootElement();
					
					// itterate over list of all connections ! also non direct connections!
					for(Element placeDto:rootFromConnectionlist.getDescendants(Filters.element("PlaceDto"))){
						if(placeDto.getChildText("Type").equals("Station") && !placeDto.getChildText("IataCode").equals(originID)){
							for(Element quotes:rootFromConnectionlist.getDescendants(Filters.element("QuoteDto"))){
								if(quotes.getChildText("Direct").equals("true") && quotes.getChild("OutboundLeg").getChildText("DestinationId").equals(placeDto.getChildText("PlaceId"))){
	
									GregorianCalendar departureDate = new GregorianCalendar();
									departureDate.setTime(Formatation.StringToDate("yyyy-MM-dd", (quotes.getChild("OutboundLeg").getChildText("DepartureDate")).replace('T', ' ')));								
									Date quoteDate = Formatation.StringToDate("yyyy-MM-dd", (quotes.getChildText("QuoteDateTime")).replace('T', ' '));
									//converts the Date to Local date first (because dayOfWeek function from day is not working and Date is outdated replaced by LocalDate) and calculates the dayOfWeek afterwards
									//Monday == 1 , .. , Sunday == 7 (departureDate.DAY_OF_WEEK returns 0 for Sunday 1 for Monday and so on)
									int dayOfWeek = (departureDate.DAY_OF_WEEK == 0) ? 7 : departureDate.DAY_OF_WEEK;
									
									//generates the connection object for this connection
									connection = new Connection(new Place(airport.getAttributeValue("Id"), airport.getAttributeValue("Id"), Place.AIRPORT), new Place(placeDto.getChildText("IataCode"), placeDto.getChildText("IataCode"), Place.AIRPORT), Double.parseDouble(quotes.getChildText("MinPrice")), departureDate, Connection.PLANE,
											true, quoteDate, dayOfWeek);
									flightMap.put(connection);
								}
							}
						}
					}
					
				}catch(JDOMException | IOException | ParseException | InterruptedException e){
					logger.warn("Problem by reading the connectionlist xml file from getFlightMap in class SkyscannerCache: origin: " + originID + " | " + e.toString());
				}
		});
		System.out.println("collecting flights DONE");
		return flightMap;
	}
	
	/**
	 * Creates an ConnectionList object from all the information in the XML File for one direction
	 * @param rootFromConnectionXML root Element of the Full XML response from the Skyscanner API
	 * @param con XML Fragment of all Connections
	 * @param direction OUTBOUND variable can be used for outbound || INBOUND variable can be used for inbound
	 * @return List with all connection information available in the XML File for one direction
	 * @throws ParseException
	 */
	private Connection createConnectionListFromXML(Element rootFromConnectionXML, Element con, String direction) throws ParseException{
		Connection connectionObject = null;
		Place originObject = null;
		Place destinationObject = null;
		
		//get place information about origin and destination place
		for(Element place:rootFromConnectionXML.getDescendants(Filters.element("PlaceDto"))){
			if(place.getChildText("PlaceId").equals(con.getChild(direction).getChildText("OriginId"))){
				originObject = getPlaceFromXML(place);
			}
			if(place.getChildText("PlaceId").equals(con.getChild(direction).getChildText("DestinationId"))){
				destinationObject = getPlaceFromXML(place);
			}
		}
		
		// if no information for origin or destination place available throw a exception
		if(originObject == null){
			throw new NullPointerException("No origin place found!");
		}
		if(destinationObject == null){
			throw new NullPointerException("No destinaton place found!");
		}
		//Create a new ConnectionList object with the origin and destination place objects created bevore
		connectionObject = new Connection(originObject, destinationObject);
		
		// get a List of all Carriers and add it to the ConnectionList object of this connection
		connectionObject.setCarrier(getCarrierListFromXML(con.getChild(direction).getChild("CarrierIds"), rootFromConnectionXML));
		
		//set connection properties
		connectionObject.setType(Connection.PLANE);
		connectionObject.setDirect((con.getChild("Direct").equals("true")) ? true : false);
		connectionObject.setPrice(Double.parseDouble(con.getChildText("MinPrice")));
		//reads an Date from the XML file, cuts the time in the end of and parses it into Date format
		GregorianCalendar departureDate = new GregorianCalendar();
		departureDate.setTime(Formatation.StringToDate("yyyy-MM-dd", (con.getChild(direction).getChildText("DepartureDate")).replace('T', ' ')));
		connectionObject.setDepartureDate(departureDate);
		//The Date String comes in the Format yyyy-MM-ddTkk:mm:ss therefore the T has to be replaced by a space before it can be parsed to Date
		connectionObject.setQuoteDateTime(Formatation.StringToDate("yyyy-MM-dd kk:mm:ss", (con.getChildText("QuoteDateTime")).replace('T', ' ')));
		
		
		return connectionObject;
	}
	
	
	/**
	 * Writes the Placeinformation from a Skyscanner XML into a Place object
	 * @param place PlaceDto XML Snippet from the Place
	 * @return Place Object with placeinformations from the XML Snippet
	 */
	private static Place getPlaceFromXML(Element place){
		Place placeObject = new Place();
		
		if(place.getChildText("Name") != null){
			placeObject.setName(place.getChildText("Name"));
		}
		if(place.getChildText("IataCode") != null){
			placeObject.setIata(place.getChildText("IataCode"));
			Query querry;
			try {
				querry = new Query((new DatabaseConnection()).getConnection());
				placeObject.setLatitude(querry.getLatitudeFromPlace(placeObject));
				placeObject.setLongitude((querry.getLongitudeFromPlace(placeObject)));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if(place.getChildText("CityName") != null){
			placeObject.setCity(place.getChildText("CityName"));
		}
		if(place.getChildText("CountryName") != null){
			placeObject.setCountry(place.getChildText("CountryName"));
		}
		//Type Station is a Airport in Skyscanner always
		if(place.getChildText("Type").equals("Station")){
			placeObject.setType(Place.AIRPORT);
		}
		
		
		return placeObject;
	}
	
	/**
	 * Gives the names of the carriers from a XML file with fligh connections back
	 * @param carrierNumbers XML Element with the numbers of the carriers
	 * @param carrierNames XML Element with the same numbers as in carrierNumbers and the depending names of the carriers
	 * @return A list of all carriers that are reffert to in the carrierNumbers file
	 */
	private static LinkedBlockingQueue<Carrier> getCarrierListFromXML(Element carrierNumbers, Element carrierNames){
		LinkedBlockingQueue<Carrier> carrierList = new LinkedBlockingQueue<Carrier>();
		
		for(Element num:carrierNumbers.getDescendants(Filters.element("int"))){
			for(Element name:carrierNames.getDescendants(Filters.element("CarriersDto"))){
				if(num.getText().equals(name.getChildText("CarrierId"))){
					carrierList.add(new Carrier(name.getChildText("Name")));
				}
			}
		}	
		return carrierList;
	}
	
	/**
	 * Changes the country currency and language Settings for querries
	 * @param country Country
	 * @param currency Currency
	 * @param language Language
	 */
	public void changeNationalSettings(String country, String currency, String language) {
		url = new SkyscannerURL(country, currency, language);
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
	
	/**
	 * Returns a list of all Places from the given type from Skyscanner
	 * @param placeType Skyscanner String representation of the Type of places (Continent, Country, City, Airport)
	 * @return list of all places of the given type skyscanner knows
	 * @throws IOException 
	 * @throws JDOMException 
	 * @throws ClientProtocolException 
	 */
	public LinkedList<Place> getPlaceList(String placeType) throws ClientProtocolException, JDOMException, IOException{
		LinkedList<Place> placeList = new LinkedList<Place>();
		
		SAXBuilder builder = new SAXBuilder();
		Document placesXML = builder.build(getInput(url.getAllPlacesURL()));
		Element rootFromPlacesXML = placesXML.getRootElement();
		
		for(Element obj:rootFromPlacesXML.getDescendants(Filters.element(placeType))){
			Place place = new Place();
			
			//Set place ID for all places
			if(obj.getAttributeValue("Id") != null){
				place.setId(obj.getAttributeValue("Id"));
			}
			
			//Set place name for all places
			if(obj.getAttributeValue("Name") != null){
				place.setName(obj.getAttributeValue("Name"));
			}
			
			//Set "on continent", currency and language only for countries
			if(placeType.equals("Country")){
				place.setContinent(obj.getParentElement().getParentElement().getAttributeValue("Id"));
				if(obj.getChildText("CurrencyId") != null){
					place.setCurrency(obj.getChildText("CurrencyId"));
				}
				if(obj.getChildText("LanguageId") != null){
					place.setLanguage(obj.getChildText("LanguageId"));
				}
			}
			
			//Set iata code and location for city and airport
			//Set "in country" for city
			//Set "in city" for airport
			if(placeType.equals("City") || placeType.equals("Airport")){
				//Set location Location for Airport and City
				String[] latlong = obj.getAttributeValue("Location").split(",");
				place.setLatitude(Double.parseDouble(latlong[0]));
				place.setLongitude(Double.parseDouble(latlong[1]));
				
				if(placeType.equals("City")){
					place.setIata(obj.getAttributeValue("IataCode"));
					place.setCountry(obj.getAttributeValue("CountryId"));
				}
				if(placeType.equals("Airport")){
					place.setIata(obj.getAttributeValue("Id"));
					place.setCity(obj.getAttributeValue("CityId"));
				}
			}
			
			if(obj.getAttributeValue("Name") != null){
				place.setName(obj.getAttributeValue("Name"));
			}
			
			placeList.add(place);
		}
		
		return placeList;
	}
		
	
	

}
