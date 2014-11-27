package tools;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This class offers different methods to get input from the user.
 * 
 * @author Christian Olenberger
 * 
 */
public class InputOptionCollection {

	/**
	 * Opens a dialog where the user can select one file.
	 * 
	 * @return Selected File.
	 */
	public static File selectFile() {
		return selectFile("");
	}

	/**
	 * Opens a dialog with the given directory where the user can select one
	 * file.
	 * 
	 * @param defaultDirectory
	 *            The directory the FileChooser should open.
	 * 
	 * @return Selected File.
	 */
	public static File selectFile(String defaultDirectory) {
		// used code example:
		// http://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
		JFileChooser chooser = new JFileChooser();
		if (!defaultDirectory.isEmpty()) {
			chooser = new JFileChooser(defaultDirectory);
		}
		int selection = chooser.showOpenDialog(null);
		if (selection == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		} else {
			// Used code example: http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null, "Could not find file.");
			return null;
		}
	}

	/**
	 * Opens a dialog where the user can select multiple files.
	 * 
	 * @return Selected Files.
	 */
	public static File[] selectMutipleFiles() {
		// used code example:
		// http://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		int selection = chooser.showOpenDialog(null);
		if (selection == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFiles();
		}
		// Because not possible to create a default file
		return null;
	}

	/**
	 * Opens a save-dialog where the user can select a file.
	 * 
	 * @return Selected file.
	 */
	public static File saveFile() {
		// create new file (used code example:
		// http://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html)
		JFileChooser chooser = new JFileChooser();
		int selection = chooser.showSaveDialog(null);
		if (selection == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		} else {
			return null;
		}
	}

	/**
	 * Opens a dialog where the user can add the number of bars in the chart.
	 * 
	 * @return The separator.
	 */
	public static String getUserInput(String message, String barText,
			String defaultValue) {
		// Used code example:
		// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
		return (String) JOptionPane.showInputDialog(null, message, barText,
				JOptionPane.QUESTION_MESSAGE, null, null, defaultValue);
	}

	/**
	 * Opens a dialog where the user can decide whether to choose yes or no.
	 * 
	 * @return TRUE if selected yes else FALSE.
	 */
	public static boolean getYesNoSelection(String messageText, String barText) {
		// Used code example:
		// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
		int selection = JOptionPane.showConfirmDialog(null, messageText,
				barText, JOptionPane.YES_NO_OPTION);
		if (selection == JOptionPane.YES_OPTION) {
			return true;
		}
		return false;
	}
}
