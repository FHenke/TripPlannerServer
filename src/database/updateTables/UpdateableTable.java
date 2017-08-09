package database.updateTables;

import java.io.IOException;

public interface UpdateableTable{
	
	/**
	 * proceeds an update on a specified Table from the Database by Using the Skyscaner API for all Places
	 */
	public void proceed() throws IOException;
}
