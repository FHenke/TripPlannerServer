package database;

import java.sql.ResultSet;
import java.sql.SQLException;
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

		ResultSet querryResult = conn.createStatement().executeQuery("SELECT ST_Y(location) As latitude, ST_X(location) As longitude, name FROM airports WHERE iata_code = '" + airport.getIata().toUpperCase() + "';");
		querryResult.next();
		airport.setLatitude(querryResult.getDouble("latitude"));
		airport.setLongitude(querryResult.getDouble("longitude"));
		airport.setName(querryResult.getString("name"));
		
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
		int counter = 0;
		
		//get the number of connections and create an array with this size
		querryResult = conn.createStatement().executeQuery("SELECT count(*) AS size FROM (SELECT distinct origin, destination FROM flight_connections) AS distinctConnections;");
		querryResult.next();
		String[][] result = new String[querryResult.getInt("size")][2];
		
		//get All distinct connections from the Database
		querryResult = conn.createStatement().executeQuery("SELECT DISTINCT origin, destination FROM flight_connections;");
		
		//Write the Iata code from each received connection to the StringArray
		while(querryResult.next()){
			result[counter][0] = querryResult.getString("origin");
			result[counter][1] = querryResult.getString("destination");
			counter++;
		}
			
		return result;
		
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
}
