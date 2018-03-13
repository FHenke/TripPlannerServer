package utilities;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import api.GoogleMapsDistance;
import api.utilities.GoogleMaps;

public class Request extends Connection {

	private String methode = "";
	private long departureDateEpochTime = 0;
	private long returnDateEpochTime = 0;
	private boolean[] transportation;
	private boolean showAlternatives = false;
	private boolean isDepartureTime = true;
	private int priceForHoure = 1000;
	
	//final static values that can only be changed in code
	private static final int amountOfOriginAirports = 1;
	private static final int amountOfDestinationAirports = 10;
	private static final int AmountOfConnectionsToShow = 5;
	//private static final int exploitMethod = pathCalculation.recursiveBreadthFirst.SearchNode.Best_CONNECTIONS;
	private static final int exploitMethod = pathCalculation.recursiveBreadthFirst.SearchNode.Best_CONNECTIONS;
	
	
	
	
	public Request(Place origin, Place destination) {
		super(origin, destination);
	}




	/**
	 * @return the methode
	 */
	public String getMethode() {
		return methode;
	}




	/**
	 * @return the departureDate
	 */
	public GregorianCalendar getDepartureDateString() {
		GregorianCalendar greg = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		greg.setTimeInMillis(departureDateEpochTime);
		return greg;
	}




	/**
	 * @return the arrivalDateString
	 */
	public GregorianCalendar getReturnDateString() {
		try{
			GregorianCalendar greg = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			greg.setTimeInMillis(returnDateEpochTime);
			return greg;
		}catch(Exception e){
			//happens if there is no return connection requested
			return null;
		}
	}




	/**
	 * @return the transportation
	 */
	public boolean[] getTransportation() {
		return transportation;
	}


	


	/**
	 * @return the priceForHoure
	 */
	public int getPriceForHoure() {
		return priceForHoure;
	}




	/**
	 * @return the originAirport
	 */
	public int getAmountOfOriginAirports() {
		return amountOfOriginAirports;
	}




	/**
	 * @return the destinationAirports
	 */
	public int getAmountOfDestinationAirports() {
		return amountOfDestinationAirports;
	}

	
	/**
	 * @return the destinationAirports
	 */
	public int getExploitMethod() {
		return exploitMethod;
	}

	
//#######

	/**
	 * @return the amountOfConnectionstoShow
	 */
	public int getAmountOfConnectionsToShow() {
		return AmountOfConnectionsToShow;
	}


	/**
	 * @param priceForHoure the priceForHoure to set
	 */
	public void setPriceForHoure(int priceForHoure) {
		this.priceForHoure = priceForHoure;
	}




	/**
	 * @return the showAlternatives
	 */
	public boolean isShowAlternatives() {
		return showAlternatives;
	}




	/**
	 * @return the isDepartureTime
	 */
	public boolean isDepartureTime() {
		return isDepartureTime;
	}




	/**
	 * @param isDepartureTime the isDepartureTime to set
	 */
	public void setDepartureTime(boolean isDepartureTime) {
		this.isDepartureTime = isDepartureTime;
	}




	/**
	 * @param showAlternatives the showAlternatives to set
	 */
	public void setShowAlternatives(boolean showAlternatives) {
		this.showAlternatives = showAlternatives;
	}




	/**
	 * @param arrivalDateString the arrivalDateString to set
	 */
	public void setReturnDateString(long returnDateMillisec) {
		this.returnDateEpochTime = returnDateMillisec;
	}




	/**
	 * @param transportation the transportation to set
	 */
	public void setTransportation(boolean[] transportation) {
		this.transportation = transportation;
	}




	/**
	 * @param departureDate the departureDate to set
	 */
	public void setDepartureDateString(long departureDateMillisec) {
		this.departureDateEpochTime = departureDateMillisec;
	}




	/**
	 * @param methode the methode to set
	 */
	public void setMethode(String methode) {
		this.methode = methode;
	}
	

	/**
	 * Returns different modes of Transportation
	 * @return if Transportation Car is set true
	 */
	public boolean transportationIsCar(){
		return transportation[0];
	}
	/**
	 * Returns different modes of Transportation
	 * @return if Transportation Public Transport is set true
	 */
	public boolean transportationIsPublicTransit(){
		return transportation[1];
	}
	/**
	 * Returns different modes of Transportation
	 * @return if Transportation Bicycle is set true
	 */
	public boolean transportationIsBicycle(){
		return transportation[2];
	}
	/**
	 * Returns different modes of Transportation
	 * @return if Transportation Walk is set true
	 */
	public boolean transportationIsWalk(){
		return transportation[3];
	}
	/**
	 * Returns different modes of Transportation
	 * @return if Transportation Airplane is set true
	 */
	public boolean transportationIsAirplane(){
		return transportation[4];
	}

	/**
	 * Returns the "best" way to move from the given possiblilities. The order is
	 * 1. Driving
	 * 2. Public Transport
	 * 3. Bicycle
	 * 4. Walking
	 * 5. Airplane
	 * @return Best way to move according to the above list
	 */
	public String getBestTransportation(){
		if(transportation[0])
			return GoogleMaps.DRIVING;
		if(transportation[1])
			return GoogleMaps.TRANSIT;
		if(transportation[2])
			return GoogleMaps.BICYCLING;
		if(transportation[3])
			return GoogleMaps.WALKING;
		return null;
	}
	
}
