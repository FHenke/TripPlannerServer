package database.updateTables;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


import api.EStream;
import database.DatabaseConnection;
import database.Querry;
import utilities.Connection;

public class UpdateFlights extends UpdateTable {
	
	
	//PreparedStatements precompile the Statement and executes it then with different values, therefor runtime improvement
	PreparedStatement selectIdenticalDirectEntries = null;
	PreparedStatement selectConnectedEntries = null;
	PreparedStatement selectId = null;
	PreparedStatement deleteSkyscannerEntries = null;
	PreparedStatement deleteDirectEntry = null;
	PreparedStatement deleteConnectedEntry = null;
	PreparedStatement deleteConnectedFlightNumbers = null;
	PreparedStatement insertDirectEntry = null;
	PreparedStatement insertConnectedEntry = null;
	PreparedStatement insertConnectedFlights = null;
	PreparedStatement updateStatus = null;

	public UpdateFlights(java.sql.Connection conn) {
		super();
		
		try {
			selectIdenticalDirectEntries = conn.prepareStatement("SELECT * FROM flight_connections WHERE origin = ? AND destination = ? AND departure_date = ? AND flightnumber = ? AND min_price = ? AND weekday = ? AND duration = ? AND currency = ? AND operating_airline = ? AND quote_date_time = ? AND connection_number is null;");
			selectConnectedEntries = conn.prepareStatement("SELECT * FROM flight_connections WHERE origin = ? AND destination = ? AND departure_date = ? AND arrival_time = ? AND connection_number is not null;");
			selectId = conn.prepareStatement("SELECT * FROM flight_connections WHERE origin = ? AND destination = ? AND departure_date = ? AND operating_airline = ? AND arrival_time = ?;");
			deleteSkyscannerEntries = conn.prepareStatement("DELETE FROM flight_connections WHERE origin = ? AND destination = ? AND departure_date = ? AND duration is null;");
			deleteDirectEntry = conn.prepareStatement("DELETE FROM flight_connections WHERE origin = ? AND destination = ? AND departure_date = ? AND operating_airline = ? AND arrival_time = ?;");
			deleteConnectedEntry = conn.prepareStatement("DELETE FROM flight_connections WHERE connection_number = ?;");
			deleteConnectedFlightNumbers = conn.prepareStatement("DELETE FROM connected_flight_numbers WHERE connection_number = ?;");
			insertDirectEntry = conn.prepareStatement("INSERT INTO flight_connections VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			insertConnectedEntry = conn.prepareStatement("INSERT INTO flight_connections VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			insertConnectedFlights = conn.prepareStatement("INSERT INTO connected_flight_numbers VALUES (?, ?, ?, ?);");
			updateStatus = conn.prepareStatement("UPDATE states SET status = ? WHERE process = 'update_flights';");
		} catch (SQLException e) {
			logger.warn("Not able to generate prepared statements in class UpdateFlights - " + e.toString());
		}
		
	}

	@Override
	public void proceed() throws IOException {
		AtomicInteger counter = new AtomicInteger();
		int process = 0;
		int addedConnections = 0;
		int addedSubconnections = 0;
		try {
			//Get list of all connections that are cached by Skyscanner
			EStream eStream = new EStream();
			Querry querry = new Querry(DatabaseConnection.getConnection());
			String[][] connectionList = querry.getAllConnectionsWhithouDuration(UpdateDatabase.getUpdateDate());
			
			counter.set(0);

			try {			
			
				//iterates over all places in placelist to add them to the database if not already existing
				while(counter.get() < connectionList.length){
					String[] connection = connectionList[counter.get()];
					//TODO: remove
					//Status output
					if((int) (counter.get() * 100 / connectionList.length) > process){
						process = counter.get() * 100 / connectionList.length;
						UpdateDatabase.setStatus("save: " + process + "% (" + addedConnections + " [" + addedSubconnections + "] new connections / " + counter.get() + " requests)");
						System.out.println("save: " + process + "% (" + addedConnections + " [" + addedSubconnections + "] new connections / " + counter.get() + " requests)");
					}
					
					//System.out.println("Step: " + counter.get());
					if(counter.get() == 26000)
						return;
					
					// get the Connection from the API
					LinkedBlockingQueue<Connection> flightList = eStream.getAllConnections(connection[0], connection[1], UpdateDatabase.getUpdateDate(), null);
					
					if(flightList == null)
						return;	
					
					if(!flightList.isEmpty())
						addedConnections++;
					
					//Iterates over all flights found for this connection
					for(Connection flight : flightList){
						addedSubconnections++;
						addConnectionToDatabase(flight);
					}
					//sets the state for this progress in the database
					updateStatus.setString(1, "" + counter.incrementAndGet());
					updateStatus.executeUpdate();
				}
			} catch (SQLException e) {
				logger.warn("Problem by writing connection to Database - " + e.toString());
			}
		} catch (IOException | SQLException e) {
			logger.warn("Problem by reading or loading the XML file from the Skyscanner API - " + e.toString());
		}
	}
	
	
	private void addConnectionToDatabase(Connection flight) throws SQLException{
		
		if(!flight.isDirect()){
			int connectionNumber = Querry.getNextConnectionNumber();
			int partPosition = 1; // describes on which position this part of the connection has to be
			for(Connection subConnection : flight.getSubConnections()){
				addConnectionToDatabase(subConnection);
				if(!isFlightInDatabase(flight, connectionNumber))
					addConnectionFlight(connectionNumber, partPosition, subConnection.getDepartureDate(), subConnection.getSummary());
				partPosition++;
			}
			addFlightToDatabase(flight, connectionNumber);
		}
		
		addFlightToDatabase(flight, -1);
		
	}
	
	
	private void addFlightToDatabase(Connection flight, int connectionNumber){
		try{	
			// in this first step the result will be deletet to write a new one to the database in the next step
			// a result should only be overwritten if:
			// -> the new result has a price and the old one not
			// -> the price of the new result is cheaper than the price of the old result
			// -> the new result has a price and the old result is older than one day (also if the price is even higher)
			boolean replace = false;
			if(shouldEntrieOverwritten(flight, connectionNumber)){
				//if yes drop the old entry for adding the new one later
				deletOldEntriesFromDatabase(flight, connectionNumber);
				replace = true;
			}
			//a new result should be written to the database if:
			// -> replace is true
			// -> this flight is not in the database
			if(!isFlightInDatabase(flight, connectionNumber) || replace == true){
				
				
				if(connectionNumber == -1){
					insertDirectEntry.setString(1, flight.getOrigin().getIata());
					insertDirectEntry.setString(2, flight.getDestination().getIata());
					insertDirectEntry.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
					insertDirectEntry.setDouble(4, flight.getPrice());
					insertDirectEntry.setTimestamp(5, new java.sql.Timestamp(flight.getQuoteDateTime().getTime()));
					insertDirectEntry.setInt(6, flight.getWeekday());
					insertDirectEntry.setString(7, flight.getSummary());
					insertDirectEntry.setTimestamp(8, new java.sql.Timestamp(flight.getArrivalDate().getTimeInMillis()));
					insertDirectEntry.setInt(9, (int) flight.getDuration().getMillis());
					insertDirectEntry.setString(10, flight.getCurrency());
					insertDirectEntry.setString(11, flight.getFirstCarrier().getCarrierName());
					insertDirectEntry.executeUpdate();
				}
				else{
					insertConnectedEntry.setString(1, flight.getOrigin().getIata());
					insertConnectedEntry.setString(2, flight.getDestination().getIata());
					insertConnectedEntry.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
					insertConnectedEntry.setDouble(4, flight.getPrice());
					insertConnectedEntry.setTimestamp(5, new java.sql.Timestamp(flight.getQuoteDateTime().getTime()));
					insertConnectedEntry.setInt(6, flight.getWeekday());
					insertConnectedEntry.setString(7, null);
					insertConnectedEntry.setTimestamp(8, new java.sql.Timestamp(flight.getArrivalDate().getTimeInMillis()));
					insertConnectedEntry.setInt(9, (int) flight.getDuration().getMillis());
					insertConnectedEntry.setString(10, flight.getCurrency());
					insertConnectedEntry.setString(11, flight.getFirstCarrier().getCarrierName());
					insertConnectedEntry.setInt(12, connectionNumber);
					insertConnectedEntry.executeUpdate();
				}	
				//System.out.println("Inserted entry for: (" + flight.getOrigin().getName() + "-" + flight.getDestination().getName() + ")");
			}

			if(connectionNumber == -1){
				GregorianCalendar gregTmp = new GregorianCalendar(flight.getDepartureDate().get(1), flight.getDepartureDate().get(2), flight.getDepartureDate().get(5), 0, 0, 0);
				//Delete The entry without a departure and arrival time
				deleteSkyscannerEntries.setString(1, flight.getOrigin().getIata());
				deleteSkyscannerEntries.setString(2, flight.getDestination().getIata());
				deleteSkyscannerEntries.setTimestamp(3, new java.sql.Timestamp(gregTmp.getTimeInMillis()));
				deleteSkyscannerEntries.executeUpdate();
			}
		}catch(SQLException e){
			logger.warn("Problem by writing following Airport to Database: " + flight.getOrigin().getId() + " : " + flight.getDestination().getId() + " ([" + connectionNumber + "]" +  flight.getSummary() + ")   - " + e.toString());
		}
	}
	
	
	private void addConnectionFlight(int connectionNumber, int partPosition, GregorianCalendar departureDate, String flightNumber) throws SQLException{
		insertConnectedFlights.setInt(1, connectionNumber);
		insertConnectedFlights.setInt(2, partPosition);
		insertConnectedFlights.setString(3, flightNumber);
		insertConnectedFlights.setTimestamp(4, new java.sql.Timestamp(departureDate.getTimeInMillis()));
		insertConnectedFlights.executeUpdate();
	}

	
	private PreparedStatement prepareSelectIdenticalDirectEntries(Connection flight) throws SQLException{
		selectIdenticalDirectEntries.setString(1, flight.getOrigin().getIata());
		selectIdenticalDirectEntries.setString(2, flight.getDestination().getIata());
		selectIdenticalDirectEntries.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
		selectIdenticalDirectEntries.setString(4, flight.getSummary());
		selectIdenticalDirectEntries.setDouble(5, flight.getPrice());
		selectIdenticalDirectEntries.setInt(6, flight.getWeekday());
		selectIdenticalDirectEntries.setInt(7, (int) flight.getDuration().getMillis());
		selectIdenticalDirectEntries.setString(8, flight.getCurrency());
		selectIdenticalDirectEntries.setString(9, flight.getFirstCarrier().getCarrierName());
		selectIdenticalDirectEntries.setTimestamp(10, new java.sql.Timestamp(flight.getQuoteDateTime().getTime()));
		return selectIdenticalDirectEntries;
	}
	
	
	private boolean isFlightInDatabase(Connection flight, int connectionNumber) throws SQLException{
		//distinguish between connected and direct flights
				if(connectionNumber == -1){
					selectId.setString(1, flight.getOrigin().getIata());
					selectId.setString(2, flight.getDestination().getIata());
					selectId.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
					selectId.setString(4, flight.getFirstCarrier().getCarrierName());
					selectId.setTimestamp(5, new java.sql.Timestamp(flight.getArrivalDate().getTimeInMillis()));
					// in this first step the result will be deletet to write a new one to the database in the next step
					// a result should only be overwritten if:
					// -> the new result has a price and the old one not
					// -> the price of the new result is cheaper than the price of the old result
					// -> the new result has a price and the old result is older than one day (also if the price is even higher)
					ResultSet sameIDResult;
					if((sameIDResult = selectId.executeQuery()).next()){
						return true;
					}
					return false;
				}else{
					//selectConnectedEntries = conn.prepareStatement("SELECT * FROM flight_connections WHERE origin = 1 AND destination = 2 AND departure_date = 3 AND arrival_date = 4 AND connection_number is not null;");
					selectConnectedEntries.setString(1, flight.getOrigin().getIata());
					selectConnectedEntries.setString(2, flight.getDestination().getIata());
					selectConnectedEntries.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
					selectConnectedEntries.setTimestamp(4, new java.sql.Timestamp(flight.getArrivalDate().getTimeInMillis()));
					// in this first step the result will be deletet to write a new one to the database in the next step
					// a result should only be overwritten if:
					// -> the new result has a price and the old one not
					// -> the price of the new result is cheaper than the price of the old result
					// -> the new result has a price and the old result is older than one day (also if the price is even higher)
					ResultSet sameIDResult;
					if((sameIDResult = selectConnectedEntries.executeQuery()).next()){
						return true;
					}
					return false;
				}
		
	}
	
	
	private boolean shouldEntrieOverwritten(Connection flight, int connectionNumber) throws SQLException{
		//distinguish between connected and direct flights
		if(connectionNumber == -1){
			selectId.setString(1, flight.getOrigin().getIata());
			selectId.setString(2, flight.getDestination().getIata());
			selectId.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
			selectId.setString(4, flight.getFirstCarrier().getCarrierName());
			selectId.setTimestamp(5, new java.sql.Timestamp(flight.getArrivalDate().getTimeInMillis()));
			// in this first step the result will be deletet to write a new one to the database in the next step
			// a result should only be overwritten if:
			// -> the new result has a price and the old one not
			// -> the price of the new result is cheaper than the price of the old result
			// -> the new result has a price and the old result is older than one day (also if the price is even higher)
			ResultSet sameIDResult;
			if((sameIDResult = selectId.executeQuery()).next() && flight.hasPrice() && (sameIDResult.getDouble("min_price") > flight.getPrice() || sameIDResult.getDate("quote_date_time").getTime() < flight.getQuoteDateTime().getTime() - 86400000)){
				return true;
			}
			return false;
		}else{
			//selectConnectedEntries = conn.prepareStatement("SELECT * FROM flight_connections WHERE origin = 1 AND destination = 2 AND departure_date = 3 AND arrival_date = 4 AND connection_number is not null;");
			selectConnectedEntries.setString(1, flight.getOrigin().getIata());
			selectConnectedEntries.setString(2, flight.getDestination().getIata());
			selectConnectedEntries.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
			selectConnectedEntries.setTimestamp(3, new java.sql.Timestamp(flight.getArrivalDate().getTimeInMillis()));
			// in this first step the result will be deletet to write a new one to the database in the next step
			// a result should only be overwritten if:
			// -> the new result has a price and the old one not
			// -> the price of the new result is cheaper than the price of the old result
			// -> the new result has a price and the old result is older than one day (also if the price is even higher)
			ResultSet sameIDResult;
			if((sameIDResult = selectConnectedEntries.executeQuery()).next() && flight.hasPrice() && (sameIDResult.getDouble("min_price") > flight.getPrice() || sameIDResult.getDate("quote_date_time").getTime() < flight.getQuoteDateTime().getTime() - 86400000)){
				return true;
			}
			return false;
		}
	}
	
	private void deletOldEntriesFromDatabase(Connection flight, int connectionNumber) throws SQLException{
		//distinguish between connected and direct flights
		if(connectionNumber == -1){
			deleteDirectEntry.setString(1, flight.getOrigin().getIata());
			deleteDirectEntry.setString(2, flight.getDestination().getIata());
			deleteDirectEntry.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
			deleteDirectEntry.setString(4, flight.getFirstCarrier().getCarrierName());
			deleteDirectEntry.setTimestamp(5, new java.sql.Timestamp(flight.getArrivalDate().getTimeInMillis()));
			deleteDirectEntry.executeUpdate();
		}else{
			int oldConnectionNumber = getConnectionNumber(flight);
			deleteConnectedEntry.setInt(1, oldConnectionNumber);
			deleteConnectedEntry.executeUpdate();
			deleteConnectedFlightNumbers.setInt(1, oldConnectionNumber);
			deleteConnectedFlightNumbers.executeUpdate();
		}
		
	}
	
	
	private int getConnectionNumber(Connection flight) throws SQLException{
		selectConnectedEntries.setString(1, flight.getOrigin().getIata());
		selectConnectedEntries.setString(2, flight.getDestination().getIata());
		selectConnectedEntries.setTimestamp(3, new java.sql.Timestamp(flight.getDepartureDate().getTimeInMillis()));
		selectConnectedEntries.setTimestamp(3, new java.sql.Timestamp(flight.getArrivalDate().getTimeInMillis()));
		ResultSet result = selectConnectedEntries.executeQuery();
		result.next();
		return result.getInt("connection_number");
		
	}
	
}
