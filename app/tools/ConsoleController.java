package tools;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This class allows you to enable or disable the possibility to print on the
 * console.
 * 
 * @author Christian Olenberger
 * 
 */
public class ConsoleController {

	private static final PrintStream normalStream = System.out;

	/**
	 * This method disables the possibility to print on the console.
	 */
	public static void disableConsolePrint() {
		// Used code example:
		// http://stackoverflow.com/questions/8363493/hiding-system-out-print-calls-of-a-class
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		}));
	}

	/**
	 * This method enables the possibility to print on the console.
	 */
	public static void enableConsolePrint() {
		// Used code example:
		// http://stackoverflow.com/questions/8363493/hiding-system-out-print-calls-of-a-class
		System.setOut(normalStream);
	}
}
