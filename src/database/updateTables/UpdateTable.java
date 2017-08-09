/**
 * 
 */
package database.updateTables;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Florian
 * Class with functions that all update classes will need, so thet they can extend from this class
 */
public abstract class UpdateTable implements UpdateableTable{
	
	protected static final Logger logger = LogManager.getLogger(UpdateContinents.class);
	
	protected Connection conn = null;
	
	
	public UpdateTable(Connection conn){
		this.conn = conn;
	}
	
	protected boolean isQuerryEmpty(PreparedStatement querry) throws SQLException{
		//if no next element is available the querry returned no results in this case it returns next
		return querry.executeQuery().next();
	}
	
	/**
	 * proceeds an update on a specified Table from the Database by Using the Skyscaner API for all Places
	 */
	public abstract void proceed() throws IOException;
	
	
}
