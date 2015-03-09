package controllers.managers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;

import tools.DataTypes.TimedMessage;

/**
 * This class creates a TagCloud. It extracts the words tagged with a HashTags
 * and generates picture in a JPanel.
 * 
 * @author Christian Olenberger
 * 
 */
public class TagCloudBuilder {

	static ArrayList<String> wordList = new ArrayList<String>();
	static ArrayList<Integer> wordCounter = new ArrayList<Integer>();

	/**
	 * This method return a JScrollPane containing a tag cloud of tagged words,
	 * generated from a given word list.
	 * 
	 * @param listOfWords
	 *            The list of sentences where the taggesd words should be
	 *            extracted.
	 * @param tagSymbol
	 *            The symbol tagging the words.
	 * @return A JScrollPane containing the tag cloud.
	 */
	public static JScrollPane createTagCloud(
			ArrayList<TimedMessage> listOfWords, char tagSymbol) {
		// Used code example:
		// http://stackoverflow.com/questions/11481482/how-can-i-generate-a-tag-cloud-in-java-with-opencloud
		Random random = new Random();
		JPanel containerPanel = new JPanel();
		// Used code example:
		// http://www.java2s.com/Code/JavaAPI/java.awt/ToolkitgetScreenSize.htm
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		screenSize.width = 200;
		screenSize.height = 150;
		containerPanel.setPreferredSize(screenSize);
		containerPanel.setBackground(Color.white);
		JScrollPane resultPanel = new JScrollPane(containerPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		TagCloudBuilder.countWords(TagCloudBuilder.getTaggedWords(listOfWords,
				tagSymbol));
		// select a random taggedWord and add it to panel
		for (int i = 0; wordList.size() != 0; i = random.nextInt(wordList
				.size())) {
			JLabel label = new JLabel(wordList.get(i));

			int count = wordCounter.get(i);
			if (count > 50)
				count = 50;
			// Set font size and color using the words count
			label.setFont(label.getFont().deriveFont(count + 10.0f));
			int randomInt = random.nextInt(100);
			label.setForeground(new Color(randomInt + count * 3,
					randomInt + count * 3, randomInt
							+ count * 3));
			containerPanel.add(label);
			wordList.remove(i);
			wordCounter.remove(i);
			if (wordList.size() == 0) {
				break;
			}
		}
		return resultPanel;
	}

	/**
	 * This method initializes the class variables wordList and wordCounter by
	 * counting the words in a given list.
	 * 
	 * @param words
	 *            The list of words.
	 */
	private static void countWords(ArrayList<String> listOfWords) {
		boolean found = false;
		// Search through all words
		for (String countWord : listOfWords) {
			for (int i = 0; i < wordList.size(); i++) {
				// if not in list wordCounter than add or just increment
				if (countWord.equals(wordList.get(i))) {
					wordCounter.set(i, wordCounter.get(i) + 1);
					found = true;
					break;
				}
			}
			if (found) {
				found = false;
			} else {
				wordList.add(countWord);
				wordCounter.add(1);
			}
		}
	}
	public static String getHtmlTagCloud(ArrayList<TimedMessage> listOfWords, char tagSymbol){
		String result = "";
		TagCloudBuilder.countWords(TagCloudBuilder.getTaggedWords(listOfWords,
				tagSymbol));
		Random random = new Random();
		int cutoff = 10;
		// select a random taggedWord and add it to panel
		for (int i = 0; wordList.size() != 0; i = random.nextInt(wordList
				.size())) {			
			int count = wordCounter.get(i);
			if (count > 50)
				count = 50;
			if (count == 1 && cutoff > 0)
				cutoff--;
			if (count == 1 && cutoff == 0)
			{
				wordList.remove(i);
				wordCounter.remove(i);
				continue;
			}
			result += getLabel(wordList.get(i), count);
			wordList.remove(i);
			wordCounter.remove(i);
			if (wordList.size() == 0) {
				break;
			}
		}
		return result;

	}
	/**
	 * This method extracts words that are tagged with a given symbol and
	 * returns them in an ArrayList.
	 * 
	 * @param dataList
	 *            List of sentences.
	 * @return List of extracted words.
	 */
	private static ArrayList<String> getTaggedWords(
			ArrayList<TimedMessage> dataList, char tagSymbol) {
		// Initialisation
		ArrayList<String> result = new ArrayList<String>();
		boolean foundHashTag;
		char currChar;
		String currPost;
		String taggedWord = "";
		for (int postNumber = 0; postNumber < dataList.size(); postNumber++) {
			// Check every sentence
			currPost = dataList.get(postNumber).getMessage();
			foundHashTag = false;
			if (currPost != null) {
				for (int charPos = 0; charPos < currPost.length(); charPos++) {
					// Check every char
					currChar = currPost.charAt(charPos);
					if (isUpperCase(currChar)) // to lower
					{
						currChar = (char) (currChar + 32);
					}
					if (currChar == tagSymbol) { // Check whether tagged
						if (foundHashTag) {
							// If already tagged, flush and start new word
							result.add(taggedWord);
							taggedWord = "";
							taggedWord += currChar;
						} else { // Add char to word
							taggedWord += currChar;
							foundHashTag = true;
						}
					} else if (isAcceptedChar(currChar) && foundHashTag) {
						// if found tag then add char to word
						taggedWord += currChar;
					} else { // if not found tag
						if (foundHashTag) {
							// end of word, flush and start new word
							foundHashTag = false;
							result.add(taggedWord);
							taggedWord = "";
						}
					}

					if (charPos == currPost.length() - 1 && foundHashTag) {
						// if end of post, flush and reset
						foundHashTag = false;
						result.add(taggedWord);
						taggedWord = "";
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method returns a boolean wether the given char is a letter, digit or
	 * umlaut.
	 * 
	 * @param currChar
	 *            Given char.
	 * @return true if letter, digit or umlaut else false.
	 */
	private static boolean isAcceptedChar(char currChar) {
		if (((int) currChar >= (int) 'A' && (int) currChar <= (int) 'Z')
				|| ((int) currChar >= (int) 'a' && (int) currChar <= (int) 'z')
				|| ((int) currChar >= (int) '0' && (int) currChar <= (int) '9')
				|| (int) currChar == (int) '�' || (int) currChar == (int) '�'
				|| (int) currChar == (int) '�' || (int) currChar == (int) '�'
				|| (int) currChar == (int) '�' || (int) currChar == (int) '�') {
			return true;
		}

		return false;
	}

	/**
	 * Checks whether the given char is an upper letter
	 * 
	 * @param currChar
	 *            The char to check.
	 * @return TRUE if char is an upper letter.
	 */
	private static boolean isUpperCase(char currChar) {
		return (int) currChar >= 'A' && (int) currChar <= 'Z';
	}
	
	private static String getLabel(String word, int count){
		Random random = new Random();
		String color = "rgb(" + (random.nextInt(100) + count * 3) +","+
				(random.nextInt(100) + count * 3) +","+ (random.nextInt(100)
				+ count * 3)+")";
		String Style = "style=\"margin: 4px; ";
		Style += "font-size:"+ (count + 13)+"px; ";
		Style += "color:" + color + ";";
		String result = "<label ";
		result += Style +"\">" + word + "</label>";
		return result;
	}
}
