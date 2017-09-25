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
			/*try {
				api.GoogleMapsDistance distance = new api.GoogleMapsDistance();
				LinkedList<Place> originPlaceList = new LinkedList<Place>();
				originPlaceList.add(request.getOrigin());
				LinkedList<Place> destinationPlaceList = new LinkedList<Place>();
				destinationPlaceList.add(request.getDestination());
				connectionList = distance.getConnection(originPlaceList, destinationPlaceList, request.getDepartureDateString(), true, api.utilities.GoogleMaps.DRIVING, "", "de");
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}*/
		}
		
		//If Method is GoogleMapsDirecton uses Google Maps Direction Service only
		if(request.getMethode().equalsIgnoreCase("GoogleMapsDirection")){
			GoogleMapsDirection direction = new GoogleMapsDirection();
			connectionList.addAll(direction.getConnectionList(request));
			
			/*try {
				api.GoogleMapsDirection direction = new api.GoogleMapsDirection();
				connectionList = direction.getConnection(request.getOrigin(), request.getDestination(), request.getDepartureDateString(), true, api.utilities.GoogleMaps.DRIVING, "", "de", request.isShowAlternatives());
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}*/
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
	
	
	private String getGoogleTransportationString(String transportation){
		String googleTransportation = "";
		switch(transportation){
			case "BusOnly": googleTransportation = api.utilities.GoogleMaps.TRANSIT;
							break;
			case "CarOnly": googleTransportation = api.utilities.GoogleMaps.DRIVING;
							break;
			case "WalkingOnly": googleTransportation = api.utilities.GoogleMaps.WALKING;
							break;
			case "BicyclingOnly": googleTransportation = api.utilities.GoogleMaps.BICYCLING;
							break;
		}
		return googleTransportation;
	}
}

