package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;






public class CloseApplicationListener implements Runnable {
	static ServerSocket socket;

    static Socket connection;
    
    public CloseApplicationListener(){
    	
    }
	
	//----------------------------------------------------------------
	@Override
	public void run(){
	    int port = 4309;
	    
		System.out.println("Close Listener is active.");

        try  {
        	String inputString="";
        	socket = new ServerSocket(port);
        	connection = socket.accept();

        	InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
        	BufferedReader input = new BufferedReader(inputStream);

        	inputString = input.readLine();
        	System.out.println("The input is: " + inputString);

        	connection.close();
        	socket.close();
        	
        	//Close System
        	if(inputString.equals("stop")){
        		LineCoordinatesOnly.closeSocket();
        		RequestListener.closeSocket();
        		System.out.println("Close application...");
        		System.exit(0);
        	}
        	
        	System.out.println("Closing...");
            
             
        } catch (IOException e)  {
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
