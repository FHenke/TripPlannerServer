package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.utilities.SQLUtilities;
import utilities.Connection;
import utilities.Place;

public class QueryConnection {

	protected static final Logger logger = LogManager.getLogger(QueryConnection.class);

	
	public static LinkedBlockingQueue<Connection> getAllOutboundConnections(Place originAirport, Place destinationAirport) throws SQLException{
		java.sql.Timestamp time = new java.sql.Timestamp((long) 1523105259 * 1000);
		ResultSet outboundConnections = getAllOutboundConnectionsAprxPrice(originAirport.getIata(), destinationAirport.getIata(), true, false, false);
		LinkedBlockingQueue<Connection> connectionList = SQLUtilities.getConnectionListFromResultSet(outboundConnections);
		return connectionList;
	}
	
	private static ResultSet getAllOutboundConnectionsAprxPrice(String origin_iata, String destination_iata, boolean allowZeroPrice, boolean allowIncompleteData, boolean allowConnectedFlights) throws SQLException{
		ResultSet queryResult;
		String queryString = "SELECT origin.iata_code AS org_iata, origin.name AS org_name, ST_Y(origin.location) As org_latitude, ST_X(origin.location) As org_longitude, "
				+ "destination.iata_code AS dst_iata, destination.name AS dst_name, ST_Y(destination.location) As dst_latitude, ST_X(destination.location) As dst_longitude, "
				+ "connections.departure_date, connections.arrival_time, connections.min_price, connections.weekday, connections.flightnumber, connections.duration, connections.currency, connections.operating_airline, connections.connection_number as connection_number "
				+ "FROM airports origin, airports destination, connections_with_aprx_price as connections "
				+ "WHERE connections.origin = '" + origin_iata + "' "
				+ "and connections.destination = '" + destination_iata + "' ";
		if(!allowZeroPrice)
			queryString += "and min_price is not ";
		if(!allowIncompleteData)
			queryString += "and duration is not null ";
		if(!allowConnectedFlights)
			queryString += "and flightnumber is not null and connections.connection_number is null ";
		queryString += "and destination.iata_code = connections.destination "
				+ "and origin.iata_code = connections.origin;";
		System.out.println(queryString);
		try {
			queryResult = DatabaseConnection.getConnection().createStatement().executeQuery(queryString);
		} catch (SQLException e) {
			logger.error("Cant Query connection for: " + origin_iata + " to " + destination_iata + "\n " + e);
			throw new SQLException("Cant Query connection for: " + origin_iata + " to " + destination_iata + "\n ");
		}	
		return queryResult;
	}
}
