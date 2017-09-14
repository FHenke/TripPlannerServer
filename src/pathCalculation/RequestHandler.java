package pathCalculation;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdom2.JDOMException;

import api.utilities.GoogleMaps;
import utilities.Connection;
import utilities.Request;

public class RequestHandler {

	
	public RequestHandler(){
		
	}
	
	public LinkedBlockingQueue<Connection> solveRequest(Request request){
		/*api.SkyscannerCache cache = new api.SkyscannerCache();
		api.SkyscannerLive live = new api.SkyscannerLive();
		api.GoogleMapsDistance distance = new api.GoogleMapsDistance();*/
		
		LinkedBlockingQueue<Connection> connectionList = null;
		
		try {
			api.GoogleMapsDirection direction = new api.GoogleMapsDirection();
			connectionList = direction.getConnection(request.getOrigin(), request.getDestination(), request.getDepartureDateString(), true, GoogleMaps.DRIVING, "", "de");
		} catch (IllegalStateException | IOException | JDOMException e) {
			System.out.println("FAIL");
			e.printStackTrace();
		}
		
		return connectionList;
	}
}

