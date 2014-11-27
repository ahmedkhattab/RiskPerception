package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represent a Grade Point Average. 
 * This class includes:
 * <ul>
 * <li> The model structure (fields, plus getters and setters).
 * <li> Some methods to facilitate form display and manipulation (makeGPAMap, etc.).
 * <li> Some fields and methods to "fake" a database of GPAs.
 * </ul>
 */
public class PreprocessingChoice {
  private int id;
  private String name;

  public PreprocessingChoice(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  /**
   * Create a map of Language choices -> boolean where the boolean is true if the language is selected.
   * @return A map of Language choices to boolean indicating which one is selected
   */
  public static Map<String, Boolean> makePreprocessingMap() {
    Map<String, Boolean> preprocessingMap = new TreeMap<String, Boolean>();
    for (PreprocessingChoice choice : allChoices) {
    	preprocessingMap.put(choice.getName(),  false);
    }
    return preprocessingMap;
  }

  /**
   * Return the GPA instance in the database with name 'gpa' or null if not found.
   * @param gpa The gpa
   * @return The GradePointAverage instance, or null.
   */
  public static PreprocessingChoice findChoice(String choiceName) {
    for (PreprocessingChoice choice : allChoices) {
      if (choiceName.equals(choice.getName())) {
        return choice;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return this.name;
  }

  /** Fake a database of GPAs. */
  private static List<PreprocessingChoice> allChoices = new ArrayList<>();

  /** Instantiate the fake database of Languages. */
  static {
	  allChoices.add(new PreprocessingChoice(1, "Remove links (starting with 'http')"));
	  allChoices.add(new PreprocessingChoice(2, "Remove all '#'"));
	  allChoices.add(new PreprocessingChoice(3, "Remove all '@'"));
	  allChoices.add(new PreprocessingChoice(4, "Remove all but letters and numbers"));
	  allChoices.add(new PreprocessingChoice(5, "Standardize Text"));
  }


}
