package sockets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class SendCommand implements Runnable {
	String command = "";
	static Socket socket;

    static Socket connection;
    
    public SendCommand(String command){
    	this.command = command;
    }
	
	//----------------------------------------------------------------
	@Override
	public void run(){

	    int port = 4309;
	    
		System.out.println("Command Server is running.");

        try  {
        	socket = new Socket("localhost", port);
        	
            BufferedWriter osw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        	//output
        	osw.write(command, 0, command.length());
        	osw.flush();

        	

        	socket.close();
        	System.out.println("Sended " + command);
            
             
        } catch (IOException e)  {
            System.out.println("Socket connection fail!ed: " + e.toString());
            return;
        }
        
	}
}

