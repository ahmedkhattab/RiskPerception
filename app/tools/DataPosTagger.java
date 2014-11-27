package tools;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import tools.DataTypes.TimedMessage;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * This class uses the Standford POS-Tagger to tag a whole data construct.
 * 
 * @author Christian Olenberger
 * 
 */
public class DataPosTagger {

	private static MaxentTagger tagger;
	private static boolean taggerSet = false;
	private static boolean ignoreModel = false;

	/**
	 * This method tags every String in the ArrayList.
	 * 
	 * @param list
	 *            The listArray that should be tagged.
	 * @return The tagged listArray.
	 */
	public static ArrayList<TimedMessage> tagArrayList(
			ArrayList<TimedMessage> list) {
		ArrayList<TimedMessage> result = new ArrayList<TimedMessage>();
		if (!taggerSet) {
			if (ignoreModel || !setModel()) {
				return list;
			}
		}
		// Used code example:
		// http://www.galalaly.me/index.php/2011/05/tagging-text-with-stanford-pos-tagger-in-java-applications/
		for (int i = 0; i < list.size(); i++) {
			result.add(new TimedMessage(list.get(i).getDate(), tagger
					.tagString(list.get(i).getMessage())));
		}
		return result;
	}

	/**
	 * This method tags every String in the ArrayList.
	 * 
	 * @param list
	 *            The listArray that should be tagged.
	 * @return The tagged listArray.
	 */
	public static ArrayList<String> tagStringList(ArrayList<String> list) {
		ArrayList<String> result = new ArrayList<String>();
		if (!taggerSet) {
			if (ignoreModel || !setModel()) {
				return list;
			}
		}
		// Used code example:
		// http://www.galalaly.me/index.php/2011/05/tagging-text-with-stanford-pos-tagger-in-java-applications/
		for (int i = 0; i < list.size(); i++) {
			result.add(tagger.tagString(list.get(i)));
		}
		return result;
	}

	/**
	 * This method tags every String in a given array.
	 * 
	 * @param array
	 *            The array that should be tagged.
	 * @return The tagged array.
	 */
	public static String[] tagArray(String[] array) {
		if (!taggerSet) {
			if (ignoreModel || !setModel()) {
				return array;
			}
		}
		String[] result = new String[array.length];
		// Used code example:
		// http://www.galalaly.me/index.php/2011/05/tagging-text-with-stanford-pos-tagger-in-java-applications/
		for (int i = 0; i < array.length; i++) {
			result[i] = tagger.tagString(array[i]);
		}
		return result;
	}

	/**
	 * This method tags a String.
	 * 
	 * @param text
	 *            The text that should be tagged.
	 * @return The tagged text.
	 */
	public static String tagString(String text) {
		if (!taggerSet) {
			if (ignoreModel || !setModel()) {
				return text;
			}
		}
		// Used code example:
		// http://www.galalaly.me/index.php/2011/05/tagging-text-with-stanford-pos-tagger-in-java-applications/
		return tagger.tagString(text);
	}

	/**
	 * Opens a dialog where the user can select the model for the POS-Tagger.
	 * 
	 * @return TRUE if could set the tagger.
	 */
	public static boolean setModel() {
		// Used code example:
		// http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
		JOptionPane.showMessageDialog(null,
				"Please select model for the Standford-POS-Tagger.");
		File selectedFile = InputOptionCollection
				.selectFile("pos_tagger_models");
		if (selectedFile == null) {
			ignoreModel = true;
			return false;
		}
		// Used code example:
		// http://www.galalaly.me/index.php/2011/05/tagging-text-with-stanford-pos-tagger-in-java-applications/
		tagger = new MaxentTagger(selectedFile.getAbsolutePath());
		taggerSet = true;
		return true;
	}

	/**
	 * Setter for the ignore model variable.
	 * 
	 * @param ignoreModelSelection
	 *            If TRUE the tagger will act like there is no model und just
	 *            return the input.
	 */
	public static void setIgnoreModel(boolean ignoreModelSelection) {
		ignoreModel = ignoreModelSelection;
	}
}
