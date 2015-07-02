package controllers;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;

import org.jongo.MongoCursor;

import models.ClassifierChoice;
import models.PreprocessingChoice;
import models.Tweet;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import tools.Utils;
import views.formdata.AdminFormData;
import views.formdata.ClassificationFormData;
import views.html.classification;

import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.managers.ClassificationManager;
import controllers.managers.ClassifierCollection;

/**
 * This controller is the server side code for the classification
 * page. Used to handle serving the page, 
 * form submissions and ajax requests
 * 
 * @author Ahmed Khattab
 */
public class ClassificationController extends Controller {
	
	/**
	 * Returns the index page
	 * 
	 * @return The page containing the search form.
	 */
	public static Result classification() {
		if(!session().containsKey("ID"))
			session("ID",  UUID.randomUUID().toString());
		session("CLASSIFIER",  "DEFAULT");
		ClassificationFormData queryData = new ClassificationFormData();
		Form<ClassificationFormData> formData = Form.form(
				ClassificationFormData.class).fill(queryData);
	
		return ok(classification.render(formData,
				PreprocessingChoice.makePreprocessingMap(), ClassifierChoice.makeClassifiersMap(), false, false, AdminFormData.makeTrackingMap()));
	}
	
	public static Result handleFetchClassify(String fromDate, String toDate, String projectName) {
		if(fromDate.isEmpty() || toDate.isEmpty())
			return internalServerError("Error ! Dates were not specified");
		if(projectName.isEmpty())
			return internalServerError("Error ! Project name not specified");

		MongoCursor<twitter4j.Status> tweets = Tweet.findByDate(fromDate,
				toDate, projectName);
		if(tweets.count() == 0)
		{
			return internalServerError("No tweets found in the specified time window");
		}
		ClassificationManager classificationManager;
		if(session().get("CLASSIFIER").equals("DEFAULT"))
			classificationManager = (ClassificationManager) Utils.loadObject("default_class");
		else
			classificationManager = (ClassificationManager) Utils.loadObject(session("ID")+"_class");
		try {
			classificationManager.setRawTweets(tweets);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!classificationManager.classifyData(false))
			return internalServerError();
		response().setContentType("application/json");
		ObjectNode result = Json.newObject();
		result.put("loaded", classificationManager.classifiedDataSize());
		result.put("message", " Successfully classified "+classificationManager.classifiedDataSize()+" message(s)");
		classificationManager.reset();
		Utils.saveObject(session("ID")+"_class_plot", classificationManager);
		return ok(result);

	}
	
