package controllers;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.UUID;

import models.PreprocessingChoice;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import tools.StringRemover;
import tools.TextStandardizer;
import tools.Utils;
import tools.DataTypes.TimedMessage;
import views.formdata.EmotionFormData;
import views.html.emotion;

import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.managers.DataManager;
import controllers.managers.EmotionMeasurementManager;

/**
 * The controller for the single page of this application.
 * 
 * @author Ahmed Khattab
 */
public class EmotionMeasurementController extends Controller {
	
	/**
	 * Returns the form for emotion measurement, allowing the user to load data
	 * from file/use previously queried data, select preprocessing operations to
	 * perform, and visualize/download the results
	 * 
	 * @return The page containing the emotion measurement form.
	 */
	public static Result emotion() {
		if(!session().containsKey("ID")){
			session("ID",  UUID.randomUUID().toString());
			System.out.println(session("ID"));
		}
		DataManager dataManager = new DataManager();
		Object loadedObject = Utils.loadObject(session("ID")+"_data");
		if(loadedObject !=null)
			dataManager = (DataManager) loadedObject;
		else
			Utils.saveObject(session("ID")+"_data", dataManager);

		if(Utils.loadObject(session("ID")+"_emotion")==null)
			Utils.saveObject(session("ID")+"_emotion", new EmotionMeasurementManager());

		EmotionFormData emotionData = new EmotionFormData();
		Form<EmotionFormData> formData = Form.form(EmotionFormData.class).fill(
				emotionData);
		if (dataManager.getData().size() == 0) {
			flash("error", "No data loaded");
			return ok(emotion.render(formData,
					PreprocessingChoice.makePreprocessingMap(), false, false));
		} else {
			return ok(emotion.render(formData,
					PreprocessingChoice.makePreprocessingMap(), true, false));
		}
	}

	public static Result handleEmotionUpload() {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart fileToUpload = body.getFile("file");
		int msgColumnPosition;
		int dateColumnPosition;
		try {
			msgColumnPosition = Integer.parseInt(body.asFormUrlEncoded().get(
					"msg_column_pos")[0]) - 1;
		} catch (NumberFormatException e) {
			msgColumnPosition = 1;
		}
		try {
			dateColumnPosition = Integer.parseInt(body.asFormUrlEncoded().get(
					"date_column_pos")[0]) - 1;
		} catch (NumberFormatException e) {
			dateColumnPosition = 0;
		}
		boolean addAfterMsg = false;
		if (body.asFormUrlEncoded().get("add_after_msg") != null)
			addAfterMsg = true;

		String separator = body.asFormUrlEncoded().get("separator")[0];
		if (separator.length() == 0)
			separator = ";";
		if (fileToUpload != null) {
			File uploadedFile = fileToUpload.getFile();
			/*
			Path temp = tempimg.toPath();
			Path newFile = new File("public/uploaded/"
					+ fileToUpload.getFilename()).toPath();
					*/
			try {
				//Files.move(temp, newFile, REPLACE_EXISTING);
				DataManager dataManager = (DataManager) Utils.loadObject(session("ID")+"_data");
				
				dataManager.setDataFromFileAdaptor(dateColumnPosition,
						msgColumnPosition, separator, addAfterMsg,
						uploadedFile);
				Utils.saveObject(session("ID")+"_data", dataManager);
				response().setContentType("application/json");
				ObjectNode result = Json.newObject();
				result.put("file", uploadedFile.toString());
				result.put("loaded", dataManager.getData().size());
				
				return ok(result);
			} catch (IOException | NumberFormatException | ParseException e) {
				e.printStackTrace();
			}
		}
		return internalServerError();
	}

	/**
	 * Returns an array list of data after performing the preprocessing
	 * operations chosen by the user
	 * 
	 * @param String
	 *            [] preprocessing list of the preprocessing commands as chosen
	 *            by the user.
	 * 
	 * @return The page containing the form and data.
	 */
	public static ArrayList<TimedMessage> preprocess(ArrayList<TimedMessage> data, String[] preprocessing) {
		if (preprocessing == null)
			return data;
		for (String command : preprocessing) {

			switch (PreprocessingChoice.findChoice(command).getId()) {
			case 1:
				data = StringRemover.removeHTTPFromTimedMessages(data);
				break;
			case 2:
				data = StringRemover.removeFromTimedMessage(data, "#");
				break;
			case 3:
				data = StringRemover.removeFromTimedMessage(data, "@");
				break;
			case 4:
				data = StringRemover.removeAllFromTimedMessage(data);
				break;
			case 5:
				data = TextStandardizer.standardizeTextList(data);
			default:
				break;
			}

		}
		return data;
	}

