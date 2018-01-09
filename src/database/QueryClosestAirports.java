package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import database.utilities.SQLUtilities;
import utilities.Connection;
import utilities.Place;

public class QueryClosestAirports {
	
	protected static final Logger logger = LogManager.getLogger(QueryClosestAirports.class);
	protected static java.sql.Connection conn = DatabaseConnection.getConnection();
	
	
	/**
	 * Generates a Connection List with all Beeline Connections between the given place and the given amount of closest airports
	 * @param place Place to the airports
	 * @param amountOfAirportsToReturn amount of closest airports that should be returned, can be between 1 and 25
	 * @param placeIsOrigin defines if the given place is the origin or destination
	 * @return
	 * @throws SQLException
	 */
	public static LinkedBlockingQueue<Connection> getClosestAirports(Place place, int amountOfAirportsToReturn, boolean placeIsOrigin) throws SQLException{
		ResultSet queryResult = queryClosestAirports(place, amountOfAirportsToReturn);
		LinkedBlockingQueue<Connection> airportList = generateBeelineConnection(queryResult, place, placeIsOrigin);
		return airportList;
	}
	
	/**
	 * returns a list with the given amount of closest (beeline) airports from the given place.
	 * @param place the Place to which the airports should be as close as possible
	 * @param amountOfAirportsToReturn the amount of closest airports that should be returned
	 * @return a list with the given amount of closest (beeline) airports from the given place.
	 * @throws SQLException
	 */
	public static LinkedList<Place> getClosestAirports(Place place, int amountOfAirportsToReturn) throws SQLException{
		ResultSet queryResult = queryClosestAirports(place, amountOfAirportsToReturn);
		LinkedList<Place> airportList = generatePlaceList(queryResult);
		return airportList;
	}
	
	
	private static ResultSet queryClosestAirports(Place place, int amountOfAirportsToReturn) throws SQLException{
		ResultSet queryResult;
		String querryString = "SELECT ST_Distance_sphere(airports.location, ST_GeomFromText('POINT("
				+ place.getLongitude()
				+ " "
				+ place.getLatitude()
				+ ")',-1)) AS distance, airports.iata_code AS iata_code, airports.name AS name, ST_X(airports.location) AS longitude, ST_Y(airports.location) AS latitude "
				+ "FROM airports, (select distinct origin from flight_connections) As flights WHERE airports.iata_code = flights.origin ORDER BY distance"
				+ " LIMIT " + amountOfAirportsToReturn + ";";
		try {
			queryResult = conn.createStatement().executeQuery(querryString);
		} catch (SQLException e) {
			logger.error("Cant Query closest airports for: " + place.getName() + "\n " + e);
			throw new SQLException("Cant Query closest airports for: " + place.getName());
		}	
		return queryResult;
	}
	
	private static LinkedBlockingQueue<Connection> generateBeelineConnection(ResultSet queryResult, Place place, boolean placeIsOrigin) throws SQLException{
		LinkedBlockingQueue<Connection> airportList = new LinkedBlockingQueue<Connection>();
		try {
			while(queryResult.next()){
				utilities.Connection airportElement;
				Place airport = SQLUtilities.generatePlaceFromResultSet(queryResult);
				if(placeIsOrigin)
					airportElement = new utilities.Connection(place, airport);
				else
					airportElement = new utilities.Connection(airport, place);
				airportElement.setBeeline(queryResult.getInt("distance"));
				airportElement.setType(Connection.BEELINE);
				airportList.add(airportElement);
			}
		} catch (SQLException e) {
			logger.error("Cant generate list for closest airports (beeline): " + place.getName() + "\n " + e);
			throw new SQLException("Cant generate list for closest airports (beeline): " + place.getName());
		}
		return airportList;
	}
	
	private static LinkedList<Place> generatePlaceList(ResultSet queryResult) throws SQLException{
		LinkedList<Place> airportList = new LinkedList<Place>();
		try {
			while(queryResult.next()){
				airportList.add(SQLUtilities.generatePlaceFromResultSet(queryResult));
			}
		} catch (SQLException e) {
			logger.error("Cant generate list for closest airports." + "\n " + e);
			throw new SQLException("Cant generate list for closest airports.");
		}
		return airportList;
	}
	
	/*private static Place generatePlace(ResultSet queryResult) throws SQLException{
		Place airport = new Place(queryResult.getDouble("lng"), queryResult.getDouble("lat"));
		airport.setIata(queryResult.getString("iata"));
		airport.setName(queryResult.getString("name") + " Airport");
		airport.setType(Place.AIRPORT);
		return airport;
	}*/
}
