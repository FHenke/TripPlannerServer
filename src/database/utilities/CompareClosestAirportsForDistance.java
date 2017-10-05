package database.utilities;

import java.util.Comparator;

public class CompareClosestAirportsForDistance implements Comparator<ClosestAirportListElement> {

	@Override
	public int compare(ClosestAirportListElement airport0, ClosestAirportListElement airport1) {
		if(airport0.getConnection().getDistance() < airport1.getDistance()){
			return -1;
		}if(airport0.getConnection().getDistance() > airport1.getDistance()){
			return 1;
		}
		return 0;
	}

}
