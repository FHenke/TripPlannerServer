/**
 * 
 */
package database.updateTables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdom2.JDOMException;

import api.SkyscannerCache;


/**
 * @author Florian
 *
 */
public class UpdateFlightConnections extends UpdateTable {

	/**
	 * @param conn
	 */
	public UpdateFlightConnections(Connection conn) {
		super(conn);
	}

	/**
	 * proceeds an update on the flight_connections Table from the Database by Using the Skyscaner API cached connections
	 */
	@Override
	public void proceed() throws IOException {
		AtomicInteger counter = new AtomicInteger();
		int process = 0;
		try {
			//Get list of all connections that are cached by Skyscanner
			SkyscannerCache skyCache = new SkyscannerCache();
			Collection<utilities.Connection> connectionList = skyCache.getFlightMap();
			
	
	
			
			//PreparedStatements precompile the Statement and executes it then with different values, therefor runtime improvement
			try {
				PreparedStatement selectIdenticalEntries = conn.prepareStatement("SELECT * FROM flight_connections WHERE origin = ? AND destination = ? AND departure_date = ? AND (min_price = ? OR (min_price IS NULL AND ? IS NULL)) AND quote_date_time = ? AND (weekday = ? OR (weekday IS NULL AND ? IS NULL));");
				//The idea is just to distinguish between different weekdays not between every date,
				//that means it is possible that for every connection is a single entry for every weekday in the database
				PreparedStatement selectId = conn.prepareStatement("SELECT * FROM flight_connections WHERE origin = ? AND destination = ? AND weekday = ?;");
				PreparedStatement deleteEntry = conn.prepareStatement("DELETE FROM flight_connections WHERE origin = ? AND destination = ? AND weekday = ?;");
				PreparedStatement insertEntry = conn.prepareStatement("INSERT INTO flight_connections VALUES (?, ?, ?, ?, ?, ?);");
		
			
			
				//iterates over all places in placelist to add them to the database if not already existing
				for(utilities.Connection connection : connectionList){
					//ToDo: remove
					if((int) (counter.getAndIncrement() * 100 / connectionList.size()) > process){
						process = counter.incrementAndGet() * 100 / connectionList.size();
						System.out.println("save: " + process + "%");
					}
					try{
						//check if the exact entry exists in the database already
						selectIdenticalEntries.setString(1, connection.getOrigin().getId());
						selectIdenticalEntries.setString(2, connection.getDestination().getId());
						selectIdenticalEntries.setTimestamp(3, new java.sql.Timestamp(connection.getDepartureDate().getTimeInMillis()));
						selectIdenticalEntries.setDouble(4, connection.getPrice());
						selectIdenticalEntries.setDouble(5, connection.getPrice());
						selectIdenticalEntries.setTimestamp(6, new java.sql.Timestamp(connection.getQuoteDateTime().getTime()));
						selectIdenticalEntries.setInt(7, connection.getWeekday());
						selectIdenticalEntries.setInt(8, connection.getWeekday());
						if(!super.isQuerryEmpty(selectIdenticalEntries)){
							//if not check if the id (origin, destination, weekday) exists in the database already
							selectId.setString(1, connection.getOrigin().getId());
							selectId.setString(2, connection.getDestination().getId());
							selectId.setInt(3, connection.getWeekday());
							if(super.isQuerryEmpty(selectId)){
								//if yes drop the old entry for adding the new one later
								deleteEntry.setString(1, connection.getOrigin().getId());
								deleteEntry.setString(2, connection.getDestination().getId());
								deleteEntry.setInt(3, connection.getWeekday());
								deleteEntry.executeUpdate();
							}
							//if not add the new dataset
							insertEntry.setString(1, connection.getOrigin().getId());
							insertEntry.setString(2, connection.getDestination().getId());
							insertEntry.setTimestamp(3, new java.sql.Timestamp(connection.getDepartureDate().getTimeInMillis()));
							insertEntry.setDouble(4, connection.getPrice());
							insertEntry.setTimestamp(5, new java.sql.Timestamp(connection.getQuoteDateTime().getTime()));
							insertEntry.setInt(6, connection.getWeekday());
							insertEntry.executeUpdate();
						}
					}catch(SQLException e){
						logger.warn("Problem by writing following Airport to Database:" + connection.getOrigin().getId() + " : " + connection.getDestination().getId() + "   - " + e.toString());
					}
				}
			} catch (SQLException e) {
				logger.warn("Problem by writing updated Airport to Database - " + e.toString());
			}
		} catch (JDOMException | IOException e) {
			logger.warn("Problem by reading or loading the XML file from the Skyscanner API - " + e.toString());
		}

	}

}
