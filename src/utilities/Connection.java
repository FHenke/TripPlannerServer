package utilities;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.Duration;
import org.joda.time.Period;
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
	public final static int FOOT = 8;
	public final static int OTHER = 99;
	
	public final static int MONDAY = 1;
	public final static int TUESDAY = 2;
	public final static int WEDNESDAY = 3;
	public final static int THURSDAY = 4;
	public final static int FRIDAY = 5;
	public final static int SATURDAY = 6;
	public final static int SUNDAY = 7;
	
	
	private Place origin;
	private Place destination;
	private double price;
	private Duration duration;
	private Date arrivalDate;
	private Date departureDate;
	private int type;
	private CarrierList carrier;
	private boolean direct;
	private Date quoteDateTime;
	private LinkedBlockingQueue<Connection> subConnections;
	private LinkedBlockingQueue<Connection> returnConnection;
	private int weekday;
	private int distance; //distance in meter
	
	/**
	 * empty constructor
	 */
	public Connection(Place origin, Place destination){
		this.origin = origin;
		this.destination = destination;
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
	public Connection(Place origin, Place destination, double price, Date departureDate, int type,
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
	}
	
	
	/**
	 * 
	 * @param origin
	 * @param destination
	 * @param price
	 * @param duration
	 * @param originDate
	 * @param destinationDate
	 */
	public Connection(int type, Place origin, Place destination, double price, Duration duration, Date originDate,
			Date destinationDate) {
		super();
		this.type = type;
		this.origin = origin;
		this.destination = destination;
		this.price = price;
		this.duration = duration;
		this.arrivalDate = originDate;
		this.departureDate = destinationDate;
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
	public Date getArrivalDate() {
		return arrivalDate;
	}


	/**
	 * @return the departureDate
	 */
	public Date getDepartureDate() {
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
	public void setArrivalDate(Date arrivalDate) {
		this.arrivalDate = arrivalDate;
	}


	/**
	 * @param departureDate the departureDate to set
	 */
	public void setDepartureDate(Date departureDate) {
		this.departureDate = departureDate;
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
	
}
