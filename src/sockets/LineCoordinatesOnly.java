package sockets;

import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

import utilities.Connection;
import utilities.Request;

import java.io.*;

public class LineCoordinatesOnly {
	static ServerSocket socket;

    static Socket connection;
    static boolean closeApplication = false;
	
	//----------------------------------------------------------------
	public static void version1(){
	    String jsonRequest = new String();
	    OutputStreamWriter osw;

	    int port = 4308;
	    
		System.out.println("Server is running.");

        try  {
        	String solutionJson="";
        	socket = new ServerSocket(port);
        	connection = socket.accept();

        	InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
        	osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        	BufferedReader input = new BufferedReader(inputStream);

        	jsonRequest = input.readLine();
        	System.out.println("The input is: " + jsonRequest);

        	//Calculate path
        	Request request = JsonConverter.jsonToRequest(jsonRequest);
        	System.out.println(Connection.dateToString(request.getDepartureDateString()));
        	pathCalculation.RequestHandler requestHandler = new pathCalculation.RequestHandler();
        	LinkedBlockingQueue<Connection> connectionList = requestHandler.solveRequest(request);
        	solutionJson = JsonConverter.getJson(connectionList);
        	//System.out.println("Solution: " + solutionJson);

        	//output
        	String str = solutionJson;
        	osw.write(str, 0, str.length());
        	osw.flush();

        	

        	connection.close();
        	socket.close();
        	System.out.println("Closing...");
            
             
        } catch (IOException e)  {
        	if(!closeApplication)
        		System.out.println("Socket connection fail!ed: " + e.toString());
            return;
        }

        
        
        version1();
        
	}
	
	public static void closeSocket(){
		try {
			if(socket != null)
				socket.close();
		} catch (IOException e) {
				System.out.println("Cant't close the socket connection");
		}
	}
		
}
