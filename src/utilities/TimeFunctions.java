package utilities;

import java.util.GregorianCalendar;

public class TimeFunctions {

	
	public static GregorianCalendar cloneAndAddHoures(GregorianCalendar date, int houresToAdd){
		GregorianCalendar newDate = (GregorianCalendar) date.clone();
		newDate.add(GregorianCalendar.HOUR_OF_DAY, houresToAdd);
		return newDate;
	}
	
	
	public static long getLocalMillisec(GregorianCalendar date){
		long timeInMillisec = date.getTimeInMillis();
		timeInMillisec += date.get(GregorianCalendar.DST_OFFSET) + date.get(GregorianCalendar.ZONE_OFFSET);
		return timeInMillisec;
	}
}
