package pathCalculation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdom2.JDOMException;

import api.utilities.GoogleMaps;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class RequestHandler {

	
	public RequestHandler(){
		
	}
	
	@SuppressWarnings("null")
	public LinkedBlockingQueue<Connection> solveRequest(Request request){
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		//If Method is GoogleMapsDistance uses Google Maps Distance Service only
		if(request.getMethode().equalsIgnoreCase("GoogleMapsDistance")){
			GoogleMapsDistance distance = new GoogleMapsDistance();
			connectionList.addAll(distance.getConnectionList(request));
		}
		
		//If Method is GoogleMapsDirecton uses Google Maps Direction Service only
		if(request.getMethode().equalsIgnoreCase("GoogleMapsDirection")){
			GoogleMapsDirection direction = new GoogleMapsDirection();
			connectionList.addAll(direction.getConnectionList(request));
		}
		
		
		//If Method is SkyscannerCacheOnly uses Skyscanner Cache Service only
		/*if(request.getMethode().equalsIgnoreCase("SkyscannerCacheOnly")){
			try {
				api.SkyscannerCache direction = new api.SkyscannerCache();
				connectionList = direction.getAllConnections(request.getOrigin().getIata(), request.getDestination().getIata(), request.getDepartureDateString(), null);
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}*/
		
		if(request.getMethode().equalsIgnoreCase("All")){
			try {
				SkyscannerCache skyCache = new SkyscannerCache();
				return skyCache.getConnectionList(request, connectionList);
			} catch (IllegalStateException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}

		return connectionList;
	}
}

