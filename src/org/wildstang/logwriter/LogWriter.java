package org.wildstang.logwriter;

public class LogWriter
{
	
	public static void main(String[] args)
	{
		Thread log = new LogThread();
		log.start();
		Thread debug = new DebugThread();
		log.start();
	}

}
