package sockets;

import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

import utilities.Connection;
import utilities.Request;

import java.io.*;

public class LineCoordinatesOnly {
	
	
	//----------------------------------------------------------------
	
	
	public static void version2(){
	    ServerSocket socket;

	    Socket connection;
	    String jsonRequest = new String();
	    OutputStreamWriter osw;

	    int port = 4308;
	    
		System.out.println("Signal Server 2 is running.");

        try  {

        	socket = new ServerSocket(port);
        	connection = socket.accept();

        	InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
        	osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        	BufferedReader input = new BufferedReader(inputStream);

        	jsonRequest = input.readLine();
        	System.out.println("The input is: " + jsonRequest);
        	
        	Request request = JsonConverter.jasonToRequest(jsonRequest);
        	System.out.println(Connection.dateToString(request.getDepartureDateString()));
        	pathCalculation.RequestHandler requestHandler = new pathCalculation.RequestHandler();
        	LinkedBlockingQueue<Connection> connectionList = requestHandler.solveRequest(request);
        	String solutionJson = JsonConverter.getJson(connectionList);
        	// --bis hier funktioniert es, beim Senden ist es nicht klar ob der Java code nicht sendet, oder der php code nicht empfängt

        	System.out.println("Solution: " + solutionJson);
        	
        	String str = solutionJson + "\n";
        	osw.write(str, 0, str.length());
        	osw.flush();

        	connection.close();
        	socket.close();
        	System.out.println("Closing...");
            
             
        } catch (IOException e)  {
            System.out.println("Fail!: " + e.toString());
        }

        
        
        version2();
        
	}
	
	
	//Sending only
	public static void testSend() throws UnknownHostException, IOException{
		System.out.println("testSend");
	    Socket socket = null;
	    OutputStreamWriter osw;
	    String str = "Hello World\n";
	    socket = new Socket("localhost", 4308);
	    
	    osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
	    osw.write(str, 0, str.length());
	    osw.flush();

	    
		socket.close();
	}
		
	
}
