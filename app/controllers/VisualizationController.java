package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.jongo.MongoCursor;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.dataLayer.dataCollectors.TwitterCollector;
import controllers.managers.ClassificationManager;
import models.ClassifiedStatus;
import models.PreprocessingChoice;
import models.Tweet;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import tools.Utils;
import twitter4j.User;
import views.formdata.AdminFormData;
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
				PreprocessingChoice.makePreprocessingMap(), false, false, AdminFormData.makeTrackingMap()));
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

	private static ArrayList<ArrayList<ClassifiedStatus>> classify(
			Iterable<twitter4j.Status> tweets, boolean ignoreNeutrals) {
		ClassificationManager classificationManager;
		classificationManager = (ClassificationManager) Utils
				.loadObject("default_class");
		// Utils.saveObject(session("ID")+"_class", classificationManager);
		ArrayList<ClassifiedStatus> normals = new ArrayList<ClassifiedStatus>();
		ArrayList<ClassifiedStatus> replies = new ArrayList<ClassifiedStatus>();
		try {
			classificationManager.setRawTweets(tweets);
		} catch (IOException e) {
			Logger.error("could not set raw tweets");
			Logger.error(e.getMessage());
			return null;
		}

		if (!classificationManager.classifyData(false))
		{
			Logger.error("classfication failed");
			return null;
		}
		classificationManager.reset();
		Map<Long, String> classifiedData = classificationManager
				.getClassifiedDataMap();
		for (twitter4j.Status tweet : tweets) {
			String tweetClass = classifiedData.get(tweet.getId());
			if(ignoreNeutrals && tweetClass.equals("neutral"))
				continue;
			if(tweet.getInReplyToUserId() != -1)
				replies.add(new ClassifiedStatus(tweet, tweetClass));
			else
				normals.add(new ClassifiedStatus(tweet, tweetClass));
			
		}
		int total = normals.size() + replies.size();
		Logger.info("classifyed "+ total +" tweets");

		ArrayList<ArrayList<ClassifiedStatus>> result = new ArrayList<ArrayList<ClassifiedStatus>>();
		result.add(normals);
		result.add(replies);
		return result;
		
	}

	public static Result fetch(String fromDate, String toDate, String projectName, boolean ignoreNeutrals) {
		
		MongoCursor<twitter4j.Status> tweets = Tweet.findByDate(fromDate,
				toDate, projectName);
		
		Logger.info("classifying "+tweets.count()+" tweets");
		ArrayList<ArrayList<ClassifiedStatus>> classifiedAll = classify(tweets, ignoreNeutrals);
		ArrayList<ClassifiedStatus> classifiedTweets = classifiedAll.get(0);
		ArrayList<ClassifiedStatus> classifiedReplies = classifiedAll.get(1);
		
		ObjectNode result = Json.newObject();
		ObjectNode interactions = result.putObject("interactions");
		for (ClassifiedStatus classifiedStatus : classifiedTweets) {
			twitter4j.Status thisStatus = classifiedStatus.getStatus();
			User thisUser = thisStatus.getUser();
			
			if(thisStatus.getRetweetedStatus() == null)
			{
				if(interactions.get(thisStatus.getId()+"") == null)
				{
					ObjectNode interaction = interactions.putObject(thisStatus.getId()+"");
					interaction.put("creator", thisUser.getName());
					interaction.put("popularity",thisUser.getFollowersCount());
					interaction.put("class", classifiedStatus.getClassOfstatus());
					interaction.put("message",thisStatus.getText());
					interaction.put("singleton", true);
					interaction.putArray("retweetedBy");
					interaction.putArray("replies");
				}
			}
			if(thisStatus.getRetweetedStatus() != null)
			{
				twitter4j.Status retweetedStatus = thisStatus.getRetweetedStatus();
				if(interactions.get(retweetedStatus.getId()+"") == null)
				{
					User originator = retweetedStatus.getUser();
					ObjectNode interaction = interactions.putObject(retweetedStatus.getId()+"");
					interaction.put("creator", originator.getName());
					interaction.put("popularity",originator.getFollowersCount());
					interaction.put("message",retweetedStatus.getText());
					//using the same class of the retweet (right ?)
					interaction.put("class", classifiedStatus.getClassOfstatus());
					interaction.put("singleton", false);
					interaction.putArray("replies");
					interaction.putArray("retweetedBy").addObject().put("name",thisUser.getName())
					.put("popularity", thisUser.getFollowersCount());
				}
				else
				{
					ObjectNode originalTweet = (ObjectNode)interactions.get(retweetedStatus.getId()+"");
					ArrayNode edges = (ArrayNode)originalTweet.get("retweetedBy");
					originalTweet.put("singleton", false);
					edges.addObject().put("name",thisUser.getName()).put("popularity", thisUser.getFollowersCount());
				}
			}
		}
		TwitterCollector collector = null;
		for (ClassifiedStatus classifiedStatus : classifiedReplies) {
			twitter4j.Status thisStatus = classifiedStatus.getStatus();
			User thisUser = thisStatus.getUser();
			if(thisStatus.getInReplyToStatusId() != -1)
			{
				long originalStatusId = thisStatus.getInReplyToStatusId();
				ObjectNode reply;
				System.out.println(originalStatusId);
				if(interactions.get(originalStatusId+"") != null)
				{
					ObjectNode originalTweet = (ObjectNode)interactions.get(originalStatusId+"");
					ArrayNode replies = (ArrayNode)originalTweet.get("replies");
					originalTweet.put("singleton", false);
					reply = replies.addObject();
				}
				else
				{
					twitter4j.Status originalStatus = (collector == null) ? new TwitterCollector().getStatus(originalStatusId)
							: collector.getStatus(originalStatusId);
					if(originalStatus == null)
					{
						continue;
					}
					else
					{
						ObjectNode interaction = interactions.putObject(originalStatus.getId()+"");
						interaction.put("creator", originalStatus.getUser().getName());
						interaction.put("popularity",originalStatus.getUser().getFollowersCount());
						interaction.put("class", "neutral");
						interaction.put("message",originalStatus.getText());
						interaction.putArray("retweetedBy");
						interaction.put("singleton", false);
						reply = interaction.putArray("replies").addObject();
					
					}
				}
					reply.put("id", thisStatus.getId()+"");
					reply.put("creator", thisUser.getName());
					reply.put("popularity",thisUser.getFollowersCount());
					reply.put("class", classifiedStatus.getClassOfstatus());
					reply.put("message",thisStatus.getText());
					reply.put("singleton", false);
					reply.putArray("retweetedBy");
					reply.putArray("replies");
			}
		}
		int total = classifiedTweets.size()+classifiedReplies.size();
		result.put("message", "Successfully fetched " + total + " tweets");
		response().setContentType("application/json");
		return ok(result);

	}
}