package controllers.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.ClassificationController;
import play.Play;
import play.libs.Json;
import tools.InputOptionCollection;
import tools.DataTypes.ClassCounter;
import tools.DataTypes.TimedClassifiedData;
import tools.DataTypes.TimedMessage;
import twitter4j.Status;

/**
 * This class is used to save the datasets necessary for classification and to
 * perform the classification. This class only uses sparse format because mixing
 * different representations cause worse results. Sparse format is better for
 * text classification because is need less space to save feature vectors.
 * 
 * @author Christian Olenberger
 * 
 */
public class ClassificationManager implements Serializable{

	private static final long serialVersionUID = -3879626621671430251L;
	// Value definition
	public static final int SET_TRAINING_DATA = 0;
	public static final int SET_CLASSIFICATION_DATA = 1;

	public static final int Y_AXIS_INSTANCE_COUNT = 0;
	public static final int Y_AXIS_METRIC = 1;
	public static final int Y_AXIS_NOMINAL = 2;

	// data fields
	private ClassifierCollection classifier;
	private Dataset trainingData;
	private boolean hasTrainedClassifier = false;
	// The date for the classification/classified instance. Works because the
	// datasets are not sorted or changed so the index of the instance is
	// matching
	//private ArrayList<Date> datesByIndexFromClassification;
	//private ArrayList<Date> datesByIndexFromTrain;
	private ArrayList<Date> datesByIndex;
	private ArrayList<Long> tweetIdByIndex;
	private Dataset classificationData;
	private Dataset classifiedData;
	private ArrayList<String> trainedWordList;
	private boolean useBehindSeparator = false;
	private String dummyClass;
	// If the results are save you need to calculate it only once.
	private Map<Object, PerformanceMeasure> crossValidationResult;

	/**
	 * This method gets a sparse formatted file, turns it into a dataset.
	 * 
	 * @param file
	 *            The sparse formatted file with training data.
	 * @param fileTarget
	 *            Use file for training or classification(e.g. SET_TRAINING_DATA
	 *            or SET_CLASSIFICATION_DATA).
	 * @throws IOException
	 *             Problem while reading file.
	 */
	private void setSparseData(File file, int fileTarget) throws IOException {
		// Used a javaML code example:
		// http://java-ml.sourceforge.net/content/load-data-file
		if (fileTarget == ClassificationManager.SET_TRAINING_DATA) {
			this.trainingData = FileHandler
					.loadSparseDataset(file, 0, " ", ":");
		} else if (fileTarget == ClassificationManager.SET_CLASSIFICATION_DATA) {
			this.classificationData = FileHandler.loadSparseDataset(file, 0,
					" ", ":");
			System.out.println(this.classificationData.size());
		}
		file.delete();
	}

	/**
	 * This method set the classifier. The wanted classifier can be specified by
	 * his value (e.g. ClassifierCollection.SVM_CLASSIFIER).
	 * 
	 * @param classifierValue
	 *            The class value for the classifier. You can use the static
	 *            fields in class ClassifierCollector to set a classifier.
	 */
	public void setClassifier(int classifierValue) {
		this.classifier = new ClassifierCollection(classifierValue);
	}

	/**
	 * This method sets the current classifier.
	 * 
	 * @param classifier
	 *            Classifier that should be set.
	 */
	public void setClassifier(Classifier classifier) {
		this.classifier = new ClassifierCollection(classifier);
	}

	/**
	 * Trains the classifier by using the already defined dataset for training.
	 */
	public boolean trainClassifier() {
		if (this.classifier != null && this.trainingData != null) {
			//WaitCalculatingForm.showCalculatingWindow();
			this.classifier.trainClassifier(trainingData);
			this.dummyClass = trainingData.classes().first().toString();
			//WaitCalculatingForm.hideCalculatingWindow();
			hasTrainedClassifier = true;
			return true;
		} else {
			if (this.classifier == null) {
				// Used code example: http://java-tutorial.org/joptionpane.html
				JOptionPane.showMessageDialog(null,
						"No classifier found. Select classifier first.");
			} else if (this.trainingData == null) {
				// Used code example: http://java-tutorial.org/joptionpane.html
				JOptionPane.showMessageDialog(null,
						"No training data found. Set training data first.");
			}
			return false;
		}
	}
	
	public boolean setRawTweets(Iterable<Status> tweets) throws IOException {
		
		String instanceTitle = "";
		ArrayList<String> wordList = new ArrayList<String>();
		wordList = getWordsList(null, SET_CLASSIFICATION_DATA, false);
		instanceTitle = this.dummyClass;
		// initialize reader and writer
		File resultFile = getFile(true);
		PrintWriter writer = new PrintWriter(resultFile);
		// read file
		String resultLine;
		ArrayList<Long> tweetIdList = new ArrayList<Long>();
		ArrayList<Date> dateList = new ArrayList<Date>();
		for (Status status : tweets) {
			resultLine = instanceTitle;
			// create line in sparse format
			String[] words = new String[1];
			String messageText = status.getText();
			words = messageText.split(" ");
			ArrayList<ClassCounter> wordCounts = getWordCounterList(wordList,
					words);
			resultLine = resultLine + createSparseLine(wordList, wordCounts);
			dateList.add(status.getCreatedAt());

			tweetIdList.add(status.getId());
			writer.println(resultLine);
		}
		// Close method
		writer.flush();
		writer.close();
		setSparseData(resultFile, SET_CLASSIFICATION_DATA);
		tweetIdByIndex = tweetIdList;
		datesByIndex = dateList;
		System.out.println(datesByIndex.size() + "!!!!");
		
		return true;		
	}

