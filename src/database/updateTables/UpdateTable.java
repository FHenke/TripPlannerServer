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

import database.DatabaseConnection;

/**
 * @author Florian
 * Class with functions that all update classes will need, so thet they can extend from this class
 */
public abstract class UpdateTable implements UpdateableTable{
	
	protected static final Logger logger = LogManager.getLogger(UpdateTable.class);
	
	protected Connection conn = DatabaseConnection.getConnection();
	
	
	public UpdateTable(Connection conn){
		this.conn = conn;
	}
	
	public UpdateTable(){
		
	}
	
	/**
	 * 
	 * @param querry
	 * @return false if querry is empty, returns true if querry is NOT empty
	 * @throws SQLException
	 */
	protected boolean isQuerryEmpty(PreparedStatement querry) throws SQLException{
		//if no next element is available the querry returned no results in this case it returns next
		return querry.executeQuery().next();
	}
	
	/**
	 * 
	 * @param querry
	 * @return false if querry has no elements, returns true if query has elements
	 * @throws SQLException
	 */
	protected boolean hasQuerryResults(PreparedStatement querry) throws SQLException{
		//if no next element is available the querry returned no results in this case it returns next
		return querry.executeQuery().next();
	}
	
	/**
	 * proceeds an update on a specified Table from the Database by Using the Skyscaner API for all Places
	 */
	public abstract void proceed() throws IOException;
	
	
}
