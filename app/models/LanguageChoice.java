package models;

import java.util.ArrayList;
import java.util.Arrays;
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
public class LanguageChoice {
  private String id;
  private String name;

  public LanguageChoice(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  /**
   * Create a map of Language choices -> boolean where the boolean is true if the language is selected.
   * @return A map of Language choices to boolean indicating which one is selected
   */
  public static Map<String, Boolean> makeLanguageMap() {
    Map<String, Boolean> langMap = new TreeMap<String, Boolean>();
    for (LanguageChoice lang : allLangs) {
    	langMap.put(lang.getName(),  false);
    }
    return langMap;
  }

  /**
   * @return A list of GPA ranges in sorted order.
   */
  public static List<String> getLangList() {
    String[] nameArray = {"4.0", "3.0 - 3.9", "2.0 - 2.9", "1.0 - 1.9"};
    return Arrays.asList(nameArray);
  }

  /**
   * Return the GPA instance in the database with name 'gpa' or null if not found.
   * @param gpa The gpa
   * @return The GradePointAverage instance, or null.
   */
  public static LanguageChoice findLang(String langName) {
    for (LanguageChoice lang : allLangs) {
      if (langName.equals(lang.getName())) {
        return lang;
      }
    }
    return null;
  }

  /**
   * Define a default GPA, used for form display.
   * @return The default GPA.
   */
  public static LanguageChoice getDefaultLang() {
    return allLangs.get(0);
  }

  @Override
  public String toString() {
    return this.name;
  }

  /** Fake a database of GPAs. */
  private static List<LanguageChoice> allLangs = new ArrayList<>();

  /** Instantiate the fake database of Languages. */
  static {
	  allLangs.add(new LanguageChoice("en", "English"));
	  allLangs.add(new LanguageChoice("de", "Deutsch"));
	  allLangs.add(new LanguageChoice("fr", "French"));
  }


}
