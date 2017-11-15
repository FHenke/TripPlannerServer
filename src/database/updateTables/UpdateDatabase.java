/**
 * 
 */
package database.updateTables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Florian
 *
 */
public class UpdateDatabase {
	
	public static void proceed(Connection dbConnection) throws SQLException, IOException{
		new UpdateContinents(dbConnection).proceed();
		System.out.println("Continents updated!");
		new UpdateCountries(dbConnection).proceed();
		System.out.println("Countries updated!");
		new UpdateCities(dbConnection).proceed();
		System.out.println("Cities updated!");
		new UpdateAirports(dbConnection).proceed();
		System.out.println("Airports updated!");
		//new UpdateFlightConnections(dbConnection).proceed();
		//System.out.println("Flight Connections updated!");
		new UpdateFlights(dbConnection).proceed();
		System.out.println("Flight Connections updated!");

	}
	
}
