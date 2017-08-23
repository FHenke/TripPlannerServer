package utilities;

public class Place {
	public final static int COORDINATES = 1;
	public final static int AIRPORT = 2;
	public final static int TRAIN_STATION = 3;
	public final static int ADDRESS = 4;
	public final static int PORT = 5;
	public final static int BUS_STATION = 6;
	public final static int CONTINENT = 11;
	public final static int COUNTRY = 12;
	public final static int CITY = 13;
	public final static int OTHER = 99;
	
	
	private String id = null;
	private String name = null;
	private int type = 0;
	private double longitude = Double.MAX_VALUE;
	private double latitude = Double.MAX_VALUE;
	private String city = null;
	private String country = null;
	private String continent = null;
	private String street = null;
	private String housenumber = null;
	private String iata = null;
	private String language = null;
	private String currency = null;
	
	

	
	public Place(){
	
	}
	
	public Place(String name, double longitude, double latitude){
		this.name = name;
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public Place(double longitude, double latitude){
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public Place(String name){
		this.name = name;
	}	
	
	/**
	 * @param id
	 * @param type
	 */
	public Place(String id, int type) {
		this.id = id;
		this.type = type;
	}
	
	
	public Place(String id, String iata, int type) {
		this.id = id;
		this.iata = iata;
		this.type = type;
	}
	

	public Place(String name, int type, int longitude, int latitude, String city, String country, String continent, String street, String housenumber){
		this.name = name;
		this.type = type;
		this.longitude = longitude;
		this.latitude = latitude;
		this.city = city;
		this.country = country;
		this.continent = continent;
		this.street = street;
		this.housenumber = housenumber;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @return the continent
	 */
	public String getContinent() {
		return continent;
	}

	/**
	 * @return the street
	 */
	public String getStreet() {
		return street;
	}

	/**
	 * @return the housenumber
	 */
	public String getHousenumber() {
		return housenumber;
	}

	/**
	 * @return the iata
	 */
	public String getIata() {
		return iata;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @return the currency
	 */
	public String getCurrency() {
		return currency;
	}
	
	
	
	
	
	

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param iata the iata to set
	 */
	public void setIata(String iata) {
		this.iata = iata;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @param continent the continent to set
	 */
	public void setContinent(String continent) {
		this.continent = continent;
	}

	/**
	 * @param street the street to set
	 */
	public void setStreet(String street) {
		this.street = street;
	}

	/**
	 * @param housenumber the housenumber to set
	 */
	public void setHousenumber(String housenumber) {
		this.housenumber = housenumber;
	}
	
	
	
	public boolean hasName(){
		return (name != null);
	}
	
	public boolean hasType(){
		return (type != 0);
	}
	
	public boolean hasCoordinates(){
		return (longitude != Double.MAX_VALUE && latitude != Double.MAX_VALUE);
	}
	
	public boolean hasCity(){
		return (city != null);
	}
	
	public boolean hasCountry(){
		return (country != null);
	}
	
	public boolean hasContinent(){
		return (continent != null);
	}
	
	public boolean hasStreet(){
		return (street != null);
	}
	
	public boolean hasHousenumber(){
		return (housenumber != null);
	}
	
	public boolean hasIata(){
		return (iata != null);
	}
	
	public boolean hasLanguage(){
		return (language != null);
	}
	
	public boolean hasCurrency(){
		return (currency != null);
	}
	
	
	
	
	
	
	
	
	/**
	 * Converts the Typenumber of a place to the String used by the Skyscanner API for this type of places
	 * @param type Value describes the type of the Place
	 * @return String used by Skyscanner for describing the type of the Place | null if no representation of this value is available
	 */
	public static String PlaceTypeToSkyscannerString(int type){
		String typeString = null;
		switch (type) {
			case 2: typeString = "Airport";
					break;
			case 11: typeString = "Continent";
					break;
			case 12: typeString = "Country";
					break;
			case 13: typeString = "City";
					break;
			default: typeString = null;
					break;
		}
		return typeString;
	}
	
	
}
