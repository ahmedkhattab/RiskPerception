package controllers;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.jongo.MongoCursor;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.managers.ClassificationManager;
import models.ClassifiedStatus;
import models.PreprocessingChoice;
import models.Tweet;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import twitter4j.Status;
import play.mvc.Result;
import tools.Utils;
import twitter4j.User;
import views.formdata.VisualizationFormData;
import views.html.visualization;

/**
 * The controller for the single page of this application.
 * 
 * @author Ahmed Khattab
 */
public class VisualizationController extends Controller {

	/**
	 * Returns the form for emotion measurement, allowing the user to load data
	 * from file/use previously queried data, select preprocessing operations to
	 * perform, and visualize/download the results
	 * 
	 * @return The page containing the emotion measurement form.
	 */
	public static Result visualization() {
		if (!session().containsKey("ID")) {
			session("ID", UUID.randomUUID().toString());
			System.out.println(session("ID"));
		}

		VisualizationFormData visData = new VisualizationFormData();
		Form<VisualizationFormData> formData = Form.form(
				VisualizationFormData.class).fill(visData);
		return ok(visualization.render(formData,
				PreprocessingChoice.makePreprocessingMap(), false, false));
	}

	/**
	 * Process a form submission. First we bind the HTTP POST data to an
	 * instance of VisualizationFormData. The binding process will invoke the
	 * VisualizationFormData.validate() method. If errors are found, re-render
	 * the page, displaying the error data. If errors not found, render the page
	 * with the good data.
	 * 
	 * @return The index page with the results of validation.
	 */

	public static Result postVisualization() {
		return TODO;
	}

	private static Iterable<ClassifiedStatus> classify(
			Iterable<twitter4j.Status> tweets) {
		ClassificationManager classificationManager;
		classificationManager = (ClassificationManager) Utils
				.loadObject("default_class");
		// Utils.saveObject(session("ID")+"_class", classificationManager);
		ArrayList<ClassifiedStatus> results = new ArrayList<ClassifiedStatus>();
		try {
			classificationManager.setRawTweets(tweets);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		if (!classificationManager.classifyData(false))
			return null;
		classificationManager.reset();
		Utils.saveObject(session("ID") + "_class_plot", classificationManager);
		Map<Long, String> classifiedData = classificationManager
				.getClassifiedDataMap();
		for (twitter4j.Status tweet : tweets) {
			String tweetClass = classifiedData.get(tweet.getId());
			results.add(new ClassifiedStatus(tweet, tweetClass));
		}
		return results;
	}

	public static Result fetch(String fromDate, String toDate) {
		MongoCursor<twitter4j.Status> tweets = Tweet.findByDate(fromDate,
				toDate);
		Logger.info("classifying "+tweets.count()+" tweets");
		Iterable<ClassifiedStatus> classifiedTweets = classify(tweets);
		ObjectNode users = Json.newObject();
		
		for (ClassifiedStatus classifiedStatus : classifiedTweets) {
			twitter4j.Status thisStatus = classifiedStatus.getStatus();
			User thisUser = thisStatus.getUser();
			
			if(!thisStatus.isRetweet() && !users.has(thisUser.getName()))
			{
				ObjectNode user = users.putObject(thisUser.getName());
				user.put("popularity",thisUser.getFollowersCount());
				user.put("class", classifiedStatus.getClassOfstatus());
				user.putArray("retweetedBy");
			}
			if(thisStatus.isRetweet())
			{
				User origin = thisStatus.getRetweetedStatus().getUser();
				if(users.get(origin.getName()) == null)
					{
						ObjectNode user = users.putObject(origin.getName());
						user.put("popularity",origin.getFollowersCount());
						user.put("class", classifiedStatus.getClassOfstatus());
						user.putArray("retweetedBy").add(thisUser.getName());
						continue;
					}
				ArrayNode edges = (ArrayNode)users.get(origin.getName()).get("retweetedBy");
				edges.add(thisUser.getName());
			}
		}

		response().setContentType("application/json");
		return ok(users);

	}
}