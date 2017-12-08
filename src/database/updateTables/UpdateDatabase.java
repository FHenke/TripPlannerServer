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
public class UpdateDatabase {
	
	private static GregorianCalendar updateDate = null;
	
	public static void proceed(GregorianCalendar date) throws SQLException, IOException{
		Connection dbConnection = DatabaseConnection.getConnection();
		updateDate = date;
		/*new UpdateContinents(dbConnection).proceed();
		System.out.println("Continents updated!");
		new UpdateCountries(dbConnection).proceed();
		System.out.println("Countries updated!");
		new UpdateCities(dbConnection).proceed();
		System.out.println("Cities updated!");
		new UpdateAirports(dbConnection).proceed();
		System.out.println("Airports updated!");*/
		//new UpdateFlightConnections(dbConnection).proceed();
		System.out.println("Flight Connections updated!");
		new UpdateFlights(dbConnection).proceed();
		System.out.println("Flight Connections updated!");

	}
	
	public static GregorianCalendar getUpdateDate(){
		return updateDate;
	}
	
}
