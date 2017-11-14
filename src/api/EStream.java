package api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import api.utilities.eStream.CacheResponse;
import api.utilities.eStream.Proposal;
import api.utilities.eStream.ResultSet;
import api.utilities.eStream.Segment;
import utilities.Connection;
import utilities.XMLUtilities;

public class EStream implements API {

protected static final Logger logger = LogManager.getLogger(EStream.class);
private static final String[] KEYS = {"2e4c18c566ec29f80f5b62156edebefc707ba1e0", "9759124eb23195416e37f9afb3760989282ce190"};
private static final AtomicInteger KeyToken = new AtomicInteger(0);
private int successConnections = 0;
	
	/**
	 * Empty Constructor
	 */
	public EStream(){
		
	}
	
	public LinkedBlockingQueue<Connection> getAllConnections(String origin, String destination, GregorianCalendar outboundDate, GregorianCalendar inboundDate) throws JsonSyntaxException, IOException{
		String requestString = getRequestURL(origin, destination, outboundDate, inboundDate);
		ResultSet resultSet = getResultSet(requestString, 100);
		
		//System.out.println(resultSet.getProposals()[0].getLegs()[0].getSegments()[0].getFlightNumber());
		
		if(resultSet != null){
			if(resultSet.getMessage().equals("success")){
				return getOnlyCheepestConnection(resultSet);
			} else{
				System.out.println(resultSet.getMessage());
				return new LinkedBlockingQueue<Connection>();
			}	
		}else{
			return new LinkedBlockingQueue<Connection>();
		}
		
	}
	
	private LinkedBlockingQueue<Connection> getDirectConnection(ResultSet results){
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		for(Proposal proposal : results.getProposals()){
			if(proposal.getLegs()[0].getSegments().length == 1){
				try{
					Connection connection = new Connection(results.getProposals()[0], proposal.getLegs()[0].getSegments()[0]);
					connectionList.add(connection);
				}catch(SQLException e){
					logger.error("Connection can't be added to Connection list because of SQL Exception: " + e.toString());
				}
			}
		}
		
		return connectionList;
	}
	
	//####################################
	
	private LinkedBlockingQueue<Connection> getAllDirectConnections(ResultSet results){
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		// points out whether the first connection is a direct connection or not.
		// if it is a direct connection the price is valid for this connection,
		// if not the price is valid for a connected flight and cant be used in this case.
		boolean directFirstConnection = false;
		if(results.getProposals()[0].getLegs()[0].getSegments().length == 1)
			directFirstConnection = true;
		
		for(Proposal proposal : results.getProposals()){
			for(Segment segment : proposal.getLegs()[0].getSegments()){
				try{
					if(directFirstConnection){
						Connection connection = new Connection(proposal, segment);
						connectionList.add(connection);
					}else{
						Connection connection = new Connection(null, segment);
						connectionList.add(connection);
					}
				}catch(SQLException e){
					logger.error("Connection can't be added to Connection list because of SQL Exception: " + e.toString());
				}
			}
		}
		
		return connectionList;
	}
	
	//###################################
	
	private LinkedBlockingQueue<Connection> getOnlyCheepestConnection(ResultSet results){
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		for(Segment segment : results.getProposals()[0].getLegs()[0].getSegments()){
			try{
				Connection connection = new Connection(results.getProposals()[0], segment);
				connectionList.add(connection);
			}catch(SQLException e){
				logger.error("Connection (" + segment.getOrigin() + "-" + segment.getDestination() + ") can't be added to Connection list because of SQL Exception: " + e.toString());
				return new LinkedBlockingQueue<Connection>();
			}
		}
		
		return connectionList;
	}
	
	
	/**
	 * Generates the request URL from the passed parameters
	 * @param origin IATA code of origin airport
	 * @param destination IATA code of destination airport
	 * @param outboundDate date and time of outbound connection
	 * @param inboundDate date and time of inbound connection
	 * @return Full URL string for API request
	 */
	private String getRequestURL(String origin, String destination, GregorianCalendar outboundDate, GregorianCalendar inboundDate){
		//Request parameters to URL
		String requestString = "https://api.travelcloudpro.eu/v1/cache/shopping?searchPhrase=" + getDateStringFromCalendar(outboundDate) + origin + destination;
		if(inboundDate != null){
			requestString = "https://api.travelcloudpro.eu/v1/cache/shopping?searchPhrase=" + getDateStringFromCalendar(inboundDate) + destination + origin;
		}
		requestString += "&pointOfSale=DE";
		
		System.out.println("Connection: " + origin + " - " + destination + " (" + successConnections + "/" + KeyToken + ")");
		
		return requestString;
	}
	
