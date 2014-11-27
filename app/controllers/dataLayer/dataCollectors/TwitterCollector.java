package controllers.dataLayer.dataCollectors;

import java.util.ArrayList;

import tools.DataTypes.TimedMessage;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * This class connects to Twitter, generates a query and makes a list out of the
 * received Tweets.
 * 
 * @author Christian Olenberger
 * 
 */
public class TwitterCollector implements controllers.dataLayer.IDataCollector {

	/**
	 * TwitterAPIs Consumer Key
	 */
	private static final String consumerKey = "mqICNtFi7hxvRhj8o8RBfSYa2";
	/**
	 * TwitterAPIs Consumer Secret
	 */
	private static final String consumerSecret = "Faq3briCeFx3VQ5ZlLt7DysJZ729c74M82jwYgt3d5KwRRa7qk";
	/**
	 * TwitterAPIs Access Token
	 */
	private static final String accessToken = "2370240043-NfcTdLnUzGX8XoLZcvVhOnnBinQA38NfRP6Oa0d";
	/**
	 * TwitterAPI Acces Token Secret
	 */
	private static final String accessTokenSecret = "qaM3do66mkw1tLntvWCbsN9B8TYGFBlGELVYSdscc2Srp";

	/**
	 * Current status of class
	 */
	private static String status = "Ready";

	/**
	 * Results per Page (max. 100).
	 */
	private static int tweetsPerPage = 100; // max. 100
	/**
	 * Maximal number of considered pages
	 */
	private static int maxPages = 10;

	/**
	 * This method returns a list of Tweets in specific time interval, build by
	 * a given query and in a given language.
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
	 * @return List of Tweets containing the given text.
	 */
	public ArrayList<TimedMessage> getData(String apiQuery, String since,
			String until, String language) {
		// Used code example: http://twitter4j.org/en/code-examples.html
		Twitter twitter = getTwitter();
		Query query = createQuery(apiQuery, since, until, language);
		ArrayList<TimedMessage> tweetList = createTweetList(twitter, query);
		status = "Ready";
		return tweetList;
	}

	/**
	 * This method uses a given query to receive a list of Tweets (including
	 * text, user, location, etc.). It extracts the text and adds it to a list.
	 * This is repeated for every page.
	 * 
	 * @param twitter
	 *            Twitter-Object to access Tweets.
	 * @param query
	 *            The query specifying the Tweets wanted.
	 * @return List of Tweets specified by the given query.
	 */
	private ArrayList<TimedMessage> createTweetList(Twitter twitter, Query query) {
		// Used code example: http://twitter4j.org/en/code-examples.html
		// Initialisation
		ArrayList<TimedMessage> tweetList = new ArrayList<TimedMessage>();
		QueryResult result;
		int pageCounter = 0;

		// Used code example:
		// http://stackoverflow.com/questions/17344921/twitter4j-multiple-queries-twitter-api-1-1
		while (query != null && pageCounter < maxPages) {
			try {
				// Send query and add text to list
				result = twitter.search(query);
				for (Status tweet : result.getTweets()) {
					TimedMessage timedMessage = new TimedMessage(
							tweet.getCreatedAt(), tweet.getText());
					tweetList.add(timedMessage);
				}
				// Jump to next page
				query = result.nextQuery();
				status = "Twitter: Searched through Page " + (pageCounter + 1);
				pageCounter++;
			} catch (TwitterException twitterEx) {
				// Show ExeptionMessage
				status = "Twitter: Searched through: Page " + (pageCounter + 1);
				// System.out.println(twitterEx.getMessage());
				return tweetList;
			}
		}

		return tweetList;
	}

	/**
	 * This method returns a Twitter-Object using the Customer Key, Customer
	 * Secret, Access Token and Access Token Secret.
	 * 
	 * @return Twitter-Object
	 */
	private static Twitter getTwitter() {
		// Used code example: http://twitter4j.org/en/code-examples.html
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(consumerKey);
		cb.setOAuthConsumerSecret(consumerSecret);
		cb.setOAuthAccessToken(accessToken);
		cb.setOAuthAccessTokenSecret(accessTokenSecret);
		status = "Twitter: Connecting to Twitter";
		return new TwitterFactory(cb.build()).getInstance();
	}

	/**
	 * This method returns a query to get Tweets.
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
	 * @return The query using the given parameters.
	 */
	private Query createQuery(String apiQuery, String since, String until,
			String language) {
		// Used code example: http://twitter4j.org/en/code-examples.html
		// Create Query
		status = "Twitter: Creating Query";
		Query query = new Query(apiQuery);
		query.setLang(language);
		if (since != "") {
			query.setSince(since);
		}
		if (until != "") {
			query.setUntil(until);
		}
		query.setCount(tweetsPerPage);

		return query;
	}

	/**
	 * Set the number of Tweets per Page (max. 100).
	 * 
	 * @param number
	 *            Number of Tweets per Page (max. 100).
	 */
	public static void setTweetsPerPage(int number) {
		if (tweetsPerPage <= 100) {
			tweetsPerPage = number;
		}
	}

	/**
	 * Set the number of pages that are considered.
	 * 
	 * @param number
	 *            Number of pages.
	 */
	public static void setMaxPages(int number) {
		maxPages = number;
	}

	/**
	 * Returns current status of the class.
	 * 
	 * @return Status text.
	 */
	public static String getStatus() {
		return status;
	}
}
