package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import models.LanguageChoice;
import models.PreprocessingChoice;
import play.Routes;
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
import views.formdata.QueryFormData;
import views.html.index;
import views.html.emotion;

import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * The controller for the single page of this application.
 * 
 * @author Philip Johnson
 */
public class Application extends Controller {
	public static DataManager dataManager = new DataManager();
	public static EmotionMeasurementManager emotionMeasurementManager = new EmotionMeasurementManager();

	/**
	 * Returns the index page 
	 * 
	 * @return The page containing the search form.
	 */
	public static Result index() {
		QueryFormData queryData = new QueryFormData();
		Form<QueryFormData> formData = Form.form(QueryFormData.class).fill(
				queryData);
		return ok(index.render(formData, LanguageChoice.makeLanguageMap(),
				false));
	}

	/**
	 * Returns the page where the form for emotion measurement, allowing the user
	 * to load data from file/use previously queried data, select preprocessing 
	 * operations to perform, and visualize/download the results
	 * 
	 * @return The page containing the emotion measurement form.
	 */
	public static Result emotion() {
		EmotionFormData emotionData = new EmotionFormData();
		Form<EmotionFormData> formData = Form.form(EmotionFormData.class).fill(
				emotionData);
		if(dataManager.getData().size() == 0){
			flash("error", "No data loaded");
			return ok(emotion.render(formData,
					PreprocessingChoice.makePreprocessingMap(), false));
		}
		else
		{
		return ok(emotion.render(formData,
				PreprocessingChoice.makePreprocessingMap(), true));
		}
	}
	
	public static Result handleUpload() {
		MultipartFormData body = request().body().asMultipartFormData();
        FilePart fileToUpload = body.getFile("file");
        int msgColumnPosition = Integer.parseInt(body.asFormUrlEncoded().get("msg_column_pos")[0]);
        int dateColumnPosition = Integer.parseInt(body.asFormUrlEncoded().get("date_column_pos")[0]);
        boolean addAfterMsg = false;
        if(body.asFormUrlEncoded().get("add_after_msg") != null)
        	 addAfterMsg =Boolean.parseBoolean(body.asFormUrlEncoded().get("add_after_msg")[0]);
       
        String separator = body.asFormUrlEncoded().get("separator")[0];
            if (fileToUpload != null) {
                File tempimg = fileToUpload.getFile();
                Path temp = tempimg.toPath();
                Path newFile = new File("public/uploaded/"+fileToUpload.getFilename()).toPath();
                try {
						Files.move(temp, newFile, REPLACE_EXISTING);
						dataManager.setDataFromFileAdaptor(dateColumnPosition, msgColumnPosition, separator, addAfterMsg, newFile.toFile());
						response().setContentType("application/json");
				        ObjectNode result = Json.newObject();
				        System.out.println(newFile.getFileName().toString());
				        result.put("file", newFile.getFileName().toString());
				        result.put("loaded", dataManager.getData().size());
				        return ok(result);
				} catch (IOException | NumberFormatException | ParseException e) {
					e.printStackTrace();
				}
            }
            
            flash("error", "Problem uploading file");
            EmotionFormData emotionData = new EmotionFormData();
    		Form<EmotionFormData> formData = Form.form(EmotionFormData.class).fill(
    				emotionData);
    		return badRequest(emotion.render(formData,
					PreprocessingChoice.makePreprocessingMap(), false));
	}
	
