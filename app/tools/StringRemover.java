package tools;

import java.util.ArrayList;

import tools.DataTypes.AssessedTimedMessage;
import tools.DataTypes.TimedMessage;

/**
 * This class removes a symbol from all Strings in a special data construct.
 * 
 * @author Christian Olenberger
 * 
 */
public class StringRemover {

	/**
	 * Removes a given String from all TimedMessages in a ArrayList.
	 * 
	 * @param list
	 *            ArrayList containing the TimedMessages.
	 * @param toRemove
	 *            The String that should be removed.
	 * @return A new ArrayList containing the TimedMessages without the String.
	 */
	public static ArrayList<TimedMessage> removeFromTimedMessage(
			ArrayList<TimedMessage> list, String toRemove) {
		System.out.println("here");
		ArrayList<TimedMessage> result = new ArrayList<TimedMessage>();

		for (int i = 0; i < list.size(); i++) {
			TimedMessage element = list.get(i);
			result.add(new TimedMessage(element.getDate(), element.getMessage()
					.replaceAll(toRemove, "")));
		}
		return result;
	}

	/**
	 * Removes a given String from all Strings in a ArrayList.
	 * 
	 * @param list
	 *            ArrayList containing the Strings.
	 * @param toRemove
	 *            The String that should be removed.
	 * @return A new ArrayList containing the Strings without the String.
	 */
	public static ArrayList<String> removeFromStringList(
			ArrayList<String> list, String toRemove) {
		ArrayList<String> result = new ArrayList<String>();

		for (int i = 0; i < list.size(); i++) {
			String text = list.get(i).replaceAll(toRemove, "");
			result.add(text);
		}
		return result;
	}

