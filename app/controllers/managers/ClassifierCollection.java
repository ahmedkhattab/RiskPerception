package controllers.managers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import controllers.modifiedWekaClasses.WekaClassifier;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.ZeroR;
import net.sf.javaml.classification.evaluation.CrossValidation;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import tools.ConsoleController;
import tools.InputOptionCollection;
import tools.DataTypes.ClassCounter;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.rules.OneR;
import weka.classifiers.trees.J48;
import weka.core.SelectedTag;

/**
 * This class implements a selection of different classifiers by using the
 * "LibSVM" and WEKA libraries from the java-machine-learning-library.
 * 
 * @author Christian Olenberger
 * 
 */
public class ClassifierCollection implements Serializable {

	private static final long serialVersionUID = -7863387301110822685L;
	// Possible classifiers
	public static final int SVM_CLASSIFIER = 0;
	public static final int K_NEAREST_NEIGHBORS = 1;
	public static final int Zero_Rule = 2;
	public static final int NAIVE_BAYES = 3;
	public static final int J48 = 4;
	public static final int ONE_RULE = 5;

	/**
	 * Dataset used to train the classifier.
	 */
	private Dataset trainingData;

	/**
	 * Classifier-Object
	 */
	private Classifier classifier;

	/**
	 * Default constructor to create a classifier without training.
	 */
	public ClassifierCollection(int classifierValue) {
		setClassifier(classifierValue);
	}

	/**
	 * Create ClassifierCollection with existing classifier.
	 */
	public ClassifierCollection(Classifier classifier) {
		this.classifier = classifier;
	}

	/**
	 * Constructor with given dataset for training.
	 * 
	 * @param trainingData
	 *            The dataset to train the classifier
	 */
	public ClassifierCollection(int classifierValue, Dataset trainingData) {
		this.trainingData = trainingData;
		setClassifier(classifierValue);
		// Disable console to avoid training result print
		ConsoleController.disableConsolePrint();
		classifier.buildClassifier(trainingData);
		this.trainingData = null;
		ConsoleController.enableConsolePrint();
	}

	/**
	 * This method decides which classifer is used for this instance.
	 * 
	 * @param classifierValue
	 *            The value that stands for the classifier (e.g.
	 *            ClassifierCollection.SVM_Classifier).
	 */
	private void setClassifier(int classifierValue) {
		switch (classifierValue) {
		case ClassifierCollection.SVM_CLASSIFIER:
			//setSVMClassifier();
			setDefaultSVMClassifier();
			break;
		case ClassifierCollection.K_NEAREST_NEIGHBORS:
			setKNearestNeighborsClassifier();
			break;
		case ClassifierCollection.Zero_Rule:
			setZeroRuleClassifier();
			break;
		case ClassifierCollection.NAIVE_BAYES:
			setNaiveBayesClassifier();
			break;
		case ClassifierCollection.J48:
			setJ48Classifier();
			break;
		case ClassifierCollection.ONE_RULE:
			setOneRuleClassifier();
			break;
		default:
			//setSVMClassifier();
			setDefaultSVMClassifier();
		}

	}

	/**
	 * This method opens a dialog where the user can decide to change the
	 * parameter of the svm_classifier. After that the classifier of this object
	 * is set with the given parameters.
	 */
	/*private void setSVMClassifier() {
		// Used code example:
		// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
		if (InputOptionCollection.getYesNoSelection(
				"Do you want to change the parameter of the SVM-Classifier?",
				"Set Parameter")) {
			try {
				// from documentation:
				// http://weka.sourceforge.net/doc.stable/weka/classifiers/functions/LibSVM.html#setSVMType%28weka.core.SelectedTag%29
				this.classifier = new WekaClassifier(
						new SVMClassifierForm().getClassifier());
			} catch (Exception e) {
				this.classifier = new WekaClassifier(new LibSVM());
			}
		} else {
			this.classifier = new WekaClassifier(new LibSVM());
		}
	}*/
	
	private void setDefaultSVMClassifier() {
		// Used code example:
		// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
		LibSVM libSVM = new LibSVM();
		libSVM.setKernelType(new SelectedTag(0, LibSVM.TAGS_KERNELTYPE));
		this.classifier = new WekaClassifier(libSVM);
	}
	
	/**
	 * This class trains the classifier by using the given dataset.
	 * 
	 * @param trainingData
	 *            The given training dataset.
	 */
	public void trainClassifier(Dataset trainingData) {
		// Disable console to avoid training result print
		ConsoleController.disableConsolePrint();
		this.classifier.buildClassifier(trainingData);
		ConsoleController.enableConsolePrint();
	}

	/**
	 * This method opens a dialog where the user can decide to change the
	 * parameter of the k_nearest_neighbors_classifier. After that the
	 * classifier of this object is set with the given parameters.
	 * 
	 * @throws NumberFormatException
	 *             Problem while reading integer.
	 */
	private void setKNearestNeighborsClassifier() throws NumberFormatException {
		// Used code example: http://java-tutorial.org/joptionpane.html
		String enteredNumber = InputOptionCollection.getUserInput(
				"Enter the number of Nearest Neighbors (int):",
				"Enter number of neighbors", "20");
		int numberOfNeighbors;
		numberOfNeighbors = Integer.parseInt(enteredNumber);
		// Used code example:
		// http://java-ml.sourceforge.net/content/classification-basics
		this.classifier = new KNearestNeighbors(numberOfNeighbors);
	}

