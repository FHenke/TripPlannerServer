/**
 * 
 */
package database.updateTables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;


import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import api.SkyscannerCache;
import utilities.*;

public class UpdateCountries extends UpdateTable{
	
	public UpdateCountries(Connection conn){
		super(conn);
	}
	
	/**
	 * proceeds an update on the Countries Table from the Database by Using the Skyscaner API for all Places
	 */
	@Override
	public void proceed() throws IOException{
		try {
			//Get list of all Places thats are Countries from Skyscanner
			SkyscannerCache skyCache = new SkyscannerCache();
			LinkedList<Place> placeList = skyCache.getPlaceList(Place.PlaceTypeToSkyscannerString(Place.COUNTRY));
	
	
			
			//PreparedStatements precompile the Statement and executes it then with different values, therefor runtime improvement
			try {
				PreparedStatement selectIdenticalEntries = conn.prepareStatement("SELECT * FROM countries WHERE country_id = ? AND name = ? AND (currency = ?  OR (currency IS NULL AND ? IS NULL)) AND (language = ? OR (language IS NULL AND ? IS NULL)) AND continent_id = ?;");
				PreparedStatement selectId = conn.prepareStatement("SELECT * FROM countries WHERE country_id = ?;");
				PreparedStatement insertEntry = conn.prepareStatement("INSERT INTO countries VALUES (?, ?, ?, ?, ?);");
				PreparedStatement updateEntry = conn.prepareStatement("UPDATE cities SET name = ?, currency = ?, language = ?, continent_id = ? WHERE country_id = ?;");
		
			
			
				//iterates over all places in placelist to add them to the database if not already existing
				for(Place place : placeList){
					try{
						//check if the exact entry exists in the database already
						selectIdenticalEntries.setString(1, place.getId());
						selectIdenticalEntries.setString(2, place.getName());
						selectIdenticalEntries.setString(3, place.getCurrency());
						selectIdenticalEntries.setString(4, place.getCurrency());
						selectIdenticalEntries.setString(5, place.getLanguage());
						selectIdenticalEntries.setString(6, place.getLanguage());
						selectIdenticalEntries.setString(7, place.getContinent());
						if(!super.isQuerryEmpty(selectIdenticalEntries)){
							//if not, check if the id exists in the database already
							selectId.setString(1, place.getId());
							if(super.isQuerryEmpty(selectId)){
								//if yes delete the Entry for inserting th new one afterwards
								updateEntry.setString(1, place.getName());
								updateEntry.setString(2, place.getCurrency());
								updateEntry.setString(3, place.getLanguage());
								updateEntry.setString(4, place.getContinent());
								updateEntry.setString(5, place.getId());
								updateEntry.executeUpdate();
							}else{
								//if not add the dataset to the table
								insertEntry.setString(1, place.getId());
								insertEntry.setString(2, place.getName());
								insertEntry.setString(3, place.getCurrency());
								insertEntry.setString(4, place.getLanguage());
								insertEntry.setString(5, place.getContinent());
								insertEntry.executeUpdate();
							}
						}
					}catch(SQLException e){
						logger.warn("Problem by writing following Country to Database:" + place.getName() + "   - " + e.toString());
						throw new IOException("Problem by writing updated Country to Database - " + e.toString());
					}
				}
			} catch (SQLException e) {
				logger.warn("Problem by writing updated Country to Database - " + e.toString());
				throw new IOException("Problem by writing updated Countries to Database - " + e.toString());
			}
		} catch (JDOMException | IOException e) {
			logger.warn("Problem by reading or loading the XML file from the Skyscanner API - " + e.toString());
			throw new IOException("Problem by reading or loading the XML file from the Skyscanner API - " + e.toString());
		}
	}
}