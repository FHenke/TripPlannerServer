package api;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class testLive{


		public static void test(){
			String u = "http://partners.api.skyscanner.net/apiservices/pricing/v1.0";
			String apiKey = "un825353286098573492646341649584";
			try {
				URL url = new URL(u);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				
				//connection.setRequestProperty("Accept-Charset", charset);
				//connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
			
		        connection.setRequestMethod("POST");
		        connection.setConnectTimeout(500 * 1000);
		        connection.setReadTimeout(500 * 1000);
		        connection.setRequestProperty("Cache-Control", "no-cache");
		        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		        connection.setRequestProperty("Accept", "application/xml");
		        connection.setDoOutput(true);
		        connection.setDoInput(true);
	
	
		        String Parameters = "apiKey=" + apiKey +
		                "&country=UK" +
		                "&currency=GBP" +
		                "&locale=en-GB" +
		                "&originplace=EDI" +
		                "&destinationplace=LHR" +
		                "&outbounddate=2017-08-01" +
		                "&intbounddate=2017-08-08" +
		                "&locationschema=Iata" +
		                "&adults=1";
		        //Parameter
		        byte[] postData = Parameters.getBytes(StandardCharsets.UTF_8);
		        int postDataLength = postData.length;
		        connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		        OutputStream os = connection.getOutputStream();
		        os.write(postData);
		        os.flush();
	
	
		        int responseCode = connection.getResponseCode();
		        System.out.println(responseCode);
			} catch(Exception e){
				System.out.println(e);
			}
			
			
		}

}
