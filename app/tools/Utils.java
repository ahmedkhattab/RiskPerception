package tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    int d = Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	public static String formatDate(String date)
	{
		Date dateObj;
		try {
			dateObj = new SimpleDateFormat("dd.MM.yyyy").parse(date);
			return new SimpleDateFormat("yyyy-MM-dd").format(dateObj);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
