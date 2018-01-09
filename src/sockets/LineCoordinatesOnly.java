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
        	System.out.println("Solution: " + solutionJson);

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
	
	public static void version2(){
	    ServerSocket socket;

	    Socket connection;
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
        	
        	if(jsonRequest.startsWith("{")){
        		System.out.println("1. Ausgabe");
	        	//Calculate path
	        	Request request = JsonConverter.jsonToRequest(jsonRequest);
	        	System.out.println(Connection.dateToString(request.getDepartureDateString()));
	        	pathCalculation.RequestHandler requestHandler = new pathCalculation.RequestHandler();
	        	LinkedBlockingQueue<Connection> connectionList = requestHandler.solveRequest(request);
	        	solutionJson = JsonConverter.getJson(connectionList);
	        	System.out.println("Solution: " + solutionJson);
	        	
	        	//output
	        	String str = "1234567890" + solutionJson;
	        	osw.write(str, 0, str.length()-1);
	        	osw.flush();
        	
        	}else{
        		try {
    				TimeUnit.SECONDS.sleep(10);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        		osw.write("0000000000,", 0, 11);
            	System.out.println("2. Ausgabe");
            	String str = "{\"origin\":{\"name\":\"Hannover, Germany\",\"type\":0,\"longitude\":9.7320104,\"latitude\":52.3758916},\"destination\":{\"name\":\"Hamburg, Germany\",\"type\":0,\"longitude\":9.9936819,\"latitude\":53.5510846},\"price\":0.0,\"duration\":{\"iMillis\":6286000},\"type\":0,\"direct\":false,\"weekday\":0,\"distance\":158764,\"subConnections\":[],\"returnConnection\":[]}]";
            	osw.write(str, 0, str.length());
            	osw.flush();
        	}
        	
        	
        	
        	/*
        	str = "test\n";
        	osw.write(str, 0, str.length());
        	osw.flush();
        	
        	
        	try {
				TimeUnit.MINUTES.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	*/
        	

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
