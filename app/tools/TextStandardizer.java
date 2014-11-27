package tools;

import java.util.ArrayList;

import tools.DataTypes.TimedMessage;

/**
 * This is class standardize a given. It converts all letters to lower case,
 * removes commas and dots and multiple use of '!' and '?'
 * 
 * @author Christian Olenberger
 * 
 */
public class TextStandardizer {

	private static char[] separators = new char[] { '.', ',', ':', ')', '}',
			']', '/', '\'', '-', '\"' };
	private static char[] prefix = new char[] { '(', '{', '[', '\'', '\"', '*',
			'-' };

	/**
	 * This method standardizes the texts in a given ArrayList by using the
	 * standardize method.
	 * 
	 * @param testList
	 *            ArrayList of Strings containing the text.
	 * @return ArrayList with standardized text.
	 */
	public static ArrayList<TimedMessage> standardizeTextList(
			ArrayList<TimedMessage> textList) {
		ArrayList<TimedMessage> resultList = new ArrayList<TimedMessage>();

		for (TimedMessage text : textList) // All messages
		{
			TimedMessage timedMessage = new TimedMessage(text.getDate(),
					standardizeTextString(text.getMessage()));
			resultList.add(timedMessage);
		}
		return resultList;
	}

	/**
	 * This method standardizes the texts in a given ArrayList by using the
	 * standardize method.
	 * 
	 * @param testList
	 *            ArrayList of Strings containing the text.
	 * @return ArrayList with standardized text.
	 */
	public static ArrayList<String> standardizeTextStringList(
			ArrayList<String> textList) {
		ArrayList<String> resultList = new ArrayList<String>();

		for (String text : textList) // All messages
		{
			String message = standardizeTextString(text);
			resultList.add(message);
		}
		return resultList;
	}

	/**
	 * This method standradize the texts in a given Array by using the
	 * standardize method.
	 * 
	 * @param array
	 *            Array of Strings containing the text.
	 * @return Array with standardized text.
	 */
	public static String[] standardizeTextArray(String[] array) {
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			String resultString = standardizeTextString(array[i]);
			result[i] = resultString;
		}

