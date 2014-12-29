package controllers;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import models.LanguageChoice;
import models.PreprocessingChoice;
import play.Routes;
import play.cache.Cache;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import tools.Utils;
import views.formdata.EmotionFormData;
import views.formdata.QueryFormData;
import views.html.emotion;
import views.html.index;
import controllers.managers.ClassificationManager;
import controllers.managers.DataManager;
import controllers.managers.EmotionMeasurementManager;

/**
 * The controller for the single page of this application.
 * 
 * @author Ahmed Khattab
 */
public class MainController extends Controller {

	/**
	 * Returns the index page
	 * 
	 * @return The page containing the search form.
	 */
	public static Result index() {
		if(!session().containsKey("ID"))
			session("ID",  UUID.randomUUID().toString());
		if(Utils.loadObject(session("ID")+"_data")==null)
			Utils.saveObject(session("ID")+"_data", new DataManager());

		QueryFormData queryData = new QueryFormData();
		Form<QueryFormData> formData = Form.form(QueryFormData.class).fill(
				queryData);
		return ok(index.render(formData, LanguageChoice.makeLanguageMap(),
				false));
	}

	/**
	 * handle saving of data collected to a csv and serving it to client
	 * 
	 * @return The index page with the results of validation.
	 */
	public static Result saveData(String dataType) {
		try {
			if (dataType.equals("raw-data")) {
				DataManager dataManager = (DataManager) Utils.loadObject(session("ID")+"_data");
				File dataFile = dataManager.getDatainFile();
				if (dataFile != null)
					response().setContentType("application/x-download");
				response().setHeader("Content-disposition",
						"attachment; filename=collected_data.csv");
				return ok(dataFile);
			} else if (dataType.equals("em-values")) {
				EmotionMeasurementManager emManager = (EmotionMeasurementManager) Utils.loadObject(session("ID")+"_emotion");
				File dataFile = emManager.saveResultsToFile();
				response().setContentType("application/x-download");
				response().setHeader("Content-disposition",
						"attachment; filename=Emotion_Measurement_Results.csv");
				return ok(dataFile);
			} else if (dataType.equals("classified-data")) {
				ClassificationManager classificationManager = (ClassificationManager) Utils.loadObject(session("ID")+"_class");
				File dataFile = classificationManager.saveResultsToFile();
				classificationManager.reset();
				Utils.saveObject(session("ID")+"_class", classificationManager);
				response().setContentType("application/x-download");
				response().setHeader("Content-disposition",
						"attachment; filename=Classified_Data.csv");
				return ok(dataFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
			flash("error", "Error downloading data");
			EmotionFormData emotionData = new EmotionFormData();
			Form<EmotionFormData> formData = Form.form(EmotionFormData.class)
					.fill(emotionData);
			return ok(emotion.render(formData,
					PreprocessingChoice.makePreprocessingMap(), false, false));
		}
		return TODO;
	}
	
	

	/**
	 * Process the search form submission. First we bind the HTTP POST data to
	 * an instance of QueryFormData. The binding process will invoke the
	 * QueryFormData.validate() method. If errors are found, re-render the page,
	 * displaying the error data. If errors not found, render the page with the
	 * good data.
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
			DataManager dataManager = (DataManager) Utils.loadObject(session("ID")+"_data");

			dataManager.setTwitterMaxPages(Integer.parseInt(query.numOfSites));
			LanguageChoice lang;
			if (query.lang.equals(""))
				lang = LanguageChoice.getDefaultLang();
			else
				lang = LanguageChoice.findLang(query.lang);
			dataManager.collectData(query.query,
					Utils.formatDate(query.fromDate),
					Utils.formatDate(query.toDate), lang.getId(), true, false,
					false);
			Utils.saveObject(session("ID")+"_data", dataManager);
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
		return ok(Routes.javascriptRouter("jsRoutes",
				routes.javascript.EmotionMeasurementController.handleEmotionUpload(),
				routes.javascript.ClassificationController.handleTrainingUpload(),
				routes.javascript.ClassificationController.handleClassificationUpload(),
				routes.javascript.EmotionMeasurementController.handlePlot()));
	}
}
