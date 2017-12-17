/**
 * 
 */
package database.updateTables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.GregorianCalendar;


import database.DatabaseConnection;

/**
 * @author Florian
 *
 */
public class UpdateDatabase implements Runnable{
	
	private static GregorianCalendar updateDate = null;
	boolean updatePlaces = true;
	boolean useSkyscanner = true;
	boolean useEStreaming = true;
	
	public UpdateDatabase(GregorianCalendar updateDate, boolean updatePlaces, boolean useSkyscanner, boolean useEStreaming){
		this.updateDate = updateDate;
		this.updatePlaces = updatePlaces;
		this.useSkyscanner = useSkyscanner;
		this.useEStreaming = useEStreaming;
	}
	
	public void proceed(){
		Connection dbConnection = DatabaseConnection.getConnection();
		
		try {
			if(updatePlaces){
				new UpdateContinents(dbConnection).proceed();
				System.out.println("Continents updated!");
				new UpdateCountries(dbConnection).proceed();
				System.out.println("Countries updated!");
				new UpdateCities(dbConnection).proceed();
				System.out.println("Cities updated!");
				new UpdateAirports(dbConnection).proceed();
				System.out.println("Airports updated!");
			}
			if(useSkyscanner){
				new UpdateFlightConnections(dbConnection).proceed();
				System.out.println("Flight Connections updated!");
			}
			if(useEStreaming){
				new UpdateFlights(dbConnection).proceed();
				System.out.println("Flight Connections updated!");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static GregorianCalendar getUpdateDate(){
		return updateDate;
	}

	@Override
	public void run() {
		proceed();
	}
	
}
