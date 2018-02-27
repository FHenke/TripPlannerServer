package database.updates;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import api.utilities.TimeZoneInfo;
import database.DatabaseConnection;
import utilities.Place;

public class correctDuration {

	protected static final Logger logger = LogManager.getLogger(correctDuration.class);
	
	public static int addTimezoneToAirports(){
		LinkedBlockingQueue<Place> airportList = null;
		int counter = 0;
		int errorcounter = 10;
		
		try {
			airportList = getAirportsWithoutTimezone();
		} catch (SQLException e) {
			logger.error("AirportList cant be generated. " + e.toString());
			return 0;
		}
		
		System.out.println(airportList.size() + " Airports without time zone info");
		
		for(Place airport : airportList){
			try {
				TimeZoneInfo timeZoneInfo = api.GoogleMapsTimeZone.getTimeZoneInfo(new GregorianCalendar(2018, 4 - 1, 5, 0, 0, 0), airport);
				if(setTimezoneOfAirport(airport, timeZoneInfo) == 1){
					System.out.println(counter++);
					errorcounter = 10;
				}else{
					System.out.println("Error");
				}
				
				if(errorcounter == 0){
					return counter;
				}
				
			} catch (IllegalStateException | IOException | JDOMException e) {
				logger.error("Timezone info for " + airport.getIata() + " can't be obtained. " + e.toString());
				System.out.println("Timezone info for " + airport.getIata() + " can't be obtained.");
				errorcounter--;
			} catch (SQLException e) {
				logger.error("Timezone info for " + airport.getIata() + " can't be written to database. " + e.toString());
				System.out.println("Timezone info for " + airport.getIata() + " can't be written to database.");
				errorcounter--;
			}
		}
		
		
		return counter;
 	}
	
	
	
	
	
	private static LinkedBlockingQueue<Place> getAirportsWithoutTimezone() throws SQLException{
		LinkedBlockingQueue<Place> airportList = new LinkedBlockingQueue<Place>();
		try{
			ResultSet queryResult = DatabaseConnection.getConnection().createStatement().executeQuery("SELECT ST_Y(location) As latitude, ST_X(location) As longitude, iata_code FROM airports WHERE summer_time IS null;"); //timezone = ''
			while(queryResult.next()){
				Place airport = new Place();
				airport.setLatitude(queryResult.getDouble("latitude"));
				airport.setLongitude(queryResult.getDouble("longitude"));
				airport.setIata(queryResult.getString("iata_code"));
				airportList.add(airport);
			}
		}catch(SQLException e){
			throw new SQLException("AirportList cant be created. " + e);
		}
		return airportList;
	}
	
	private static int setTimezoneOfAirport(Place airport, TimeZoneInfo timeZoneInfo) throws SQLException{
		if(!timeZoneInfo.getTimeZoneId().equals("")){
			int summerTime = ((int) (timeZoneInfo.getRawOffset() + timeZoneInfo.getDstOffset())) * 1000;
			int winterTime = ((int) timeZoneInfo.getRawOffset()) * 1000;
			return DatabaseConnection.getConnection().createStatement().executeUpdate("UPDATE airports SET timezone = '" + timeZoneInfo.getTimeZoneId() + "', summer_time = " + summerTime + ", winter_time = " + winterTime + " WHERE iata_code = '" + airport.getIata() + "';");
		}
		else{
			return 0;
		}
	}
	
	
	
}
