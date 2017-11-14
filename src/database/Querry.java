package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PSQLException;

import database.updateTables.UpdateContinents;
import utilities.Place;

public class Querry {

	
	protected static final Logger logger = LogManager.getLogger(Querry.class);
	
	protected Connection conn = null;
	
	public Querry(Connection conn){
		this.conn = conn;
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
	public Place setAirportinformationFromDatabase(Place airport) throws SQLException{

		ResultSet querryResult = conn.createStatement().executeQuery("SELECT ST_Y(location) As latitude, ST_X(location) As longitude, name FROM airports WHERE iata_code = '" + airport.getIata().toUpperCase() + "';");
		querryResult.next();
		airport.setLatitude(querryResult.getDouble("latitude"));
		airport.setLongitude(querryResult.getDouble("longitude"));
		airport.setName(querryResult.getString("name"));
		
		return airport;
	}
}
