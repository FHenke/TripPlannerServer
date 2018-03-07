package utilities;

import java.util.Calendar;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.Duration;


public class UpdateConnectionAfterAddingSubConnection {

	
	public UpdateConnectionAfterAddingSubConnection(){
		
	}
	
	public void updateConnectionsAddingHead(Connection connection, Connection subConnection){
		updateDuration(connection, subConnection);
		updateDistance(connection, subConnection);
		updateDepartureDate(connection, subConnection);
		updateOrigin(connection, subConnection);
		updatePrice(connection, subConnection);
		updateCarrier(connection, subConnection);
		updateDirect(connection, subConnection);
		updateSummary(connection, subConnection);
		updateBeeline(connection, subConnection);
		updateCode(connection, subConnection);
		updateSubConnectionsSetAhead(connection, subConnection);
	}
	
	public void updateConnection(Connection connection, Connection subConnection){
		updateDuration(connection, subConnection);
		updateDistance(connection, subConnection);
		updateArrivalDate(connection, subConnection);
		updateDestination(connection, subConnection);
		updatePrice(connection, subConnection);
		updateCarrier(connection, subConnection);
		updateDirect(connection, subConnection);
		updateSummary(connection, subConnection);
		updateBeeline(connection, subConnection);
		updateCode(connection, subConnection);
		updateSubConnections(connection, subConnection);
	}
	
	public void updateDuration(Connection connection, Connection subConnection){
		Duration duration = new Duration(0);
		
		if(subConnection.getDuration() != null){
			if(connection.getDuration().getMillis() != 0){
				//first part of connection
				long durationInMillis = connection.getDuration().getMillis();
				//time between connections
				if(subConnection.getDepartureDate() != null && connection.getArrivalDate() != null && subConnection.getDepartureDate().after(connection.getArrivalDate()))
					durationInMillis += subConnection.getDepartureDate().getTimeInMillis() - connection.getArrivalDate().getTimeInMillis();
				if(subConnection.getArrivalDate() != null && connection.getDepartureDate() != null && subConnection.getArrivalDate().before(connection.getDepartureDate()))
					durationInMillis += connection.getDepartureDate().getTimeInMillis() - subConnection.getArrivalDate().getTimeInMillis();
				//duration of new part of connection
				durationInMillis += subConnection.getDuration().getMillis();
				//duration between last arrival time and arrival time from new sub connection
				duration = duration.plus(durationInMillis);
			}
			else{
				duration = duration.plus(subConnection.getDuration().getMillis());
			}
			connection.setDuration(duration);
		}
		
		
	}
	
	public void updateDistance(Connection connection, Connection subConnection){
		long distance = (long) connection.getDistance() + subConnection.getDistance();
		if(distance >= Integer.MAX_VALUE)
			connection.setDistance(Integer.MAX_VALUE);
		else
			connection.setDistance((int) distance); 
	}
	
	public void updateArrivalDate(Connection connection, Connection subConnection){
		connection.setArrivalDate(subConnection.getArrivalDate());
		// if no departure time is available set departure time as well
		if(connection.getDepartureDate() == null && subConnection.getDepartureDate() != null)
			connection.setDepartureDate(subConnection.getDepartureDate());
	}
	
	public void updateDestination(Connection connection, Connection subConnection){
		connection.setDestination(subConnection.getDestination());
	}
	
	public void updateOrigin(Connection connection, Connection subConnection){
		connection.setOrigin(subConnection.getOrigin());
	}
	
	public void updatePrice(Connection connection, Connection subConnection){
		connection.addPrice(subConnection.getPrice());
	}
	
	public void updateCarrier(Connection connection, Connection subConnection){
		if(subConnection.getFirstCarrier() != null){
			connection.getCarriers().addAll(subConnection.getCarriers());
		}
	}
	
	public void updateDirect(Connection connection, Connection subConnection){
		if(!connection.getSubConnections().isEmpty())
			connection.setDirect(false);
	}
	
	public void updateSummary(Connection connection, Connection subConnection){
		if(subConnection.getSummary() != null){
			if(connection.getSummary() == null)
				connection.setSummary(subConnection.getSummary());
			else
				connection.setSummary(connection.getSummary() + ", " + subConnection.getSummary());
		}
	}
	
	public void updateBeeline(Connection connection, Connection subConnection){
		long distance = (long) connection.getBeeline() + subConnection.getBeeline();
		if(distance >= Integer.MAX_VALUE)
			connection.setBeeline(Integer.MAX_VALUE);
		else
			connection.setBeeline((int) distance); 
	}
	
	public void updateCode(Connection connection, Connection subConnection){
		if(subConnection.getCode() != null){
			if(connection.getCode() == null)
				connection.setCode(subConnection.getCode());
			else
				connection.setCode(connection.getCode() + ", " + subConnection.getCode());
		}
	}
	
	public void updateSubConnections(Connection connection, Connection subConnection){
		connection.getSubConnections().add(subConnection);
	}
	
	public void updateDepartureDate(Connection connection, Connection subConnection){
		connection.setDepartureDate(subConnection.getDepartureDate());
		// if no departure time is available set departure time as well
		if(connection.getArrivalDate() == null && subConnection.getArrivalDate() != null)
			connection.setArrivalDate(subConnection.getDepartureDate());
	}
	
	public void updateSubConnectionsSetAhead(Connection connection, Connection subConnection){
		LinkedBlockingQueue<Connection> newSubConnections = new LinkedBlockingQueue<Connection>();
		newSubConnections.add(subConnection);
		newSubConnections.addAll(connection.getSubConnections());
		connection.setSubConnections(newSubConnections); 
	}
	
	
}
