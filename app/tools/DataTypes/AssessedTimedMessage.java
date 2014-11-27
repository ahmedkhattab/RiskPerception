package tools.DataTypes;

import java.util.Date;

/**
 * This class saves a message, the time this message was created and the
 * assessment the message got (e.g. the class after a classification or a
 * numeric value).
 * 
 * @author Christian Olenberger
 * 
 */
public class AssessedTimedMessage {

	// data fields
	private String assessment;
	private String message;
	private Date date;

	/**
	 * Constructor
	 * 
	 * @param date
	 *            The date the message was created.
	 * @param message
	 *            The message that was collected.
	 * @param assessment
	 *            The assessment for the message (e.g. class or value).
	 */
	public AssessedTimedMessage(String assessment, Date date, String message) {
		this.assessment = assessment;
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
	 * Getter for the assessment.
	 * 
	 * @return The assessment.
	 */
	public String getAssessment() {
		return this.assessment;
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
	 * Setter for the assessment.
	 * 
	 * @param assessment
	 *            The assessment.
	 */
	public void setAssessment(String assessment) {
		this.assessment = assessment;
	}

	/**
	 * Converts information to a String.
	 */
	public String toString() {
		return "(" + assessment + "," + date + "," + message + ")";
	}
}
