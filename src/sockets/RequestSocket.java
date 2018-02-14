package sockets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class RequestSocket implements Runnable {
	String command = "";
	static Socket socket;

    
    public RequestSocket(String command){
    	this.command = command;
    }
	
	//----------------------------------------------------------------
	@Override
	public void run(){

	    int port = 4310;
	    
		System.out.println("2Command Server is running.");

        try  {
        	socket = new Socket("localhost", port);
        	
            BufferedWriter osw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        	//output
        	osw.write(command, 0, command.length());
        	osw.flush();
        	
        	System.out.println("2Command sended. " + command );
        	
        	//Response
        	InputStreamReader inputStream = new InputStreamReader(socket.getInputStream());
        	BufferedReader input = new BufferedReader(inputStream);

        	String inputString = input.readLine();
        	System.out.println("The response is: " + inputString);
        	

        	socket.close();
        	System.out.println("Sended " + command);
            
             
        } catch (IOException e)  {
            System.out.println("Socket connection fail!ed: " + e.toString());
            return;
        }
        
	}
}
