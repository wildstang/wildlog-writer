package org.wildstang.logwriter;

import java.io.File;
import java.io.IOException;

public class LogWriter
{
	
	public static void main(String[] args)
	{
		File file = new File("/robot-logs/log.data");
		if(!file.exists())
		{
			try {
				file.createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Starting threads...");
		Thread log = new LogThread();
		log.start();
		Thread debug = new DebugThread();
		debug.start();
	}

}
