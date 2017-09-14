package utilities;

import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Request extends Connection {

	private String methode = "";
	private String departureDateString = "";
	
	
	
	
	public Request(Place origin, Place destination) {
		super(origin, destination);
		// TODO Auto-generated constructor stub
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
		String[] dateArray = departureDateString.split(" ");
		GregorianCalendar greg = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		greg.set(Integer.parseInt(dateArray[0]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[2]), Integer.parseInt(dateArray[3]), Integer.parseInt(dateArray[4]));
		
		return greg;
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

	
	
}