	/**
	 * Removes a given String from all Strings in a Array.
	 * 
	 * @param array
	 *            Array containing the Strings.
	 * @param toRemove
	 *            The String that should be removed.
	 * @return A new Array containing the Strings without the String.
	 */
	public static String[] removeFromArray(String[] array, String toRemove) {
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].replaceAll(toRemove, "");
		}

		return result;
	}

	/**
	 * Removes a given String from the given text in a Array.
	 * 
	 * @param String
	 *            Text where the String should be removed.
	 * @param toRemove
	 *            The String that should be removed.
	 * @return The text without the String.
	 */
	public static String removeFromString(String text, String toRemove) {
		return text.replaceAll(toRemove, "");
	}

	/**
	 * Removes a given String from all AssessedTimedMessages in a ArrayList.
	 * 
	 * @param list
	 *            ArrayList containing the AssessedTimedMessages.
	 * @param toRemove
	 *            The String that should be removed.
	 * @return A new ArrayList containing the AssessedTimedMessages without the
	 *         String.
	 */
	public static ArrayList<AssessedTimedMessage> removeAssessedTimedMessage(
			ArrayList<AssessedTimedMessage> list, String toRemove) {
		ArrayList<AssessedTimedMessage> result = new ArrayList<AssessedTimedMessage>();

		for (int i = 0; i < list.size(); i++) {
			AssessedTimedMessage element = list.get(i);
			result.add(new AssessedTimedMessage(element.getAssessment(),
					element.getDate(), element.getMessage().replaceAll(
							toRemove, "")));
		}

		return result;
	}

	/**
	 * Removes all chars that are not a letter or number or space from all
	 * TimedMessages in a given ArrayList.
	 * 
	 * @param list
	 *            ArrayList containing the TimedMessages.
	 * @return A new ArrayList containing the TimedMessages (containing only
	 *         numbers. letters and spaces).
	 */
	public static ArrayList<TimedMessage> removeAllFromTimedMessage(
			ArrayList<TimedMessage> list) {
		ArrayList<TimedMessage> result = new ArrayList<TimedMessage>();
		for (int i = 0; i < list.size(); i++) {
			String text = list.get(i).getMessage();
			String resultString = removeAllFromString(text);
			TimedMessage message = new TimedMessage(list.get(i).getDate(),
					resultString);
			result.add(message);
		}

		return result;
	}

	/**
	 * Removes all chars that are not a letter or number or space from all
	 * String in a given ArrayList.
	 * 
	 * @param list
	 *            ArrayList containing the Strings.
	 * @return A new ArrayList containing the Strings (containing only numbers.
	 *         letters and spaces).
	 */
	public static ArrayList<String> removeAllFromStringList(
			ArrayList<String> list) {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			String text = list.get(i);
			String resultString = removeAllFromString(text);
			result.add(resultString);
		}

		return result;
	}

	/**
	 * Removes all chars that are not a letter or number or space from all
	 * Strings in a given Array.
	 * 
	 * @param array
	 *            Array containing the Strings.
	 * @return A new Array containing the String (containing only numbers.
	 *         letters and spaces).
	 */
	public static String[] removeAllFromArray(String[] array) {
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			String text = array[i];
			String resultString = removeAllFromString(text);
			result[i] = resultString;
		}

		return result;
	}

	/**
	 * This method removes all symbols but letters, numbers and spaces from a
	 * given String.
	 * 
	 * @param text
	 *            The String you want to remove all symbols but letters and
	 *            numbers.
	 * 
	 * @return The new String containing only letters and numbers (and spaces).
	 */
	public static String removeAllFromString(String text) {
		String resultString = "";
		for (int s = 0; s < text.length(); s++) {
			char c = text.charAt(s);
			if (isLetterOrNumber(c) || c == ' ') {
				resultString = resultString + c;
			}
		}
		return resultString;
	}

	/**
	 * Checks whether a given char is a letter or a number.
	 * 
	 * @param c
	 *            The char you want to check.
	 * @return TRUE if the given char is a letter or a number;
	 */
	private static boolean isLetterOrNumber(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
				|| (c >= '0' && c <= '9') || c == '�' || c == '�' || c == '�'
				|| c == '�' || c == '�' || c == '�' || c == '�';
	}

	/**
	 * This method gets a array of String and searches for a 'http' to appear.
	 * Then is removes everything in the String until the next space.
	 * 
	 * @param array
	 *            The array containing the Strings.
	 * @return New array where the 'http' is removed from every String.
	 */
	public static String[] removeHTTPFromArray(String[] array) {
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = removeHTTPFromString(array[i]);
		}
		return result;
	}

	/**
	 * This method gets a list of TimedMessages and searches for a 'http' to
	 * appear. Then is removes everything in the message until the next space.
	 * 
	 * @param list
	 *            The arrayList containing the TimedMessages.
	 * @return New arrayList where the 'http' is removed from every
	 *         TimedMessage.
	 */
	public static ArrayList<TimedMessage> removeHTTPFromTimedMessages(
			ArrayList<TimedMessage> list) {
		ArrayList<TimedMessage> result = new ArrayList<TimedMessage>();
		for (int i = 0; i < list.size(); i++) {
			TimedMessage resultMessage = new TimedMessage(
					list.get(i).getDate(), removeHTTPFromString(list.get(i)
							.getMessage()));
			result.add(resultMessage);
		}
		return result;
	}

	/**
	 * This method gets a list of String and searches for a 'http' to
	 * appear. Then is removes everything in the String until the next space.
	 * 
	 * @param list
	 *            The arrayList containing the Strings.
	 * @return New arrayList where the 'http' is removed from every
	 *         String.
	 */
	public static ArrayList<String> removeHTTPFromArrayList(
			ArrayList<String> list) {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			result.add(removeHTTPFromString(list.get(i)));
		}
		return result;
	}

	/**
	 * This method gets a String and searches for a 'http' to appear. Then is
	 * removes everything until the next space.
	 * 
	 * @param text
	 *            The text you want t check.
	 * @return String without 'http'.
	 */
	public static String removeHTTPFromString(String text) {
		String result = text;
		int posHTTP = result.indexOf("http");
		while (posHTTP != -1) {
			char charAtPos = result.charAt(posHTTP);
			while (charAtPos != ' ') {
				result = removeCharAt(result, posHTTP);
				if (posHTTP >= result.length()) {
					break;
				} else {
					charAtPos = result.charAt(posHTTP);
				}

			}
			result = removeCharAt(result, posHTTP);
			posHTTP = result.indexOf("http");
		}
		return result;
	}

	/**
	 * Gets a String and removes the char at the given position.
	 * 
	 * @param text
	 *            The String where the char should be removed.
	 * @param pos
	 *            The position of the chat that should be removed.
	 * @return The String without the char.
	 */
	private static String removeCharAt(String text, int pos) {
		if (pos >= text.length() || pos < 0) {
			return text;
		}
		String result = text;
		if (pos != result.length()) {
			result = result.substring(0, pos)
					+ result.substring(pos + 1, result.length());
		} else {
			result = result.substring(0, pos);
		}
		return result;
	}

}
