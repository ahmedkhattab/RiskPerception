package views.formdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;
import play.Play;
import play.data.Form;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.Required;
import play.libs.Json;

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
	 

	  /** Required for form instantiation. */
	  public AdminFormData() {
	  }

	  public static Map<String, Boolean> makeTrackingMap() {
		    Map<String, Boolean> trackingMap = new HashMap<String, Boolean>();
		    trackingMap.put("", false);
		    File dir = Play.application().getFile("private/tracking");
			  File[] directoryListing = dir.listFiles();
			  if (directoryListing != null) {
			    for (File child : directoryListing) {
			    	String projectName = child.getName();
			    	int pos = projectName.lastIndexOf(".");
			    	if (pos > 0) {
			    		projectName = projectName.substring(0, pos);
			    	}
			    	trackingMap.put(projectName, false);
			    }
			  }
			  return trackingMap;
		  }
	  public static Map<String, String> makeKeywordsMap() {
		    Map<String, String> trackingMap = new HashMap<String, String>();
		    File dir = Play.application().getFile("private/tracking");
			  File[] directoryListing = dir.listFiles();
			  if (directoryListing != null) {
			    for (File child : directoryListing) {
			    	String projectName = child.getName();
			    	int pos = projectName.lastIndexOf(".");
			    	if (pos > 0) {
			    		projectName = projectName.substring(0, pos);
			    	}
			    	String keywords = "";
					try {
						keywords = Json.parse(new FileInputStream(child)).toString();
					} catch (FileNotFoundException e) {
						Logger.error(e.getMessage());
					}
			    	trackingMap.put(projectName, keywords);
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
			return Json.parse(new FileInputStream(Play.application().getFile("private/tracking/"+projectName+".json"))).toString();
		} catch (FileNotFoundException e) {
			Logger.error(e.getMessage());
			return null;
		}
		
	}
	}

