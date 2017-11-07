package database.utilities;

import java.util.Comparator;

import utilities.Connection;

public class CompareClosestAirportsForDistance implements Comparator<Connection> {

	@Override
	public int compare(Connection connection0, Connection connection1) {
		if(connection0.getDistance() < connection1.getDistance()){
			return -1;
		}if(connection0.getDistance() > connection1.getDistance()){
			return 1;
		}
		return 0;
	}

}
