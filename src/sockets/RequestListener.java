package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import database.updateTables.UpdateDatabase;
import utilities.Connection;
import utilities.Request;

public class RequestListener implements Runnable{
	
	static ServerSocket socket;

    static Socket connection;
    static boolean closeApplication = false;
    
    public RequestListener(){
    	
    }
	
	//----------------------------------------------------------------
    @Override
	public void run(){
	    String command = new String();
	    OutputStreamWriter osw;

	    int port = 4310;
	    
		System.out.println("Server Listens for commands.");

        try  {
        	String response="";
        	socket = new ServerSocket(port);
        	connection = socket.accept();

        	InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
        	osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        	BufferedReader input = new BufferedReader(inputStream);

        	command = input.readLine();
        	System.out.println("The input is: " + command);

        	//Process command
        	if(command.equals("status")){
        		response = UpdateDatabase.getStatus();
        	}

        	//output
        	osw.write(response, 0, response.length());
        	osw.flush();

        	

        	connection.close();
        	socket.close();
            
             
        } catch (IOException e)  {
        	if(!closeApplication)
        		System.out.println("Socket connection fail!ed: " + e.toString());
            return;
        }

        
        
        run();
        
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
