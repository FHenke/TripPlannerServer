package api.utilities;

import java.util.GregorianCalendar;

import utilities.Place;

public class APIRequest {

	Place origin;
	Place destination;
	GregorianCalendar departureDate;
	GregorianCalendar returnDate;
	
	
	/**
	 * Constructor excluding return connection
	 * @param origin Place of origin
	 * @param destination Place of destination
	 * @param departureDate Departure date including time
	 * @param returnDate return date including Time
	 */
	public APIRequest(Place origin, Place destination, GregorianCalendar departureDate){
		this.origin = origin;
		this.destination = destination;
		this.departureDate = departureDate;
	}	
	
	/**
	 * Constructor including return connection
	 * @param origin Place of origin
	 * @param destination Place of destination
	 * @param departureDate Departure date including time
	 * @param returnDate return date including Time
	 */
	public APIRequest(Place origin, Place destination, GregorianCalendar departureDate, GregorianCalendar returnDate){
		this.origin = origin;
		this.destination = destination;
		this.departureDate = departureDate;
		this.returnDate = returnDate;
	}
	
}
