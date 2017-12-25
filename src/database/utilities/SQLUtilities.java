package database.utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;

import database.QueryClosestAirports;
import utilities.CarrierList;
import utilities.Connection;
import utilities.Place;

public class SQLUtilities {

	protected static final Logger logger = LogManager.getLogger(SQLUtilities.class);

	
	public static GregorianCalendar toGregorianCalendar(java.sql.Timestamp time){
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(time.getTime());
		return calendar;
	}
	
	public static LinkedBlockingQueue<Connection> getConnectionListFromResultSet(Place origin, ResultSet result) throws SQLException{
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		while(result.next()){
			connectionList.add(getConnectionFromResultSet(origin, result));
		}
		
		return connectionList;
	}
	
	public static Connection getConnectionFromResultSet(Place origin, ResultSet result) throws SQLException{
		Place destination = generatePlaceFromResultSet(result);
		Connection connection = new Connection(origin, destination);
		
		connection.setDepartureDate(toGregorianCalendar(result.getTimestamp("departure_date")));
		connection.setArrivalDate(toGregorianCalendar(result.getTimestamp("arrival_time")));
		connection.setPrice(result.getDouble("min_price"));
		connection.setWeekday(result.getInt("weekday"));
		connection.setCode(result.getString("flightnumber"));
		connection.setDuration(new Duration(result.getInt("duration")));
		connection.setCurrency(result.getString("currency"));
		connection.setCarrier(new CarrierList(result.getString("operating_airline")));
		connection.setType(Connection.PLANE);
		connection.setSummary(destination.getName());
	
		return connection;
	}
	
	public static Place generatePlaceFromResultSet(ResultSet result) throws SQLException{
		Place place = null;
		try{
			place = new Place(result.getString("name") + " Airport", result.getDouble("longitude"), result.getDouble("latitude"));
			place.setIata(result.getString("iata_code"));
			place.setId(result.getString("iata_code"));
			place.setType(Place.AIRPORT);
		}catch(Exception e){
			logger.error("Cant read this place from ResultSet: " + place.getName() + "\n " + e);
			throw new SQLException("Cant read this place from ResultSet: " + place.getName() + "\n ");
		}
		return place;
	}
}
