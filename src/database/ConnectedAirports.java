package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.utilities.SQLUtilities;
import utilities.Connection;
import utilities.Place;

public class ConnectedAirports {

	
	protected static final Logger logger = LogManager.getLogger(ConnectedAirports.class);
	protected static java.sql.Connection conn = DatabaseConnection.getConnection();
	private static long millisecondsOfDay = 1000 * 60 * 60 * 24;
	
	public ConnectedAirports(){
		
	}
	
	public static LinkedBlockingQueue<Connection> getAllOutboundConnections(Place airport) throws SQLException{
		java.sql.Timestamp time = new java.sql.Timestamp((long) 1523105259 * 1000);
		ResultSet outboundConnections = getAllOutboundConnectionsAprxPrice(airport.getIata(), time, millisecondsOfDay, true, false, false);
		LinkedBlockingQueue<Connection> connectionList = SQLUtilities.getConnectionListFromResultSetWhithDestinations(airport, outboundConnections);
		return connectionList;
	}
	
	/**
	 * Returns all airports than can be reached from the given airport within one daay
	 * @param airport origin airport
	 * @param date departure date
	 * @return List of connections to the airports
	 * @throws SQLException
	 */
	public static LinkedBlockingQueue<Connection> getAllOutboundConnectionsWithinOneDay(Place airport, GregorianCalendar date) throws SQLException{
		java.sql.Timestamp time = new java.sql.Timestamp(date.getTimeInMillis());
		ResultSet outboundConnections = getAllOutboundConnectionsAprxPrice(airport.getIata(), time, millisecondsOfDay, true, false, false);
		LinkedBlockingQueue<Connection> connectionList = SQLUtilities.getConnectionListFromResultSetWhithDestinations(airport, outboundConnections);
		return connectionList;
	}	
	
	private static ResultSet getAllOutboundConnectionsAprxPrice(String iata, java.sql.Timestamp departureTime, long timeperiode, boolean allowZeroPrice, boolean allowIncompleteData, boolean allowConnectedFlights) throws SQLException{
		ResultSet queryResult;
		String queryString = "SELECT airports.iata_code, airports.name, ST_Y(airports.location) As latitude, ST_X(airports.location) As longitude, connections.departure_date, connections.arrival_time, connections.min_price, connections.weekday, connections.flightnumber, connections.duration, connections.currency, connections.operating_airline "
				+ "FROM airports, connections_with_aprx_price as connections "
				+ "WHERE connections.origin = '" + iata + "' "
				+ "and connections.connection_number is null ";
		if(!allowZeroPrice)
			queryString += "and min_price is not ";
		if(!allowIncompleteData)
			queryString += "and duration is not null ";
		if(!allowConnectedFlights)
			queryString += "and flightnumber is not null ";
		queryString += "and connections.departure_date between '" + departureTime + "' and '" + new java.sql.Timestamp(departureTime.getTime() + timeperiode) + "' "
				+ "and airports.iata_code = connections.destination "
				+ "ORDER BY departure_date;";
		try {
			queryResult = conn.createStatement().executeQuery(queryString);
		} catch (SQLException e) {
			logger.error("Cant Query closest airports for: " + iata + "\n " + e);
			throw new SQLException("Cant Query closest airports for: " + iata);
		}	
		return queryResult;
	}
	
	
	public static LinkedBlockingQueue<Connection> getAllInboundConnectionsWithinFiveDays(Place airport, GregorianCalendar date) throws SQLException{
		java.sql.Timestamp time = new java.sql.Timestamp(date.getTimeInMillis());
		//ResultSet inboundConnections = getAllInboundConnections(airport.getIata(), time, millisecondsOfDay * 5, true, false, false);
		ResultSet inboundConnections = getAllInboundConnectionsAprxPrice(airport.getIata(), time, millisecondsOfDay * 5, true, false, false);
		LinkedBlockingQueue<Connection> connectionList = SQLUtilities.getConnectionListFromResultSetWhithOrigins(airport, inboundConnections);
		return connectionList;
	}	
	
	private static ResultSet getAllInboundConnectionsAprxPrice(String iata, java.sql.Timestamp departureTime, long timeperiode, boolean allowZeroPrice, boolean allowIncompleteData, boolean allowConnectedFlights) throws SQLException{
		ResultSet queryResult;
		String queryString = "SELECT airports.iata_code, airports.name, ST_Y(airports.location) As latitude, ST_X(airports.location) As longitude, connections.departure_date, connections.arrival_time, connections.min_price, connections.weekday, connections.flightnumber, connections.duration, connections.currency, connections.operating_airline "
				+ "FROM airports, connections_with_aprx_price as connections "
				+ "WHERE connections.destination = '" + iata + "' "
				+ "and connections.connection_number is null ";
		if(!allowZeroPrice)
			queryString += "and min_price is not null ";
		if(!allowIncompleteData)
			queryString += "and duration is not null ";
		if(!allowConnectedFlights)
			queryString += "and flightnumber is not null ";
		queryString += "and connections.departure_date between '" + departureTime + "' and '" + new java.sql.Timestamp(departureTime.getTime() + timeperiode) + "' "
				+ "and airports.iata_code = connections.origin;";
		try {
			queryResult = conn.createStatement().executeQuery(queryString);
		} catch (SQLException e) {
			logger.error("Cant Query closest airports for: " + iata + "\n " + e);
			throw new SQLException("Cant Query closest airports for: " + iata);
		}	
		return queryResult;
	}
	
	
	
	
}