	public static Result handlePlot(String plotType, int param) {
		EmotionMeasurementManager emManager = (EmotionMeasurementManager) Utils.loadObject(session("ID")+"_emotion");
		switch (plotType) {
		case "em-val":
			return handleEmotionValuesPlot(emManager, EmotionMeasurementManager.Y_AXIS_INSTANCE_EMOTION_VALUE);
		case "mov-avg":
			return handleMovingAveragePlot(emManager, param, EmotionMeasurementManager.Y_AXIS_INSTANCE_EMOTION_VALUE);
		case "val-reg":
			return handleRegressionPlot(emManager, EmotionMeasurementManager.Y_AXIS_INSTANCE_EMOTION_VALUE);
		case "count-reg":
			return handleRegressionPlot(emManager, EmotionMeasurementManager.Y_AXIS_INSTANCE_COUNTER);
		case "count-date":
			return handleEmotionValuesPlot(emManager, EmotionMeasurementManager.Y_AXIS_INSTANCE_COUNTER);
		case "measurable":
			return handleMeasurablePlot(emManager);
		case "val-dist":
			return handleDistributionPlot(emManager);
		default:
			return badRequest();
		}
	}

	private static Result handleRegressionPlot(EmotionMeasurementManager emManager, int axis) {
		ObjectNode result = emManager.getJsonRegression(axis);
		response().setContentType("application/json");
		return ok(result);
	}

	private static Result handleDistributionPlot(EmotionMeasurementManager emManager) {
		ObjectNode result = emManager.getJsonCategoryChart();
		response().setContentType("application/json");
		return ok(result);
	}

	private static Result handleMeasurablePlot(EmotionMeasurementManager emManager) {
		ObjectNode result = emManager.getJsonPieChart();
		response().setContentType("application/json");
		return ok(result);
	}

	private static Result handleMovingAveragePlot(EmotionMeasurementManager emManager, int param, int axis) {
		ObjectNode result = emManager.getJsonMovingAverage(
				axis, param);
		response().setContentType("application/json");
		return ok(result);
	}

	private static Result handleEmotionValuesPlot(EmotionMeasurementManager emManager, int axis) {
		ObjectNode result = emManager.getJsonTimeSeries(axis);
		response().setContentType("application/json");
		return ok(result);
	}

	/**
	 * Process a form submission. First we bind the HTTP POST data to an
	 * instance of EmotionFormData. The binding process will invoke the
	 * EmotionFormData.validate() method. If errors are found, re-render the
	 * page, displaying the error data. If errors not found, render the page
	 * with the good data.
	 * 
	 * @return The index page with the results of validation.
	 */

	public static Result postEmotion() {
		Form<EmotionFormData> form = Form.form(EmotionFormData.class)
				.bindFromRequest();
		if (form.hasErrors()) {
			flash("error", "Please correct errors above.");
			return badRequest(emotion.render(form,
					PreprocessingChoice.makePreprocessingMap(), false, false));
		} else {
			DataManager dataManager = (DataManager) Utils.loadObject(session("ID")+"_data");

			if (dataManager.getData().size() == 0) {
				flash("error",
						"Error: first, some data have to be collected or loaded from file");
				return badRequest(emotion.render(form,
						PreprocessingChoice.makePreprocessingMap(), false,
						false));
			} else {
				try {
					EmotionMeasurementManager emManager = (EmotionMeasurementManager) Utils.loadObject(session("ID")+"_emotion");

					emManager
							.setDefaultEmotionTableFile(EmotionMeasurementManager.USE_WARRINER);

					String[] checkedVal = request().body().asFormUrlEncoded()
							.get("preprocessing[]");
					boolean fromQuery = false;
					if (request().body().asFormUrlEncoded()
							.get("datasource-select") != null)
						fromQuery = request().body().asFormUrlEncoded()
								.get("datasource-select")[0]
								.equals("fromQuery");

					ArrayList<TimedMessage> data = preprocess(dataManager.getData(), checkedVal);
					emManager.assessMessages(data);
					flash("success", "Emotion measurement for "
							+ emManager.getAssessedMessages()
									.size()
							+ " message(s) completed successfully");
					Utils.saveObject(session("ID")+"_emotion", emManager);
					
					return ok(emotion.render(form,
							PreprocessingChoice.makePreprocessingMap(),
							fromQuery, true));
				} catch (IOException e) {
					e.printStackTrace();
					return badRequest(emotion.render(form,
							PreprocessingChoice.makePreprocessingMap(), false,
							false));
				}
			}
		}
	}
}