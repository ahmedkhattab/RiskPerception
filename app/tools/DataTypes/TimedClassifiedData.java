package tools.DataTypes;

import java.util.ArrayList;
import java.util.SortedSet;

import net.sf.javaml.core.Instance;

import org.jfree.data.time.Second;

import controllers.managers.ClassNumberForm;

/**
 * This class was created to aggregate instances by date.
 * 
 * @author Christian Olenberger
 * 
 */
public class TimedClassifiedData {

	// data fields
	private static ClassNumberForm form;
	private Second date;
	private ArrayList<Instance> classifiedData;

	/**
	 * Constructor
	 * 
	 * @param date
	 *            Date for this object.
	 * @param classifiedData
	 *            Data for this object.
	 */
	public TimedClassifiedData(Second date, ArrayList<Instance> classifiedData) {
		this.date = date;
		this.classifiedData = classifiedData;
	}

	/**
	 * Getter for the date.
	 * 
	 * @return The date of this object.
	 */
	public Second getDate() {
		return date;
	}

	/**
	 * Setter for the the date.
	 * 
	 * @param date
	 *            The date you want to set.
	 */
	public void setDate(Second date) {
		this.date = date;
	}

	/**
	 * Getter for classifiedData.
	 * 
	 * @return The classified instances of this object.
	 */
	public ArrayList<Instance> getClassifiedData() {
		return classifiedData;
	}

	/**
	 * Setter for the classifiedData.
	 * 
	 * @param classifiedData
	 *            The classifiedData you want to set.
	 */
	public void setClassifiedData(ArrayList<Instance> classifiedData) {
		this.classifiedData = classifiedData;
	}

	/**
	 * Adds a instance to the classifiedData.
	 * 
	 * @param instance
	 *            Instance you want to add.
	 */
	public void addInstance(Instance instance) {
		this.classifiedData.add(instance);
	}

	public double getClassesAverage() {
		ArrayList<ClassCounter> result = TimedClassifiedData.form
				.getDefaultClassCounter();
		for (int i = 0; i < classifiedData.size(); i++) {
			int index = getIndexOfClass(result, classifiedData.get(i));
			if (index != -1) {
				result.get(index).incCount();
			}
		}
		//LegendForm.setClassCounter(result);
		double value = Double.MIN_VALUE;
		double counter = 0;
		for (int i = 0; i < result.size(); i++) {
			if (!result.get(i).isEmpty()) {
				value = value + i * result.get(i).getCount();
				counter = counter + result.get(i).getCount();
			}
		}
		if (counter != 0) {
			return value / counter;
		} else {
			return Double.MIN_VALUE;
		}
	}

	public static void setClassNumberForm(SortedSet<Object> classNames) {
		TimedClassifiedData.form = new ClassNumberForm(classNames);
	}

	/**
	 * This method counts the appearance of a class in the classified dataset.
	 * 
	 * @param classSet
	 *            All possible classes.
	 * @return A arrayList containing the classes and the count.
	 */
	public ArrayList<ClassCounter> getClassCount(SortedSet<Object> classSet) {
		Object[] classes = classSet.toArray();
		ArrayList<ClassCounter> classCounter = new ArrayList<ClassCounter>();
		// Collect classes
		for (int i = 0; i < classes.length; i++) {
			ClassCounter element = new ClassCounter(classes[i].toString(), 0);
			classCounter.add(element);
		}
		// Count classes
		for (int i = 0; i < classifiedData.size(); i++) {
			int index = getIndexOfClass(classCounter, classifiedData.get(i));
			if (index != -1) {
				classCounter.get(index).incCount();
			}
		}
		return classCounter;
	}

	/**
	 * This method returns the position of a class in the given arrayList.
	 * 
	 * @param classList
	 *            The list of ClassCounters to search through.
	 * @param instance
	 *            The instance which classified class is searched.
	 * @return Position of the class in the arrayList or -1 if could not find
	 *         it.
	 */
	private int getIndexOfClass(ArrayList<ClassCounter> classList,
			Instance instance) {
		for (int i = 0; i < classList.size(); i++) {
			if (classList.get(i).getName()
					.equals(instance.classValue().toString())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns a human readable String of this object.
	 */
	public String toString() {
		return "(" + date + "-" + classifiedData + ")";
	}

}
