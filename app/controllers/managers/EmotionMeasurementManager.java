package controllers.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import tools.InputOptionCollection;
import tools.DataTypes.AssessedTimedMessage;
import tools.DataTypes.DateValueCounter;
import tools.DataTypes.TimedMessage;

/**
 * This class contains different methods to measure the emotions and visualize
 * the results. It allows to preprocess the data, add or use wordLists to
 * measure the emotions and visualize the results by using different kinds of
 * charts.
 * 
 * @author Christian Olenberger
 * 
 */
public class EmotionMeasurementManager implements Serializable {

	private static final long serialVersionUID = 5665586968362710256L;
	// Defined values
	public static final int Y_AXIS_INSTANCE_COUNTER = 0;
	public static final int Y_AXIS_INSTANCE_EMOTION_VALUE = 1;

	public static final int USE_SELECTED_FILE = 0;
	public static final int USE_ANEW = 1;
	public static final int USE_WARRINER = 2;

	// data fields
	private File emotionTableFile;
	private HashMap<String, Double> emotionValues;
	private ArrayList<AssessedTimedMessage> assessedResult;

	/**
	 * Opens a dialog where the user can select the file with the tagged words.
	 * 
	 * @return File-Path
	 */
	public String selectFile() {
		File selectedFile = InputOptionCollection.selectFile();
		if (selectedFile != null) {
			this.emotionTableFile = selectedFile;
			return this.emotionTableFile.getAbsolutePath();
		}
		return "";
	}

	/**
	 * This method uses one of the default emotion-value files from the project
	 * folder and sets it as the current emotionTableFile.
	 * 
	 * @param fileToUse
	 *            Decide whether to use the ANEW-Dataset or the
	 *            WARRINER-Dataset.
	 * @return TRUE if could set file successfully else false.
	 * @throws NumberFormatException
	 *             Wrong number.
	 * @throws IOException
	 *             Problem while reading file.
	 */
	public boolean setDefaultEmotionTableFile(int fileToUse)
			throws NumberFormatException, IOException {
		if (fileToUse == EmotionMeasurementManager.USE_ANEW) {
			emotionTableFile = new File("emotion_datasets/ANEW_Dataset.csv");
			setEmotionValueFile(fileToUse);
			return true;
		} else if (fileToUse == EmotionMeasurementManager.USE_WARRINER) {
			emotionTableFile = new File("emotion_datasets/WARRINER_Dataset.csv");
			setEmotionValueFile(fileToUse);
			return true;
		}
		return false;

	}

