package pathCalculation.recursiveBreadthFirst;

import java.util.Comparator;
import utilities.Connection;

class ConnectionComparator implements Comparator<Connection>{

	private int priceForHour = 0;
	
	public ConnectionComparator(int priceForHour){
		this.priceForHour = priceForHour;
	}
	
	@Override
	public int compare(Connection con1, Connection con2) {
		if(con1.getVirtualPrice((double) priceForHour) < con2.getVirtualPrice((double) priceForHour))
			return -1;
		if(con1.getVirtualPrice((double) priceForHour) > con2.getVirtualPrice((double) priceForHour))
			return 1;
		return 0;
	}
}