	public static Result handleClassificationUpload() {
		Form<ClassificationFormData> form = Form.form(
				ClassificationFormData.class).bindFromRequest();
		ClassificationManager classificationManager;
		if(session().get("CLASSIFIER").equals("DEFAULT"))
			classificationManager = (ClassificationManager) Utils.loadObject("default_class");
		else
			classificationManager = (ClassificationManager) Utils.loadObject(session("ID")+"_class");
		//Utils.saveObject(session("ID")+"_class", classificationManager);
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fileToUpload = body.getFile("file");
		boolean addAfterMsg = false;
		if (body.asFormUrlEncoded().get("add_after_msg") != null)
			addAfterMsg = true;
		boolean addTrainingData = false;
		if (body.asFormUrlEncoded().get("add_training_data") != null)
			addTrainingData = true;
		//ClassificationManager classificationManager = new ClassificationManager();
		classificationManager.setUseBehindSeparator(addAfterMsg);
		String separator = body.asFormUrlEncoded().get("separator")[0];
		if (separator.length() == 0)
			separator = ";";
		if (fileToUpload != null) {
			File dataFile = fileToUpload.getFile();
			/*Path temp = dataFile.toPath();
			Path newFile = new File("public/uploaded/"
					+ fileToUpload.getFilename()).toPath();*/
			try {
				//Files.move(temp, newFile, REPLACE_EXISTING);
				try {
					classificationManager.setData(dataFile, 0,
							ClassificationManager.SET_CLASSIFICATION_DATA, separator, false);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if(!classificationManager.classifyData(addTrainingData))
					return internalServerError();
				response().setContentType("application/json");
				ObjectNode result = Json.newObject();
				result.put("file", dataFile.getName());
				result.put("loaded", classificationManager.classifiedDataSize());
				result.put("message", " Successfully classified "+classificationManager.classifiedDataSize()+" message(s)");
				classificationManager.reset();
				Utils.saveObject(session("ID")+"_class_plot", classificationManager);
				return ok(result);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				//Utils.saveObject(session("ID")+"_class", classificationManager);
				return internalServerError();
			}
		}
		return internalServerError();
	}
	public static Result handleTrainingUpload() {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fileToUpload = body.getFile("file");
		boolean addAfterMsg = false;
		if (body.asFormUrlEncoded().get("training_add_after_msg") != null)
			addAfterMsg = true;
		int classifier = ClassifierChoice.findChoice(body.asFormUrlEncoded().get("classifiers")[0]).getId();
		

		ClassificationManager classificationManager = new ClassificationManager();
		
		classificationManager.setUseBehindSeparator(addAfterMsg);
		String separator = body.asFormUrlEncoded().get("training_separator")[0];
		if (separator.length() == 0)
			separator = ";";
		if (fileToUpload != null) {
			File dataFile = fileToUpload.getFile();
			/*Path temp = dataFile.toPath();
			Path newFile = new File("public/uploaded/"
					+ fileToUpload.getFilename()).toPath();*/
			try {
				//Files.move(temp, newFile, REPLACE_EXISTING);
				classificationManager
						.setClassifier(classifier);
				classificationManager.setData(dataFile, 0,
						ClassificationManager.SET_TRAINING_DATA, separator,
						true);
				classificationManager.trainClassifier();
		
				response().setContentType("application/json");
				ObjectNode result = Json.newObject();
				result.put("file", dataFile.getName());
				result.put("loaded", classificationManager.trainingSetSize());
				result.put("message", " Classifier trained with "+classificationManager.trainingSetSize()+" message(s)");
				session("CLASSIFIER",  "CUSTOM");
				Utils.saveObject(session("ID")+"_class", classificationManager);
				return ok(result);
			} catch (IOException | NumberFormatException | ParseException e) {
				e.printStackTrace();
				return internalServerError();
			}
		}
		return internalServerError();
	}
	
	public static Result handlePlot(String plotType, int param) {
		if(plotType.equals("cross-validation")){
			ClassificationManager classificationManager = (ClassificationManager) Utils.loadObject(session("ID")+"_class");
			return handleCrossValidationPlot(classificationManager, param);
		}
		else
		{
			ClassificationManager classificationManager = (ClassificationManager) Utils.loadObject(session("ID")+"_class_plot");
			switch (plotType) {
				case "class-nominal":
					return handleClassificationValuesPlot(classificationManager, ClassificationManager.Y_AXIS_NOMINAL);
				case "mov-avg":
					return handleMovingAveragePlot(classificationManager, param, ClassificationManager.Y_AXIS_METRIC);
				case "val-reg":
					return handleRegressionPlot(classificationManager, ClassificationManager.Y_AXIS_METRIC);
				case "count-reg":
					return handleRegressionPlot(classificationManager, ClassificationManager.Y_AXIS_INSTANCE_COUNT);
				case "count-date":
					return handleClassificationValuesPlot(classificationManager, ClassificationManager.Y_AXIS_INSTANCE_COUNT);
				case "measurable":
					return handleMeasurablePlot(classificationManager);
				case "val-dist":
					return handleDistributionPlot(classificationManager);
				default:
					return badRequest();
			}
		}
	}
	
	private static Result handleCrossValidationPlot(
			ClassificationManager classManager, int param) {
		ObjectNode result = classManager.getJsonCrossValidation(param);
		response().setContentType("application/json");
		return ok(result);
	}

	private static Result handleRegressionPlot(
			ClassificationManager classManager, int axis) {
		ObjectNode result = classManager.getJsonRegression(axis);
		response().setContentType("application/json");
		return ok(result);
	}
	
	private static Result handleMeasurablePlot(
			ClassificationManager classManager) {
		ObjectNode result = classManager.getJsonPieChart();
		response().setContentType("application/json");
		return ok(result);
	}
	
	private static Result handleDistributionPlot(
			ClassificationManager classManager) {
		return TODO;
	}
	
	private static Result handleClassificationValuesPlot(
			ClassificationManager classManager, int axis) {
		ObjectNode result = classManager.getJsonTimeSeries(axis);
		response().setContentType("application/json");
		return ok(result);
	}
	
	private static Result handleMovingAveragePlot(
			ClassificationManager classManager, int param, int axis) {
		ObjectNode result = classManager.getJsonMovingAverage(axis, param);
		response().setContentType("application/json");
		return ok(result);
	}

}
