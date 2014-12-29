package controllers.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JOptionPane;

import controllers.dataLayer.dataCollectors.FacebookCollector;
import controllers.dataLayer.dataCollectors.TwitterCollector;
import tools.InputOptionCollection;
import tools.DataTypes.TimedMessage;
import controllers.dataLayer.DataContainer;

/**
 * This class is used to manage the data which was collected and stored in the
 * DataContainer.
 * 
 * @author Christian Olenberger
 * 
 */
public class DataManager implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6899478501053723581L;
	/**
	 * The DataContainer the DataManager is responsible for.
	 */
	private DataContainer dataContainer;
	private boolean useBehindSeparator = false;

	/**
	 * Create DataManager with new DataContainer.
	 */
	public DataManager() {
		this.dataContainer = new DataContainer();
	}

	/**
	 * HashMap of the Ids from the collected data (from file). Value is just a
	 * dummy.
	 */
	// Uses HashMap because it is very effective for find object.
	public HashMap<String, Object> IdsOfObjects = new HashMap<String, Object>();

	/**
	 * Create DataManager with given DataContainer.
	 * 
	 * @param dataContainer
	 *            The DataContainer the DataManager should be responsible for.
	 */
	public DataManager(DataContainer dataContainer) {
		this.dataContainer = dataContainer;
	}

	/**
	 * This method adds a list of Tweets to the existing dataContainer,
	 * containing a given Text. It does not consider a specific time interval
	 * and default language english.
	 * 
	 * @param apiQuery
	 *            The query matching the <a
	 *            href="https://dev.twitter.com/docs/using-search">TwitterAPI
	 *            rules</a>.
	 */
	public void addDataFromTwitter(String apiQuery) {
		dataContainer.addDataFromTwitter(apiQuery, "", "", "en");
	}

	/**
	 * This method adds a list of Tweets to the existing dataContainer,
	 * containing a given Text in a given language. It does not consider a
	 * specific time interval.
	 * 
	 * @param apiQuery
	 *            The query matching the <a
	 *            href="https://dev.twitter.com/docs/using-search">TwitterAPI
	 *            rules</a>.
	 * @param language
	 *            <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1
	 *            code</a> for the language.
	 */
	public void addDataFromTwitter(String apiQuery, String language) {
		dataContainer.addDataFromTwitter(apiQuery, "", "", language);
	}

	/**
	 * This method adds a list of Tweets to the existing dataContainer, in a
	 * specific time interval, containing a given text. Language is by default
	 * english.
	 * 
	 * @param apiQuery
	 *            The query matching the <a
	 *            href="https://dev.twitter.com/docs/using-search">TwitterAPI
	 *            rules</a>.
	 * @param since
	 *            The beginning of the time interval (Date-Pattern: YYYY-MM-DD).
	 * @param until
	 *            The end of the time interval (Date-Pattern: YYYY-MM-DD).
	 */
	public void addDataFromTwitter(String apiQuery, String since, String until) {
		dataContainer.addDataFromTwitter(apiQuery, since, until, "en");
	}

	/**
	 * This method adds a list of Tweets to the existing dataContainer, in
	 * specific time interval, containing a given text and in a given language.
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
		dataContainer.addDataFromTwitter(apiQuery, since, until, language);
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
		dataContainer.addDataFromFacebook(apiQuery, since, until, language);
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
		dataContainer.addDataFromTumblr(apiQuery, since, until, language);
	}

	/**
	 * This method adds data from selected source to the dataContainer
	 * 
	 * @param addTwitter
	 *            true if you want to add data from Twitter
	 * @param addFacebook
	 *            true if you want to add data from Facebook
	 * @param addTumblr
	 *            true if you want to add data from Tumblr
	 */
	public void collectData(String text, String since, String until,
			String language, boolean addTwitter, boolean addFacebook,
			boolean addTumblr) {
		removeData();
		if (addTwitter) {
			addDataFromTwitter(text, since, until, language);
		}
		if (addFacebook) {
			addDataFromFacebook(text, since, until, language);
		}
		if (addTumblr) {
			addDataFromTumblr(text, since, until, language);
		}
	}

	/**
	 * Return the data stored in the dataContainer.
	 * 
	 * @return List of stored data.
	 */
	public ArrayList<TimedMessage> getData() {
		return dataContainer.getData();
	}

	/**
	 * Removes the current data.
	 */
	public void removeData() {
		this.dataContainer.setData(new ArrayList<TimedMessage>());
	}

	/**
	 * Returns the status of the data container.
	 * 
	 * @return Text with status information.
	 */
	public String getStatus() {
		return "Status: " + dataContainer.getStatus();
	}

	/**
	 * Sets the max. number of pages requested from Twitter.
	 * 
	 * @param maxPages
	 *            Number of pages max. requested.
	 */
	public void setTwitterMaxPages(int maxPages) {
		TwitterCollector.setMaxPages(maxPages);
	}

	/**
	 * Sets the Facebook-Acces-Token (see:
	 * https://developers.facebook.com/tools/explorer/).
	 * 
	 * @param token
	 *            Access-Token from Facebook.
	 */
	public void setFacebookAccessToken(String token) {
		FacebookCollector.setAccessToken(token);
	}

	/**
	 * This method opens a dialog where the user can select one or mor text
	 * files. The text is split into the sentences and then set a the current
	 * data.
	 * 
	 * @return TRUE if the text could be set as the current data else false.
	 * @throws IOException
	 *             Problem while reading file.
	 * @throws ParseException
	 *             Problem while parsing date.
	 */
	public boolean setTextFromFile() throws IOException, ParseException {
		// Used code example: http://www.javaschubla.de/2007/javaerst0250.html
		File[] selectedFiles = InputOptionCollection.selectMutipleFiles();
		if (selectedFiles == null || selectedFiles.length == 0) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null, "Could not find file.");
			dataContainer.setData(new ArrayList<TimedMessage>());
			return false;
		}
		ArrayList<TimedMessage> resultList = new ArrayList<TimedMessage>();
		for (int i = 0; i < selectedFiles.length; i++) {
			FileReader fileReader = new FileReader(selectedFiles[i]);
			BufferedReader textReader = new BufferedReader(fileReader);
			String dateString = textReader.readLine();
			Date date = parseDate(dateString);
			String line = textReader.readLine();
			String resultText = "";
			while (line != null) {
				if (!line.isEmpty()) {
					resultText = resultText + " " + line;
				}
				line = textReader.readLine();
			}
			ArrayList<String> sentences = splitIntoSentences(resultText);
			for (int s = 0; s < sentences.size(); s++) {
				TimedMessage message = new TimedMessage(date, sentences.get(s));
				resultList.add(message);
			}
			textReader.close();
		}
		dataContainer.setData(resultList);
		return true;
	}

	/**
	 * This method gets a Text and splits it into sentences (used separators:
	 * '.','!','?').
	 * 
	 * @param text
	 *            The text that should be split into sentences.
	 * @return A arrayList containing the calculated sentences.
	 */
	private ArrayList<String> splitIntoSentences(String text) {
		ArrayList<String> resultList = new ArrayList<>();
		String sentence = "";
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '.' || c == '!' || c == '?') {
				resultList.add(sentence);
				sentence = "";
			} else {
				sentence = sentence + c;
			}
		}
		if (!sentence.isEmpty()) {
			resultList.add(sentence);
		}
		return resultList;
	}

	/**
	 * This method gets one or more files with collected data, extracts the data
	 * and sets it as the current data.
	 * 
	 * @return TRUE if could extract and set as current data else FALSE.
	 * @throws IOException
	 *             Problem while reading file.
	 * @throws ParseException
	 *             Problem while reading date.
	 * @throws NumberFormatException
	 *             Could not parse input.
	 */
	public boolean setDataFromFile(boolean withoutDuplicates)
			throws IOException, ParseException, NumberFormatException {
		// read and convert file
		// Used code example: http://www.javaschubla.de/2007/javaerst0250.html
		File[] selectedFiles = InputOptionCollection.selectMutipleFiles();
		if (selectedFiles == null || selectedFiles.length == 0) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null, "Could not find file.");
			dataContainer.setData(new ArrayList<TimedMessage>());
			return false;
		}
		// set information
		int dateColumn = 1;
		int messageColumn = 3;
		String separator = ",";
		int idColumn = -1;
		if (withoutDuplicates) {
			String idColumnString = InputOptionCollection.getUserInput(
					"Enter the position of the ID column (start at 0):",
					"Position of date column", "0");
			if (idColumnString == null) {
				return false;
			} else {
				idColumn = Integer.parseInt(idColumnString);
			}
		}
		String dateColumnString = InputOptionCollection.getUserInput(
				"Enter the position of the date column (start at 0):",
				"Position of date column", "0");
		String messageColumnString = InputOptionCollection.getUserInput(
				"Enter the position of the message column (start at 0):",
				"Position of message column", "0");
		String separatorString = InputOptionCollection.getUserInput(
				"Add the symbol which is used a the separator:",
				"Separator Symbol", ";");
		boolean addBehindMessage = InputOptionCollection.getYesNoSelection(
				"Do you want to add every column behind the message column to the message? "
						+ "(E.g. when the data contains the separator)",
				"Add Information");
		this.useBehindSeparator = addBehindMessage;
		if (dateColumnString != null || messageColumnString != null
				|| separatorString != null) {
			dateColumn = Integer.parseInt(dateColumnString);
			messageColumn = Integer.parseInt(messageColumnString);
			separator = separatorString.substring(0, 1);
		} else {
			return false;
		}
		ArrayList<TimedMessage> resultList = extractDataFromFiles(
				selectedFiles, dateColumn, messageColumn, addBehindMessage,
				separator, idColumn);
		dataContainer.setData(resultList);
		return true;
	}
	
	public boolean setDataFromFileAdaptor(int dateColumn, int messageColumn, String separator, boolean addAfterMsg, File dataFile)
			throws IOException, ParseException, NumberFormatException {		
		ArrayList<TimedMessage> resultList = extractDataFromFiles(
				new File[]{dataFile}, dateColumn, messageColumn, addAfterMsg,
				separator, -1);
		dataContainer.setData(resultList);
		return true;
	}

	/**
	 * This method extracts the data from the files and converts them to a
	 * arrayList.
	 * 
	 * @param selectedFiles
	 *            The selected files.
	 * @param dateColumn
	 *            Position of the date column (from 0).
	 * @param messageColumn
	 *            Position of the message column (from 0).
	 * @param addBehindToMessage
	 *            True if you want to add all columns else false.
	 * @param separator
	 *            The used separator.
	 * @return An arrayList containing the information from the selected files.
	 * @throws IOException
	 *             Problem while reading file.
	 * @throws ParseException
	 *             Problem while parsing date.
	 */
	private ArrayList<TimedMessage> extractDataFromFiles(File[] selectedFiles,
			int dateColumn, int messageColumn, boolean addBehindToMessage,
			String separator, int idColumn) throws IOException, ParseException {
		ArrayList<TimedMessage> resultList = new ArrayList<TimedMessage>();
		for (int i = 0; i < selectedFiles.length; i++) {
			FileReader fileReader = new FileReader(selectedFiles[i]);
			BufferedReader textReader = new BufferedReader(fileReader);
			ArrayList<TimedMessage> messages = convertToTimedMessages(
					textReader, dateColumn, messageColumn, addBehindToMessage,
					separator, idColumn);
			resultList.addAll(messages);
		}
		return resultList;
	}

	/**
	 * This method gets a initialized BufferedReader, extracts the necessary
	 * information (by using the dateColumn, messageColumn, separator) and
	 * converts it into a TimedMessage-ArrayList.
	 * 
	 * @param reader
	 *            The initialized BufferedReader.
	 * @param dateColumn
	 *            The position of the date column (begin with 0).
	 * @param messageColumn
	 *            The position of the message column (begin with 0).
	 * @param addBehindToMessage
	 *            True if you want to add all columns else false.
	 * @param separator
	 *            The String that is used to separate the data.
	 * @return Extracted information in a ArrayList of TimedMessages.
	 * @throws IOException
	 *             Problem while reading file.
	 * @throws ParseException
	 *             Problem while parsing date.
	 */
	private ArrayList<TimedMessage> convertToTimedMessages(
			BufferedReader reader, int dateColumn, int messageColumn,
			boolean addBehindToMessage, String separator, int idColumn)
			throws IOException, ParseException {
		ArrayList<TimedMessage> result = new ArrayList<TimedMessage>();
		String line = reader.readLine();
		int maxValue = maxValue(idColumn, dateColumn, messageColumn);
		while (line != null) {
			String[] columns = line.split(separator);
			// Used code example:
			// http://stackoverflow.com/questions/9431927/how-to-convert-date-tostring-back-to-date
			if (columns.length > maxValue) {
				String dateString = columns[dateColumn];
				Date date = parseDate(dateString);
				if (idColumn > -1) {
					String idString = columns[idColumn];
					if (!IdsOfObjects.containsKey(idString)) {
						IdsOfObjects.put(idString, null);
						TimedMessage timedMessage = new TimedMessage(date,
								extractMessage(columns, messageColumn,
										addBehindToMessage));
						result.add(timedMessage);
					}
				} else {
					TimedMessage timedMessage = new TimedMessage(date,
							extractMessage(columns, messageColumn,
									addBehindToMessage));
					result.add(timedMessage);
				}
			}
			line = reader.readLine();
		}
		reader.close();
		return result;
	}

	/**
	 * Gets the data and returns only the messageColumn or returns the
	 * messageColumn the all other columns behind this column.
	 * 
	 * @param data
	 *            The data you want to extract.
	 * @param messageColumn
	 *            The position of the messageColumn.
	 * @param addBehindToMessage
	 *            True if you want to add all columns else false.
	 * @return The extracted message.
	 */
	private String extractMessage(String[] data, int messageColumn,
			boolean addBehindToMessage) {
		if (addBehindToMessage) {
			String result = "";
			for (int i = messageColumn; i < data.length; i++) {
				result = result + data[i];
			}
			return result;
		} else {
			return data[messageColumn];
		}
	}

	/**
	 * Returns the largest value of the three given Integers.
	 * 
	 * @param id
	 *            Position of the idColumn.
	 * @param date
	 *            Position of the dateColumn.
	 * @param message
	 *            Position of the messageColumn.
	 * @return Largest Integer.
	 */
	private int maxValue(int id, int date, int message) {
		int result = 0;
		if (id > date) {
			result = id;
		} else {
			result = date;
		}
		if (result < message) {
			result = message;
		}
		return result;
	}

	/**
	 * This method gets a date as string in long format (like
	 * java.util.date.toString) or short (dd.mm.yyyy) and converts it back into
	 * a Date object.
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
					"E MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
			return formatter.parse(dateString);
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat("dd.mm.yyyy");
			return formatter.parse(dateString);
		}
	}

	/**
	 * Writes the data from the dataContainer into a file that must be selected.
	 * 
	 * @return TRUE if could save data successfully else FALSE.
	 * @throws IOException
	 *             Problem with file.
	 */
	public boolean saveDatainFile() throws IOException {
		File selectedFile = InputOptionCollection.saveFile();
		ArrayList<TimedMessage> data = dataContainer.getData();
		if (selectedFile == null || data == null) {
			return false;
		}
		PrintWriter writer = new PrintWriter(selectedFile);
		for (int i = 0; i < data.size(); i++) {
			writer.println(i + ";" + data.get(i).getDate() + ";"
					+ data.get(i).getMessage());
			writer.flush();
		}
		writer.close();
		return true;
	}
	
	/**
	 * 
	 * 
	 * @return TRUE if could save data successfully else FALSE.
	 * @throws IOException
	 *             Problem with file.
	 */
	public File getDatainFile() throws IOException {
		File newFile = new File("collected_data.csv");
		ArrayList<TimedMessage> data = dataContainer.getData();
		if (data == null) {
			return null;
		}
		PrintWriter writer = new PrintWriter(newFile);
		for (int i = 0; i < data.size(); i++) {
			writer.println(i + ";" + data.get(i).getDate() + ";"
					+ data.get(i).getMessage());
			writer.flush();
		}
		writer.close();
		return newFile;
	}
	public boolean getUseBehindSeparator() {
		return this.useBehindSeparator;
	}
}
