/**
 * 
 */
package api;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdom2.JDOMException;

import utilities.Connection;

/**
 * @author Florian
 *
 */
public interface API {

	public LinkedBlockingQueue<Connection> getAllConnections(String origin, String destination, Date outboundDate, Date inboundDate) throws IOException, JDOMException, Exception;
}
