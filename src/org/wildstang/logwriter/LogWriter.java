package org.wildstang.logwriter;

public class LogWriter
{
	
	public static void main(String[] args)
	{
		System.out.println("Starting threads...");
		Thread log = new LogThread();
		log.start();
		Thread debug = new DebugThread();
		debug.start();
	}

}