	/**
	 * Returns an array list of data after performing the preprocessing operations
	 * chosen by the user
	 * 
	 * @param String[] preprocessing
	 *            list of the preprocessing commands as chosen by the user.
	 *            
	 * @return The page containing the form and data.
	 */
	public static ArrayList<TimedMessage> preprocess(String[] preprocessing) {
		ArrayList<TimedMessage> data = dataManager.getData();
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
	
	/**
	 * handle saving of data collected to a csv and serving
	 * it to client
	 *  
	 * @return The index page with the results of validation.
	 */
	public static Result saveData() {
		try {
			File dataFile = dataManager.getDatainFile();
			if (dataFile == null)

			response().setContentType("application/x-download");
			response().setHeader("Content-disposition",
					"attachment; filename=collected_data.csv");
			return ok(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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
					PreprocessingChoice.makePreprocessingMap(), false));
		} else {
			if(dataManager.getData().size() == 0){
				flash("error", "Error: first, some data have to be collected or loaded from file");
				return badRequest(emotion.render(form,
						PreprocessingChoice.makePreprocessingMap(), false));
			}
			else {
			try {
				emotionMeasurementManager
						.setDefaultEmotionTableFile(EmotionMeasurementManager.USE_WARRINER);
				
				String[] checkedVal = request().body().asFormUrlEncoded().get("preprocessing[]");
				ArrayList<TimedMessage> data = preprocess(checkedVal);
				emotionMeasurementManager.assessMessages(data);
				File dataFile = emotionMeasurementManager.saveResultsToFile();
				response().setContentType("application/x-download");
				response().setHeader("Content-disposition",
						"attachment; filename=Emotion_Measurement_Results.csv");
				return ok(dataFile);
			} catch (IOException e) {
				e.printStackTrace();
				return badRequest(emotion.render(form,
						PreprocessingChoice.makePreprocessingMap(), false));
			}
		}
	}
	}

	/**
	 * Process a form submission. First we bind the HTTP POST data to an
	 * instance of QueryFormData. The binding process will invoke the
	 * QueryFormData.validate() method. If errors are found, re-render the
	 * page, displaying the error data. If errors not found, render the page
	 * with the good data.
	 * 
	 * @return The index page with the results of validation.
	 */

	public static Result postIndex() {

		Form<QueryFormData> formData = Form.form(QueryFormData.class)
				.bindFromRequest();
		if (formData.hasErrors()) {

			flash("error", "Please correct errors above.");
			return badRequest(index.render(formData,
					LanguageChoice.makeLanguageMap(), false));
		} else {
			QueryFormData query = formData.get();

			dataManager.setTwitterMaxPages(Integer.parseInt(query.numOfSites));
			dataManager.collectData(query.query,
					Utils.formatDate(query.fromDate),
					Utils.formatDate(query.toDate),
					LanguageChoice.findLang(query.lang).getId(), true, false,
					false);
			flash("success",
					"Finished: Collected "
							+ Integer.toString(dataManager.getData().size())
							+ " message(s)");
			return ok(index.render(formData, LanguageChoice.makeLanguageMap(),
					dataManager.getData().size() > 0));
		}
		/*
		 * // Get the submitted form data from the request object, and run
		 * validation. Form<StudentFormData> formData =
		 * Form.form(StudentFormData.class).bindFromRequest();
		 * 
		 * if (formData.hasErrors()) { // Don't call formData.get() when there
		 * are errors, pass 'null' to helpers instead. flash("error",
		 * "Please correct errors above."); return
		 * badRequest(index.render(formData, Hobby.makeHobbyMap(null),
		 * GradeLevel.getNameList(), GradePointAverage.makeGPAMap(null),
		 * Major.makeMajorMap(null) )); } else { // Convert the formData into a
		 * Student model instance. Student student =
		 * Student.makeInstance(formData.get()); flash("success",
		 * "Student instance created/edited: " + student); return
		 * ok(index.render(formData, Hobby.makeHobbyMap(formData.get()),
		 * GradeLevel.getNameList(),
		 * GradePointAverage.makeGPAMap(formData.get()),
		 * Major.makeMajorMap(formData.get()) )); }
		 */

	}
	
	public static Result javascriptRoutes() {
	    response().setContentType("text/javascript");
	    return ok(
	        Routes.javascriptRouter("jsRoutes",
	            routes.javascript.Application.handleUpload()
	        )
	    );
	}
}
