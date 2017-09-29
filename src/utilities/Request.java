package utilities;

import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Request extends Connection {

	private String methode = "";
	private String departureDateString = "";
	private String returnDateString = "";
	private boolean[] transportation;
	private boolean showAlternatives = false;
	
	
	
	
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
		String[] dateArray = departureDateString.split(" |/|\\:");
		GregorianCalendar greg = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		greg.set(Integer.parseInt(dateArray[0]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[2]), Integer.parseInt(dateArray[3]), Integer.parseInt(dateArray[4]));
		
		return greg;
	}




	/**
	 * @return the arrivalDateString
	 */
	public GregorianCalendar getReturnDateString() {
		try{
			String[] dateArray = returnDateString.split(" |\\|\\:");
			GregorianCalendar greg = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			greg.set(Integer.parseInt(dateArray[0]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[2]), Integer.parseInt(dateArray[3]), Integer.parseInt(dateArray[4]));
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
	 * @return the showAlternatives
	 */
	public boolean isShowAlternatives() {
		return showAlternatives;
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
	public void setReturnDateString(String arrivalDateString) {
		this.returnDateString = arrivalDateString;
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
	public void setDepartureDateString(String departureDateString) {
		this.departureDateString = departureDateString;
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

	
	
}
