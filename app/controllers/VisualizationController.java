package controllers;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.UUID;

import models.PreprocessingChoice;
import play.data.Form;
import play.libs.Json;
import play.cache.*;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import tools.StringRemover;
import tools.TextStandardizer;
import tools.Utils;
import tools.DataTypes.TimedMessage;
import views.formdata.EmotionFormData;
import views.formdata.VisualizationFormData;
import views.html.visualization;

import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.managers.DataManager;
import controllers.managers.EmotionMeasurementManager;

/**
 * The controller for the single page of this application.
 * 
 * @author Ahmed Khattab
 */
public class VisualizationController extends Controller {
	
	/**
	 * Returns the form for emotion measurement, allowing the user to load data
	 * from file/use previously queried data, select preprocessing operations to
	 * perform, and visualize/download the results
	 * 
	 * @return The page containing the emotion measurement form.
	 */
	public static Result visualization() {
		if(!session().containsKey("ID")){
			session("ID",  UUID.randomUUID().toString());
			System.out.println(session("ID"));
		}

		VisualizationFormData visData = new VisualizationFormData();
		Form<VisualizationFormData> formData = Form.form(VisualizationFormData.class).fill(
				visData);
		return ok(visualization.render(formData,
					PreprocessingChoice.makePreprocessingMap(), true, false));
	}
	
	/**
	 * Process a form submission. First we bind the HTTP POST data to an
	 * instance of VisualizationFormData. The binding process will invoke the
	 * VisualizationFormData.validate() method. If errors are found, re-render the
	 * page, displaying the error data. If errors not found, render the page
	 * with the good data.
	 * 
	 * @return The index page with the results of validation.
	 */

	public static Result postVisualization() {
		return TODO;
	}

}