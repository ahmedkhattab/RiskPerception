package tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
	public static final String DATA_STORE = "datastore";

	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
		  Integer.parseInt(str);  
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
			e.printStackTrace();
			return null;
		}
	}
	public static boolean saveObject(String key, Serializable object){
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                new FileOutputStream(DATA_STORE+"/"+key));
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
			objectOutputStream.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	public static Object loadObject(String key){
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(
			        new FileInputStream(DATA_STORE+"/"+key));
			Object object = objectInputStream.readObject();
			objectInputStream.close();
			return object;
		} catch (IOException e) {
			System.out.println(key + " File was not found");
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	public static boolean isValidJSON(String json) {
		   boolean valid = false;
		   try {
		      JsonParser parser = new ObjectMapper().getFactory()
		            .createParser(json);
		      while (parser.nextToken() != null) {
		      }
		      valid = true;
		   } catch (JsonParseException jpe) {
		      //jpe.printStackTrace();
		   } catch (IOException ioe) {
		      //ioe.printStackTrace();
		   }

		   return valid;
		}
}
