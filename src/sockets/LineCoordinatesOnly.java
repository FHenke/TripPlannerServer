package sockets;

import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import utilities.Connection;
import utilities.Request;

import java.io.*;

public class LineCoordinatesOnly {
	static ServerSocket socket;

    static Socket connection;
	
	//----------------------------------------------------------------
	public static void version1(){
	    String jsonRequest = new String();
	    OutputStreamWriter osw;

	    int port = 4308;
	    
		System.out.println("Signal Server 2 is running.");

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
            System.out.println("Socket connection fail!ed: " + e.toString());
            return;
        }

        
        
        version1();
        
	}
	
	public static void closeSocket(){
		try {
			connection.close();
			socket.close();
		} catch (IOException e) {
		
		}
	}
		
}
