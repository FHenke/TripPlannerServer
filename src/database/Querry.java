package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PSQLException;

import database.updateTables.UpdateContinents;
import utilities.Connection;
import utilities.Place;

public class Querry {

	
	protected static final Logger logger = LogManager.getLogger(Querry.class);
	
	protected static java.sql.Connection conn = null;
	
	public Querry(java.sql.Connection conn){
		this.conn = conn;
	}
	
	public Querry(){
		this.conn = DatabaseConnection.getConnection();
	}
	
	public double getLatitudeFromPlace(Place place) throws PSQLException, SQLException{
		double latitude = Double.MAX_VALUE;
			
			ResultSet querryResult = conn.createStatement().executeQuery("SELECT ST_Y(location) As latitude FROM airports WHERE iata_code = '" + place.getIata().toUpperCase() + "';");
			querryResult.next();
			latitude = querryResult.getDouble("latitude");
		
		return latitude;
	}

	
	public double getLongitudeFromPlace(Place place) throws PSQLException, SQLException{
		
		double longitude = Double.MAX_VALUE;

			ResultSet querryResult = conn.createStatement().executeQuery("SELECT ST_X(location) As longitude FROM airports WHERE iata_code = '" + place.getIata().toUpperCase() + "';");
			querryResult.next();
			longitude = querryResult.getDouble("longitude");
		
		return longitude;
	}
	
	/**
	 * Sets atitude, longitude and airport name to a pPlace object
	 * @param airport Place objects which containes a IATA code
	 * @return Place object where latitude, longitude and airport name was added
	 * @throws SQLException Thrown if no entry for this IATA code was found
	 */
	public static Place setAirportinformationFromDatabase(Place airport) throws SQLException{
		
		if(conn == null){
			conn = DatabaseConnection.getConnection();
		}
		try{
			ResultSet querryResult = conn.createStatement().executeQuery("SELECT ST_Y(location) As latitude, ST_X(location) As longitude, name FROM airports WHERE iata_code = '" + airport.getIata().toUpperCase() + "';");
			querryResult.next();
			airport.setLatitude(querryResult.getDouble("latitude"));
			airport.setLongitude(querryResult.getDouble("longitude"));
			airport.setName(querryResult.getString("name") + " Airport");
		}catch(SQLException e){
			//logger.warn("Error 234: Unable to get Information of Airport (" + airport.getId() + ") because of SQL Exception: " + e.toString());
			throw new SQLException("Airport not in Databse");
		}
		return airport;
	}
	
	/**
	 * Generates an array with all connections available in the Database (kind of a Flightmap)
	 * each connection represents a pair of origin and destination in the form ORIGIN_IATADESTINATIONIATA
	 * (for example: FRAHAM for the connectiion Frankfurt to Hamburg)
	 * @return Array of unique connections.
	 * @throws SQLException
	 */
	public String[][] getAllAvailableConnections() throws SQLException{
		ResultSet querryResult;
		int size = 0;
		String query = "SELECT DISTINCT origin, destination FROM flight_connections;";
		
		//get the number of connections and create an array with this size
		size = getSizeOfResult(query);
		
		//get All distinct connections from the Database
		querryResult = conn.createStatement().executeQuery(query);
		
		return getConnectionStringArray(querryResult, size);
	}
	
	/**
	 * Generates an array with all connections available in the Database (kind of a Flightmap)
	 * each connection represents a pair of origin and destination in the form ORIGIN_IATADESTINATIONIATA
	 * (for example: FRAHAM for the connectiion Frankfurt to Hamburg)
	 * @return Array of unique connections.
	 * @throws SQLException
	 */
	public String[][] getAllConnectionsWhithouDuration(GregorianCalendar date) throws SQLException{
		ResultSet querryResult;
		int size = 0;
		String query = "SELECT distinct origin, destination FROM ( SELECT origin, destination, min_price, duration, CAST(CAST(departure_date AS date) AS timestamp) AS departure_date FROM flight_connections ) AS x WHERE departure_date = '" + date.get(1) + "-" + (date.get(2) + 1) + "-" + date.get(5) + " 00:00:00' AND duration is null;";
		
		System.out.println(query);
		
		//get the number of connections and create an array with this size
		size = getSizeOfResult(query);
		
		//get All distinct connections from the Database
		querryResult = conn.createStatement().executeQuery(query);
		
		return getConnectionStringArray(querryResult, size);
	}
	
	
	/**
	 * gets a Result set and generates an Array with the origin and destination from it
	 * @param resultSet result set must have one column for origin and one for destination
	 * @param size amount of entries of the result set
	 * @return Array with origin and destination pair in each line
	 * @throws SQLException
	 */
	private String[][] getConnectionStringArray(ResultSet resultSet, int size) throws SQLException{
		String[][] result = new String[size][2];
		int counter = 0;
		//Write the Iata code from each received connection to the StringArray
		while(resultSet.next()){
			result[counter][0] = resultSet.getString("origin");
			result[counter][1] = resultSet.getString("destination");
			counter++;
		}
		return result;
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	private int getSizeOfResult(String query) throws SQLException{
		int size = 0;
		ResultSet querryResult;

		querryResult = conn.createStatement().executeQuery("SELECT count(*) AS size FROM (" + query.substring(0, query.length()-1) + ") AS query;");
		querryResult.next();
		size = querryResult.getInt("size");
		
		return size;
	}
	
	/**
	 * Gets the Status counter of update flights from the state table
	 * to save the actual position
	 * @return status of update Flights
	 * @throws SQLException
	 */
	public int getStatusOfUpdateFlights() throws SQLException{
		ResultSet querryResult = conn.createStatement().executeQuery("SELECT status FROM states WHERE process = 'update_flights';");
		querryResult.next();
		return querryResult.getInt("status");
	}
	
	/**
	 * returns the next unused number that can be used as connection number (for number the connected flights consecutively)
	 * searches the max number and increase it by one
	 * @return next valid number for connected flights
	 * @throws SQLException
	 */
	public static int getNextConnectionNumber() throws SQLException{
		ResultSet querryResult = conn.createStatement().executeQuery("select max(connection_number) from connected_flight_numbers;");
		querryResult.next();
		return querryResult.getInt("max") + 1;
	}
	
}