	/**
	 * Converts the file and extracts the necessary data.
	 * 
	 * @param fileToUse
	 *            Select whether the file was selected by the user or is one of
	 *            the dafault files.
	 * 
	 * @return TRUE if could extract data.
	 * 
	 * @throws IOException
	 *             Problem while reading file.
	 * 
	 * @throws NumberFormatException
	 *             Problem while parsing integer.
	 */
	public boolean setEmotionValueFile(int fileToUse) throws IOException,
			NumberFormatException {
		if (emotionTableFile == null) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null,
					"Select file with emotion values first.");
			return false;
		}
		// necessary values
		int wordColumn = 0;
		int valueColumn = 2;
		String separator = ",";
		boolean hasCaption = true;
		// set values
		if (fileToUse == EmotionMeasurementManager.USE_SELECTED_FILE) {
			String wordColumnString = InputOptionCollection.getUserInput(
					"Enter the position of the word column (start at 0):",
					"Position of word column", "0");
			String valueColumnString = InputOptionCollection.getUserInput(
					"Enter the position of the value column (start at 0):",
					"Position of value column", "0");
			String separatorString = InputOptionCollection.getUserInput(
					"Add the symbol which is used as the separator:",
					"Separator Symbol", ",");
			if (wordColumnString != null || valueColumnString != null
					|| separatorString != null) {
				wordColumn = Integer.parseInt(wordColumnString);
				valueColumn = Integer.parseInt(valueColumnString);
				separator = separatorString.substring(0, 1);
			} else {
				return false;
			}
			hasCaption = InputOptionCollection.getYesNoSelection(
					"Do the File has a Caption-Row?", "Caption-Row");
		} else {
			if (fileToUse == EmotionMeasurementManager.USE_ANEW) {
				wordColumn = 0;
			} else {
				wordColumn = 1;
			}
		}

		// read and convert file
		// Used code example: http://www.javaschubla.de/2007/javaerst0250.html
		FileReader fileReader = new FileReader(emotionTableFile);
		BufferedReader TextReader = new BufferedReader(fileReader);
		String line = "";
		// read line
		if (hasCaption) {// skip head line
			line = TextReader.readLine();
		}
		line = TextReader.readLine();
		emotionValues = new HashMap<String, Double>();
		while (line != null) {
			// Used code example:
			// http://stackoverflow.com/questions/4234985/how-to-for-each-the-hashmap
			String[] data = line.split(separator);
			String key = data[wordColumn];
			Double value = Double.parseDouble(data[valueColumn]);
			emotionValues.put(key, value);
			line = TextReader.readLine();
		}

		// read line
		line = TextReader.readLine();
		TextReader.close();
		fileReader.close();
		return true;
	}

	/**
	 * This method assess all Messages in a List by using the assessMessage
	 * method.
	 * 
	 * @param list
	 *            The ArrayList that should be assessed.
	 * @return The assessed ArrayList.
	 */
	public void assessMessages(ArrayList<TimedMessage> list) {
		ArrayList<AssessedTimedMessage> result = new ArrayList<AssessedTimedMessage>();

		for (int i = 0; i < list.size(); i++) {
			long startTime = System.currentTimeMillis();
			result.add(assessMessage(list.get(i)));
			double resultTime = (System.currentTimeMillis() - startTime) * 1.0 / 1000;
			resultTime = Math.round(resultTime * 100) * 1.0 / 100;
			//WaitCalculatingForm.setText("Calculating " + i + " of "
					//+ list.size() + " (" + resultTime + " sec./inst.)");
		}

		this.assessedResult = result;
		System.out.println(this.assessedResult.size());
	}

	/**
	 * This method uses the emotion values to assess the the emotionality of the
	 * message (by calculating the avaerage for all words).
	 * 
	 * @param message
	 *            The message that should be assessed.
	 * @return The value added Message or -1 (at value field) if assessment was
	 *         not possible.
	 */
	private AssessedTimedMessage assessMessage(TimedMessage message) {
		String[] text = message.getMessage().split(" ");
		double value = 0;
		double count = 0;
		for (int i = 0; i < text.length; i++) {
			if (emotionValues.get(text[i]) != null) {
				value = value + emotionValues.get(text[i]);
				count++;
			}
		}
		double endResult = -1;
		if (count != 0) {
			endResult = (Math.round((value / count) * 100.0) / 100.0);
		}
		return new AssessedTimedMessage(Double.toString(endResult),
				message.getDate(), message.getMessage());
	}

	/**
	 * Getter for the current EmotionValues.
	 * 
	 * @return Current EmotionValues.
	 */
	public HashMap<String, Double> getEmotionValues() {
		return emotionValues;
	}

	/**
	 * This method opens a window containing a PieChart to visualize how much
	 * messages were measurable.
	 */
	public JPanel showMeasurablePieChart() {
		// Used code example:
		// http://www.vogella.com/tutorials/JFreeChart/article.html
		if (assessedResult == null) {
			return new JPanel();
		}
		//WaitCalculatingForm.showCalculatingWindow();
		DefaultPieDataset dataset = convertToPieDataset();
		JFreeChart chart = ChartFactory.createPieChart(
				"Measureable - Not Measureable", dataset, true, true, false);
		//WaitCalculatingForm.hideCalculatingWindow();
		return new ChartPanel(chart);
	}

	/**
	 * Converts the assessed dataset to a PieDataset and returns this dataset.
	 * 
	 * @return To PieDataSet converted assessed data.
	 */
	private DefaultPieDataset convertToPieDataset() {
		// Used code example:
		// http://www.vogella.com/tutorials/JFreeChart/article.html
		DefaultPieDataset dataset = new DefaultPieDataset();
		double measurable = 0;
		double notMeasurable = 0;
		for (int i = 0; i < assessedResult.size(); i++) {
			double assassment = Double.parseDouble(assessedResult.get(i)
					.getAssessment());
			if (assassment == -1) {
				notMeasurable++;
			} else {
				measurable++;
			}
		}
		dataset.setValue("Measurable", measurable);
		dataset.setValue("Not Measurable", notMeasurable);
		return dataset;
	}
	public ObjectNode getJsonPieChart() {
		double measurable = 0;
		double notMeasurable = 0;
		for (int i = 0; i < assessedResult.size(); i++) {
			double assassment = Double.parseDouble(assessedResult.get(i)
					.getAssessment());
			if (assassment == -1) {
				notMeasurable++;
			} else {
				measurable++;
			}
		}
		ObjectNode result = Json.newObject();
		ArrayNode dataArray = result.putArray("data");
		ArrayNode array = dataArray.addArray();
		array.add("Measurable").add(measurable);
		array = dataArray.addArray();
		array.add("Not measurable").add(notMeasurable);
		return result;
	}

	/**
	 * Converts the assessed dataset to a TimeSeriesCollection and returns this
	 * Collection.
	 * 
	 * @param yAxisValue
	 *            Decide what to do with the data(e.g.
	 *            EmotionMeasurementManager.Y_AXIS_INSTANCE_COUNTER).
	 * @return To LineChartDataSet converted assessed data.
	 */
	private TimeSeriesCollection convertToTimeSeriesCollection(int yAxisValue) {
		// Used code example:
		// http://dvillela.servehttp.com:4000/apostilas/jfreechart_tutorial.pdf
		TimeSeries series = new TimeSeries("Emotion value");
		TreeMap<String, DateValueCounter> timeByDate = aggregateByDate();
		if (timeByDate.size() < 2) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null,
					"Not enough dates in the dataset.");
		}
		for (DateValueCounter element : timeByDate.values()) {
			Day day = convertDate(element.getDate());
			double value = 0;
			if (yAxisValue == EmotionMeasurementManager.Y_AXIS_INSTANCE_COUNTER) {
				value = element.getCounter();
			} else {
				value = element.getAverage();
			}
			series.add(day, value);
		}
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(series);
		return dataset;
	}
	
	
	public ObjectNode getJsonTimeSeries(int yAxisValue) {
		TreeMap<String, DateValueCounter> timeByDate = aggregateByDate();
		ObjectNode result = Json.newObject();
		ArrayNode dataArray = result.putArray("data");
		for (DateValueCounter element : timeByDate.values()) {
			ArrayNode array = dataArray.addArray();
			double value = 0;
			if (yAxisValue == EmotionMeasurementManager.Y_AXIS_INSTANCE_COUNTER) {
				value = element.getCounter();
			} else {
				value = element.getAverage();
			}
			array.add(element.getDate().getTime()).add(value);
			
		}
		return result;
		
	}
	/**
	 * This method convert from Date-Format to Day-Format.
	 * 
	 * @param date
	 *            Date in Date-Format.
	 * @return The given date in Day-Format.
	 */
	private Day convertDate(Date date) {
		// Used code example:
		// http://dvillela.servehttp.com:4000/apostilas/jfreechart_tutorial.pdf
		Day result = new Day(date);
		return result;
	}

	/**
	 * This method aggregates the assessedData. All messages collected on the
	 * same day are grouped to one group.
	 * 
	 * @return The aggregated assessedData.
	 */
	private TreeMap<String, DateValueCounter> aggregateByDate() {
		Comparator<String> comparator = new Comparator<String>() {
			  public int compare(String o1, String o2) {
				  if(o1.equals(o2))
					  return 0;
			    try {
					if(new SimpleDateFormat("dd.MM.yyyy").parse(o1).before(new SimpleDateFormat("dd.MM.yyyy").parse(o2)))
						return -1;
					else
						return 1;
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return -1000;
			  }
			};
		TreeMap<String, DateValueCounter> map = new TreeMap<String, DateValueCounter>(comparator);
		for (int i = 0; i < assessedResult.size(); i++) {
			AssessedTimedMessage item = assessedResult.get(i);
			if (Double.parseDouble(item.getAssessment()) != -1) {
				if (map.containsKey(convertDatetoString(item.getDate()))) {
					DateValueCounter currCounter = map
							.get(convertDatetoString(item.getDate()));
					currCounter.addValue(Double.parseDouble(item
							.getAssessment()));
					currCounter.incCounter();
				} else {
					Date currDate = item.getDate();
					double value = Double.parseDouble(assessedResult.get(i)
							.getAssessment());
					map.put(convertDatetoString(currDate),
							new DateValueCounter(currDate, value, 1));
				}
			}
		}
		return map;
	}

	/**
	 * Converts a date to a String in this format "dd.MM.yyyy".
	 * 
	 * @param date
	 *            The date you want to create a string from.
	 * @return The date as string.
	 */
	private String convertDatetoString(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		return format.format(date);
	}

	/**
	 * Creates a ChartPanel containing a LineChart to visualize the measured
	 * results.
	 * 
	 * @param yAxisValue
	 *            Decide what to do with the data(e.g.
	 *            EmotionMeasurementManager.Y_AXIS_INSTANCE_COUNTER).
	 * @return A ChartPanel with a LineChart by using the selected operation.
	 */
	public JPanel showTimeSeriesChart(int yAxisValue) {
		// Used code example:
		// http://dvillela.servehttp.com:4000/apostilas/jfreechart_tutorial.pdf
		if (assessedResult == null) {
			return new JPanel();
		}
	//	WaitCalculatingForm.showCalculatingWindow();
		TimeSeriesCollection dataset = convertToTimeSeriesCollection(yAxisValue);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Results",
				"Time", "Emotion Values", dataset, true, true, false);
		XYPlot plot = chart.getXYPlot();
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));
	//	WaitCalculatingForm.hideCalculatingWindow();
		return new ChartPanel(chart);
	}

	/**
	 * This method opens a window containing a BarChart to visualize the
	 * measured results.
	 */
	public JPanel showBarChart() {
		// Used code example:
		// http://dvillela.servehttp.com:4000/apostilas/jfreechart_tutorial.pdf
		if (assessedResult == null) {
			return new JPanel();
		}
		DefaultCategoryDataset dataset = convertToCategoryDataset();
		JFreeChart chart = ChartFactory.createBarChart("Emotion Values",
				"Ranges", "Count Of Instances", dataset,
				PlotOrientation.VERTICAL, false, true, false);
		return new ChartPanel(chart);
	}

	/**
	 * Separates the assessed data into different groups, converts it into
	 * DefaultCategoryDataset and returns the result.
	 * 
	 * @return The grouped and converted data.
	 * 
	 * @throws NumberFormatException
	 *             Problem while parsing input.
	 */
	private DefaultCategoryDataset convertToCategoryDataset()
			throws NumberFormatException {
		double minValue = 1;
		double maxValue = 9;
		int numberOfBars = 8;
		minValue = Double.parseDouble(InputOptionCollection.getUserInput(
				"Insert the minimum Value of the scale (double):",
				"Minimum Value", "1.0"));
		maxValue = Double.parseDouble(InputOptionCollection.getUserInput(
				"Insert the maximum Value of the scale (double):",
				"Maximum Value", "9.0"));
		numberOfBars = Integer
				.parseInt(InputOptionCollection
						.getUserInput(
								"Insert the number of bars you want to have on the chart (int):",
								"Number of bars", "8"));
	//	WaitCalculatingForm.showCalculatingWindow();
		double range = maxValue - minValue;
		double rangePerBarExact = range * 1.0 / numberOfBars;
		double rangePerBar = ((Math.round((rangePerBarExact * 100)) * 1.0) / 100 * 1.0);

		int[] counter = new int[numberOfBars];

		for (int i = 0; i < assessedResult.size(); i++) {
			int category = numberOfBars - 1;
			while (category > -1) {
				double assessment = Double.parseDouble(assessedResult.get(i)
						.getAssessment());
				if (assessment > (minValue + (rangePerBar * category))) {
					counter[category]++;
					break;
				} else if (assessment == minValue) {
					counter[0]++;
					break;
				}
				category--;
			}
		}
		DefaultCategoryDataset dataset = createDefaultCategoryDataset(counter,
				minValue, rangePerBar);
	//	WaitCalculatingForm.hideCalculatingWindow();
		return dataset;
	}
	public ObjectNode getJsonCategoryChart()
			throws NumberFormatException {
		double minValue = 1;
		double maxValue = 9;
		int numberOfBars = 8;
	//	WaitCalculatingForm.showCalculatingWindow();
		double range = maxValue - minValue;
		double rangePerBarExact = range * 1.0 / numberOfBars;
		double rangePerBar = ((Math.round((rangePerBarExact * 100)) * 1.0) / 100 * 1.0);
		ObjectNode result = Json.newObject();
		ArrayNode dataArray = result.putArray("data");
		int[] counter = new int[numberOfBars];

		for (int i = 0; i < assessedResult.size(); i++) {
			int category = numberOfBars - 1;
			while (category > -1) {
				double assessment = Double.parseDouble(assessedResult.get(i)
						.getAssessment());
				if (assessment > (minValue + (rangePerBar * category))) {
					counter[category]++;
					break;
				} else if (assessment == minValue) {
					counter[0]++;
					break;
				}
				category--;
			}
		}
		for(int i=0; i<counter.length; i++) {
			dataArray.add(counter[i]);
		}
		return result;
	}
	/**
	 * This method uses the given parameter to convert a array (containing the
	 * group counts) to a DefaultCategoryDataset.
	 * 
	 * @param counter
	 *            The array containing the groups counts.
	 * @param minValue
	 *            Minimum Value of the scale.
	 * @param rangePerBar
	 *            The a range of a single bar.
	 * @return The DefaultCategoryDataset generated from the given array.
	 */
	private DefaultCategoryDataset createDefaultCategoryDataset(int[] counter,
			double minValue, double rangePerBar) {
		// Used code example:
		// http://dvillela.servehttp.com:4000/apostilas/jfreechart_tutorial.pdf
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 0; i < counter.length; i++) {
			double from = Math.round((minValue + (i * rangePerBar)) * 100) * 1.0 / 100;
			double to = Math.round((minValue + ((i + 1) * rangePerBar)) * 100) * 1.0 / 100;
			if (i == (counter.length - 1)) {
				dataset.addValue(counter[i], "Emotion Value", from + "<");
			} else {
				dataset.addValue(counter[i], "Emotion Value", from + "< >="
						+ to);
			}
		}
		return dataset;
	}
	
	public File saveResultsToFile() throws FileNotFoundException {
		File resultsFile = new File("EmotionMeasurement_results.csv");
		PrintWriter writer = new PrintWriter(resultsFile);
		writer.println("Index;Date;AssessedValue;Message");
		for (int i = 0; i < assessedResult.size(); i++) {
			AssessedTimedMessage element = assessedResult.get(i);
			writer.println((i + 1)
					+ ";"
					+ element.getDate().toString()
					+ ";"
					+ element.getAssessment()
					+ ";"
					+ element.getMessage().replaceAll("\n", "")
							.replaceAll("" + (char) 13, ""));
		}
		writer.flush();
		writer.close();
		return resultsFile;
	}
	
	public ObjectNode getJsonRegression(int yAxisValue){
		ObjectNode result = Json.newObject();
		ArrayNode dataArray = result.putArray("data");
		TimeSeriesCollection timeSeriesCollection = convertToTimeSeriesCollection(yAxisValue);
		TimeSeries timeSeries = timeSeriesCollection.getSeries(0);
		String yAxisName = "Emotion Value";
		if (yAxisValue == Y_AXIS_INSTANCE_COUNTER) {
			yAxisName = "Instance Count";
		}
		XYSeries xySeries = TimeSeriesPostProcessor.convertTimeSeriesToIndex(
				timeSeries, yAxisValue, yAxisName);
		for (XYDataItem element : (List<XYDataItem>)xySeries.getItems()){
			ArrayNode array = dataArray.addArray();
			array.add(element.getXValue()).add(element.getYValue());
		}
		return result;
	}
	
	public ObjectNode getJsonMovingAverage(int yAxisValue, int movAvgRangeSize){
		TreeMap<String, DateValueCounter> timeByDate = aggregateByDate();
		ObjectNode result = Json.newObject();
		ArrayNode dataArray = result.putArray("data");
		TimeSeries timeSeries = convertToTimeSeriesCollection(yAxisValue)
				.getSeries(0);
		TimeSeries movAvgTimeSeries = TimeSeriesPostProcessor
				.movAvgOnTimeSeries(timeSeries, movAvgRangeSize);
		for (TimeSeriesDataItem element : (List<TimeSeriesDataItem>)movAvgTimeSeries.getItems()){
			ArrayNode array = dataArray.addArray();
			array.add(element.getPeriod().getMiddleMillisecond()).add(element.getValue().doubleValue());
		}
		return result;
		
	}
	/**
	 * This method creates a Panel containing the data with moving average
	 * process.
	 * 
	 * @param yAxisValue
	 *            Decide what to do with the data(e.g.
	 *            EmotionMeasurementManager.Y_AXIS_INSTANCE_COUNTER).
	 * @param movRangeSize
	 *            The range of the moving average process. How many item should
	 *            be used to calculate one item.
	 * @return A Panel with the data with moving average process chart by using
	 *         the given operation.
	 */
	public JPanel showMovingAverageChart(int yAxisValue, int movAvgRangeSize) {
		if (assessedResult == null) {
			return new JPanel();
		}
	//	WaitCalculatingForm.showCalculatingWindow();
		TimeSeries timeSeries = convertToTimeSeriesCollection(yAxisValue)
				.getSeries(0);
		TimeSeries movAvgTimeSeries = TimeSeriesPostProcessor
				.movAvgOnTimeSeries(timeSeries, movAvgRangeSize);
		TimeSeriesCollection dataset = new TimeSeriesCollection(
				movAvgTimeSeries);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Results",
				"Time", "Emotion Values", dataset, true, true, false);
		XYPlot plot = chart.getXYPlot();
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));
	//	WaitCalculatingForm.hideCalculatingWindow();
		return new ChartPanel(chart);
	}

	/**
	 * The method aggregates the data on every day by using a given method and
	 * saves the result in a file that must be selected.
	 * 
	 * @param yAxisValue
	 *            The method that should be used to aggregate the data (e.g.
	 *            Y_AXIS_METRIC).
	 * @return TRUE if the data could be saved successfully.
	 * @throws IOException
	 *             Problem while writung into the file.
	 */
	public boolean saveAggregatedData(int yAxisValue) throws IOException {
		if (assessedResult == null) {
			return false;
		}
		TimeSeries timeSeries = convertToTimeSeriesCollection(yAxisValue)
				.getSeries(0);
		File resultFile = InputOptionCollection.saveFile();
		PrintWriter writer = new PrintWriter(resultFile);
		for (int i = 0; i < timeSeries.getItemCount(); i++) {
			TimeSeriesDataItem item = timeSeries.getDataItem(i);
			Date date = item.getPeriod().getStart();
			double value = item.getValue().doubleValue();
			writer.println(date + ";" + value);
			writer.flush();
		}
		writer.close();
		return true;
	}

	public ArrayList<AssessedTimedMessage> getAssessedMessages() {
		return this.assessedResult;
	}
}
