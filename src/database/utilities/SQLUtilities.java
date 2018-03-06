package database.utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;

import utilities.Carrier;
import utilities.CarrierList;
import utilities.Connection;
import utilities.Place;

public class SQLUtilities {

	protected static final Logger logger = LogManager.getLogger(SQLUtilities.class);
	
	/**
	 * Converts a result set that includes all outbound connections for a given origin into a list of connections
	 * @param origin Origin Place (Airport)
	 * @param result Result set whith place information about the destination airport for all connections to the given destination
	 * @return List of all connections from the result set
	 * @throws SQLException
	 */
	public static LinkedBlockingQueue<Connection> getConnectionListFromResultSetWhithDestinations(Place origin, ResultSet result) throws SQLException{
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		while(result.next()){
			Place destination = generatePlaceFromResultSet(result);
			Connection connection = new Connection(origin, destination);
			connectionList.add(getConnectionFromResultSet(connection, result));
		}
		return connectionList;
	}
	
	/**
	 * Converts a result set that includes all inbound connections for a given destination into a list of connections
	 * @param origin Destination Place (Airport)
	 * @param result Result set whith place information about the origin airport for all connections to the given destination
	 * @return List of all connections from the result set
	 * @throws SQLException
	 */
	public static LinkedBlockingQueue<Connection> getConnectionListFromResultSetWhithOrigins(Place destination, ResultSet result) throws SQLException{
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		while(result.next()){
			Place origin = generatePlaceFromResultSet(result);
			Connection connection = new Connection(origin, destination);
			connectionList.add(getConnectionFromResultSet(connection, result));
		}
		return connectionList;
	}
	
	public static Connection getConnectionFromResultSet(Connection connection, ResultSet result) throws SQLException{
		
		connection.setDepartureDate(toGregorianCalendar(result.getTimestamp("departure_date")));
		connection.setArrivalDate(toGregorianCalendar(result.getTimestamp("arrival_time")));
		connection.setPrice(result.getDouble("min_price"));
		connection.setWeekday(result.getInt("weekday"));
		connection.setCode(result.getString("flightnumber"));
		connection.setDuration(new Duration(result.getInt("duration")));
		connection.setCurrency(result.getString("currency"));
		connection.addCarrier(new Carrier(result.getString("operating_airline")));
		connection.setType(Connection.PLANE);
		connection.setSummary(connection.getDestination().getName());
		if(connection.getCode() != null){
			connection.setDirect(true);
		}
	
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
			logger.error("Can't read this place from ResultSet: " + place.getName() + "\n " + e);
			throw new SQLException("Cant read this place from ResultSet: " + place.getName() + "\n ");
		}
		return place;
	}
	
	public static GregorianCalendar toGregorianCalendar(java.sql.Timestamp time){
		//GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(time.getTime());
		return calendar;
	}
	
	
	public static LinkedBlockingQueue<Connection> getConnectionListFromResultSet(ResultSet result) throws SQLException{
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		while(result.next()){
			Place origin = generateOriginPlaceFromResultSet(result);
			Place destination = generateDestinationPlaceFromResultSet(result);
			Connection connection = new Connection(origin, destination);
			connectionList.add(getConnectionFromResultSet(connection, result));
		}
		return connectionList;
	}
	
	public static Place generateOriginPlaceFromResultSet(ResultSet result) throws SQLException{
		Place place = null;
		try{
			place = new Place(result.getString("org_name") + " Airport", result.getDouble("org_longitude"), result.getDouble("org_latitude"));
			place.setIata(result.getString("org_iata"));
			place.setId(result.getString("org_iata"));
			place.setType(Place.AIRPORT);
		}catch(Exception e){
			logger.error("Can't read this place from ResultSet: " + place.getName() + "\n " + e);
			throw new SQLException("Cant read this place from ResultSet: " + place.getName() + "\n ");
		}
		return place;
	}
	
	public static Place generateDestinationPlaceFromResultSet(ResultSet result) throws SQLException{
		Place place = null;
		try{
			place = new Place(result.getString("dst_name") + " Airport", result.getDouble("dst_longitude"), result.getDouble("dst_latitude"));
			place.setIata(result.getString("dst_iata"));
			place.setId(result.getString("dst_iata"));
			place.setType(Place.AIRPORT);
		}catch(Exception e){
			logger.error("Can't read this place from ResultSet: " + place.getName() + "\n " + e);
			throw new SQLException("Cant read this place from ResultSet: " + place.getName() + "\n ");
		}
		return place;
	}
	
	
}