		return result;
	}

	/**
	 * The method standardize a given text text. That means converting to lower
	 * Case, remove unnecessary spaces and remove unnecessary prefix and suffix.
	 * 
	 * @param text
	 *            The text that should be standardized.
	 * @return The standardized text.
	 */
	public static String standardizeTextString(String text) {
		// Code is long but it is easier to maintain/correct/get overview if all
		// conditions are at the same place.
		// Another reason is that in every condition the variables resultString,
		// foundSpace, isSymbol are changed so it is hard to separator the
		// conditions into methods because a method can only return one value
		String resultString = "";
		boolean foundSpace = false;
		boolean isSymbol = false;
		if (text == null) {
			return "";
		}
		for (int i = 0; i < text.length(); i++) // Check every character
		{// no if else to avoid bugs
			char c = text.charAt(i);

			if (isUpperLetter(c)) {// To lower
				c = (char) (c + 32);
			}

			if (c == ' ') {// is space?
				if (foundSpace) {// already found space?
					continue;
				}
				resultString = resultString + " ";
				foundSpace = true;
				isSymbol = false;
				continue;
			}

			if (isLetterOrNumber(c)) {// is letter or number?
				resultString = resultString + c;
				foundSpace = false;
				isSymbol = false;
				continue;
			}

			if (i > 0 && i < text.length() - 1 && c == '\'')// if ' is in a word
															// (e.x. can't)
			{
				if (isLetterOrNumber(text.charAt(i - 1))
						&& isLetterOrNumber(text.charAt(i + 1))) {
					resultString = resultString + c;
					foundSpace = false;
					isSymbol = false;
					continue;
				}
			}

			if (i != 0) {
				if (isInArray(c, prefix)) {
					if (i < text.length() - 1) {// is not last char
						if (isLetterOrNumber(text.charAt(i - 1))
								&& isLetterOrNumber(text.charAt(i + 1))) {
							// if prefix is between two letters or numbers it
							// is unlikely to be part of a smiley/symbol
							resultString = resultString + " ";
							foundSpace = false;
							isSymbol = false;
							continue;
						}
						if (!isLetterOrNumber(text.charAt(i - 1))) {// if prefix
							if (isLetterOrNumber(text.charAt(i + 1))) {
								// is there a next visible char than it
								// is a unnecessary prefix
								foundSpace = false;
								isSymbol = false;
								continue;
							}
						}
						// it could be a smiley
						if (!isLetterOrNumber(text.charAt(i - 1))) {
							resultString = resultString + c;
							foundSpace = false;
							isSymbol = false;
							continue;
						}
					}
				}
			} else {
				if (isInArray(c, prefix)) {// if prefix
					if (i < text.length() - 1) {// is not last char
						if (isLetterOrNumber(text.charAt(i + 1))) {
							// is there a next visible char than it
							// is a unnecessary prefix
							foundSpace = false;
							isSymbol = false;
							continue;
						}
					}
					// it could be a smiley
					resultString = resultString + c;
					foundSpace = false;
					isSymbol = false;
					continue;
				}
			}

			if (isInArray(c, separators)) {// if separator
				if (i != 0 && i < text.length() - 1) {
					if (c != ':') {// is not first char
						if (isLetterOrNumber(text.charAt(i - 1))
								&& isLetterOrNumber(text.charAt(i + 1))) {
							// if suffix is between two letters or numbers it
							// is unlikely to be part of a smiley/symbol
							resultString = resultString + " ";
							foundSpace = false;
							isSymbol = false;
							continue;
						}
						if (isLetterOrNumber(text.charAt(i - 1))) {
							// if previous char is letter or number
							// it is unlikely that this char is part
							// of a smiley/symbol
							foundSpace = false;
							isSymbol = false;
							continue;
						}
					}
					if (i < text.length() - 1 && c == ':')// not last char
					{
						if (text.charAt(i + 1) == ' ') {
							// if next next char is space it is
							// unlikely to be part of a smiley/symbol
							foundSpace = false;
							isSymbol = false;
							continue;
						}
					}
					// maybe part of smiley
					if (isLetterOrNumber(text.charAt(i - 1))) {
						resultString = resultString + " ";
					}
					resultString = resultString + c;
					foundSpace = false;
					isSymbol = false;
					continue;
				}
			}

			if (c >= '!' && (int) c != 127) {// Is visible char
				if (!isSymbol) {
					resultString = resultString + " ";
					isSymbol = true;
				}
				resultString = resultString + c;
				foundSpace = false;
			}

		}

		return resultString;
	}

	/**
	 * Returns whether the given char is a letter.
	 * 
	 * @param c
	 *            The given char.
	 * @return TRUE if the char is a letter or a number else FALSE;
	 */
	private static boolean isLetterOrNumber(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
				|| (c >= 'A' && c <= 'Z') || (c == '�') || (c == '�')
				|| (c == '�') || (c == '�') || (c == '�') || (c == '�');
	}

	/**
	 * Returns whether the given char is a upper letter.
	 * 
	 * @param c
	 *            The given char.
	 * @return TRUE if the char is a upper letter.
	 */
	private static boolean isUpperLetter(char c) {
		return (c >= 'A' && c <= 'Z') || c == '�' || c == '�' || c == '�';
	}

	/**
	 * Returns whether the given char is in the given array.
	 * 
	 * @param c
	 *            The given char.
	 * @return TRUE if the char is in the array else FALSE.
	 */
	private static boolean isInArray(char c, char[] array) {
		for (int i = 0; i < array.length; i++) {
			if (c == array[i]) {
				return true;
			}
		}

		return false;
	}

}
