package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.EStream;

public class DatabaseConnection {
	protected static final Logger logger = LogManager.getLogger(DatabaseConnection.class);
	
	private static final String DATABASE_NAME = "TripPlanner";
	private static final String DATABASE_HOST = "localhost";
	private static final String DATABASE_USER = "henke";
	//private static final String DATABASE_USER = "postgres";
	//private static final String DATABASE_KEY = "TripPlanner";
	private static final String DATABASE_KEY = "trip";
	private static final String DATABASE_PORT = "5432";
	
	private static Connection conn = ConnectToDatabase();
	
	/**
	 * Empty constructor, Connects to the Database by using the default values
	 * @throws SQLException Error by connecting to the Database
	 */
	public DatabaseConnection() throws SQLException{
		//ConnectToDatabase();
	}
	
	/**
	 * Builds the url used for connecting to a postgresqldatabase with JDBC using the classvariables
	 * @return URL for connecting with a postgresql Database via JDBC
	 */
	private static String BuildJdbcUrl(){
		return "jdbc:postgresql://" + DATABASE_HOST + ":" + DATABASE_PORT + "/" + DATABASE_NAME;
	}
	
	/**
	 * Connects to A Postgresql Database via aJDBC by using the class variables
	 * @throws SQLException a Error occured by connecting to the Database
	 */
	private static Connection ConnectToDatabase(){
		try {
			return DriverManager.getConnection(BuildJdbcUrl(), DATABASE_USER, DATABASE_KEY);
			//return DriverManager.getConnection(BuildJdbcUrl(), DATABASE_USER, null);
		} catch (SQLException e) {
			logger.error("Database connection can't be established: " + e.toString());
		}
		return null;
	}
	
	public static Connection getConnection(){
		return conn;
	}
	
	//TODO: remove this function
	public void SimpleQuerry() throws SQLException{
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM public.continents;");
		
		while (rs.next())
		{
		    System.out.print("Column 1 returned: ");
		    System.out.println(rs.getString(1));
		}
		rs.close();
		st.close();

	}
	
}
