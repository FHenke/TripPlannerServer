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

public class ConnectedHotspots {

	protected static final Logger logger = LogManager.getLogger(ConnectedHotspots.class);
	protected static java.sql.Connection conn = DatabaseConnection.getConnection();
	private static long millisecondsOfDay = 1000 * 60 * 60 * 24;
	
	public static LinkedBlockingQueue<Connection> getAllOutboundConnectionsWithinOneDay(Place originAirport, Place destinationAirport, GregorianCalendar date) throws SQLException{
		java.sql.Timestamp time = new java.sql.Timestamp(date.getTimeInMillis());
		//ResultSet outboundConnections = getAllOutboundConnections(airport.getIata(), time, millisecondsOfDay, true, false, false);
		ResultSet outboundConnections = getAllOutboundConnections(originAirport.getIata(), destinationAirport.getIata(), time, millisecondsOfDay, true, false, false);
		LinkedBlockingQueue<Connection> connectionList = SQLUtilities.getConnectionListFromResultSetWhithDestinations(originAirport, outboundConnections);
		return connectionList;
	}
	
	private static ResultSet getAllOutboundConnections(String originIata, String destinationIata, java.sql.Timestamp departureTime, long timeperiode, boolean allowZeroPrice, boolean allowIncompleteData, boolean allowConnectedFlights) throws SQLException{
		ResultSet queryResult;
		String queryString = "SELECT airports.iata_code, airports.name, ST_Y(airports.location) As latitude, ST_X(airports.location) As longitude, connections.departure_date, connections.arrival_time, connections.min_price, connections.weekday, connections.flightnumber, connections.duration, connections.currency, connections.operating_airline "
				+ "FROM airports, connections_with_aprx_price as connections "
				+ "WHERE connections.origin = '" + originIata + "' "
				+ "AND connections.connection_number IS NULL "
				+ "AND (connections.destination IN (SELECT iata_code FROM hotspots) OR connections.destination = '" + destinationIata + "')";
		if(!allowZeroPrice)
			queryString += "and min_price != 0.0 ";
		if(!allowIncompleteData)
			queryString += "and duration is not null ";
		if(!allowConnectedFlights)
			queryString += "and flightnumber is not null and connection_number is null ";
		queryString += "and connections.departure_date between '" + departureTime + "' and '" + new java.sql.Timestamp(departureTime.getTime() + timeperiode) + "' "
				+ "and airports.iata_code = connections.destination;";
		
		try {
			queryResult = conn.createStatement().executeQuery(queryString);
		} catch (SQLException e) {
			logger.error("Cant Query connected airports for: " + originIata + "\n " + queryString + "\n" + e);
			throw new SQLException("Cant Query connected airports for: " + originIata);
		}	
		return queryResult;
	}
	
	
	public static LinkedBlockingQueue<Connection> getAllInboundConnectionsWithinFiveDays(Place destinationAirport, GregorianCalendar date) throws SQLException{
		java.sql.Timestamp time = new java.sql.Timestamp(date.getTimeInMillis());
		//ResultSet outboundConnections = getAllOutboundConnections(airport.getIata(), time, millisecondsOfDay, true, false, false);
		ResultSet inboundConnections = getAllInboundConnections(destinationAirport.getIata(), time, millisecondsOfDay * 5, true, false, false);
		LinkedBlockingQueue<Connection> connectionList = SQLUtilities.getConnectionListFromResultSetWhithOrigins(destinationAirport, inboundConnections);
		return connectionList;
	}
	
	private static ResultSet getAllInboundConnections(String destinationIata, java.sql.Timestamp departureTime, long timeperiode, boolean allowZeroPrice, boolean allowIncompleteData, boolean allowConnectedFlights) throws SQLException{
		ResultSet queryResult;
		String queryString = "SELECT airports.iata_code, airports.name, ST_Y(airports.location) As latitude, ST_X(airports.location) As longitude, connections.departure_date, connections.arrival_time, connections.min_price, connections.weekday, connections.flightnumber, connections.duration, connections.currency, connections.operating_airline "
				+ "FROM airports, connections_with_aprx_price as connections "
				+ "WHERE connections.destination = '" + destinationIata + "' "
				+ "AND connections.connection_number IS NULL "
				+ "AND (connections.origin IN (SELECT iata_code FROM hotspots))";
		if(!allowZeroPrice)
			queryString += "and min_price != 0.0 ";
		if(!allowIncompleteData)
			queryString += "and duration is not null ";
		if(!allowConnectedFlights)
			queryString += "and flightnumber is not null ";
		queryString += "and connections.departure_date between '" + departureTime + "' and '" + new java.sql.Timestamp(departureTime.getTime() + timeperiode) + "' "
				+ "and airports.iata_code = connections.origin;";
		
		try {
			queryResult = conn.createStatement().executeQuery(queryString);
		} catch (SQLException e) {
			logger.error("Cant Query connected airports for: " + destinationIata + "\n " + queryString + "\n" + e);
			throw new SQLException("Cant Query connected airports for: " + destinationIata);
		}	
		return queryResult;
	}
}
