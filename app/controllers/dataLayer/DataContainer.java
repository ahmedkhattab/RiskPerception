package controllers.dataLayer;

import java.util.ArrayList;

import controllers.dataLayer.dataCollectors.FacebookCollector;
import controllers.dataLayer.dataCollectors.TumblrCollector;
import controllers.dataLayer.dataCollectors.TwitterCollector;
import tools.DataTypes.TimedMessage;

/**
 * This class generates an DataContainer-Object containing the data collected
 * from social media. You can add data to the container from social media by
 * using the implemented methods.
 * 
 * @author Christian Olenberger
 * 
 */
public class DataContainer {

	// Defined labels
	private static final int STATUS_TWITTER = 0;
	private static final int STATUS_FACEBOOK = 1;
	private static final int STATUS_TUMBLR = 3;
	private static final int STATUS_FINISHED = 4;

	/**
	 * An ArrayList containing the collected data.
	 */
	private ArrayList<TimedMessage> data;

	/**
	 * 0 for Twitter, 1 for Facebook, 3 for Tumblr
	 */
	private int statusCode = STATUS_TWITTER;

	/**
	 * Default constructor.
	 */
	public DataContainer() {
		this.data = new ArrayList<TimedMessage>();
	}

	/**
	 * Create DataContainer-Object with already existing data.
	 * 
	 * @param data
	 *            Already existing data.
	 */
	public DataContainer(ArrayList<TimedMessage> data) {
		this.data = data;
	}

	/**
	 * This method adds a list of Tweets to the existing data, in specific time
	 * interval, containing a given text and in a given language.
	 * 
	 * @param apiQuery
	 *            The query matching the <a
	 *            href="https://dev.twitter.com/docs/using-search">TwitterAPI
	 *            rules</a>.
	 * @param since
	 *            The beginning of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param until
	 *            The end of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param language
	 *            <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1
	 *            code</a> for the language.
	 */
	public void addDataFromTwitter(String apiQuery, String since, String until,
			String language) {
		statusCode = STATUS_TWITTER;
		IDataCollector twitterCollector = new TwitterCollector();
		data.addAll(twitterCollector.getData(apiQuery, since, until, language));
	}

	/**
	 * This method adds a list of public messages to the existing data, in
	 * specific time interval, containing a given text and in a given language.
	 * 
	 * @param apiQuery
	 *            String of words that should be contained.
	 * @param since
	 *            The beginning of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param until
	 *            The end of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param language
	 *            <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1
	 *            code</a> for the language.
	 */
	public void addDataFromFacebook(String apiQuery, String since,
			String until, String language) {
		statusCode = STATUS_FACEBOOK;
		IDataCollector facebookCollector = new FacebookCollector();
		data.addAll(facebookCollector.getData(apiQuery, since, until, language));
		statusCode = STATUS_FINISHED;
	}

	/**
	 * This method adds a list of public messages to the existing data, in
	 * specific time interval, containing a given text and in a given language.
	 * 
	 * @param apiQuery
	 *            String of words that should be contained.
	 * @param since
	 *            The beginning of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param until
	 *            The end of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param language
	 *            <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1
	 *            code</a> for the language.
	 */
	public void addDataFromTumblr(String apiQuery, String since, String until,
			String language) {
		statusCode = STATUS_TUMBLR;
		IDataCollector tumblrCollector = new TumblrCollector();
		data.addAll(tumblrCollector.getData(apiQuery, since, until, language));
		statusCode = STATUS_FINISHED;
	}

	/**
	 * Get all data stored in this object.
	 * 
	 * @return The stored data.
	 */
	public ArrayList<TimedMessage> getData() {
		return data;
	}

	/**
	 * Set new data and remove old data.
	 * 
	 * @param dataList
	 *            The data to set.
	 */
	public void setData(ArrayList<TimedMessage> dataList) {
		data = dataList;
	}

	/**
	 * Add new data to the already existing data.
	 * 
	 * @param dataList
	 *            The data you want to add.
	 */
	public void addData(ArrayList<TimedMessage> dataList) {
		data.addAll(dataList);
	}

	/**
	 * Returns the current status of the data collection.
	 * 
	 * @return Status text.
	 */
	public String getStatus() {
		switch (statusCode) {
		case STATUS_TWITTER:
			return TwitterCollector.getStatus();
		case STATUS_FACEBOOK:
			return FacebookCollector.getStatus();
		case STATUS_TUMBLR:
			return TumblrCollector.getStatus();
		case STATUS_FINISHED:
			return "Collected " + data.size() + " message(s).";
		default:
			return "Wrong use of statusCode variable";
		}
	}

}
