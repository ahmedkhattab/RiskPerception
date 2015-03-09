import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import akka.actor.Cancellable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.InsertOptions;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

import controllers.managers.ClassificationManager;
import controllers.managers.ClassifierCollection;
import controllers.managers.DataManager;
import play.*;
import play.libs.Akka;
import play.libs.Json;
import scala.concurrent.duration.FiniteDuration;
import tools.CustomMapper;
import tools.MongoFactory;
import tools.Utils;
import twitter4j.Status;
import uk.co.panaxiom.playjongo.MongoClientFactory;
import uk.co.panaxiom.playjongo.PlayJongo;

import org.apache.commons.lang3.StringUtils;
import org.jongo.*;

public class Global extends GlobalSettings {
	
	private Cancellable job;
	@Override
	public void onStart(Application app) {
		if (Utils.loadObject("default_class") == null) {
			ClassificationManager classificationManager = new ClassificationManager();
			classificationManager
					.setClassifier(ClassifierCollection.SVM_CLASSIFIER);
			File trainingDataFile = new File(
					"public/datasets/Datensatz 1_v1.csv");
			try {
				classificationManager.setData(trainingDataFile, 0,
						ClassificationManager.SET_TRAINING_DATA, ";", true);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			System.out.println("training default classifier");
			classificationManager.trainClassifier();
			Utils.saveObject("default_class", classificationManager);
		}
		runJob();
	}

	private void runJob() {

		Long delayInSeconds;

		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 16);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		Date plannedStart = c.getTime();
		Date today = new Date();
		Date nextRun;
		if (today.after(plannedStart)) {
			c.add(Calendar.DAY_OF_WEEK, 1);
			nextRun = c.getTime();
		} else {
			nextRun = c.getTime();
		}
		delayInSeconds = (nextRun.getTime() - today.getTime()) / 1000; // To
																		// convert
																		// milliseconds
																	// to
																		// seconds.
		Runnable showTime = new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("running job @:"+ new Date().toString());
					JsonNode root = Json.parse(new FileInputStream("private/keywords.json"));
	
					  ObjectMapper objectMapper = new ObjectMapper();

					    ArrayList<String> keywords = objectMapper.readValue(
					    		root.get("keywords").toString(),
					            objectMapper.getTypeFactory().constructCollectionType(
					            		ArrayList.class, String.class));
					String query = StringUtils.join(keywords, " OR "); 
					System.out.println(query);
					/*for(JsonNode keyword : keywords){
						query += keyword.toString()+" OR ";
					}*/
					DataManager dataManager = new DataManager();
					dataManager.setTwitterMaxPages(100);
					Date current = new Date();
					SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
					Calendar now = Calendar.getInstance();
					now.setTime(current);
					now.add(Calendar.DAY_OF_WEEK, -7);
					dataManager.collectRawData(query,
							sdfDate.format(now.getTime()),
							sdfDate.format(current),
							"en");
					ArrayList<DBObject> tweets = dataManager.getRawData();
					DBCollection tweetsCollection = PlayJongo.getCollection("tweets").getDBCollection();
					tweetsCollection.insert(tweets, new InsertOptions().continueOnError(true));				
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		};
		FiniteDuration delay = FiniteDuration.create(delayInSeconds, TimeUnit.SECONDS);
		FiniteDuration frequency = FiniteDuration.create(1, TimeUnit.DAYS);
		job = Akka.system()
				.scheduler()
				.schedule(delay, frequency, showTime,
						Akka.system().dispatcher());
	}

	@Override
	public void onStop(Application app) {
		Logger.info("Application shutdown...");
		job.cancel();
	}
}