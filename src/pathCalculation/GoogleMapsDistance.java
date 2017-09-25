package pathCalculation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdom2.JDOMException;

import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class GoogleMapsDistance {
	
	public GoogleMapsDistance(){
		
	}
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request){
		
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		api.GoogleMapsDistance distance = new api.GoogleMapsDistance();
		LinkedList<Place> originPlaceList = new LinkedList<Place>();
		originPlaceList.add(request.getOrigin());
		LinkedList<Place> destinationPlaceList = new LinkedList<Place>();
		destinationPlaceList.add(request.getDestination());
		
		if(request.transportationIsCar()){
			try {
				connectionList.addAll(distance.getConnection(originPlaceList, destinationPlaceList, request.getDepartureDateString(), true, api.utilities.GoogleMaps.DRIVING, "", "de"));
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		if(request.transportationIsPublicTransit()){
			try {
				connectionList.addAll(distance.getConnection(originPlaceList, destinationPlaceList, request.getDepartureDateString(), true, api.utilities.GoogleMaps.TRANSIT, "", "de"));
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		if(request.transportationIsBicycle()){
			try {
				connectionList.addAll(distance.getConnection(originPlaceList, destinationPlaceList, request.getDepartureDateString(), true, api.utilities.GoogleMaps.BICYCLING, "", "de"));
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		if(request.transportationIsWalk()){
			try {
				connectionList.addAll(distance.getConnection(originPlaceList, destinationPlaceList, request.getDepartureDateString(), true, api.utilities.GoogleMaps.WALKING, "", "de"));
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		
		return connectionList;
	}
}
