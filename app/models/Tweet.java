package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.co.panaxiom.playjongo.PlayJongo;

public class Tweet {

    public static MongoCollection tweets() {
        return PlayJongo.getCollection("tweets");
    }

    @JsonProperty("_id")
    public ObjectId id;

    public String name;

    public void insert() {
        tweets().save(this);
    }

    public void remove() {
        tweets().remove(this.id);
    }

    public static Tweet findByName(String name) {
        return tweets().findOne("{name: #}", name).as(Tweet.class);
    }
    
    public static MongoCursor<Tweet> findByDate(String fromDate, String toDate) {
    	Date from;
    	Date to;
		try {
			from = new SimpleDateFormat("dd.MM.yyyy").parse(fromDate);
			to = new SimpleDateFormat("dd.MM.yyyy").parse(toDate);
		} catch (ParseException e) {
			return null;
		}
        return tweets().find("{created_at: {$lte : #, $gte : #}}", to, from ).as(Tweet.class);
    }

}