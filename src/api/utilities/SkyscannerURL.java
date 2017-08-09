/**
 * 
 */
package api.utilities;

/**
 * @author Florian
 *
 */
public class SkyscannerURL {

	private static final String API_URL = "http://partners.api.skyscanner.net/apiservices";
	private static final String API_KEY = "yf305338938960673162289244070319";
	
	private String country;
	private String currency;
	private String language;
	private String origin;
	private String destination;
	private String outboundDate;
	private String inboundDate;
	
	/**
	 * Default constructor
	 * sets country to DE, currency to EUR and language to de-DE
	 */
	public SkyscannerURL(){
		this.country = "DE";
		this.currency = "EUR";
		this.language = "de-DE";
	}
	
	/**
	 * @param country
	 * @param currency
	 * @param language
	 */
	public SkyscannerURL(String country, String currency, String language) {
		super();
		this.country = country;
		this.currency = currency;
		this.language = language;
	}

	/**
	 * @param country
	 * @param currency
	 * @param language
	 * @param origin
	 * @param destination
	 * @param outboundDate
	 * @param inboundDate
	 */
	public SkyscannerURL(String country, String currency, String language, String origin, String destination,
			String originDate, String destiationDate) {
		super();
		this.country = country;
		this.currency = currency;
		this.language = language;
		this.origin = origin;
		this.destination = destination;
		this.outboundDate = originDate;
		this.inboundDate = destiationDate;
	}
	
	/**
	 * Sets the parameters for a route
	 * @param origin
	 * @param destination
	 * @param outboundDate
	 * @param inboundDate
	 */
	public void setRoute(String origin, String destination, String outboundDate, String inboundDate){
		this.origin = origin;
		this.destination = destination;
		this.outboundDate = outboundDate;
		this.inboundDate = inboundDate; 
	}
	
	/**
	 * Creates the URL for getting a cached route from skyscanner
	 * @return
	 */
	public String getQuotesURL(){
		String url = "";
		
		// checks if one of the necessary attributes is null, throws an Exception in that case		
		if(country == null || currency == null || language == null || origin == null || destination == null || outboundDate == null){
			throw new NullPointerException("One of the required attributes is null (country, currency, language, origin, destination, outbounddate)");
		}
		// creates the URL
		url = API_URL + "/browsequotes/v1.0/" + country + "/" + currency + "/" + language + "/" +  origin + "/" + destination + "/" + outboundDate;
		// adds the inbounddate if available
		if(inboundDate != null){
			 url += "/" + inboundDate;
		}
		// adds the api key to the url
		url += "?apiKey=" + API_KEY;
		return url;
	}
	
	/**
	 * Creates the URL for getting a cached route from skyscanner
	 * @param origin place
	 * @param destination
	 * @param outboundDate
	 * @param inboundDate
	 * @return Url for Cached Skyscanner Connections
	 */
	public String getQuotesURL(String origin, String destination, String outboundDate, String inboundDate){
		String url = "";
		
		// checks if one of the necessary attributes is null, throws an Exception in that case		
		if(country == null || currency == null || language == null || origin == null || destination == null || outboundDate == null){
			throw new NullPointerException("One of the required attributes is null (country, currency, language, origin, destination, outbounddate)");
		}
		// creates the URL
		url = API_URL + "/browsequotes/v1.0/" + country + "/" + currency + "/" + language + "/" +  origin + "/" + destination + "/" + outboundDate;
		// adds the inbounddate if available
		if(inboundDate != null){
			 url += "/" + inboundDate;
		}
		// adds the api key to the url
		url += "?apiKey=" + API_KEY;
		return url;
	}
	
	/**
	 * gets the URL for getting a Autosuggest response from Skyscaner
	 * @param input
	 * @return
	 */
	public String getAutosuggestURL(String input){
		// creates the URL
		return API_URL + "/autosuggest/v1.0/" + country + "/" + currency + "/" + language + "?query=" + input + "&apiKey=" + API_KEY;

	}
	
	/**
	 * Skyscanner URL for getting a XML file with all places skyscanner knows
	 * @return Skyscanner Url for getting a XML file of all places Skyscanner knows
	 */
	public String getAllPlacesURL(){
		return API_URL + "/geo/v1.0?apiKey=" + API_KEY;
	}
}
