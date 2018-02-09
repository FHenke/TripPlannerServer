package sockets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import utilities.Connection;
import utilities.Request;

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
        	String str = "stop";
        	osw.write(str, 0, str.length());
        	osw.flush();

        	

        	socket.close();
        	System.out.println("Sended " + str);
            
             
        } catch (IOException e)  {
            System.out.println("Socket connection fail!ed: " + e.toString());
            return;
        }
        
	}
}

