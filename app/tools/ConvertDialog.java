package tools;

import javax.swing.JOptionPane;

/**
 * This class opens a dialog where the user can decide whether he wants to
 * convert the data.
 * 
 * @author Christian Olenberger
 * 
 */
public class ConvertDialog {

	/**
	 * This method opens a dialog the FileFormatInformationForm if selected and
	 * a dialog where an integer represents the selected operation (see:
	 * FileFormatInformationForm).
	 * 
	 * @return 0: Extract dataset. 1: Convert file. 2: Use file.
	 */
	public static int getConvertOperation() {
		// Ask for dialog for more information
		// Used code example:
		// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
		// Open dialog for input
		Object[] btnText = { "1. Extract dataset from text",
				"2. Convert file into sparse format",
				"3. Use file (already in sparse format)" };
		int convertSelection = JOptionPane.showOptionDialog(null,
				"Select file operation:", "Select operation",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, btnText, btnText[2]);
		return convertSelection;
	}
}
