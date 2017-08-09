import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdom2.JDOMException;

import api.SkyscannerLive;
import api.testLive;
import api.utilities.GoogleMaps;
import database.updateTables.*;
import utilities.Connection;

public class TripPlanner {

	public static void main(String[] args) {
		api.SkyscannerCache test = new api.SkyscannerCache();
		api.GoogleMapsDistance distance = new api.GoogleMapsDistance();
		api.GoogleMapsDirection direction = new api.GoogleMapsDirection();
		
		try{
			//test.getAllConnections( "FRA", "JFK", new GregorianCalendar(2017, 7, 22).getTime(), new GregorianCalendar(2017, 7, 25).getTime());
			//direction.getAllConnections( "Göttingen", "Hannover" + URLEncoder.encode("|", "UTF-8") + "Frankfurt" + URLEncoder.encode("|", "UTF-8") + "Berlin" + URLEncoder.encode("|", "UTF-8") + "Padderborn+Germany", new GregorianCalendar(2017, 7, 22).getTime(), new GregorianCalendar(2017, 7, 25).getTime());
			//direction.getAllConnections( "51.536102,9.925805", "52.377847,9.740254", new GregorianCalendar(2017, 7, 22).getTime(), new GregorianCalendar(2017, 7, 25).getTime());
			//direction.getAllConnections( "Hannover", "Berlin", new GregorianCalendar(2017, 6, 29).getTime(), new GregorianCalendar(2017, 7, 25).getTime());
			//LinkedBlockingQueue<Connection> connectionList = distance.getConnection(testObjects.PLACELIST_3(), testObjects.PLACELIST_3(), new GregorianCalendar(2017, 7, 22).getTime(), true, GoogleMaps.DRIVING, "de", GoogleMaps.FERRIES);
			//test.getAllConnections( "FRA", "JFK", new GregorianCalendar(2017, 8, 22).getTime(), null);
			//test.getAutosuggest("pari");
			//printConnectionList(connectionList);
			
		}catch(Exception e){
			System.out.println(e);
		}
		
		
		try{
			UpdateDatabase.proceed(new database.DatabaseConnection().getConnection());
		}catch(IOException | SQLException e){
			System.out.println(e);
		}
		/**/
		/*
		
		
		try {
			SkyscannerLive.test();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR " + e);
		}
		*/
		
		//testLive.test();
		
		//test.test();
		System.out.println("Done");
	}
	
	private static void printConnectionList(LinkedBlockingQueue<Connection> connectionList){
		for(Connection connection : connectionList){
			System.out.println(connection.getOrigin().getCity() + " - " + connection.getDestination().getCity() + ": " + connection.getDistance() + " in " + connection.getDuration().toString());
		}
		
	}

}
