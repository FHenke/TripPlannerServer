/**
 * 
 */
package database.updateTables;

import java.io.IOException;
import java.sql.Connection;

import java.util.GregorianCalendar;


import database.DatabaseConnection;

/**
 * @author Florian
 *
 */
public class UpdateDatabase implements Runnable{
	
	private static String status = "NO STATUS";
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
				status = "Continents updated!";
				System.out.println(status);
				new UpdateCountries(dbConnection).proceed();
				status = "Countries updated!";
				System.out.println(status);
				new UpdateCities(dbConnection).proceed();
				status = "Cities updated!";
				System.out.println(status);
				new UpdateAirports(dbConnection).proceed();
				status = "Airports updated!";
				System.out.println(status);
			}
			if(useSkyscanner){
				new UpdateFlightConnections(dbConnection).proceed();
				status = "Flight Connections updated!";
				System.out.println(status);
			}
			if(useEStreaming){
				new UpdateFlights(dbConnection).proceed();
				status = "Flight Connections updated!";
				System.out.println(status);
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
	
	public static void setStatus(String newStatus){
		status = newStatus;
	}
	
	public static String getStatus(){
		return status;
	}
}
