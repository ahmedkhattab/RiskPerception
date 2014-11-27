package controllers.dataLayer;

import java.util.ArrayList;

import tools.DataTypes.TimedMessage;

/**
 * Standardized Interface for the data collectors
 * 
 * @author Christian Olenberger
 * 
 */
public interface IDataCollector {

	/**
	 * This method returns a list of public messages in specific time interval,
	 * build by a given query and in a given language.
	 * 
	 * @param apiQuery
	 *            The query you are searching for.
	 * @param since
	 *            The beginning of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param until
	 *            The end of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param language
	 *            <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1
	 *            code</a> for the language.
	 * @return List of public messages containing the given text.
	 */
	public ArrayList<TimedMessage> getData(String apiQuery, String since,
			String until, String language);

}
