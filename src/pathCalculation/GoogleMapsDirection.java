package pathCalculation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdom2.JDOMException;

import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class GoogleMapsDirection {
	
	public GoogleMapsDirection(){
		
	}
	
	public LinkedBlockingQueue<Connection> getConnectionList(Request request){
		
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		api.GoogleMapsDirection direction = new api.GoogleMapsDirection();
		
		if(request.transportationIsCar()){
			try {
				connectionList.addAll(direction.getConnection(request.getOrigin(), request.getDestination(), request.getDepartureDateString(), true, api.utilities.GoogleMaps.DRIVING, "", "de", request.isShowAlternatives()));
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		if(request.transportationIsPublicTransit()){
			try {
				connectionList.addAll(direction.getConnection(request.getOrigin(), request.getDestination(), request.getDepartureDateString(), true, api.utilities.GoogleMaps.TRANSIT, "", "de", request.isShowAlternatives()));
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		if(request.transportationIsBicycle()){
			try {
				connectionList.addAll(direction.getConnection(request.getOrigin(), request.getDestination(), request.getDepartureDateString(), true, api.utilities.GoogleMaps.BICYCLING, "", "de", request.isShowAlternatives()));
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		if(request.transportationIsWalk()){
			try {
				connectionList.addAll(direction.getConnection(request.getOrigin(), request.getDestination(), request.getDepartureDateString(), true, api.utilities.GoogleMaps.WALKING, "", "de", request.isShowAlternatives()));
			} catch (IllegalStateException | IOException | JDOMException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		
		//includes the highest level
		LinkedBlockingQueue<Connection> headConnectionList = new LinkedBlockingQueue<Connection>();
		for(Connection connection : connectionList){
			Connection headConnection = new Connection(connection.getType(), connection.getOrigin(), connection.getDestination(), connection.getPrice(), connection.getDuration(),
					connection.getDepartureDate(), connection.getArrivalDate());
			headConnection.setSummary(connection.getSummary());
			headConnection.addSubconnection(connection);
			headConnectionList.add(headConnection);
		}
		
		return headConnectionList;
	}
}
