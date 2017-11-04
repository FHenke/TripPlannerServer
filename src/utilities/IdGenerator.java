package utilities;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
	static AtomicInteger id = new AtomicInteger();
	
	public static int getNewID(){
		return id.incrementAndGet();
	}
}
