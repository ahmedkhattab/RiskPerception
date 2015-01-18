package controllers.managers;

import java.util.ArrayList;
import java.util.SortedSet;

import tools.DataTypes.ClassCounter;

/**
 * This class opens a dialog where the user can add a number to map the classes
 * to numeric values. (This class was generated by WindowBuilder and modified).
 * 
 * @author Christian Olenberger
 * 
 */
public class ClassNumberForm {

	private Object[] classNames;

	/**
	 * Create the frame.
	 */
	public ClassNumberForm(SortedSet<Object> classes) {
		
		this.classNames = classes.toArray();
	}

	/**
	 * This method returns a arrayList with default ClassCounters.
	 * 
	 * @return
	 */
	/*
	public ArrayList<ClassCounter> getDefaultClassCounter() {
		ArrayList<ClassCounter> result = initArrayList(getMaxValueFromInput());
		for (int i = 0; i < classNames.length; i++) {
			result.add(new ClassCounter());
		}
		for (int i = 0; i < classNames.length; i++) {
			ClassCounter classCounter = new ClassCounter(
					classNames[i].toString(), 0);
			if (!textFields.get(i).getText().isEmpty()) {
				result.set(Integer.parseInt(textFields.get(i).getText()),
						classCounter);
			}
		}
		return result;
	}
	*/
	public ArrayList<ClassCounter> getDefaultClassCounter() {
		ArrayList<ClassCounter> result = new ArrayList<ClassCounter>();
		for (int i = 0; i < classNames.length; i++) {
			result.add(new ClassCounter());
		}
		for (int i = 0; i < classNames.length; i++) {
			ClassCounter classCounter = new ClassCounter(
					classNames[i].toString(), 0);
			result.set(i,
						classCounter);
		}
		return result;
	}

}