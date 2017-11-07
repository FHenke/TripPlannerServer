package database.utilities;

import java.util.Comparator;

import utilities.Connection;

public class CompareClosestAirportsForDuration implements Comparator<Connection> {

	@Override
	public int compare(Connection airport0, Connection airport1) {
		if(airport0.getDuration().isLongerThan(airport1.getDuration())){
			return 1;
		}if(airport0.getDuration().isShorterThan(airport1.getDuration())){
			return -1;
		}
		return 0;
	}

}
