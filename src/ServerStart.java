import sockets.LineCoordinatesOnly;

public class ServerStart implements Runnable {
	
	public ServerStart(){
		
	}
	
	@Override
	public void run(){
		LineCoordinatesOnly.version1();
	}
	
	
}
