/**
 * 
 */
package api;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdom2.JDOMException;

import utilities.Connection;

/**
 * @author Florian
 *
 */
public interface API {

	/**
	 * 
	 * @param origin where to Start
	 * @param destination where to go
	 * @param outboundDate date when the trip starts
	 * @param inboundDate date for comming back
	 * @return List with all available connections. All outbound connections in the beginning in the list, and all inbound connections in the end of the list.
	 * @throws IOException
	 * @throws JDOMException
	 * @throws Exception
	 */
	public LinkedBlockingQueue<Connection> getAllConnections(String origin, String destination, GregorianCalendar outboundDate, GregorianCalendar inboundDate) throws IOException, JDOMException, Exception;
}
