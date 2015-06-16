package models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jongo.ResultHandler;

import play.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import de.undercouch.bson4jackson.BsonFactory;
import tools.Utils;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.Status;
import uk.co.panaxiom.playjongo.PlayJongo;

public class Tweet{

    public static MongoCollection tweets(String projectName) {
        return PlayJongo.getCollection(projectName);
    }

    @JsonProperty("_id")
    public ObjectId id;

    public void insert(String projectName) {
        tweets(projectName).save(this);
    }

    public void remove(String projectName) {
        tweets(projectName).remove(this.id);
    }
    
    public static MongoCursor<Status> findByDate(String fromDate, String toDate, String projectName) {
    	Date from;
    	Date to;
		try {
			from = new SimpleDateFormat("dd.MM.yyyy").parse(fromDate);
			to = new SimpleDateFormat("dd.MM.yyyy").parse(toDate);
		} catch (ParseException e) {
			return null;
		}
        return tweets(projectName).find("{created_at: {$lt: #|#, $gt: #|#}}", to, from ).map(
        	    new ResultHandler<Status>() {
        	        @Override
        	        public Status map(DBObject result) {
        	        	  final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        	        	  SimpleDateFormat sf = new SimpleDateFormat(TWITTER);
        	        	  String newDate = sf.format(result.get("created_at"));
        	        	  DBObject formatedResult = (DBObject) JSON.parse(result.toString());
        	        	  formatedResult.put("created_at", newDate);
        	            	Method m;
							try {
								Status status = TwitterObjectFactory.createStatus(formatedResult.toString());
								m = TwitterObjectFactory.class.getDeclaredMethod("registerJSONObject",new Class[]{Object.class, Object.class});
								m.setAccessible(true);
								m.invoke(null, status, new JSONObject(formatedResult.toString()));
								return status;
							} catch (TwitterException e) {
								Logger.error("error during mapping query result");
								e.printStackTrace();
							} catch (NoSuchMethodException | SecurityException e) {
								Logger.error("error during mapping query result");
								e.printStackTrace();
							} catch (IllegalAccessException
									| IllegalArgumentException
									| InvocationTargetException e) {
								Logger.error("error during mapping query result");
								e.printStackTrace();
							} catch (JSONException e) {
								Logger.error("error during mapping query result");
								e.printStackTrace();
							}
							return null;
        	        }
        	    });
    }

}