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
import com.mongodb.DBObject;

import controllers.managers.ClassificationManager;
import models.ClassifiedStatus;
import models.PreprocessingChoice;
import models.Tweet;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import tools.Utils;
import twitter4j.TwitterObjectFactory;
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
		response().setContentType("application/json");
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
		Iterable<ClassifiedStatus> classifiedTweets = classify(tweets);
		ObjectNode jsonResult = Json.newObject();
		ArrayNode dataArray = jsonResult.putArray("tweets");
		for (ClassifiedStatus classifiedStatus : classifiedTweets) {
			dataArray.add(classifiedStatus.getClassOfstatus());
			dataArray.add(TwitterObjectFactory.getRawJSON(classifiedStatus.getStatus()));
		}

		response().setContentType("application/json");
		return ok(jsonResult);

	}
}