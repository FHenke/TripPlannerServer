/**
 * 
 */
package api;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdom2.JDOMException;

import utilities.Connection;

/**
 * @author Florian
 *
 */
public interface API {

	public LinkedBlockingQueue<Connection> getAllConnections(String origin, String destination, GregorianCalendar outboundDate, GregorianCalendar inboundDate) throws IOException, JDOMException, Exception;
}
