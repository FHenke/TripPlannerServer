package database.updateTables;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdom2.JDOMException;

import api.EStream;
import api.SkyscannerCache;
import database.DatabaseConnection;
import database.Querry;
import utilities.Connection;

public class UpdateFlights extends UpdateTable {

	public UpdateFlights(java.sql.Connection conn) {
		super(conn);
	}

	@Override
	public void proceed() throws IOException {
		AtomicInteger counter = new AtomicInteger();
		int process = 0;
		try {
			//Get list of all connections that are cached by Skyscanner
			EStream eStream = new EStream();
			Querry querry = new Querry(new DatabaseConnection().getConnection());
			String[][] connectionList = querry.getAllAvailableConnections();
			
			counter.set(querry.getStatusOfUpdateFlights());
	
			
			//PreparedStatements precompile the Statement and executes it then with different values, therefor runtime improvement
			try {
				PreparedStatement selectIdenticalEntries = conn.prepareStatement("SELECT * FROM flight_connections WHERE origin = ? AND destination = ? AND departure_date = ? AND flightnumber = ? AND min_price = ? AND weekday = ? AND duration = ? AND currency = ? AND operating_airline = ? AND quote_date_time = ?;");
				PreparedStatement selectId = conn.prepareStatement("SELECT * FROM flight_connections WHERE origin = ? AND destination = ? AND departure_date = ? AND operating_airline = ? AND duration = ?;");
				PreparedStatement deleteEntry = conn.prepareStatement("DELETE FROM flight_connections WHERE WHERE origin = ? AND destination = ? AND departure_date = ? AND operating_airline = ? AND duration = ?;");
				PreparedStatement insertEntry = conn.prepareStatement("INSERT INTO flight_connections VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
				PreparedStatement updateStatus = conn.prepareStatement("UPDATE states SET status = ? WHERE process = 'update_flights';");
		
			
			
				//iterates over all places in placelist to add them to the database if not already existing
				while(counter.get() < connectionList.length){
					String[] connection = connectionList[counter.get()];
					//TODO: remove
					//Status output
					if((int) (counter.get() * 100 / connectionList.length) > process){
						process = counter.get() * 100 / connectionList.length;
						System.out.println("save: " + process + "%");
					}
					
					System.out.println("Step: " + counter.get());
					if(counter.get() == 1800)
						return;
					
					LinkedBlockingQueue<Connection> flightList = eStream.getAllDirectFlights(connection[0], connection[1], new GregorianCalendar(2018, 03, 05, 0, 0, 0));
					
					if(flightList == null)
						return;
					
					if(flightList.isEmpty())
						System.out.println("No connection found");
					else
						System.out.println("--> connection found");
					
					//Iterates over all flights found for this connection
					for(Connection flight : flightList){
						try{
							//check if the exact entry exists in the database already
							selectIdenticalEntries.setString(1, flight.getOrigin().getIata());
							selectIdenticalEntries.setString(2, flight.getDestination().getIata());
							selectIdenticalEntries.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
							selectIdenticalEntries.setString(4, flight.getSummary());
							selectIdenticalEntries.setDouble(5, flight.getPrice());
							selectIdenticalEntries.setInt(6, flight.getWeekday());
							selectIdenticalEntries.setInt(7, (int) flight.getDuration().getMillis());
							selectIdenticalEntries.setString(8, flight.getCurrency());
							selectIdenticalEntries.setString(9, flight.getCarrier().getCarrierName());
							selectIdenticalEntries.setTimestamp(10, new java.sql.Timestamp(flight.getQuoteDateTime().getTime()));
							if(!super.isQuerryEmpty(selectIdenticalEntries)){
								//if not check if the id (origin, destination, weekday) exists in the database already
								selectId.setString(1, flight.getOrigin().getIata());
								selectId.setString(2, flight.getDestination().getIata());
								selectId.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
								selectId.setString(4, flight.getCarrier().getCarrierName());
								selectId.setInt(5, (int) flight.getDuration().getMillis());
								if(super.hasQuerryResults(selectId) && flight.hasPrice()){
									//if yes drop the old entry for adding the new one later
									deleteEntry.setString(1, flight.getOrigin().getIata());
									deleteEntry.setString(2, flight.getDestination().getIata());
									deleteEntry.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
									deleteEntry.setString(4, flight.getCarrier().getCarrierName());
									deleteEntry.setInt(5, (int) flight.getDuration().getMillis());
									deleteEntry.executeUpdate();
								}
								//if not add the new dataset
								if(!super.hasQuerryResults(selectId) || flight.hasPrice()){
									insertEntry.setString(1, flight.getOrigin().getIata());
									insertEntry.setString(2, flight.getDestination().getIata());
									insertEntry.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
									insertEntry.setDouble(4, flight.getPrice());
									insertEntry.setTimestamp(5, new java.sql.Timestamp(flight.getQuoteDateTime().getTime()));
									insertEntry.setInt(6, flight.getWeekday());
									insertEntry.setString(7, flight.getSummary());
									insertEntry.setTimestamp(8, new java.sql.Timestamp(flight.getArrivalDate().getTimeInMillis()));
									insertEntry.setInt(9, (int) flight.getDuration().getMillis());
									insertEntry.setString(10, flight.getCurrency());
									insertEntry.setString(11, flight.getCarrier().getCarrierName());
									insertEntry.executeUpdate();
									//System.out.println("Inserted entry for: (" + flight.getOrigin().getName() + "-" + flight.getDestination().getName() + ")");
								}
							}
						}catch(SQLException e){
							logger.warn("Problem by writing following Airport to Database:" + flight.getOrigin().getId() + " : " + flight.getDestination().getId() + "   - " + e.toString());
						}
					}
					//sets the state for this progress in the database
					updateStatus.setString(1, "" + counter.incrementAndGet());
					updateStatus.executeUpdate();
				}
			} catch (SQLException e) {
				logger.warn("Problem by writing updated Airport to Database - " + e.toString());
			}
		} catch (IOException | SQLException e) {
			logger.warn("Problem by reading or loading the XML file from the Skyscanner API - " + e.toString());
		}


	}

}
