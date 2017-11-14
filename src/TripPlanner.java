import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdom2.JDOMException;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.gson.JsonSyntaxException;

import api.EStream;
import api.SkyscannerLive;
import api.testLive;
import api.utilities.GoogleMaps;
import database.DatabaseConnection;
import database.updateTables.*;
import database.utilities.ClosestAirportListElement;
import sockets.JsonConverter;
import sockets.LineCoordinatesOnly;
import utilities.Connection;

public class TripPlanner {

	public static void main(String[] args) {
		api.SkyscannerCache cache = new api.SkyscannerCache();
		api.SkyscannerLive live = new api.SkyscannerLive();
		api.GoogleMapsDistance distance = new api.GoogleMapsDistance();
		api.GoogleMapsDirection direction = new api.GoogleMapsDirection();
		
		GregorianCalendar greg = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		greg.set(2017, 12-1, 18, 7, 20);
		
		GregorianCalendar greg2 = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		greg2.set(2017, 12-1, 22, 7, 20);
		
		/*
		try {
			database.ClosestAirports closeAirports = new database.ClosestAirports((new DatabaseConnection()).getConnection());
			
			closeAirports.createAirportsBeeline(testObjects.ROSDORF(), 9);
			closeAirports.setAirportOtherDistance(1, GoogleMaps.DRIVING);
			closeAirports.orderListByDuration();
			
			for(ClosestAirportListElement airport : closeAirports.getAirportList()){
				//System.out.println(airport.getAirport().getName() + " - " + airport.getDuration().getStandardHours() + ":" + airport.getDuration().getStandardMinutes() % 60);
				System.out.println(airport.getAirport().getName() + " - " + airport.getConnection().getDistance());
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		try{
			
			
			//LinkedBlockingQueue<Connection> connectionList = test.getAllConnections( "FRA", "JFK", new GregorianCalendar(2017, 8, 22), new GregorianCalendar(2017, 8, 25));
			//LinkedBlockingQueue<Connection> connectionList = direction.getAllConnections( "Göttingen", "Hannover" + URLEncoder.encode("|", "UTF-8") + "Frankfurt" + URLEncoder.encode("|", "UTF-8") + "Berlin" + URLEncoder.encode("|", "UTF-8") + "Padderborn+Germany", new GregorianCalendar(2017, 7, 22), new GregorianCalendar(2017, 7, 25));
			//direction.getAllConnections( "51.536102,9.925805", "52.377847,9.740254", new GregorianCalendar(2017, 7, 22).getTime(), new GregorianCalendar(2017, 7, 25).getTime());
			
			//direction.getAllConnections( "Göttingen", "Dortmund", new GregorianCalendar(2017, 8, 29, 15, 20), new GregorianCalendar(2017, 8, 25, 15, 20));
			
			
			//Google distance
			//LinkedBlockingQueue<Connection> connectionList =  distance.getAllConnections( "Hannover", "Dortmund", new GregorianCalendar(2017, 8, 29), new GregorianCalendar(2017, 8, 25));
			//printConnectionList3(connectionList);
			/*LinkedBlockingQueue<Connection> connectionList = distance.getConnection(testObjects.PLACELIST_3(), testObjects.PLACELIST_3(), greg, true, GoogleMaps.DRIVING, "de", GoogleMaps.FERRIES);
			printConnectionList(connectionList);
			
			System.out.println(JsonConverter.getJson(connectionList));*/
			
			
			
			
			//LinkedBlockingQueue<Connection> connectionList = distance.getConnection(testObjects.PLACELIST_3(), testObjects.PLACELIST_3(), greg, true, GoogleMaps.TRANSIT, GoogleMaps.FERRIES, "de");
			
			//LinkedBlockingQueue<Connection> connectionList = distance.getConnection(testObjects.PLACELIST_3(), testObjects.PLACELIST_3(), null, true, GoogleMaps.TRANSIT, GoogleMaps.FERRIES, "de");
			//-----------------------------------
			
			//Google direction
			/*LinkedBlockingQueue<Connection> connectionList = direction.getConnection(testObjects.GOETTINGEN(), testObjects.ROSDORF(), greg, true, GoogleMaps.TRANSIT, "", "de");
			printConnectionListGDirection(connectionList);
			System.out.println(JsonConverter.getJson(connectionList));*/
			
			//---------------------------------------
			//Skyscanner
			//cache.getAllConnections( "FRA", "JFK", new GregorianCalendar(2017, 8, 22), new GregorianCalendar(2017, 9, 22));
			//live.getAllConnections( "FRA", "JFK", new GregorianCalendar(2017, 8, 22), null);
			//cache.getAutosuggest("pari");
			//printConnectionList(connectionList);
			
			
			
		}catch(Exception e){
			System.out.println(e);
		}
		
		// +++ Starting the Server +++
		openSocket();
		
		
		// eStream
		/*try {
			EStream eStream = new EStream();
			eStream.getAllConnections("MUC", "HAM", greg, null);
		} catch (JsonSyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		//++Updates the entries of the database from skyscanner
		/*try{
			UpdateDatabase.proceed(new  database.DatabaseConnection().getConnection());
		}catch(IOException | SQLException e){
			System.out.println(e);
		}
		*/
		
		
		/*
		// still 403 error because it is not supported anymore
		try {
			SkyscannerLive.test();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR " + e);
		}
		*/
		
		System.out.println("Done");
	}

	
	private static void openSocket(){
		LineCoordinatesOnly.version1();
	}
	
	//for getConnection
	private static void printConnectionList(LinkedBlockingQueue<Connection> connectionList){
		for(Connection connection : connectionList){
			System.out.println(connection.getOrigin().getCity() + " - " + connection.getDestination().getCity() + ": " + connection.getDistance() + " in " + connection.durationToString());
		}
		
	}
	
	//for SkyCache
	private static void printConnectionList2(LinkedBlockingQueue<Connection> connectionList){
		for(Connection connection : connectionList){
			System.out.println(connection.getOrigin().getCity() + " - " + connection.getDestination().getCity());
		}
		
	}
	
	//Für getAllConnections
	private static void printConnectionList3(LinkedBlockingQueue<Connection> connectionList){
		for(Connection connection : connectionList){
			System.out.println(connection.getOrigin().getName() + " - " + connection.getDestination().getName() + ": " + connection.getDistance() + " in " + connection.durationToString());
			System.out.println(connection.getOrigin().getLatitude() + ":" + connection.getOrigin().getLongitude());
		}
		
	}
	
	//for getConnection
	private static void printConnectionListGDirection(LinkedBlockingQueue<Connection> connectionList){
		for(Connection connection : connectionList){
			System.out.println(connection.getOrigin().getName() + " - " + connection.getDestination().getName() + ": " + connection.getDistance() + " in " + connection.durationToString());
			if(connection.hasDepartureDate())
				System.out.println("   Departure: " + Connection.dateToString(connection.getDepartureDate()) + " " + connection.getDepartureDate().getTimeZone().getDisplayName());
			if(connection.hasArrivalDate())
				System.out.println("   Arrival: " + Connection.dateToString(connection.getArrivalDate()) + " " + connection.getArrivalDate().getTimeZone().getDisplayName());
			printConnectionListGDirection(connection.getSubConnections());
			System.out.println("-------------------------------------------------------------------------------");
		}
		
	}


}
