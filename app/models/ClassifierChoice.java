package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import controllers.managers.ClassifierCollection;

/**
 * Represent a Grade Point Average. 
 * This class includes:
 * <ul>
 * <li> The model structure (fields, plus getters and setters).
 * <li> Some methods to facilitate form display and manipulation (makeClassifiersMap, etc.).
 * <li> Some fields and methods to "fake" a database of Classifiers.
 * </ul>
 */
public class ClassifierChoice {
  private int id;
  private String name;

  public ClassifierChoice(int id, String name) {
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
  public static Map<String, Boolean> makeClassifiersMap() {
    Map<String, Boolean> classifiersMap = new TreeMap<String, Boolean>();
    for (ClassifierChoice choice : allChoices) {
    	if(choice.getId() == getDefaultClassifier().getId())
    		classifiersMap.put(choice.getName(),  true);
    	else
			classifiersMap.put(choice.getName(),  false);
    }
    return classifiersMap;
  }

  /**
   * Return the GPA instance in the database with name 'gpa' or null if not found.
   * @param gpa The gpa
   * @return The GradePointAverage instance, or null.
   */
  public static ClassifierChoice findChoice(String choiceName) {
    for (ClassifierChoice choice : allChoices) {
      if (choiceName.equals(choice.getName())) {
        return choice;
      }
    }
    return null;
  }
  
  /**
   * Define a default GPA, used for form display.
   * @return The default GPA.
   */
  public static ClassifierChoice getDefaultClassifier() {
    return allChoices.get(0);
  }
  
  @Override
  public String toString() {
    return this.name;
  }

  /** Fake a database of classifiers. */
  private static List<ClassifierChoice> allChoices = new ArrayList<>();

  /** Instantiate the fake database of Classifiers. */
  static {
	  allChoices.add(new ClassifierChoice(ClassifierCollection.SVM_CLASSIFIER, "SVM"));
	  allChoices.add(new ClassifierChoice(ClassifierCollection.NAIVE_BAYES, "Naive Bayes"));
  }


}