	/**
	 * Calls the API with the given request String and returns the response as an ResultSet object
	 * @param requestString full request URL
	 * @return ResultSet object with the response to the passed URL String
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	private ResultSet getResultSet(String requestString, int timeout) throws JsonSyntaxException, IOException{
		String urlResponse = getInput(requestString);
		XMLUtilities.writeStringToFile(urlResponse, "eStream" + KeyToken);
		System.out.println(urlResponse);
		//Converts the received JSON file into an Object
	    Gson gson = new Gson();
		CacheResponse cacheResponse = gson.fromJson(urlResponse, CacheResponse.class);
	    
		if(cacheResponse.getStatus().equals("success")){
			successConnections++;
			//Converts the JSON file into an ResultSet object and returns it
			try{
				// first decodes the Base 64 String and then decompress it
				String decompressedData = decompress(cacheResponse.getData().getBase64GzippedResponse());
				ResultSet resultSet = gson.fromJson(decompressedData, ResultSet.class);
				resultSet.setMessage("success");
				return resultSet;
			}catch(NullPointerException e){
				//if no result was found return null
				return null;
			}
		}
		if(cacheResponse.getErrorMessage().equals("RPS capacity limit exceeded")){
			if(timeout > 0){
				System.out.println("Timeout: " + timeout);
				return getResultSet(requestString, timeout - 1);
			}
			else{
				return new ResultSet(cacheResponse.getErrorMessage());
			}
		}
		else{
			return new ResultSet(cacheResponse.getErrorMessage());
		}
	}
	
	
	/**
	 * converts a Gregorian Calendar object to a date string of the format YYYYMMDDHHmm readable by eStreaming
	 * @param date Gregorian Calendar object of the date
	 * @return streng representation of the given date
	 */
	private static String getDateStringFromCalendar(GregorianCalendar date){
		String month = "";
		String day = "";
		
		//adds 0 if month has just one digit
		if((date.get(Calendar.MONTH) + 1) < 10)
			month = "0" + (date.get(Calendar.MONTH) + 1);
		else
			month = "" + (date.get(Calendar.MONTH) + 1);
		
		//adds 0 if day has just one digit
		if(date.get(Calendar.DATE) < 10)
			day = "0" + date.get(Calendar.DATE);
		else
			day = "" + date.get(Calendar.DATE);
		
		return date.get(Calendar.YEAR) + month + day;
	}
	
	/**
	 * Converts The compressed Base64 String into a JSON string
	 * @param base64GzippString compressed Base 64 String
	 * @return JSON String
	 * @throws IOException
	 */
	public static String decompress(String base64GzippString) throws IOException {
	    final StringBuilder outStr = new StringBuilder();
	    
	    byte[] compressed = Base64.decodeBase64(base64GzippString);
	    
	    if ((compressed == null) || (compressed.length == 0)) {
	      return "";
	    }
	    if (isCompressed(compressed)) {
	      final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
	      final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));

	      String line;
	      while ((line = bufferedReader.readLine()) != null) {
	        outStr.append(line);
	      }
	    } else {
	      outStr.append(compressed);
	    }
	    return outStr.toString();
	  }
	
	/**
	 * Checks if a byte Array is compressed (Gzipp) or not
	 * @param compressed compreddes byte array
	 * @return true if the byte array is compressed
	 */
	public static boolean isCompressed(final byte[] compressed) {
	    return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
	  }
	
	
	/**
	 * executes the Querry to Skyscanner API
	 * @param url URL for the Querry
	 * @return InputStream of the XML code that API responses
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	private static String getInput(String url) throws ClientProtocolException, IOException {

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);

		// add request header
		request.addHeader("AuthToken", getKey());

		HttpResponse response = client.execute(request);
		
		InputStream input = response.getEntity().getContent();
		
		java.util.Scanner s = new java.util.Scanner(input).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	/**
	 * uses round robin for assigning keys to requests. Through this way several API calls per minit can be executed.
	 * @return API key
	 */
	private static String getKey(){
		int counter = KeyToken.getAndIncrement() % KEYS.length;
		return KEYS[counter];
	}
	
}
