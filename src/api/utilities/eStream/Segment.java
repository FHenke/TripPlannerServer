package api.utilities.eStream;

import java.sql.SQLException;
import java.util.GregorianCalendar;

import org.postgresql.util.PSQLException;

import database.DatabaseConnection;
import database.Query;
import utilities.Place;

public class Segment {

	private int Duration; //Duration in minutes
	private String Origin;
	private String Destination;
	private String DepartureTime;
	private String ArrivalTime;
	private String FlightNumber;
	private String MarketingCarrier;
	private String OperatingCarrier;
	
	
	
	/**
	 * 
	 */
	public Segment() {
		super();
	}



	/**
	 * @param duration
	 * @param origin
	 * @param destination
	 * @param departureTime
	 * @param arrivalTime
	 * @param flightNumber
	 */
	public Segment(int duration, String origin, String destination, String departureTime, String arrivalTime,
			String flightNumber, String marketingCarrier, String operatingCarrier) {
		super();
		Duration = duration;
		Origin = origin;
		Destination = destination;
		DepartureTime = departureTime;
		ArrivalTime = arrivalTime;
		FlightNumber = flightNumber;
		MarketingCarrier = marketingCarrier;
		OperatingCarrier = operatingCarrier;
	}



	/**
	 * @return the duration in Milliseconds
	 */
	public int getDuration() {
		//duration has the format Dhhmm
		int minutes = Duration % 100;
		int houres = (Duration % 10000) / 100;
		int days = Duration / 10000;
		int totalMin = minutes + (houres * 60) + (days * 24 * 60);

		return totalMin * 60 * 1000;
	}



	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return Origin;
	}



	/**
	 * @return the destination
	 */
	public String getDestination() {
		return Destination;
	}



	/**
	 * @return the departureTime
	 */
	public String getDepartureTime() {
		return DepartureTime;
	}



	/**
	 * @return the arrivalTime
	 */
	public String getArrivalTime() {
		return ArrivalTime;
	}



	/**
	 * @return the flightNumber
	 */
	public String getFlightNumber() {
		return FlightNumber;
	}



	/**
	 * @return the marketingCarrier
	 */
	public String getMarketingCarrier() {
		return MarketingCarrier;
	}



	/**
	 * @return the operatingCarrier
	 */
	public String getOperatingCarrier() {
		return OperatingCarrier;
	}



	/**
	 * @param marketingCarrier the marketingCarrier to set
	 */
	public void setMarketingCarrier(String marketingCarrier) {
		MarketingCarrier = marketingCarrier;
	}



	/**
	 * @param operatingCarrier the operatingCarrier to set
	 */
	public void setOperatingCarrier(String operatingCarrier) {
		OperatingCarrier = operatingCarrier;
	}



	/**
	 * @param duration the duration to set
	 */
	public void setDuration(int duration) {
		Duration = duration;
	}



	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin) {
		Origin = origin;
	}



	/**
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		Destination = destination;
	}



	/**
	 * @param departureTime the departureTime to set
	 */
	public void setDepartureTime(String departureTime) {
		DepartureTime = departureTime;
	}



	/**
	 * @param arrivalTime the arrivalTime to set
	 */
	public void setArrivalTime(String arrivalTime) {
		ArrivalTime = arrivalTime;
	}



	/**
	 * @param flightNumber the flightNumber to set
	 */
	public void setFlightNumber(String flightNumber) {
		FlightNumber = flightNumber;
	}
	
	public GregorianCalendar getGregorianDepartureTime(){
		return StringToGregorian(this.getDepartureTime());
	}
	
	public GregorianCalendar getGregorianArrivalTime(){
		return StringToGregorian(this.getArrivalTime());
	}
	
	private GregorianCalendar StringToGregorian(String time){
		
		return new GregorianCalendar(Integer.parseInt(time.substring(0, 4)), Integer.parseInt(time.substring(4, 6)) - 1, Integer.parseInt(time.substring(6, 8)), Integer.parseInt(time.substring(8, 10)), Integer.parseInt(time.substring(10, 12)));
		
	}
	
	/**
	 * Full flight number including carrier abbreviation and number of flight
	 * Uses always number from operating Carrier
	 * @return Full flight number including carrier abbreviation and number of flight
	 */
	public String getFullFlightNumber(){
		return OperatingCarrier + "-" + FlightNumber;
	}
	
	public Place getOriginPlace() throws SQLException, PSQLException{
		return getPlace(Origin);
	}
	
	public Place getDestinationPlace() throws SQLException, PSQLException{
		return getPlace(Destination);
	}
	
	private Place getPlace(String iata) throws SQLException, PSQLException{
		Place place = new Place(iata, iata, Place.AIRPORT);
		Query querry = new Query((new DatabaseConnection()).getConnection());
		place = querry.setAirportinformationFromDatabase(place);
		
		return place;	
	}
}
