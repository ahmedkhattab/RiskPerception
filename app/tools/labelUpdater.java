package tools;

import javax.swing.JLabel;

import controllers.DataManager;

/**
 * This class creates a thread which is updating a given label.
 * 
 * @author Christian Olenberger
 * 
 */
public class labelUpdater extends Thread {

	private JLabel label;
	private DataManager dataManager;
	private int counter;

	/**
	 * Updateing a label with the status of the dataManager.
	 */
	public void run() {
		while (true) {
			if (dataManager == null) {
				label.setText(label.getText() + ".");
			} else {
				label.setText(dataManager.getStatus() + ".");
			}
			label.paintImmediately(label.getVisibleRect());
			counter++;
			if (counter == 3) {
				label.setText(label.getText().substring(0,
						label.getText().length() - 3));
				counter = 0;
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				System.out.println("Problem with Thread.sleep()"
						+ e.getMessage());
			}
		}

	}

	/**
	 * Constructor
	 * 
	 * @param label
	 *            Label that should be updated.
	 * @param dataManager
	 *            Source of information.
	 */
	public labelUpdater(JLabel label, DataManager dataManager) {
		this.label = label;
		this.dataManager = dataManager;
	}

	public void resetCounter() {
		counter = 0;
	}

	/**
	 * Constructor
	 * 
	 * @param label
	 *            Label that should be updated.
	 */
	public labelUpdater(JLabel label) {
		this.label = label;
	}

}
