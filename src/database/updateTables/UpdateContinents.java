package database.updateTables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

import org.jdom2.JDOMException;

import api.SkyscannerCache;
import utilities.*;

public class UpdateContinents extends UpdateTable{
	
	public UpdateContinents(Connection conn){
		super(conn);
	}
	
	/**
	 * proceeds an update on the Continent Table from the Database by Using the Skyscaner API for all Places
	 */
	@Override
	public void proceed() throws IOException{
		try {
			SkyscannerCache skyCache = new SkyscannerCache();
			LinkedList<Place> placeList = skyCache.getPlaceList(Place.PlaceTypeToSkyscannerString(Place.CONTINENT));
	
	
			
			//PreparedStatements precompile the Statement and executes it then with different values, therefor runtime improvement
			try {
				PreparedStatement selectIdenticalEntries = conn.prepareStatement("SELECT * FROM continents WHERE continent_id = ? AND name = ?;");
				PreparedStatement selectId = conn.prepareStatement("SELECT * FROM continents WHERE continent_id = ?;");
				PreparedStatement updateEntry = conn.prepareStatement("UPDATE continents SET name = ? WHERE continent_id = ?;");
				PreparedStatement insertEntry = conn.prepareStatement("INSERT INTO continents VALUES (?, ?);");
		
			
			
				//iterates over all places in placelist to add them to the database if not already existing
				for(Place place : placeList){
					try{
						//check if the exact entry exists in the database already
						selectIdenticalEntries.setString(1, place.getId());
						selectIdenticalEntries.setString(2, place.getName());
						if(!super.isQuerryEmpty(selectIdenticalEntries)){
							//if not check if the id exists in the database already
							selectId.setString(1, place.getId());
							if(super.isQuerryEmpty(selectId)){
								//if yes update the id with the new dataset to the id
								updateEntry.setString(1, place.getName());
								updateEntry.executeUpdate();
							}
							else{
								//if not add this dataset
								insertEntry.setString(1, place.getId());
								insertEntry.setString(2, place.getName());
								insertEntry.executeUpdate();
							}
						}
					}catch(SQLException e){
						logger.warn("Problem by writing following Continent to Database:" + place.getName() + "   - " + e.toString());
						throw new IOException("Problem by writing updated Country to Database - " + e.toString());
					}
				}
			} catch (SQLException e) {
				logger.warn("Problem by writing updated Continents to Database - " + e.toString());
				throw new IOException("Problem by writing updated Continents to Database - " + e.toString());
			}
		} catch (JDOMException | IOException e) {
			logger.warn("Problem by reading or loading the the XML file from the Skyscanner API - " + e.toString());
			throw new IOException("Problem by reading or loading the the XML file from the Skyscanner API - " + e.toString());
		}
	}
	
	public void updateCountries(){
		
	}
	
	public void updateCities(){
		
	}
	
	public void updateAirports(){
		
	}
	
	public void updateFlightConnections(){
		
	}
	
	
}