	/**
	 * This method sets a Zero-Rule-Classifier as current classifier.
	 */
	private void setZeroRuleClassifier() {
		// Used documentation:
		// http://java-ml.sourceforge.net/api/0.1.5/net/sf/javaml/classification/ZeroR.html
		ZeroR zeroR = new ZeroR();
		this.classifier = zeroR;
	}

	/**
	 * This method set a Naive-Bayes-Classifier as current classifier.
	 */
	private void setNaiveBayesClassifier() {
		this.classifier = new WekaClassifier(new NaiveBayes());
	}

	/**
	 * This method set a J48-classifier as current classifier.
	 */
	private void setJ48Classifier() {
		this.classifier = new WekaClassifier(new J48());
	}

	/**
	 * This method opens a dialog where the user can decide to change the
	 * parameter of the one_rule_classifier. After that the classifier of this
	 * object is set with the given parameters.
	 * 
	 * @throws NumberFormatException
	 *             Could not read integer.
	 */
	private void setOneRuleClassifier() throws NumberFormatException {
		// Used code example: http://java-tutorial.org/joptionpane.html
		String enteredNumber = InputOptionCollection.getUserInput(
				"Enter the minimum bucket size:", "Enter bucket size", "6");
		int bucketSize;
		bucketSize = Integer.parseInt(enteredNumber);
		// Used code example:
		// http://java-ml.sourceforge.net/content/classification-basics
		OneR oneRule = new OneR();
		oneRule.setMinBucketSize(bucketSize);
		this.classifier = new WekaClassifier(oneRule);
	}

	/**
	 * This method classifies the given dataset.
	 * 
	 * @param dataToClassify
	 *            The dataset that should be classified.
	 * @return List with every class and calculated count.
	 */
	public ArrayList<ClassCounter> classifyAggregated(Dataset dataToClassify) {

		Map<Object, Integer> resultMap = getResultMap();
		// Used code example:
		// http://java-ml.sourceforge.net/content/classification-basics
		for (Instance inst : dataToClassify) {
			Object calculatedClass = classifier.classify(inst);
			resultMap.put(calculatedClass, resultMap.get(calculatedClass) + 1);
		}
		return convertMapToList(resultMap);

	}

	/**
	 * This method classifies the given dataset.
	 * 
	 * @param dataToClassify
	 *            The dataset that should be classified.
	 * @return The classified dataset.
	 */
	public Dataset classifyDataset(Dataset dataToClassify) {
		// Used code example:
		// http://java-ml.sourceforge.net/content/classification-basics
		// http://java-ml.sourceforge.net/content/creating-instance
		Dataset result = new DefaultDataset();
		for (int i = 0; i < dataToClassify.size(); i++) {
			Instance inst = dataToClassify.get(i);
			Instance instance = inst.copy();
			//long startTime = System.currentTimeMillis();
			Object instanceClass = classifier.classify(instance);
			if(instanceClass == null)
				continue;
			instance.setClassValue(instanceClass);
			result.add(instance);
			/*	double resultTime = (System.currentTimeMillis() - startTime) * 1.0 / 1000;
			resultTime = Math.round(resultTime * 100) * 1.0 / 100;
			WaitCalculatingForm.setText("Calculating " + i + " of "
					+ dataToClassify.size() + " (" + resultTime
					+ " sec./inst.)");*/
		}
		return result;
	}

	/**
	 * Converts from Map<Object,Integer> to ArrayList<ClassCounter> because
	 * easier to handle.
	 * 
	 * @param map
	 *            The Map that should be converted.
	 * @return The converted ArrayList.
	 */
	private ArrayList<ClassCounter> convertMapToList(Map<Object, Integer> map) {
		ArrayList<ClassCounter> resultList = new ArrayList<ClassCounter>();
		// Used code example:
		// http://stackoverflow.com/questions/4234985/how-to-for-each-the-hashmap
		for (Map.Entry<Object, Integer> mapEntry : map.entrySet()) {
			resultList.add(new ClassCounter(mapEntry.getKey().toString(),
					mapEntry.getValue()));
		}
		return resultList;
	}

	/**
	 * This method creates a Map where every possible class is a Key and the
	 * value is zero.
	 * 
	 * @return A Map with class names as keys and 0 as value.
	 */
	private Map<Object, Integer> getResultMap() {
		int countClasses = trainingData.classes().size();
		Map<Object, Integer> resultMap = new HashMap<Object, Integer>();

		for (int i = 0; i < countClasses; i++) {
			resultMap.put(trainingData.classValue(i), 0);
		}

		return resultMap;
	}

	/**
	 * This method performs a 10 fold cross validation.
	 * 
	 * @param signedData
	 *            The already classified data for testing the classifier (size
	 *            must min. be the number of folds).
	 * @param folds
	 *            Number of folds.
	 * @return Crossvalidation results.
	 */
	public Map<Object, PerformanceMeasure> getCrossValidation(
			Dataset signedData, int folds) {
		// Used code example:
		// http://java-ml.sourceforge.net/content/classification-cross-validation
		if (signedData.size() < folds && folds >= 0) {
			return null;
		}
		int usedFolds = folds;
		if (usedFolds < 0) {
			usedFolds = signedData.size() + folds;
		}
		ConsoleController.disableConsolePrint();
		CrossValidation crossVal = new CrossValidation(classifier);
		Map<Object, PerformanceMeasure> crossValidationResult = crossVal
				.crossValidation(signedData, usedFolds);
		ConsoleController.enableConsolePrint();
		return crossValidationResult;
	}

	/**
	 * Returns the current classifier.
	 * 
	 * @return Current classifier.
	 */
	public Classifier getClassifier() {
		return classifier;
	}

}
