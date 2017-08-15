/**
 * 
 */
package database.updateTables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

import org.jdom2.JDOMException;

import api.SkyscannerCache;
import utilities.Place;

/**
 * @author Florian
 *
 */
public class UpdateAirports extends UpdateTable {

	public UpdateAirports(Connection conn){
		super(conn);
	}
	
	/* (non-Javadoc)
	 * @see database.updateTables.UpdateTable#proceed()
	 */
	@Override
	public void proceed() throws IOException {
		try {
			//Get list of all Places thats are Countries from Skyscanner
			SkyscannerCache skyCache = new SkyscannerCache();
			LinkedList<Place> placeList = skyCache.getPlaceList(Place.PlaceTypeToSkyscannerString(Place.AIRPORT));
	
	
			
			//PreparedStatements precompile the Statement and executes it then with different values, therefor runtime improvement
			try {
				PreparedStatement selectIdenticalEntries = conn.prepareStatement("SELECT * FROM airports WHERE airport_id = ? AND city_id = ? AND (iata_code = ? OR (iata_code IS NULL AND ? IS NULL)) AND name = ? AND ST_X(location) = ? AND ST_Y(location) = ?;");
				PreparedStatement selectId = conn.prepareStatement("SELECT * FROM airports WHERE airport_id = ?;");
				PreparedStatement insertEntry = conn.prepareStatement("INSERT INTO airports VALUES (?, ?, ?, ?, ST_GeomFromText(?, -1));");
				PreparedStatement updateEntry = conn.prepareStatement("UPDATE airports SET city_id = ?, iata_code = ?, name = ?, location = ST_GeomFromText(?, -1) WHERE airport_id = ?;");

			
			
				//iterates over all places in placelist to add them to the database if not already existing
				for(Place place : placeList){
					try{
						//check if the exact entry exists in the database already
						selectIdenticalEntries.setString(1, place.getId());
						selectIdenticalEntries.setString(2, place.getCity());
						selectIdenticalEntries.setString(3, place.getIata());
						selectIdenticalEntries.setString(4, place.getIata());
						selectIdenticalEntries.setString(5, place.getName());
						selectIdenticalEntries.setDouble(6, place.getLatitude());
						selectIdenticalEntries.setDouble(7, place.getLongitude());
						if(!super.isQuerryEmpty(selectIdenticalEntries)){
							//if not check if the id exists in the database already
							selectId.setString(1, place.getId());
							if(super.isQuerryEmpty(selectId)){
								//if yes drop the old entry for adding the new one later
								updateEntry.setString(1, place.getCity());
								updateEntry.setString(2, place.getIata());
								updateEntry.setString(3, place.getName());
								updateEntry.setString(4, "Point(" + Double.toString(place.getLatitude()) + " " + Double.toString(place.getLongitude()) + ")" );
								updateEntry.setString(5, place.getId());
								updateEntry.executeUpdate();
							}else{
								//if not add the new dataset
								insertEntry.setString(1, place.getId());
								insertEntry.setString(2, place.getCity());
								insertEntry.setString(3, place.getIata());
								insertEntry.setString(4, place.getName());
								insertEntry.setString(5, "Point(" + Double.toString(place.getLatitude()) + " " + Double.toString(place.getLongitude()) + ")" );
								insertEntry.executeUpdate();
							}
						}
					}catch(SQLException e){
						logger.warn("Problem by writing following Airport to Database:" + place.getName() + "   - " + e.toString());
						throw new IOException("Problem by writing updated Airport to Database - " + e.toString());
					}
				}
			} catch (SQLException e) {
				logger.warn("Problem by writing updated Airport to Database - " + e.toString());
				throw new IOException("Problem by writing updated Airport to Database - " + e.toString());
			}
		} catch (JDOMException | IOException e) {
			logger.warn("Problem by reading or loading the XML file from the Skyscanner API - " + e.toString());
			throw new IOException("Problem by reading or loading the XML file from the Skyscanner API - " + e.toString());
		}

	}

}
