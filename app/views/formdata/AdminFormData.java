package views.formdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;

import models.LanguageChoice;
import play.Logger;
import play.Play;
import play.data.Form;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.Required;
import play.libs.Json;
import tools.Utils;

public class AdminFormData {
	/**
	 * Backing class for the Student data form.
	 * Requirements:
	 * <ul>
	 * <li> All fields are public, 
	 * <li> All fields are of type String or List[String].
	 * <li> A public no-arg constructor.
	 * <li> A validate() method that returns null or a List[ValidationError].
	 * </ul>
	 */

	  @Required(message = "")
	  public String keywordsFile = "";
	  @Required(message = "")
	  public String projectName = "";
	  @Required(message = "")
	  public String token = "";
	  @Required(message = "")
	  public String lang = "";
	  public String customLang = "";
	  /** Required for form instantiation. */
	  public AdminFormData() {
	  }

	  public static Map<String, Boolean> makeTrackingMap() {
		    Map<String, Boolean> trackingMap = new TreeMap<String, Boolean>();
		    trackingMap.put("", true);
		    File dir = Play.application().getFile("private/tracking");
			  File[] directoryListing = dir.listFiles();
			  if (directoryListing != null) {
			    for (File child : directoryListing) {
			    	if(child.getName().equals("language_map"))
			    		continue;
			    	String projectName = child.getName();
			    	trackingMap.put(projectName, false);
			    }
			  }
			  return trackingMap;
		  }
	  
	  public Form<CreateProjectFormData> getSecondaryForm(){
			CreateProjectFormData createProjectData = new CreateProjectFormData();
			return Form.form(CreateProjectFormData.class).fill(
					createProjectData);
	  }
	  /**
	   * Validates Form<StudentFormData>.
	   * Called automatically in the controller by bindFromRequest().
	   * 
	   * Validation checks include:
	   * <ul>
	   * <li> Name must be non-empty.
	   * <li> Password must be at least five characters.
	   * <li> Hobbies (plural) are optional, but if specified, must exist in database.
	   * <li> Grade Level is required and must exist in database.
	   * <li> GPA is required and must exist in database.
	   * <li> Majors (plural) are optional, but if specified, must exist in database.
	   * </ul>
	   *
	   * @return Null if valid, or a List[ValidationError] if problems found.
	   */
	  public List<ValidationError> validate() {

	    List<ValidationError> errors = new ArrayList<>();
	    
	    

	    /*
	    // Hobbies are optional, but if supplied must exist in database.
	    if (hobbies.size() > 0) {
	      for (String hobby : hobbies) {
	        if (Hobby.findHobby(hobby) == null) {
	          errors.add(new ValidationError("hobbies", "Unknown hobby: " + hobby + "."));
	        }
	      }
	    }

	    // Grade Level is required and must exist in database.
	    if (level == null || level.length() == 0) {
	      errors.add(new ValidationError("level", "No grade level was given."));
	    } else if (GradeLevel.findLevel(level) == null) {
	      errors.add(new ValidationError("level", "Invalid grade level: " + level + "."));
	    }

	    // GPA is required and must exist in database.
	    if (gpa == null || gpa.length() == 0) {
	      errors.add(new ValidationError("gpa", "No gpa was given."));
	    } else if (GradePointAverage.findGPA(gpa) == null) {
	      errors.add(new ValidationError("gpa", "Invalid GPA: " + gpa + "."));
	    }

	    // Majors are optional, but if supplied must exist in database.
	    if (majors.size() > 0) {
	      for (String major : majors) {
	        if (Major.findMajor(major) == null) {
	          errors.add(new ValidationError("majors", "Unknown Major: " + major + "."));
	        }
	      }
	    }
	     */
	    if(errors.size() > 0)
	      return errors;

	    return null;
	  }

	public static String getKeywords(String projectName) {
		try {
			return new String(Files.readAllBytes(Play.application().getFile("private/tracking/"+projectName).toPath()), StandardCharsets.UTF_8);
		} catch (FileNotFoundException e) {
			Logger.error(e.getMessage());
			return null;
		} catch (IOException e) {
			Logger.error(e.getMessage());
			return null;
		}
		
	}
	public static LanguageChoice getLanguage(String projectName) {
			HashMap<String, String> language_map = (HashMap<String, String>) Utils.loadObject("language_map");
			if(language_map == null)
			{	
				//bootstrap a new language map if it was not already created
				language_map = new HashMap<String, String>();
				language_map.put(projectName, LanguageChoice.getDefaultLang().getId());
				Utils.saveObject("language_map", language_map);
				return LanguageChoice.getDefaultLang();
			}
			else
			{
				String entry = language_map.get(projectName);
				if(entry == null)
				{
					language_map.put(projectName, LanguageChoice.getDefaultLang().getId());
					Utils.saveObject("language_map", language_map);
					return LanguageChoice.getDefaultLang();
				}
				
				if(entry.equals("any"))
					return new LanguageChoice("any", "any");
				
				LanguageChoice lang = LanguageChoice.findLang(language_map.get(projectName));
				if(lang == null)
				{
					return new LanguageChoice(language_map.get(projectName), "custom");
				}
				else 
					return lang;

			}
	}
}

