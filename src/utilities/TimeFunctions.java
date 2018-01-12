package utilities;

import java.util.GregorianCalendar;

public class TimeFunctions {

	
	public static GregorianCalendar cloneAndAddHoures(GregorianCalendar date, int houresToAdd){
		//TODO: Remove timeString (was just for debug reasons)
		String timeString = Connection.dateToString(date) + " + " + houresToAdd;
		GregorianCalendar newDate = (GregorianCalendar) date.clone();
		newDate.add(GregorianCalendar.HOUR_OF_DAY, houresToAdd);
		timeString += " --> " + Connection.dateToString(newDate);
		//System.out.println(timeString);
		return newDate;
	}
	
	
	public static long getLocalMillisec(GregorianCalendar date){
		long timeInMillisec = date.getTimeInMillis();
		timeInMillisec += date.get(GregorianCalendar.DST_OFFSET) + date.get(GregorianCalendar.ZONE_OFFSET);
		
		return timeInMillisec;
	}
}
