package api;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import utilities.Connection;

public class SkyscannerLive implements API {

	public SkyscannerLive(){
		
	}
	
	public LinkedBlockingQueue<Connection> getAllConnections(String origin, String destination, Date originDate, Date destinationDate){
	
		return null;
	}
	
	public static void test() throws ClientProtocolException, IOException{
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://partners.api.skyscanner.net/apiservices/pricing/v1.0");
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		//nvps.add(new BasicNameValuePair("Content-Type", "application/x-www-form-urlencoded"));
		/*nvps.add(new BasicNameValuePair("cabinclass", "Economy"));
		nvps.add(new BasicNameValuePair("country", "UK"));
		nvps.add(new BasicNameValuePair("currency", "GBP"));
		nvps.add(new BasicNameValuePair("locale", "en-GB"));
		nvps.add(new BasicNameValuePair("locationSchema", "iata"));
		nvps.add(new BasicNameValuePair("originplace", "EDI"));
		nvps.add(new BasicNameValuePair("destinationplace", "LHR"));
		nvps.add(new BasicNameValuePair("outbounddate", "2017-08-12"));
		nvps.add(new BasicNameValuePair("outbounddate", "2017-09-12"));
		nvps.add(new BasicNameValuePair("adults", "1"));
		nvps.add(new BasicNameValuePair("children", "0"));
		nvps.add(new BasicNameValuePair("infants", "0"));*/
		nvps.add(new BasicNameValuePair("apikey", "yf305338938960673162289244070319"));
		
		
		
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
		System.out.println(httpclient.toString());
		HttpResponse response2 = httpclient.execute(httpPost);
		

		try {
		    System.out.println(response2.getStatusLine());
		    HttpEntity entity2 = response2.getEntity();
		    // do something useful with the response body
		    // and ensure it is fully consumed
		    EntityUtils.consume(entity2);
		} finally {
		   
		}
	}
	
	/**
	 * executes the Querry to Skyscanner API
	 * @param url URL for the Querry
	 * @return InputStream of the XML code that API responses
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	private static InputStream getInput(String url) throws ClientProtocolException, IOException {

		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);

		// add request header
		request.addHeader("Accept", "application/xml");

		HttpResponse response = client.execute(request);
		
		return response.getEntity().getContent();
	}
}
