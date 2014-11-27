package tools.DataTypes;

import java.util.Date;

/**
 * This class saves a message and the time this message was created.
 * 
 * @author Christian Olenberger
 * 
 */
public class TimedMessage {

	// data fields
	private String message;
	private Date date;

	/**
	 * Constructor
	 * 
	 * @param date
	 *            The date the message was created.
	 * @param message
	 *            The message that was collected.
	 */
	public TimedMessage(Date date, String message) {
		this.date = date;
		this.message = message;
	}

	/**
	 * Getter for the date.
	 * 
	 * @return The date;
	 */
	public Date getDate() {
		return this.date;
	}

	/**
	 * Getter for the message.
	 * 
	 * @return The message.
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Setter for the date.
	 * 
	 * @param date
	 *            The date.
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Setter for the message.
	 * 
	 * @param message
	 *            The message.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Converts information to a String.
	 */
	public String toString() {
		return "(" + date + "," + message + ")";
	}

}
