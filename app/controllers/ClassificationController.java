package controllers;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.UUID;

import models.PreprocessingChoice;
import play.cache.Cache;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import tools.Utils;
import views.formdata.ClassificationFormData;
import views.html.classification;

import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.managers.ClassificationManager;
import controllers.managers.ClassifierCollection;

/**
 * The controller for the single page of this application.
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
		ClassificationFormData queryData = new ClassificationFormData();
		Form<ClassificationFormData> formData = Form.form(
				ClassificationFormData.class).fill(queryData);
		if(Utils.loadObject(session("ID")+"_class")==null)
			Utils.saveObject(session("ID")+"_class", new ClassificationManager());
		
		return ok(classification.render(formData,
				PreprocessingChoice.makePreprocessingMap(), false, false));
	}

	public static Result handleClassificationUpload() {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fileToUpload = body.getFile("file");
		boolean addAfterMsg = false;
		if (body.asFormUrlEncoded().get("add_after_msg") != null)
			addAfterMsg = true;
		boolean classified = false;
		if (body.asFormUrlEncoded().get("already_classified") != null)
			classified = true;
		ClassificationManager classificationManager = (ClassificationManager) Utils.loadObject(session("ID")+"_class");
		System.out.println("****** loaded object");
		classificationManager.setUseBehindSeparator(addAfterMsg);
		String separator = body.asFormUrlEncoded().get("separator")[0];
		if (separator.length() == 0)
			separator = ";";
		if (fileToUpload != null) {
			File dataFile = fileToUpload.getFile();
			/*Path temp = dataFile.toPath();
			Path classificationDataPath = new File("public/uploaded/"
					+ fileToUpload.getFilename()).toPath();*/
			try {
				//Files.move(temp, classificationDataPath, REPLACE_EXISTING);
				response().setContentType("application/json");
				ObjectNode result = Json.newObject();
				result.put("file", dataFile.getName());
				if(!classificationManager.isTrained())
				{
					classificationManager
					.setClassifier(ClassifierCollection.SVM_CLASSIFIER);
					File trainingDataFile = new File("public/datasets/Datensatz 1_v1.csv");
					classificationManager.setData(trainingDataFile, 0,
							ClassificationManager.SET_TRAINING_DATA, separator,
							true);
					System.out.println("training");
					classificationManager.trainClassifier();
				}
				classificationManager.setData(dataFile, 0,
						ClassificationManager.SET_CLASSIFICATION_DATA, separator, classified);
				result.put("loaded", classificationManager.classificationSetSize());
				result.put("message", classificationManager.classificationSetSize()+" message(s) were loaded and ready for classification");

				System.out.println(Utils.saveObject(session("ID")+"_class", classificationManager) ? "****saved": "******* not saved");
				return ok(result);
			} catch (IOException | NumberFormatException | ParseException e) {
				e.printStackTrace();
				return internalServerError();
			}
		}
		return internalServerError();
	}
	public static Result handleTrainingUpload() {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fileToUpload = body.getFile("file");
		boolean addAfterMsg = false;
		if (body.asFormUrlEncoded().get("add_after_msg") != null)
			addAfterMsg = true;
		ClassificationManager classificationManager = new ClassificationManager();
		
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
				classificationManager
						.setClassifier(ClassifierCollection.SVM_CLASSIFIER);
				classificationManager.setData(dataFile, 0,
						ClassificationManager.SET_TRAINING_DATA, separator,
						true);
				classificationManager.trainClassifier();
		
				response().setContentType("application/json");
				ObjectNode result = Json.newObject();
				result.put("file", dataFile.getName());
				result.put("loaded", classificationManager.trainingSetSize());
				result.put("message", " Classifier trained with "+classificationManager.trainingSetSize()+" message(s)");
				Utils.saveObject(session("ID")+"_class", classificationManager);
				return ok(result);
			} catch (IOException | NumberFormatException | ParseException e) {
				e.printStackTrace();
				Utils.saveObject(session("ID")+"_class", classificationManager);
				return internalServerError();
			}
		}
		return internalServerError();
	}
	
	public static Result handlePlot(String plotType, int param) {
		ClassificationManager emManager = (ClassificationManager) Utils.loadObject(session("ID")+"_class");
		switch (plotType) {
		case "class-nominal":
			return handleClassificationValuesPlot(emManager, ClassificationManager.Y_AXIS_NOMINAL);
		case "mov-avg":
			return handleMovingAveragePlot(emManager, param, ClassificationManager.Y_AXIS_METRIC);
		case "val-reg":
			return handleRegressionPlot(emManager, ClassificationManager.Y_AXIS_METRIC);
		case "count-reg":
			return handleRegressionPlot(emManager, ClassificationManager.Y_AXIS_INSTANCE_COUNT);
		case "count-date":
			return handleClassificationValuesPlot(emManager, ClassificationManager.Y_AXIS_INSTANCE_COUNT);
		case "measurable":
			return handleMeasurablePlot(emManager);
		case "val-dist":
			return handleDistributionPlot(emManager);
		default:
			return badRequest();
		}
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


	public static Result postClassification() {
		Form<ClassificationFormData> form = Form.form(
				ClassificationFormData.class).bindFromRequest();
		boolean addTrainingData = false;
		if (request().body().asFormUrlEncoded().get("add_training_data") != null)
			addTrainingData = true;
		System.out.println(addTrainingData);
		ClassificationManager classificationManager = (ClassificationManager) Utils.loadObject(session("ID")+"_class");
		classificationManager.classifyData(addTrainingData);
		flash("success", "Successfully classified " + classificationManager.classifiedDataSize() + " message(s).");
		Utils.saveObject(session("ID")+"_class", classificationManager);
		return ok(classification.render(form,
				PreprocessingChoice.makePreprocessingMap(), false, true));
	}

}
