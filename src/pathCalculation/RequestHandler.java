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
	
	public LinkedBlockingQueue<Connection> solveRequest(Request request){
		/*api.SkyscannerCache cache = new api.SkyscannerCache();
		api.SkyscannerLive live = new api.SkyscannerLive();
		api.GoogleMapsDistance distance = new api.GoogleMapsDistance();*/
		LinkedBlockingQueue<Connection> connectionList = null;
		
		//If Method is GoogleMapsOnly uses Google Maps Direction Service
		if(request.getMethode().equalsIgnoreCase("GoogleMapsOnly")){
			try {
				api.GoogleMapsDirection direction = new api.GoogleMapsDirection();
				connectionList = direction.getConnection(request.getOrigin(), request.getDestination(), request.getDepartureDateString(), true, GoogleMaps.DRIVING, "", "de");
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		
		//If Method is GoogleMapsDistance uses Google Maps Distance Service only
		if(request.getMethode().equalsIgnoreCase("GoogleMapsDistance")){
			try {
				api.GoogleMapsDistance distance = new api.GoogleMapsDistance();
				LinkedList<Place> originPlaceList = new LinkedList<Place>();
				originPlaceList.add(request.getOrigin());
				LinkedList<Place> destinationPlaceList = new LinkedList<Place>();
				destinationPlaceList.add(request.getDestination());
				connectionList = distance.getConnection(originPlaceList, destinationPlaceList, request.getDepartureDateString(), true, GoogleMaps.DRIVING, "", "de");
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		
		//If Method is GoogleMapsDirecton uses Google Maps Direction Service only
		if(request.getMethode().equalsIgnoreCase("GoogleMapsDirection")){
			try {
				api.GoogleMapsDirection direction = new api.GoogleMapsDirection();
				connectionList = direction.getConnection(request.getOrigin(), request.getDestination(), request.getDepartureDateString(), true, GoogleMaps.DRIVING, "", "de");
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		
		
		//If Method is SkyscannerCacheOnly uses Skyscanner Cache Service only
		if(request.getMethode().equalsIgnoreCase("SkyscannerCacheOnly")){
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
		}
		
		

		
		return connectionList;
	}
}