	/**
	 * This method selects the operation on the file and set it as the training
	 * data (see FileFormatInformationForm).
	 * 
	 * @param file
	 *            File with training data.
	 * @param convertOperation
	 *            0: Extract dataset from text. 1: Convert file into
	 *            "sparse format". 2: File is already "sparse format".
	 * 
	 * @param fileTarget
	 *            Use file for training or classification(e.g. SET_TRAINING_DATA
	 *            or SET_CLASSIFICATION_DATA).
	 * @throws IOException
	 *             Problem while reading file.
	 * @throws ParseException
	 *             Problem while parsing date.
	 */
	public boolean setData(File file, int convertOperation, int fileTarget,
			String separator, boolean classified)throws IOException, ParseException {
		switch (convertOperation) {
		case 0:
			/*
			String separatorString = InputOptionCollection.getUserInput(
					"Add the symbol which is used as the separator:",
					"Separator Symbol", ";");
			if (separatorString != null) {
				separator = separatorString.substring(0, 1);
			} else {
				// Used code example: http://java-tutorial.org/joptionpane.html
				JOptionPane.showMessageDialog(null,
						"Wrong input. Could not set data.");
				return false;
			}*/
			convertTextFileIntoSparse(file, separator, fileTarget, false, classified);
			break;
		case 1:
			convertAndSetData(file, fileTarget);
			break;
		case 2:
			setSparseData(file, fileTarget);
			break;
		default:
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null,
					"Wrong input. Could not set data.");
		}
		return true;
	}
	/**
	 * This method converts a training file (see FileFormatInformationForm for
	 * more information) into sparse formatted dataset.
	 * 
	 * @param file
	 *            The file with training data.
	 * @param fileTarget
	 *            Use file for training or classification(eg SET_TRAINING_DATA
	 *            or SET_CLASSIFICATION_DATA).
	 * @throws IOException
	 *             Could not find file or problem while reading file.
	 */
	public boolean convertAndSetData(File file, int fileTarget)
			throws IOException {
		// show message dialog
		// used code example: http://java-tutorial.org/joptionpane.html
		JOptionPane.showMessageDialog(null,
				"Create a new File to save the converted data");
		// create new file
		// used code example:
		// http://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html)
		File selectedFile = InputOptionCollection.saveFile();
		if (selectedFile == null) {
			return false;
		}
		File newTrainFile = new File(selectedFile.getAbsolutePath());
		// create new fileWriter and fileReader
		// Used code example: http://www.javaschubla.de/2007/javaerst0250.html
		FileReader fileReader = null;
		PrintWriter fileWriter = null;
		fileWriter = new PrintWriter(newTrainFile);
		fileReader = new FileReader(file);
		convertToSparse(fileReader, fileWriter);
		// Add to training data or to classification data
		// used a javaML code example:
		// http://java-ml.sourceforge.net/content/load-data-file
		if (fileTarget == ClassificationManager.SET_TRAINING_DATA) {
			this.trainingData = FileHandler.loadSparseDataset(newTrainFile, 0,
					" ", ":");
			hasTrainedClassifier = true;
		} else if (fileTarget == ClassificationManager.SET_CLASSIFICATION_DATA) {
			this.classificationData = FileHandler.loadSparseDataset(
					newTrainFile, 0, " ", ":");
		}
		return true;
	}

	/**
	 * Uses the fileReader to read data from a data-file, converts it into
	 * sparse-format and write with the given fileReader into a new file.
	 * 
	 * @param fileReader
	 *            The fileReader containing the source file.
	 * @param fileWriter
	 *            The fileWriter containing the file to write into.
	 * @return TRUE if successfully converted the source file.
	 * @throws IOException
	 *             Problem while reading.
	 */
	private boolean convertToSparse(FileReader fileReader,
			PrintWriter fileWriter) throws IOException {
		// Used code example: http://www.javaschubla.de/2007/javaerst0250.html
		BufferedReader TextReader = new BufferedReader(fileReader);
		String line = "";
		line = TextReader.readLine();
		String result;
		while (line != null) {
			result = convertArrayToSparse(line.split(","));
			fileWriter.println(result);
			line = TextReader.readLine();
		}
		fileWriter.flush();
		return true;
	}

	/**
	 * Uses the given array with data formatted instance to convert it into
	 * sparse formatted String.
	 * 
	 * @param array
	 *            The array with data formatted instance.
	 * @return Result-String in sparse format.
	 */
	private String convertArrayToSparse(String[] array) {
		String result = array[0];
		for (int i = 1; i < array.length; i++) {
			double value = Double.parseDouble(array[i]);
			if (value != 0) {
				result = result + " " + i + ":" + value;
			}
		}
		return result;
	}

	/**
	 * This method classifies the data and returns the result.
	 * 
	 * @return Dataset with added calculated class.
	 */
	public boolean classifyData(boolean useTrainingData) {
		if (!checkForRequirementsToClassify(useTrainingData)) {
			return false;
		}
		Dataset dataToClassify = new DefaultDataset();
		//datesByIndex = new ArrayList<Date>();
		dataToClassify.addAll(classificationData);
		//if (datesByIndexFromClassification != null) {
			//datesByIndex.addAll(datesByIndexFromClassification);
		//}
		if (useTrainingData) {
			dataToClassify.addAll(trainingData);
		//	if (datesByIndexFromTrain != null) {
			//	datesByIndex.addAll(datesByIndexFromTrain);
		//	}
		}
		this.classifiedData = classifier.classifyDataset(dataToClassify);
		return true;
	}

	/**
	 * Checks whether all Requirements for the classification process are
	 * fulfilled.
	 * 
	 * @param useTrainingData
	 *            TRUE if the traningData should be added to the classification
	 *            data.
	 * @return TRUE if all requirements are fulfilled.
	 */
	private boolean checkForRequirementsToClassify(boolean useTrainingData) {
		if (classifier == null) {
			System.out.println("Classifier missing");
			return false;
		} else if (!hasTrainedClassifier) {
			System.out.println("Classifier not trained");
			return false;
		} else if (classificationData == null) {
			System.out.println("no classification data");
			return false;
		} else if (useTrainingData && trainingData == null) {
			System.out.println("training data missing");
			return false;
		}
		return true;
	}

	/**
	 * Returns the current classifier.
	 * 
	 * @return Current classifier.
	 */
	public Classifier getClassifier() {
		return classifier.getClassifier();
	}

	/**
	 * This method opens a window containing a PieChart to visualize how
	 * distribution of the instances.
	 */
	public JPanel showPieChartClasses() {
		// Used code example:
		// http://www.vogella.com/tutorials/JFreeChart/article.html
		if (classifiedData == null) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null,
					"No classified data. Add classified data first.");
			return new JPanel();
		}
		//WaitCalculatingForm.showCalculatingWindow();
		DefaultPieDataset dataset = convertToPieDataset();
		JFreeChart chart = ChartFactory.createPieChart("Class Distribution",
				dataset, true, true, false);
		//WaitCalculatingForm.hideCalculatingWindow();
		return new ChartPanel(chart);
	}

	/**
	 * Converts the classified dataset into a PieDataset and returns this
	 * dataset.
	 * 
	 * @return To PieDataSet converted assessed data.
	 */
	private DefaultPieDataset convertToPieDataset() {
		// Used code example:
		// http://www.vogella.com/tutorials/JFreeChart/article.html
		DefaultPieDataset dataset = new DefaultPieDataset();
		ClassCounter[] classCounter = initClassCounterArray(classifiedData
				.classes().size());
		for (int i = 0; i < classifiedData.size(); i++) {
			Object classValue = classifiedData.get(i).classValue();
			int pos = classifiedData.classIndex(classValue);
			if (pos != -1) {
				String name = classifiedData.get(i).classValue().toString();
				classCounter[pos].setCount(classCounter[pos].getCount() + 1);
				classCounter[pos].setName(name);
			}
		}
		for (int i = 0; i < classCounter.length; i++) {
			dataset.setValue(classCounter[i].getName(),
					classCounter[i].getCount());
		}
		return dataset;
	}
	
	public ObjectNode getJsonPieChart() {
		ClassCounter[] classCounter = initClassCounterArray(classifiedData
				.classes().size());
		for (int i = 0; i < classifiedData.size(); i++) {
			Object classValue = classifiedData.get(i).classValue();
			int pos = classifiedData.classIndex(classValue);
			if (pos != -1) {
				String name = classifiedData.get(i).classValue().toString();
				classCounter[pos].setCount(classCounter[pos].getCount() + 1);
				classCounter[pos].setName(name);
			}
		}
		ObjectNode result = Json.newObject();
		ArrayNode dataArray = result.putArray("data");
	
		for (int i = 0; i < classCounter.length; i++) {
			ArrayNode array = dataArray.addArray();
			array.add(classCounter[i].getName()).add(classCounter[i].getCount());
		}
		return result;
	}
	/**
	 * This method initializes a array with deafult ClassCounter Objects.
	 * 
	 * @param size
	 *            The length of the array.
	 * @return The initialized ClassCounter array.
	 */
	private ClassCounter[] initClassCounterArray(int size) {
		ClassCounter[] array = new ClassCounter[size];
		for (int i = 0; i < array.length; i++) {
			array[i] = new ClassCounter();
		}
		return array;
	}

	/**
	 * This method opens a window containing a BarChart to visualize the
	 * precision of the classifier.
	 */
	public JPanel showBarChartPrecision(boolean addTrainingData) {
		// Used code example:
		// http://dvillela.servehttp.com:4000/apostilas/jfreechart_tutorial.pdf
		if (classifiedData == null) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null,
					"No classified data. Add data to classify first.");
			return new JPanel();
		} else if (addTrainingData && trainingData == null) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null,
					"Could not find training data. Add training data first.");
			return new JPanel();
		}
		//WaitCalculatingForm.showCalculatingWindow();
		DefaultCategoryDataset dataset = convertToCategoryDataset(addTrainingData);
		String title = getPrecisionTitleFromDataset(dataset);
		JFreeChart chart = ChartFactory.createBarChart(title,
				"Classified as ..", "Count Of Instances", dataset,
				PlotOrientation.VERTICAL, false, true, false);
		//WaitCalculatingForm.hideCalculatingWindow();
		return new ChartPanel(chart);
	}

	/**
	 * This method uses the classified data to convert it into
	 * DefaultcCategoryDataset. For every class is calculated how much instances
	 * were classified correct and how much were classified wrong by comparing
	 * the data that should be classified and the data that already was
	 * classified.
	 * 
	 * @return The CategoryDataset with correct/wrong information for every
	 *         class.
	 */
	private DefaultCategoryDataset convertToCategoryDataset(
			boolean addTrainingData) {
		Dataset dataToCompare = new DefaultDataset();
		dataToCompare.addAll(classificationData);
		if (addTrainingData) {
			dataToCompare.addAll(trainingData);
		}
		if (dataToCompare.size() != classifiedData.size()) {
			// Used code example:
			// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
			JOptionPane.showMessageDialog(null, "Comparison  Problem.");
			return new DefaultCategoryDataset();
		}
		String[] classNames = new String[classifiedData.classes().size()];
		int[][] counters = new int[classifiedData.classes().size()][2];

		for (int i = 0; i < classifiedData.size(); i++) {
			Object classValue = classifiedData.get(i).classValue();
			int pos = classifiedData.classIndex(classValue);
			if (classifiedData.get(i).classValue() != null) {
				String name = classifiedData.get(i).classValue().toString();
				classNames[pos] = name;
				if (classValue.toString().equals(
						dataToCompare.get(i).classValue().toString())) {
					counters[pos][0]++;
				} else {
					counters[pos][1]++;
				}
			}
		}
		return generateCategoryDataset(classNames, counters);
	}

	/**
	 * This method uses the given Array of Class-Names and the array with wrong
	 * and correct counter to create a DefaultCategoryDataset.
	 * 
	 * @param name
	 *            Array of Class-Names.
	 * @param counters
	 *            The 2-dimensional array where every the first value stands for
	 *            the number of correct classified Instances and the second one
	 *            for number of wrong classified instances.
	 * @return The CategoryDataset with correct/wrong information for every
	 *         class.
	 */
	private DefaultCategoryDataset generateCategoryDataset(String[] name,
			int[][] counters) {
		// Used code example:
		// http://dvillela.servehttp.com:4000/apostilas/jfreechart_tutorial.pdf
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int i = 0; i < name.length; i++) {
			dataset.addValue(counters[i][0], "Correct", name[i]);
			dataset.addValue(counters[i][1], "Wrong", name[i]);
		}
		return dataset;
	}

	/**
	 * This method opens a window containing a BarChart to visualize the results
	 * of performing a cross-Validation test.
	 */
	public ObjectNode getJsonCrossValidation(int folds) {
		if (!checkRequirementsForCrosValidation()) {
			return null;
		}
		//WaitCalculatingForm.showCalculatingWindow();
		performCrossValidation(false, folds);
		return convertCrossValidationResultToCategoryDataset(this.crossValidationResult);
	}
	/**
	 * This method checks whether all requirements are fulfilled to perform a
	 * crossValidation
	 * 
	 * @return TRUE if all requirements are fulfilled else FALSE.
	 */
	private boolean checkRequirementsForCrosValidation() {
		if (classifier == null) {
			return false;
		} else if (trainingData == null) {
			return false;
		}
		return true;
	}

	/**
	 * Opens a form with detailed information about the crossValidation results.
	 * 
	 * @param xPos
	 *            x-Position on the screen.
	 * @param yPos
	 *            y-Position on the screen.
	 */
	/*
	public boolean showCrossValidationResults(int xPos, int yPos) {
		if (!checkRequirementsForCrosValidation()) {
			return false;
		}
		performCrossValidation(true);
		//WaitCalculatingForm.showCalculatingWindow();
		CrossValidationResultForm form = new CrossValidationResultForm(xPos,
				yPos, this.crossValidationResult);
		//WaitCalculatingForm.hideCalculatingWindow();
		form.setVisible(true);
		return true;
	}*/

	/**
	 * This method convert the results from a crossValidation-test into a
	 * Json object.
	 * 
	 * @param crossValidationResult
	 *            The result of a crossValidation-test.
	 * @return A Json Object containing the crossValidation results.
	 */
	private ObjectNode convertCrossValidationResultToCategoryDataset(
			Map<Object, PerformanceMeasure> crossValidationResult) {
		ObjectNode result = Json.newObject();
		ArrayNode classesArray = result.putArray("classes");
		ArrayNode dataArray = result.putArray("data");
		ObjectNode TP = dataArray.addObject();
		TP.put("name", "TP");
		ArrayNode TPdata = TP.putArray("data");
		
		ObjectNode FP = dataArray.addObject();
		FP.put("name", "FP");
		ArrayNode FPdata = FP.putArray("data");

		ObjectNode TN = dataArray.addObject();
		TN.put("name", "TN");
		ArrayNode TNdata = TN.putArray("data");

		ObjectNode FN = dataArray.addObject();
		FN.put("name", "FN");
		ArrayNode FNdata = FN.putArray("data");

		ObjectNode Accuracy = dataArray.addObject();
		Accuracy.put("name", "Accuracy");
		ArrayNode ACCdata = Accuracy.putArray("data");


		for (Entry<Object, PerformanceMeasure> element : crossValidationResult
				.entrySet()) {
			PerformanceMeasure Value = element.getValue();
			TPdata.add(Value.getTPRate());
			FPdata.add(Value.getFPRate());
			TNdata.add(Value.getTNRate());
			FNdata.add(Value.getFNRate());
			ACCdata.add(Value.getAccuracy());
			classesArray.add(element.getKey().toString());
		}
		return result;
	}

	/**
	 * This method returns a String with the precision-calculation-results.
	 * 
	 * @param dataset
	 *            Dataset with information how much classes were classified
	 *            right.
	 * @return The String with the precision-calculation-results.
	 */
	private String getPrecisionTitleFromDataset(DefaultCategoryDataset dataset) {
		int correct = 0;
		int wrong = 0;
		for (int i = 0; i < dataset.getColumnCount(); i++) {
			correct = correct + dataset.getValue(0, i).intValue();
			wrong = wrong + dataset.getValue(1, i).intValue();
		}
		int instances = correct + wrong;
		double precision = Math
				.round((correct * 1.0 / instances * 1.0) * 10000) * 1.0 / 100;
		return "Instances: " + instances + "  Correct: " + correct
				+ "  Wrong: " + wrong + "  Precision: " + precision + "%";
	}

	/**
	 * This method gets a file in text format, converts it into a sparse dataset
	 * that can be used by the classifiers and sets it a current training or
	 * classification data.
	 * 
	 * @param dataFile
	 *            The data in text format you want to convert.
	 * @param separator
	 *            The symbol used as the separator (String).
	 * @param fileTarget
	 *            Use file for training or classification(e.g. SET_TRAINING_DATA
	 *            or SET_CLASSIFICATION_DATA).
	 * @return TRUE if could convert and set the data successfully.
	 * @throws IOException
	 *             Problem while opening or reading file.
	 * @throws ParseException
	 *             Problem while parsing date.
	 */
	
	public boolean convertTextFileIntoSparse(File dataFile, String separator,
			int fileTarget, boolean fromCollectedData, boolean classified) throws IOException,
			ParseException {
		// Initialize variables
		// Used code example: http://www.javaschubla.de/2007/javaerst0250.html
		String instanceTitle = "";
		int alreadyClassified = 1;
		ArrayList<String> wordList = new ArrayList<String>();
		boolean addBehindMessage = useBehindSeparator;
		boolean preProcessData = false;
		int columnOffset = 1;
		//PreprocessorForm preProcessor = null;
		if (preProcessData) {
		//	preProcessor = new PreprocessorForm();
		//	wordList = getWordsList(dataFile, fileTarget, preProcessor,
			//		addBehindMessage);
		} else {
			wordList = getWordsList(dataFile, fileTarget, addBehindMessage);
		}
		// Set training data or classification data
		if (fileTarget == ClassificationManager.SET_CLASSIFICATION_DATA) {
			if (fromCollectedData) {
				instanceTitle = this.dummyClass;
				System.out.println(instanceTitle);
			} else {
				if (!classified) {
					instanceTitle = this.dummyClass;
					columnOffset = 0;
				} else {
					
					alreadyClassified = 0;
				}
			}
		}
		// initialize reader and writer
		FileReader fileReader = new FileReader(dataFile);
		BufferedReader reader = new BufferedReader(fileReader);
		File resultFile = getFile(true);
		PrintWriter writer = new PrintWriter(resultFile);
		// read file
		String line = reader.readLine();
		String resultLine;
		ArrayList<Date> dateList = new ArrayList<Date>();

		while (line != null) {
			String[] elements = line.split(separator);
			resultLine = selectInstanceTitle(elements[0], instanceTitle,
					fileTarget, alreadyClassified);
			// create line in sparse format
			String[] words = new String[1];
			String messageText = elements[columnOffset+1];
			if (addBehindMessage) {
				messageText = extractMessage(elements, columnOffset+1, true);
			}
			if (preProcessData) {
			//	if (preProcessor.isPOSSelected()) {
			//		String resultText = preProcessor
			//				.getPreproccedString(messageText);
			//		words = resultText.split(" ");
			//	} else {
			//		words = preProcessor.getPreproccedArrayList(messageText
			//				.split(" "));
			//	}
			} else {
				words = messageText.split(" ");
			}
			ArrayList<ClassCounter> wordCounts = getWordCounterList(wordList,
					words);
			resultLine = resultLine + createSparseLine(wordList, wordCounts);
			dateList.add(parseDate(elements[columnOffset]));
			writer.println(resultLine);
			line = reader.readLine();
		}
		// Close method
		writer.flush();
		reader.close();
		writer.close();
		setSparseData(resultFile, fileTarget);
		if (fileTarget == ClassificationManager.SET_TRAINING_DATA) {
			//datesByIndexFromTrain = dateList;
		} else {
			//datesByIndexFromClassification = dateList;
			datesByIndex = dateList;
			System.out.println(datesByIndex.size() + "!!!!");
		}
		return true;

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
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
			return formatter.parse(dateString);
		}
	}

	/**
	 * This method uses the fileTarget and the information from
	 * alreadyClassified to decide whether use the classified name or the
	 * dummy-name (-> standardName).
	 * 
	 * @param classifiedName
	 *            The name from the classified data.
	 * @param standardName
	 *            The dummy-name entered by the user.
	 * @param fileTarget
	 *            Use file for training or classification(e.g. SET_TRAINING_DATA
	 *            or SET_CLASSIFICATION_DATA).
	 * @param alreadyClassified
	 *            0 if data is already classified.
	 * @return standardName if date is used to be classified and not classified
	 *         else classifiedName.
	 */
	private String selectInstanceTitle(String classifiedName,
			String standardName, int fileTarget, int alreadyClassified) {
		if (fileTarget == ClassificationManager.SET_TRAINING_DATA) {
			// data is classified and the class name will be kept
			return classifiedName;
		} else {
			if (alreadyClassified == 0) {
				// data is classified and the class name will be kept
				return classifiedName;
			} else {
				// data is not classified and a dummy name is set
				return standardName;
			}
		}
	}

	/**
	 * This method uses the existing wordList from the training or creates a new
	 * one by extracting the information from the dataFile and edit the data by
	 * using the preprocessor.
	 * 
	 * @param dataFile
	 *            The dataFile containing the words to extract.
	 * @param fileTarget
	 *            Use file for training or classification(e.g. SET_TRAINING_DATA
	 *            or SET_CLASSIFICATION_DATA).
	 * @return A list of words from the file (without duplicates).
	 * @throws IOException
	 *             Problem while reading file.
	 */
	/*
	private ArrayList<String> getWordsList(File dataFile, int fileTarget,
			PreprocessorForm preProcessor, boolean addBehindMessage)
			throws IOException {
		ArrayList<String> wordList;
		// Set training data or classification data
		if (fileTarget == ClassificationManager.SET_TRAINING_DATA) {
			// set wordList to use it later for feature extraction
			if (preProcessor.isPOSSelected()) {
				wordList = getWordList(dataFile, addBehindMessage, preProcessor);
			} else {
				wordList = getWordList(dataFile, addBehindMessage);
				wordList = preProcessor.getPreproccedStringList(wordList);
			}
			this.trainedWordList = wordList;
		} else {
			// use wordList from training data
			wordList = this.trainedWordList;
		}
		return wordList;
	}
*/
	/**
	 * This method uses the existing wordList from the training or creates a new
	 * one by extracting the information from the dataFile.
	 * 
	 * @param dataFile
	 *            The dataFile containing the words to extract.
	 * @param fileTarget
	 *            Use file for training or classification(e.g. SET_TRAINING_DATA
	 *            or SET_CLASSIFICATION_DATA).
	 * @return A list of words from the file (without duplicates).
	 * @throws IOException
	 *             Problem while reading file.
	 */
	private ArrayList<String> getWordsList(File dataFile, int fileTarget,
			boolean addBehindMessage) throws IOException {
		ArrayList<String> wordList;
		// Set training data or classification data
		if (fileTarget == ClassificationManager.SET_TRAINING_DATA) {
			// set wordList to use it later for feature extraction
			wordList = getWordList(dataFile, addBehindMessage);
			this.trainedWordList = wordList;
		} else {
			// use wordList from training data
			wordList = this.trainedWordList;
		}
		return wordList;
	}

	/**
	 * This method opens a dialog where the user can decide whether he want to
	 * save the result file. If the user wants to save the result file there is
	 * opening a dialog to select the file. If the user does not wants to save
	 * the result file there is created a temporary file.
	 * 
	 * @return The empty file.
	 */
	private File getFile(boolean skipDialog) {
		if(!skipDialog){
			if (InputOptionCollection.getYesNoSelection(
					"Do you want to save the result file (in sparse format)?",
					"Save File")) {
				File selectedFile = InputOptionCollection.selectFile();
				if (selectedFile != null) {
					return selectedFile;
				} else {
					File resultFile = new File("convertFile");
					resultFile.deleteOnExit();
					return resultFile;
				}
			} else {
				File resultFile = new File("convertFile");
				resultFile.deleteOnExit();
				return resultFile;
			}
		} else {
			File resultFile = new File(Play.application().path().getPath()+"/private/convertFile");
			resultFile.deleteOnExit();
			return resultFile;
		}
	}

	/**
	 * This method convert the information from the wordList and the wordCounts
	 * into a sparse format line as a String.
	 * 
	 * @param wordList
	 *            The list containing the word position.
	 * @param wordCounts
	 *            The list containing the count for every word.
	 * @return The String formatted text line.
	 */
	private String createSparseLine(ArrayList<String> wordList,
			ArrayList<ClassCounter> wordCounts) {
		String result = "";
		for (int i = 0; i < wordCounts.size(); i++) {
			int index = 1 + wordList.indexOf(wordCounts.get(i).getName());
			if (index != 0) {
				result = result + " " + index + ":"
						+ (wordCounts.get(i).getCount() + 1);
			}
		}
		return result;
	}

	/**
	 * This method uses the given word list and an array of words to return a
	 * list of ClassCounter containing the word and how often it appears in the
	 * word array.
	 * 
	 * @param wordList
	 *            List with possible words.
	 * @param words
	 *            Array of words you want to count.
	 * @return List of ClassCounter containing the word and how often it appears
	 *         in the word array.
	 */
	private ArrayList<ClassCounter> getWordCounterList(
			ArrayList<String> wordList, String[] words) {
		ArrayList<ClassCounter> result = new ArrayList<ClassCounter>();
		for (int i = 0; i < words.length; i++) {
			ClassCounter classCounter = new ClassCounter(words[i], 0);
			if (posClassCounter(result, classCounter) != -1) {
				int pos = posClassCounter(result, classCounter);
				result.get(pos).incCount();
			} else {
				result.add(classCounter);
			}
		}
		return result;
	}

	/**
	 * Returns the position of a class counter in a classCounter list.
	 * 
	 * @param list
	 *            List with classCounters.
	 * @param classCounter
	 *            The classCounter which position should be found.
	 * @return The position of the classCounter in the list or -1 if was not
	 *         found.
	 */
	private int posClassCounter(ArrayList<ClassCounter> list,
			ClassCounter classCounter) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getName().equals(classCounter.getName())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * This method gets a File and extracts all words without duplicates.
	 * 
	 * @param reader
	 *            The already (with a file) initialized BufferedReader.
	 * @return A list of all words from the BufferedReader (without duplicates).
	 * @throws IOException
	 *             Problem while reading from file.
	 */
	private ArrayList<String> getWordList(File dataFile,
			boolean addBehindMessage) throws IOException {
		// Used code example: http://www.javaschubla.de/2007/javaerst0250.html
		FileReader fileReader = new FileReader(dataFile);
		BufferedReader reader = new BufferedReader(fileReader);
		ArrayList<String> wordList = new ArrayList<String>();
		String line = reader.readLine();
		while (line != null) {
			String[] splitMessage = line.split(";");
			String[] wordArray = splitMessage[2].split(" ");
			if (addBehindMessage) {
				wordArray = extractMessage(splitMessage, 2, true).split(" ");
			}
			for (int i = 0; i < wordArray.length; i++) {
				if (!wordList.contains(wordArray[i])) {
					wordList.add(wordArray[i]);
				}
			}
			line = reader.readLine();
		}
		reader.close();
		return wordList;
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
	 * This method uses the classified data and group it by date.
	 * 
	 * @return The classified data grouped by date.
	 */
	private TreeMap<String, TimedClassifiedData> convertToAggregatedByTime() {
		Comparator<String> comparator = new Comparator<String>() {
			  public int compare(String o1, String o2) {
				  if(o1.equals(o2))
					  return 0;
			    try {
					if(new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").parse(o1).before(new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").parse(o2)))
						return -1;
					else
						return 1;
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return -1000;
			  }
			};
		TreeMap<String, TimedClassifiedData> map = new TreeMap<String, TimedClassifiedData>(comparator);
		for (int i = 0; i < classifiedData.size(); i++) {
			if (!map.containsKey(fromDateToString(datesByIndex.get(i)))) {
				Second second = new Second(datesByIndex.get(i));
				ArrayList<Instance> data = new ArrayList<Instance>();
				data.add(classifiedData.get(i));
				TimedClassifiedData timedClassifiedData = new TimedClassifiedData(
						second, data);
				map.put(fromDateToString(datesByIndex.get(i)),
						timedClassifiedData);
			} else {
				map.get(fromDateToString(datesByIndex.get(i))).addInstance(
						classifiedData.get(i));
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
	private String fromDateToString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
		return formatter.format(date);
	}

	/**
	 * This method uses the classified data (grouped by date) and sums the
	 * elements up to one value per date. If Y_AXIS_INSTANCE_COUNT is select the
	 * methods calculates the number of instances per date. If Y_AXIS_NOMINAL is
	 * selected the method calculates the most frequent class per date. If
	 * Y_AXIS_METRIC is selected the method calculates the average class value
	 * per date.
	 * 
	 * @param yAxisValue
	 *            The operation the method should use.
	 * @return A TimeSeriesCollection containing the aggregated classified data.
	 */
	private TimeSeriesCollection createTimeSeriesCollection(int yAxisValue) {
		// Used code example:
		// http://dvillela.servehttp.com:4000/apostilas/jfreechart_tutorial.pdf
		TimeSeries series = new TimeSeries("Classified Data");
		TreeMap<String, TimedClassifiedData> timeByDate = convertToAggregatedByTime();
		if (timeByDate.size() < 2) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null,
					"Not enough dates in the dataset.");
		}
		if (yAxisValue == ClassificationManager.Y_AXIS_METRIC) {
			TimedClassifiedData.setClassNumberForm(classifiedData.classes());
		}
		for (TimedClassifiedData element : timeByDate.values()) {
			Second minute = element.getDate();
			double value = getYAxisValue(element, yAxisValue);
			if (value != Double.MIN_VALUE) {
				series.add(minute, value);
			}
		}
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(series);
		return dataset;
	}

	/**
	 * This method calculates the value for the y-Axis .
	 * 
	 * @param data
	 *            The data you want to aggregate.
	 * @param yAxisValue
	 *            The operation you want to use.
	 * @return The aggregated value from the the data.
	 */
	private double getYAxisValue(TimedClassifiedData data, int yAxisValue) {
		if (yAxisValue == ClassificationManager.Y_AXIS_INSTANCE_COUNT) {
			return data.getClassifiedData().size();
		} else if (yAxisValue == ClassificationManager.Y_AXIS_METRIC) {
			return data.getClassesAverage();
		} else return Double.MIN_VALUE;
	}
	private int getYAxisCategory(TimedClassifiedData data, int yAxisValue) {
			ArrayList<ClassCounter> classCounter = data
					.getClassCount(classifiedData.classes());
			//LegendForm.setClassCounter(classCounter);
			return getClassWithMaxValue(classCounter);
	}
	/**
	 * This method gets a arrayList of ClassCounters and returns the largest
	 * value in the list. The smallest possible value is 0.
	 * 
	 * @param classCounter
	 *            The arrayList where the largest value is searched.
	 * @return The largest value in the arrayList.
	 */
	private int getClassWithMaxValue(ArrayList<ClassCounter> classCounter) {
		int currClass = 0;
		double currValue = 0;
		for (int i = 0; i < classCounter.size(); i++) {
			if (classCounter.get(i).getCount() > currValue) {
				currValue = classCounter.get(i).getCount();
				currClass = i;
			}
		}
		return currClass;
	}
	
	public Map<Long, String> getClassifiedDataMap() {
		Map<Long, String> results = new HashMap<Long,String>();
		for (int i = 0; i < classifiedData.size(); i++) {
			results.put(tweetIdByIndex.get(i), classifiedData.get(i).classValue().toString());
		}
		return results;
	}
	
	public File saveResultsToFile() throws FileNotFoundException {
		File resultsFile = new File("Classified_Data.csv");
		PrintWriter writer = new PrintWriter(resultsFile);
		for (int i = 0; i < classifiedData.size(); i++) {
			if (datesByIndex == null) {
				writer.println((i + 1) + ";"
						+ classifiedData.get(i).classValue().toString() + ";"
						+ instanceToSparseString(classifiedData.get(i)));
			} else {
				if (datesByIndex.size() != classifiedData.size()) {
					writer.println((i + 1) + ";"
							+ classifiedData.get(i).classValue().toString()
							+ ";"
							+ instanceToSparseString(classifiedData.get(i)));
				} else {
					writer.println((i + 1) + ";"
							+ datesByIndex.get(i).toString() + ";"
							+ classifiedData.get(i).classValue().toString()
							+ ";"
							+ instanceToSparseString(classifiedData.get(i)));
				}
			}
		}
		writer.flush();
		writer.close();
		return resultsFile;
	}

	/**
	 * Convert the information from a Instance-Object into a String in
	 * sparse-format.
	 * 
	 * @param instance
	 *            The Instances object where the information should be
	 *            converted.
	 * @return The sparse-formatted String.
	 */
	private String instanceToSparseString(Instance instance) {
		Set<Map.Entry<Integer, Double>> data = instance.entrySet();
		String result = "";
		boolean firstElement = true;
		for (Entry<Integer, Double> element : data) {
			if (firstElement) {
				result = result + element.getKey() + ":" + element.getValue();
				firstElement = false;
			} else {
				result = result + " " + element.getKey() + ":"
						+ element.getValue();
			}
		}
		return result;
	}

	/**
	 * Performs a crosValidation by using user input for dataset and number of
	 * folds.
	 * 
	 * @return Results of the crossValidation.
	 */
	public boolean performCrossValidation(boolean checkForClassifier, int folds) {
		if (checkForClassifier) {
			if (this.crossValidationResult == null) {
				// Used code example: http://java-tutorial.org/joptionpane.html
				JOptionPane.showMessageDialog(null,
						"You have to perform a cross-validatio first!");
			} else {
				return false;
			}
		}
		Dataset crossValidationDataset = trainingData.copy();
		this.crossValidationResult = classifier.getCrossValidation(
				crossValidationDataset, folds);
		if(this.crossValidationResult == null)
		{
			System.out.println("cross validation is null !!!! ");
			return false;
		}
		else{
			return true;
		}
	}

	/**
	 * This method creates a Panel containing the regression chart.
	 * 
	 * @param yAxisValue
	 *            Decide what to do with the data(e.g.
	 *            EmotionMeasurementManager.Y_AXIS_INSTANCE_COUNTER).
	 * @return A Panel with the regression chart by using the given operation.
	 */
	public JPanel showRegressionChart(int yAxisValue) {
		if (!checkRequirementsForDataWithTime()) {
			return new JPanel();
		}
		//WaitCalculatingForm.showCalculatingWindow();
		TimeSeriesCollection timeSeriesCollection = createTimeSeriesCollection(yAxisValue);
		TimeSeries timeSeries = timeSeriesCollection.getSeries(0);
		String yAxisName = "Class Value";
		if (yAxisValue == ClassificationManager.Y_AXIS_INSTANCE_COUNT) {
			yAxisName = "Instance Count";
		}
		XYSeries xySeries = TimeSeriesPostProcessor.convertTimeSeriesToIndex(
				timeSeries, yAxisValue, yAxisName);
		XYSeriesCollection s = new XYSeriesCollection(xySeries);
		RegressionPanel r = new RegressionPanel();
		//WaitCalculatingForm.hideCalculatingWindow();
		return r.getRegressionPanel(s, yAxisName);
	}
	
	public ObjectNode getJsonRegression(int yAxisValue){
		if (!checkErrors()) {
			return null;
		}
		ObjectNode result = Json.newObject();
		ArrayNode dataArray = result.putArray("data");
		TimeSeriesCollection timeSeriesCollection = createTimeSeriesCollection(yAxisValue);
		TimeSeries timeSeries = timeSeriesCollection.getSeries(0);
		String yAxisName = "Class Value";
		if (yAxisValue == ClassificationManager.Y_AXIS_INSTANCE_COUNT) {
			yAxisName = "Instance Count";
		}
		XYSeries xySeries = TimeSeriesPostProcessor.convertTimeSeriesToIndex(
				timeSeries, yAxisValue, yAxisName);
		for (XYDataItem element : (List<XYDataItem>) xySeries.getItems()){
			ArrayNode array = dataArray.addArray();
			array.add(element.getXValue()).add(element.getYValue());
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
		if (!checkRequirementsForDataWithTime()) {
			return new JPanel();
		}
		//WaitCalculatingForm.showCalculatingWindow();
		TimeSeries timeSeries = createTimeSeriesCollection(yAxisValue)
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
		//WaitCalculatingForm.hideCalculatingWindow();
		return new ChartPanel(chart);
	}
	
	public ObjectNode getJsonMovingAverage(int yAxisValue, int movAvgRangeSize){
		if (!checkErrors()) {
			return null;
		}
		ObjectNode result = Json.newObject();
		ArrayNode dataArray = result.putArray("data");
		TimeSeries timeSeries = createTimeSeriesCollection(yAxisValue)
				.getSeries(0);
		TimeSeries movAvgTimeSeries = TimeSeriesPostProcessor
				.movAvgOnTimeSeries(timeSeries, movAvgRangeSize);
		for (TimeSeriesDataItem element : (List<TimeSeriesDataItem>)movAvgTimeSeries.getItems()){
			ArrayNode array = dataArray.addArray();
			array.add(element.getPeriod().getMiddleMillisecond()).add(element.getValue().doubleValue());
		}
		return result;
	}
	public ObjectNode getJsonTimeSeries(int yAxisValue) {
		if (!checkErrors()) {
			return null;
		}
		TreeMap<String, TimedClassifiedData> timeByDate = convertToAggregatedByTime();
		if (timeByDate.size() < 2) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			return null;
		}
		if (yAxisValue == ClassificationManager.Y_AXIS_METRIC) {
			TimedClassifiedData.setClassNumberForm(classifiedData.classes());
		}
		ObjectNode result = Json.newObject();
		ArrayNode dataArray = result.putArray("data");
		for (TimedClassifiedData element : timeByDate.values()) {
			ArrayNode array = dataArray.addArray();
			if (yAxisValue == ClassificationManager.Y_AXIS_NOMINAL) {
				int value = getYAxisCategory(element, yAxisValue);
				array.add(element.getDate().getMiddleMillisecond()).add(value);
			}
			else
			{
				double value = getYAxisValue(element, yAxisValue);
				if (value != Double.MIN_VALUE) {
					array.add(element.getDate().getMiddleMillisecond()).add(value);
				}
			}
		}
		return result;
		
	}
	
	/**
	 * This method opens a window containing a LineChart to visualize the
	 * classified data (sorted by time).
	 */
	public JPanel showTimeSeriesChart(int yAxisValue) {
		if (!checkRequirementsForDataWithTime()) {
			return new JPanel();
		}
		String title = "";
		String yAxis = "";
		//WaitCalculatingForm.showCalculatingWindow();
		if (yAxisValue == ClassificationManager.Y_AXIS_INSTANCE_COUNT) {
			title = "Instance Count Per Day";
			yAxis = "Sum Of Instances";
		} else if (yAxisValue == ClassificationManager.Y_AXIS_METRIC) {
			title = "Classification Results (Average Per Day)";
			yAxis = "Class Value (Average)";
		} else {
			title = "Classification Results (Most Frequent Class Per Day)";
			yAxis = "Class Value (Most Frequent)";
		}
		// Used code example:
		// http://dvillela.servehttp.com:4000/apostilas/jfreechart_tutorial.pdf
		// http://www.jfree.org/phpBB2/viewtopic.php?f=3&t=30580
		if (classifiedData.size() != datesByIndex.size()) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null, "Could not match dates.");
			TimeSeriesCollection datasetError = new TimeSeriesCollection();
			JFreeChart chartError = ChartFactory.createTimeSeriesChart(title,
					"Time", yAxis, datasetError, true, true, false);
			//WaitCalculatingForm.hideCalculatingWindow();
			return new ChartPanel(chartError);
		}
		TimeSeriesCollection dataset = createTimeSeriesCollection(yAxisValue);
		JFreeChart chart = ChartFactory.createTimeSeriesChart(title, "Time",
				yAxis, dataset, true, true, false);
		XYPlot plot = chart.getXYPlot();
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));
		if (yAxisValue == ClassificationManager.Y_AXIS_NOMINAL) {
			chart.getXYPlot().setRenderer(
					new XYLineAndShapeRenderer(false, true));
		}
		ChartPanel resultPanel = new ChartPanel(chart);
		//WaitCalculatingForm.hideCalculatingWindow();
		return resultPanel;
	}

	/**
	 * This method checks whether all requirements are fulfilled to present the
	 * time over a certain period.
	 * 
	 * @return TRUE if all requirements are fulfilled else FALSE.
	 */
	private boolean checkRequirementsForDataWithTime() {
		if (classifiedData == null) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null,
					"No classified data. Add classified data first.");
			return false;
		} else if (datesByIndex == null) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane
					.showMessageDialog(null,
							"No dates found. Check whether the data was classified correctly.");
			return false;
		} else if (classificationData == null) {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null,
					"No classification data. Add data to classify first.");
			return false;
		}
		return true;
	}
	private Boolean checkErrors() {
		if (classifiedData == null || datesByIndex == null || classificationData == null) {
			return false;
		} 
		return true;
	}

	/**
	 * This method aggregates the data on every day by using the given method.
	 * Then this aggregated data is saved in file that must be selected.
	 * 
	 * @param yAxisValue
	 *            The method how the data should be aggregated (e.g.
	 *            Y_AXIS_METIC or Y_AXIS_NOMINAL).
	 * @return TRUE if the data could be saved successfully.
	 * @throws IOException
	 *             Problem while writing into the file.
	 */
	public boolean saveAggregatedData(int yAxisValue) throws IOException {
		if (!checkRequirementsForDataWithTime()) {
			return false;
		}
		TimeSeries timeSeries = createTimeSeriesCollection(yAxisValue)
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

	public void setUseBehindSeparator(boolean value) {
		useBehindSeparator = value;
	}

	public boolean isTrained() {
		return hasTrainedClassifier;
	}
	
	public int trainingSetSize() {
		return trainingData.size();
	}
	
	public int classifiedDataSize() {
		return classifiedData.size();
	}
	public int classificationDataSize() {
		return classificationData.size();
	}
	
	public void removeTrainingData() {
		this.trainingData = null;
	}
	public void reset() {
		this.classifier = null;
		this.trainingData = null;
	}
}
