package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.updateTables.UpdateContinents;
import utilities.Place;

public class Querry {

	
	protected static final Logger logger = LogManager.getLogger(UpdateContinents.class);
	
	protected Connection conn = null;
	
	public Querry(Connection conn){
		this.conn = conn;
	}
	
	public double getLatitudeFromPlace(Place place){
		
		double latitude = Double.MAX_VALUE;
		
		try {
			
			ResultSet querryResult = conn.createStatement().executeQuery("SELECT ST_Y(location) As latitude FROM airports WHERE iata_code = '" + place.getIata().toUpperCase() + "';");
			querryResult.next();
			latitude = querryResult.getDouble("latitude");
		} catch (SQLException e) {
			System.out.println("Querry Fails");
			e.printStackTrace();
		}
		
		return latitude;
	}

	
	public double getLongitudeFromPlace(Place place){
		
		double longitude = Double.MAX_VALUE;
		
		try {
			ResultSet querryResult = conn.createStatement().executeQuery("SELECT ST_X(location) As longitude FROM airports WHERE iata_code = '" + place.getIata().toUpperCase() + "';");
			querryResult.next();
			longitude = querryResult.getDouble("longitude");
		} catch (SQLException e) {
			System.out.println("Querry Fails");
			e.printStackTrace();
		}
		
		return longitude;
	}
}
