/**
 * 
 */
package utilities;

import java.io.FileWriter;
import java.io.IOException;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * @author Florian
 *
 */
public class XMLUtilities {
	
	public static void writeXmlToFile(Element data, String filename) throws IOException{
		Format format = Format.getPrettyFormat();
		format.setEncoding("UTF-8");
		XMLOutputter outputter = new XMLOutputter(format);
		outputter.setFormat(Format.getPrettyFormat());
		outputter.output(data, new FileWriter(filename));
	}
}
