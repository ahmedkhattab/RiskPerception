package controllers.dataLayer.dataCollectors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.DataTypes.TimedMessage;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.AnswerPost;
import com.tumblr.jumblr.types.ChatPost;
import com.tumblr.jumblr.types.Dialogue;
import com.tumblr.jumblr.types.LinkPost;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.QuotePost;
import com.tumblr.jumblr.types.TextPost;
import com.tumblr.jumblr.types.VideoPost;


/**
 * This class allows to collected data from Tumblr.
 * 
 * @author Christian Olenberger
 * 
 */
public class TumblrCollector implements controllers.dataLayer.IDataCollector {

	private String consumer_Key = "b44uLgYFlbLg6YIIkJZAHjms2c2T5MwCwwwyCUzUn2f3jqGye1";
	private String consumer_Secret = "M0SL2BK091iG0pxNLVGdGub2Zg88wEMLaibAM2diXSW11OvdDh";
	private static String status = "Ready";

	/**
	 * This method collects post from Tumblr using the given parameters.
	 * 
	 * @param query
	 *            The query/text that should be contained in the posts.
	 * @param since
	 *            Is not used in this method.
	 * @param until
	 *            The end of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param language
	 *            Is not used in this method.
	 * @return List of posts containing only the messages.
	 */
	public ArrayList<TimedMessage> getData(String apiQuery, String since,
			String until, String language) {
		// Used code example: https://github.com/tumblr/jumblr
		// Used code example: http://www.tumblr.com/docs/en/api/v2
		JumblrClient client = new JumblrClient(consumer_Key, consumer_Secret);
		status = "Connected to Tumblr";
		Map<String, Object> options = new HashMap<String, Object>();
		ArrayList<TimedMessage> result = new ArrayList<TimedMessage>();
		try {
			options.put("before", new Integer((int) parseDate(until).getTime()));
		} catch (Exception e) {
			System.out.println("Problem while setting parameter.");
		}
		options.put("filter", "text");
		status = "Collecting Post";
		for (Post post : client.tagged(apiQuery, options)) {
			try {
				String text = extractText(post);
				if (!text.isEmpty()) {
					Date date = parseDate(post.getDateGMT());
					System.out.println(date);
					TimedMessage message = new TimedMessage(date, text);
					result.add(message);
				}
			} catch (Exception e) {

			}
		}
		status = "Finished Collecting";
		return result;
	}

	/**
	 * This method tests which kind of post the given post is and extracts a
	 * message if possible.
	 * 
	 * @param post
	 *            The given collected post.
	 * @return The extracted text or "".
	 */
	private String extractText(Post post) {
		if (post.getClass().equals(new TextPost().getClass())) {
			return (((TextPost) post).getBody().replaceAll("\n", " ")
					.replaceAll("" + (char) 13, " "));
		} else if (post.getClass().equals(new QuotePost().getClass())) {
			return (((QuotePost) post).getText().replaceAll("\n", " ")
					.replaceAll("" + (char) 13, " "));
		} else if (post.getClass().equals(new LinkPost().getClass())) {
			return (((LinkPost) post).getDescription().replaceAll("\n", " ")
					.replaceAll("" + (char) 13, " "));
		} else if (post.getClass().equals(new ChatPost().getClass())) {
			return getTextFromChatDialog(((ChatPost) post).getDialogue());
		} else if (post.getClass().equals(new AnswerPost().getClass())) {
			AnswerPost message = (AnswerPost) post;
			String resultString = message.getQuestion() + " "
					+ message.getAnswer();
			return resultString.replaceAll("\n", " ").replaceAll(
					"" + (char) 13, " ");
		} else if (post.getClass().equals(new PhotoPost().getClass())) {
			return ((PhotoPost) post).getCaption().replaceAll("\n", " ")
					.replaceAll("" + (char) 13, " ");
		} else if (post.getClass().equals(new VideoPost().getClass())) {
			return ((VideoPost) post).getCaption().replaceAll("\n", " ")
					.replaceAll("" + (char) 13, " ");
		} else {
			return "";
		}
	}

	/**
	 * This method gets Dialogue and creates a String with one line.
	 * 
	 * @param list
	 *            List of Dialogue.
	 * @return String containing the phrases from the Dialogues.
	 */
	private String getTextFromChatDialog(List<Dialogue> list) {
		String result = "";
		boolean firstElement = true;
		for (Dialogue dialogue : list) {
			if (firstElement) {
				firstElement = false;
			} else {
				result = result + " ";
			}
			result = result
					+ dialogue.getPhrase().replaceAll("\n", " ")
							.replaceAll("" + (char) 13, " ");
		}
		return result;
	}

	/**
	 * This method gets a date as string in long format or short and converts it
	 * back into a Date object.
	 * 
	 * @param dateString
	 *            Date as String in long fomat.
	 * @return The parsed Date object.
	 * @throws ParseException
	 *             Problem while parsing date.
	 */
	private Date parseDate(String dateString) throws ParseException {
		if (dateString.length() > 10) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss z");
			return formatter.parse(dateString);
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			return formatter.parse(dateString);
		}
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
