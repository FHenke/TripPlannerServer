package database.utilities;

import java.util.Comparator;

public class CompareClosestAirportsForDuration implements Comparator<ClosestAirportListElement> {

	@Override
	public int compare(ClosestAirportListElement airport0, ClosestAirportListElement airport1) {
		if(airport0.getConnection().getDuration().isLongerThan(airport1.getDuration())){
			return 1;
		}if(airport0.getConnection().getDuration().isShorterThan(airport1.getDuration())){
			return -1;
		}
		return 0;
	}

}
