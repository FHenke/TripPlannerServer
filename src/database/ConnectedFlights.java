package database;

import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;

import database.DatabaseConnection;
import database.utilities.SQLUtilities;
import utilities.Carrier;
import utilities.Connection;
import utilities.Place;

public class ConnectedFlights {

	protected static final Logger logger = LogManager.getLogger(ConnectedFlights.class);
	
	public static LinkedBlockingQueue<Connection> getSubConnectionsOfConnection(int connectionNumber) throws SQLException{
		try{
			java.sql.ResultSet resultSet = DatabaseConnection.getConnection().createStatement().executeQuery(generateQueryString(connectionNumber));
			return getConnectionListFromResultSet(resultSet);
		}catch(SQLException e){
			logger.error("It is not possible to request connected flight with number: " + connectionNumber + "\n" + e);
			throw new SQLException("It is not possible to request connected flight with number: " + connectionNumber);
		}
	}
	
	private static String generateQueryString(int connectionNumber) throws SQLException{
			String queryString = "SELECT DISTINCT cfn.part, fc.departure_date, fc.arrival_time, fc.origin, origin.name AS origin_name, ST_Y(origin.location) AS origin_latitude, ST_X(origin.location) AS origin_longitude, fc.destination, destination.name AS destination_name, ST_Y(destination.location) AS destination_latitude, ST_X(destination.location) AS destination_longitude, fc.flightnumber, fc.duration, fc.weekday, fc.operating_airline " 
				+ "FROM connected_flight_numbers AS cfn, flight_connections AS fc, airports AS origin, airports AS destination "
				+ "WHERE cfn.connection_number = " + connectionNumber + " "
				+ "AND cfn.flight_number = fc.flightnumber "
				+ "AND cfn.departure_date = fc.departure_date "
				+ "AND fc.origin = origin.iata_code "
				+ "AND fc.destination = destination.iata_code "
				+ "ORDER BY part;";
			return queryString;
	}
	
	private static LinkedBlockingQueue<Connection> getConnectionListFromResultSet(java.sql.ResultSet resultSet) throws SQLException{
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		try{
			while(resultSet.next()){
				connectionList.add(getConnection(resultSet));
			}
		}catch(SQLException e){
			logger.error("It is not possible to request ConnectedFlight: " + e);
			throw new SQLException("It is not possible to request ConnectedFlight!");
		}
		return connectionList;
	}
	
	private static Connection getConnection(java.sql.ResultSet result) throws SQLException{
		Place origin = generatePlaceFromResultSet(result, "origin");
		Place destination = generatePlaceFromResultSet(result, "destination");
		Connection connection = new Connection(origin, destination);
		return getConnectionFromResultSet(connection, result);
	}
	
	private static Place generatePlaceFromResultSet(java.sql.ResultSet result, String originOrDestination) throws SQLException{
		Place place = null;
		try{
			place = new Place(result.getString(originOrDestination + "_name") + " Airport", result.getDouble(originOrDestination + "_longitude"), result.getDouble(originOrDestination + "_latitude"));
			place.setIata(result.getString(originOrDestination));
			place.setId(result.getString(originOrDestination));
			place.setType(Place.AIRPORT);
		}catch(Exception e){
			logger.error("Can't read this place from ResultSet: " + place.getName() + "\n " + e);
			throw new SQLException("Cant read this place from ResultSet: " + place.getName() + "\n ");
		}
		return place;
	}
	
	private static Connection getConnectionFromResultSet(Connection connection, java.sql.ResultSet result) throws SQLException{
		try{
			connection.setDepartureDate(SQLUtilities.toGregorianCalendar(result.getTimestamp("departure_date")));
			connection.setArrivalDate(SQLUtilities.toGregorianCalendar(result.getTimestamp("arrival_time")));
			connection.setWeekday(result.getInt("weekday"));
			connection.setCode(result.getString("flightnumber"));
			connection.setDuration(new Duration(result.getInt("duration")));
			connection.addCarrier(new Carrier(result.getString("operating_airline")));
			connection.setType(Connection.PLANE);
			connection.setSummary(connection.getDestination().getName());
			connection.setPrice(0.0);
			if(connection.getCode() != null){
				connection.setDirect(true);
			}
			return connection;
		}catch(SQLException e){
			logger.error("Can't read this connection from ResultSet: " + connection.getOrigin().getName() + "\n " + e);
			throw new SQLException("Cant read this place from ResultSet: " + connection.getOrigin().getName() + "\n ");
		}
	}
	
	
}
