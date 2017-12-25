import java.util.concurrent.LinkedBlockingQueue;

import sockets.LineCoordinatesOnly;
import utilities.Connection;

public class TripPlanner {

	public static void main(String[] args) {
		
		
		// still 403 error because it is not supported anymore
		/*try {
			SkyscannerLive.test();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR " + e);
		}
		*/
		
		ServerUI serverUI = new ServerUI();
		serverUI.setVisible(true);
	}

	
	private static void openSocket(){
		LineCoordinatesOnly.version1();
	}
	
	//for getConnection
	private static void printConnectionList(LinkedBlockingQueue<Connection> connectionList){
		for(Connection connection : connectionList){
			System.out.println(connection.getOrigin().getCity() + " - " + connection.getDestination().getCity() + ": " + connection.getDistance() + " in " + connection.durationToString());
		}
		
	}
	
	//for SkyCache
	private static void printConnectionList2(LinkedBlockingQueue<Connection> connectionList){
		for(Connection connection : connectionList){
			System.out.println(connection.getOrigin().getCity() + " - " + connection.getDestination().getCity());
		}
		
	}
	
	//F�r getAllConnections
	private static void printConnectionList3(LinkedBlockingQueue<Connection> connectionList){
		for(Connection connection : connectionList){
			System.out.println(connection.getOrigin().getName() + " - " + connection.getDestination().getName() + ": " + connection.getDistance() + " in " + connection.durationToString());
			System.out.println(connection.getOrigin().getLatitude() + ":" + connection.getOrigin().getLongitude());
		}
		
	}
	
	//for getConnection
	private static void printConnectionListGDirection(LinkedBlockingQueue<Connection> connectionList){
		for(Connection connection : connectionList){
			System.out.println(connection.getOrigin().getName() + " - " + connection.getDestination().getName() + ": " + connection.getDistance() + " in " + connection.durationToString());
			if(connection.hasDepartureDate())
				System.out.println("   Departure: " + Connection.dateToString(connection.getDepartureDate()) + " " + connection.getDepartureDate().getTimeZone().getDisplayName());
			if(connection.hasArrivalDate())
				System.out.println("   Arrival: " + Connection.dateToString(connection.getArrivalDate()) + " " + connection.getArrivalDate().getTimeZone().getDisplayName());
			printConnectionListGDirection(connection.getSubConnections());
			System.out.println("-------------------------------------------------------------------------------");
		}
		
	}


}
