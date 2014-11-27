package tools.DataTypes;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This data-construct was created the save a date, a value and a counter in one
 * object.
 * 
 * @author Christian Olenberger
 * 
 */
public class DateValueCounter {

	// data fields
	private Date date;
	private double value;
	private int counter;

	/**
	 * Constructor
	 * 
	 * @param date
	 *            The date.
	 * @param value
	 *            The value.
	 * @param counter
	 *            The counter.
	 */
	public DateValueCounter(Date name, double value, int counter) {
		this.date = name;
		this.value = value;
		this.counter = counter;
	}

	/**
	 * Getter for date.
	 * 
	 * @return The date.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Add the given number to the current value.
	 * 
	 * @param value
	 *            The number to add.
	 */
	public void addValue(double value) {
		this.value = this.value + value;
	}

	/**
	 * Increments the counter.
	 */
	public void incCounter() {
		this.counter++;
	}

	/**
	 * Returns the average of this object by calculating: value/counter.
	 * 
	 * @return The average value;
	 */
	public double getAverage() {
		return (Math.round((((value / (counter * 1.0))) * 100)) * 1.0 / 100 * 1.0);
	}

	/**
	 * Setter for date
	 * 
	 * @param date
	 *            The date.
	 */
	public void setDate(Date name) {
		this.date = name;
	}

	/**
	 * Getter for value.
	 * 
	 * @return The value.
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Setter for value.
	 * 
	 * @param value
	 *            The value.
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * Getter for counter.
	 * 
	 * @return The counter.
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * Setter for counter.
	 * 
	 * @param counter
	 *            The counter.
	 */
	public void setCounter(int counter) {
		this.counter = counter;
	}

	/**
	 * toString()-Method
	 */
	public String toString() {
		return "(" + date + "," + value + "," + counter + ")";
	}

	/**
	 * Checks whether the given date is equal to the date from this object.
	 * 
	 * @param date
	 *            The given date.
	 * @return TRUE if the dates are equal.
	 */
	public boolean equalDate(Date date) {
		// Used code example:
		// http://www.christian-klisch.de/java-simpledateformat.html
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		String thisDate = format.format(this.date);
		String otherDate = format.format(date);
		return thisDate.equals(otherDate);
	}

}
