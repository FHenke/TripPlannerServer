package utilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class Connection {
	public final static int PLANE = 1;
	public final static int BUS = 2;
	public final static int TRAIN = 3;
	public final static int CAR = 4;
	public final static int TAXI = 5;
	public final static int PUBLIC_TRANSPORT = 6;
	public final static int SHIP = 7;
	public final static int WALK = 8;
	public final static int BICYCLE = 9;
	public final static int OTHER = 99;
	
	public final static int MONDAY = 1;
	public final static int TUESDAY = 2;
	public final static int WEDNESDAY = 3;
	public final static int THURSDAY = 4;
	public final static int FRIDAY = 5;
	public final static int SATURDAY = 6;
	public final static int SUNDAY = 7;
	
	public final static String ADD = "add";
	public final static String REMOVE = "remove";
	
	
	private Place origin;
	private Place destination;
	private double price = 0;
	private Duration duration;
	private GregorianCalendar arrivalDate = null;
	private GregorianCalendar departureDate = null;
	private int type;
	private CarrierList carrier;
	private boolean direct;
	private Date quoteDateTime;
	private int weekday;
	private int distance; //distance in meter
	private String polyline = null;
	private String htmlInstructions = null;
	private String summary = null;
	private LinkedBlockingQueue<Connection> subConnections = new LinkedBlockingQueue<Connection>();
	private LinkedBlockingQueue<Connection> returnConnection = new LinkedBlockingQueue<Connection>();
	private int id;
	private String action;
	private int beeline = Integer.MAX_VALUE;
	
	/**
	 * 
	 * @param origin
	 * @param destination
	 */
	public Connection(Place origin, Place destination){
		this.origin = origin;
		this.destination = destination;
		this.id = IdGenerator.getNewID();
		this.action = ADD;
	}
	
	
	
	/**
	 * @param origin
	 * @param destination
	 * @param price
	 * @param arrivalDate
	 * @param departureDate
	 * @param type
	 * @param direct
	 * @param quoteDateTime
	 * @param weekday
	 */
	public Connection(Place origin, Place destination, double price, GregorianCalendar departureDate, int type,
			boolean direct, Date quoteDateTime, int weekday) {
		super();
		this.origin = origin;
		this.destination = destination;
		this.price = price;
		this.departureDate = departureDate;
		this.type = type;
		this.direct = direct;
		this.quoteDateTime = quoteDateTime;
		this.weekday = weekday;
		this.id = IdGenerator.getNewID();
		this.action = ADD;
	}
	
	
	/**
	 * 
	 * @param origin
	 * @param destination
	 * @param price
	 * @param duration
	 * @param arrivalDate
	 * @param departureionDate
	 */
	public Connection(int type, Place origin, Place destination, double price, Duration duration,
			GregorianCalendar departureDate, GregorianCalendar arrivalDate) {
		super();
		this.type = type;
		this.origin = origin;
		this.destination = destination;
		this.price = price;
		this.duration = duration;
		this.arrivalDate = arrivalDate;
		this.departureDate = departureDate;
		this.id = IdGenerator.getNewID();
		this.action = ADD;
	}
	
	/**
	 * Constructor for passing an action to a ID
	 * @param id ID of the Connection
	 * @param action Action to perform (usually remove)
	 */
	public Connection(int id, String action){
		this.id = id;
		this.action = action;
	}

	
	/**
	 * @return the ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the origin
	 */
	public Place getOrigin() {
		return origin;
	}


	/**
	 * @return the destination
	 */
	public Place getDestination() {
		return destination;
	}


	/**
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}


	/**
	 * @return the duration
	 */
	public Duration getDuration() {
		return duration;
	}


	/**
	 * @return the arrivalDate
	 */
	public GregorianCalendar getArrivalDate() {
		return arrivalDate;
	}


	/**
	 * @return the departureDate
	 */
	public GregorianCalendar getDepartureDate() {
		return departureDate;
	}


	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}


	/**
	 * @return the carrier
	 */
	public CarrierList getCarrier() {
		return carrier;
	}


	/**
	 * @return the direct
	 */
	public boolean isDirect() {
		return direct;
	}


	/**
	 * @return the quoteDateTime
	 */
	public Date getQuoteDateTime() {
		return quoteDateTime;
	}


	/**
	 * @return the subConnections
	 */
	public LinkedBlockingQueue<Connection> getSubConnections() {
		return subConnections;
	}


	/**
	 * @return the returnConnection
	 */
	public LinkedBlockingQueue<Connection> getReturnConnection() {
		return returnConnection;
	}


	/**
	 * @return the weekday
	 */
	public int getWeekday() {
		return weekday;
	}
	
		/**
	 * @return the distance
	 */
	public int getDistance() {
		return distance;
	}
	
	/**
	 * @return the polyline
	 */
	public String getPolyline() {
		return polyline;
	}


	/**
	 * @return the htmlInstructions
	 */
	public String getHtmlInstructions() {
		return htmlInstructions;
	}	


	/**
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}
















	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}



	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}



	/**
	 * @param summary the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}



	/**
	 * @param polyline the polyline to set
	 */
	public void setPolyline(String polyline) {
		this.polyline = polyline;
	}



	/**
	 * @param htmlInstructions the htmlInstructions to set
	 */
	public void setHtmlInstructions(String htmlInstructions) {
		this.htmlInstructions = htmlInstructions;
	}
	

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(int distance) {
		this.distance = distance;
	}




	/**
	 * @param weekday the weekday to set
	 */
	public void setWeekday(int weekday) {
		this.weekday = weekday;
	}



	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(Place origin) {
		this.origin = origin;
	}


	/**
	 * @param destination the destination to set
	 */
	public void setDestination(Place destination) {
		this.destination = destination;
	}


	/**
	 * @param price the price to set
	 */
	public void setPrice(double price) {
		this.price = price;
	}


	/**
	 * @param duration the duration to set
	 */
	public void setDuration(Duration duration) {
		this.duration = duration;
	}


	/**
	 * @param arrivalDate the arrivalDate to set
	 */
	public void setArrivalDate(GregorianCalendar arrivalDate) {
		this.arrivalDate = arrivalDate;
	}


	/**
	 * @param departureDate the departureDate to set
	 */
	public void setDepartureDate(GregorianCalendar departureDate) {
		this.departureDate = departureDate;
	}
	
	public void setDepartureDate(String departureDateString){
		GregorianCalendar greg = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		greg.set(2017, 9, 22, 7, 20);
		this.departureDate = greg;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}


	/**
	 * @param carrier the carrier to set
	 */
	public void setCarrier(CarrierList carrier) {
		this.carrier = carrier;
	}


	/**
	 * @param direct the direct to set
	 */
	public void setDirect(boolean direct) {
		this.direct = direct;
	}


	/**
	 * @param quoteDateTime the quoteDateTime to set
	 */
	public void setQuoteDateTime(Date quoteDateTime) {
		this.quoteDateTime = quoteDateTime;
	}


	/**
	 * @param subConnections the subConnections to set
	 */
	public void setSubConnections(LinkedBlockingQueue<Connection> subConnections) {
		this.subConnections = subConnections;
	}


	/**
	 * @param returnConnection the returnConnection to set
	 */
	public void setReturnConnection(LinkedBlockingQueue<Connection> returnConnection) {
		this.returnConnection = returnConnection;
	}

	
	public int getBeeline() {
		return beeline;
	}



	public void setBeeline(int beeline) {
		this.beeline = beeline;
	}



	/**
	 * Checks if subconnections available
	 * @return true if subconnections available, false otherwise
	 */
	public boolean hasSubConnections(){
		return (subConnections == null) ? false : true;
	}
	
	/**
	 * Checks if returnconnection available
	 * @return true if returnconnection available, false otherwise
	 */
	public boolean hasReturnConnection(){
		return (returnConnection == null) ? false : true;
	}
	
	public boolean hasArrivalDate(){
		return (arrivalDate == null) ? false : true;
	}
	
	public boolean hasDepartureDate(){
		return (departureDate == null) ? false : true;
	}
	
	
	
	
	/**
	 * adds a subconnection to the subconnectionlist
	 * @param subconnection
	 */
	public void addSubconnection(Connection subconnection){
		this.subConnections.add(subconnection);
	}
	
	/**
	 * Returns the duration as a nice human readable String
	 * @return nice human readable String representation of the duration (HH:mm)
	 */
	public String durationToString(){
		Period p = duration.toPeriod();
		PeriodFormatter hm = new PeriodFormatterBuilder()
		    .printZeroAlways()
		    .minimumPrintedDigits(2) // gives the '01'
		    .appendHours()
		    .appendSeparator(":")
		    .appendMinutes()
		    .toFormatter();
		return hm.print(p);
	}
	
	/**
	 * Returns a nice human readable String from a Calendar object
	 * @param calendar Calendar object that should be parsed in a string
	 * @return nice human readbale String representation of a calendar object (yyyy.MM.dd HH:mm)
	 */
	public static String dateToString(GregorianCalendar calendar){
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy.MM.dd HH:mm");   // lowercase "dd"
		String test = formatter.format(calendar.getTime() );
	    return test;
	}
	
	/**
	 * Adds a value to the current price
	 * @param price
	 */
	public void addPrice(double price){
		this.price += price;
	}
}
