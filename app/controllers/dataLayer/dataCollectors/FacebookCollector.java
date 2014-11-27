package controllers.dataLayer.dataCollectors;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

import tools.DataTypes.TimedMessage;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.StatusMessage;

/**
 * This class collects data from Facebook an returns them in a list.
 * 
 * @author Christian Olenberger
 * 
 */
public class FacebookCollector implements controllers.dataLayer.IDataCollector {

	// data fields
	private static String status = "Ready";
	private static String accessToken = "";

	/**
	 * This method collects post from facebook using the given parameters.
	 * 
	 * @param query
	 *            The query/text that should be contained in the posts.
	 * @param since
	 *            The beginning of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param until
	 *            The end of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param language
	 *            <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1
	 *            code</a> for the language.
	 * @return List of posts containing only the messeages.
	 */
	public ArrayList<TimedMessage> getData(String query, String since,
			String until, String language) {
		// Used code example: http://restfb.com/#searching
		// Connect to Facebook
		status = "Facebook: Connecting to Facebook";
		FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
		status = "Facebook: Collecting Posts";
		// Execute query
		Connection<StatusMessage> publicSearch = null;
		try {
			publicSearch = facebookClient.fetchConnection("search",
					StatusMessage.class, Parameter.with("q", query),
					Parameter.with("type", "post"),
					Parameter.with("limit", 1000));
		} catch (Exception e) {
			// Used code example:
			// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
			JOptionPane.showMessageDialog(null,
					"Could not load Facebook posts.");
			return new ArrayList<TimedMessage>();
		}
		ArrayList<TimedMessage> result = new ArrayList<TimedMessage>();
		status = "Facebook: Extracting Posts";
		for (int i = 0; i < publicSearch.getData().size(); i++) {
			String message = publicSearch.getData().get(i).getMessage();
			Date date = publicSearch.getData().get(i).getUpdatedTime();
			TimedMessage timesMessage = new TimedMessage(date, message);
			result.add(timesMessage);
		}
		return result;
	}

	/**
	 * Returns the current status of the class.
	 * 
	 * @return Status text.
	 */
	public static String getStatus() {
		return status;
	}

	/**
	 * Set the Facebook Access Token you need to collect data from
	 * "developers.facebook.com/tools/explorer/".
	 * 
	 * @param accessToken
	 *            Access Token from Facebook.
	 */
	public static void setAccessToken(String accessToken) {
		FacebookCollector.accessToken = accessToken;
	}

}
