package views.formdata;

import play.data.validation.ValidationError;
import play.data.validation.Constraints.Required;

import java.util.ArrayList;
import java.util.List;

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
public class VisualizationFormData {

  
  public List<String> preprocessing = new ArrayList<>(); 
  @Required(message = "")
  public String fromDate = "";
  @Required(message = "")
  public String toDate = "";
  @Required(message = "")
  public String projectName = "";
  
  /** Required for form instantiation. */
  public VisualizationFormData() {
  }
  
  /**
   * Validates Form<VisualizationFormData>.
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
}
