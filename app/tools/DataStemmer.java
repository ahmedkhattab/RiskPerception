package tools;

import java.util.ArrayList;

import tools.DataTypes.TimedMessage;
import weka.core.stemmers.SnowballStemmer;

/**
 * This class uses the snowball stemmer from weka to stemm a whole data
 * construct. (Source: http://weka.wikispaces.com/Stemmers).
 * 
 * @author Christian Olenberger
 * 
 */
public class DataStemmer {

	/**
	 * This method gets a List of Strings and stemms every word in the list
	 * using the selected stemmer.
	 * 
	 * @param list
	 *            List of Strings.
	 * @param stemmerName
	 *            The name of the stemmer you want to use.
	 * @return Stemmed list of Strings.
	 */
	public static ArrayList<TimedMessage> stemmArrayList(
			ArrayList<TimedMessage> list, String stemmerName) {
		ArrayList<TimedMessage> result = new ArrayList<TimedMessage>();
		for (int i = 0; i < list.size(); i++) {
			String resultString = stemmString(list.get(i).getMessage(),
					stemmerName);
			result.add(new TimedMessage(list.get(i).getDate(), resultString));
		}
		return list;
	}

	/**
	 * This method gets a List of Strings and stemms every word in the list
	 * using the selected stemmer.
	 * 
	 * @param list
	 *            List of Strings.
	 * @param stemmerName
	 *            The name of the stemmer you want to use.
	 * @return Stemmed list of Strings.
	 */
	public static ArrayList<String> stemmStringList(ArrayList<String> list,
			String stemmerName) {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			String resultString = stemmString(list.get(i), stemmerName);
			result.add(resultString);
		}
		return result;
	}

	/**
	 * This method gets a array of Strings and stemms every word in the array
	 * using the selected stemmer.
	 * 
	 * @param array
	 *            Array of Strings.
	 * @param stemmerName
	 *            The name of the stemmer you want to use.
	 * @return Stemmed array of Strings.
	 */
	public static String[] stemmArray(String[] array, String stemmerName) {
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			String resultString = stemmString(array[i], stemmerName);
			result[i] = resultString;
		}
		return result;
	}

	/**
	 * This method gets a List of Strings and stemms every word in the list.
	 * 
	 * @param list
	 *            List of Strings.
	 * @return Stemmed list of Strings.
	 */
	public static ArrayList<TimedMessage> stemmArrayList(
			ArrayList<TimedMessage> list) {
		return DataStemmer.stemmArrayList(list, "porter");
	}

	/**
	 * This method gets a text and stemms every word.
	 * 
	 * @param text
	 *            Text to stemm.
	 * @param stemmerName
	 *            Name of the stemmer you want to use.
	 * @return stemmed text.
	 */
	public static String stemmString(String text, String stemmerName) {
		SnowballStemmer stemmer = new SnowballStemmer(stemmerName);
		String[] words = text.split(" ");
		String resultString = "";
		for (int w = 0; w < words.length; w++) {
			stemmer = new SnowballStemmer(stemmerName);
			if (w != 0) {
				resultString = resultString + " ";
			}
			resultString = resultString + stemmer.stem(words[w]);
		}
		return resultString;
	}

}
