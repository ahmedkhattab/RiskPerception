package tools;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This static class provides a method to open a url in the current default
 * browser.
 * 
 * @author Christian Olenberger
 * 
 */
public class URLOpener {

	/**
	 * This method gets a URL (as String) and opens it in the default browser.
	 * 
	 * @param url
	 *            The URL you want to open in a browser.
	 * @throws IOException
	 *             Problem while reading url.
	 * @throws URISyntaxException
	 *             Syntax-Problem with URL.
	 */
	public static void openURL(String url) throws IOException,
			URISyntaxException {
		// Used code example:
		// http://java-demos.blogspot.de/2012/10/open-url-in-java.html
		Desktop desktop = Desktop.getDesktop();
		desktop.browse(new URI(url));
	}

	/**
	 * This method gets a path to a html-File and opens it in the default
	 * browser.
	 * 
	 * @param path
	 *            The path to the html-File.
	 * @throws IOException
	 *             Problem with file.
	 */
	public static void openURLFromFile(String path) throws IOException {
		// Used Code:
		// http://stackoverflow.com/questions/20517434/how-to-open-html-file-using-java
		File htmlFile = new File(path);
		Desktop.getDesktop().browse(htmlFile.toURI());
	}

}
