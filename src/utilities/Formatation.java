package utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Formatation {

	
	public static Date StringToDate(String inputFormat, String date) throws ParseException{
		return (new SimpleDateFormat(inputFormat)).parse(date);
	}
}
