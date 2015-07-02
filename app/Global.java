import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.libs.Json;
import scala.concurrent.duration.FiniteDuration;
import tools.Utils;
import uk.co.panaxiom.playjongo.PlayJongo;
import akka.actor.Cancellable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.InsertOptions;
import com.mongodb.WriteResult;

import controllers.managers.ClassificationManager;
import controllers.managers.ClassifierCollection;
import controllers.managers.DataManager;

public class Global extends GlobalSettings {

	private Cancellable job;

	@Override
	public void onStart(Application app) {
		if (Utils.loadObject("default_class") == null) {
			ClassificationManager classificationManager = new ClassificationManager();
			classificationManager
					.setClassifier(ClassifierCollection.SVM_CLASSIFIER);
			File trainingDataFile = Play.application().getFile(
					"private/datasets/Datensatz 1_v1.csv");
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
		c.set(Calendar.MINUTE, 10);
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
		delayInSeconds = (nextRun.getTime() - today.getTime()) / 1000;
		Runnable showTime = new Runnable() {
			@Override
			public void run() {

				Logger.info("running job @:" + new Date().toString());
				File dir = Play.application().getFile("private/tracking");
				File[] directoryListing = dir.listFiles();
				if (directoryListing != null) {
					for (File child : directoryListing) {
						String projectName = child.getName();
						
						if(projectName.endsWith("~"))
							continue;
						Logger.info("querying for project: " + projectName);
						HashMap<String, String> language_map = (HashMap<String, String>) Utils.loadObject("language_map");
						String lang = language_map.get(projectName);
						Logger.info("querying for language: " + lang);
						
						if(lang.equals("any"))
							lang = null;
						try {
							FileInputStream fis = new FileInputStream(child);

							// Construct BufferedReader from InputStreamReader
							BufferedReader br = new BufferedReader(
									new InputStreamReader(fis));

							String keywords = br.readLine();

							while (keywords != null && keywords.startsWith("$- ")) {

								String query = keywords.substring(3);
								Logger.info(query);

								DataManager dataManager = new DataManager();
								dataManager.setTwitterMaxPages(100);
								Date current = new Date();
								SimpleDateFormat sdfDate = new SimpleDateFormat(
										"yyyy-MM-dd");
								Calendar now = Calendar.getInstance();
								now.setTime(current);
								now.add(Calendar.DAY_OF_WEEK, -7);
								dataManager.collectRawData(query,
										sdfDate.format(now.getTime()),
										sdfDate.format(current), lang);
								ArrayList<DBObject> tweets = dataManager
										.getRawData();
								Logger.info("inserting: " + tweets.size()
										+ " tweets");
								DBCollection tweetsCollection = PlayJongo
										.getCollection(projectName)
										.getDBCollection();
								try{
								tweetsCollection.insert(tweets,
										new InsertOptions()
												.continueOnError(true));
								}
								catch(Exception e)
								{
									Logger.error(e.getMessage());
								}
								keywords = br.readLine();
							}
							br.close();
							
						}
						catch (IOException e) {
							Logger.error(e.getMessage());
						}
					}
				} else {
					Logger.error("no tracking files found");
				}
			}
		};
		FiniteDuration delay = FiniteDuration.create(delayInSeconds, TimeUnit.SECONDS);
		FiniteDuration frequency = FiniteDuration.create(1, TimeUnit.DAYS);
		job = Akka
				.system()
				.scheduler()
				.schedule(delay, frequency, showTime,
						Akka.system().dispatcher());
	}

	@Override
	public void onStop(Application app) {
		Logger.info("Application shutdown...");
		if (job != null)
			job.cancel();
	}
}
