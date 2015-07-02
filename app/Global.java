import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.libs.Akka;
import scala.concurrent.duration.FiniteDuration;
import tools.Utils;
import uk.co.panaxiom.playjongo.PlayJongo;
import akka.actor.Cancellable;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.InsertOptions;

import controllers.managers.ClassificationManager;
import controllers.managers.ClassifierCollection;
import controllers.managers.DataManager;

public class Global extends GlobalSettings {

	/*
	 * The following variables are used to control the tracking job
	 * 
	 * - the tracking frequency (in days) is used to control how often
	 * the job is repeated (1 means every day)
	 * 
	 * - The tracking delay, is used to determine the time delay between
	 * starting the application and running the tracking job, this can be
	 * used to run the job at a specific hour of the day.
	 */
	final static int TRACKING_QUERY_FREQUENCY = 1;
	final static int TRACKING_DELAY = 0;
	
	
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
		
		Runnable trackingJob = new Runnable() {
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
								
								//calculate the From and To dates
								Date current = new Date();
								SimpleDateFormat sdfDate = new SimpleDateFormat(
										"yyyy-MM-dd");
								Calendar from = Calendar.getInstance();
								from.setTime(current);
								from.add(Calendar.DAY_OF_WEEK, -7);
								dataManager.collectRawData(query,
										sdfDate.format(from.getTime()),
										sdfDate.format(current), lang);
								
								//use DataManager to fetch data from Twitter
								//the API calls are done using the Twitter4j library
								ArrayList<DBObject> tweets = dataManager
										.getRawData();
								Logger.info("inserting: " + tweets.size()
										+ " tweets");
								DBCollection tweetsCollection = PlayJongo
										.getCollection(projectName)
										.getDBCollection();
								try{
									//setting continueOnError to skip errors for duplicate IDs
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
		
		
		/*
		 * This code is used to dynamically set the delay 
		 * so the job runs always at 16:00 (4:00 pm) everyday
		 * 
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
		*
		*/
		FiniteDuration delay = FiniteDuration.create(TRACKING_DELAY, TimeUnit.SECONDS);
		FiniteDuration frequency = FiniteDuration.create(TRACKING_QUERY_FREQUENCY, TimeUnit.DAYS);
		job = Akka
				.system()
				.scheduler()
				.schedule(delay, frequency, trackingJob,
						Akka.system().dispatcher());
	}

	@Override
	public void onStop(Application app) {
		Logger.info("Application shutdown...");
		if (job != null)
			job.cancel();
	}
}
