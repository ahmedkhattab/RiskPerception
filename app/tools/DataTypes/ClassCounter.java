package tools.DataTypes;

/**
 * This class saves the name of a class and an integer representing the count.
 * 
 * @author Christian Olenberger
 * 
 */
public class ClassCounter {

	private String name;
	private int count;

	/**
	 * Constructor using the given data.
	 * 
	 * @param name
	 *            The name of the class.
	 * @param count
	 *            The count of the class.
	 */
	public ClassCounter(String name, int count) {
		this.name = name;
		this.count = count;
	}

	/**
	 * Default constructor.
	 */
	public ClassCounter() {
		this.name = "";
		this.count = 0;
	}

	/**
	 * Getter for the name.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for count.
	 * 
	 * @return count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Setter for the count.
	 * 
	 * @param newCount
	 *            New count that should be set.
	 */
	public void setCount(int newCount) {
		count = newCount;
	}

	/**
	 * Setter for the name.
	 * 
	 * @param name
	 *            The name that should be set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Increments the counter of this ClassCounter.
	 */
	public void incCount() {
		this.count++;
	}

	/**
	 * Converts information to a String.
	 */
	public String toString() {
		return "(" + name + "," + count + ")";
	}

	/**
	 * Looks whether two ClassCounters are equal.
	 * 
	 * @param counter
	 * @return TRUE if ClassCounters have the same name.
	 */
	public boolean equals(ClassCounter counter) {
		return this.name.equals(counter.getName());
	}

	/**
	 * If the name is empty this object is a empty object.
	 * 
	 * @return TRUE if the name is equal with an empty String.
	 */
	public boolean isEmpty() {
		return name.equals("");
	}

}
