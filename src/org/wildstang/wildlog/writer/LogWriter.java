package org.wildstang.wildlog.writer;

import java.io.File;

public class LogWriter {

	public static void main(String[] args) {
		// Initialize all configurable parameters
		Configuration.readConfigurationFromFile(new File("wildlog.conf"));

		// For testing, print out the found configuration values
		System.out.println("PORT: " + Configuration.PORT);
		System.out.println("OUTPUT_DIRECTORY: " + Configuration.OUTPUT_DIRECTORY);
		System.out.println("OUTPUT_FILE_BASE_NAME: " + Configuration.OUTPUT_FILE_BASE_NAME);
		System.out.println("OUTPUT_FILE_EXTENSION: " + Configuration.OUTPUT_FILE_EXTENSION);
		System.out.println("USE_FILE_EXTENSION: " + Configuration.USE_FILE_EXTENSION);
		System.out.println("MAX_LOGS_TO_KEEP: " + Configuration.MAX_LOGS_TO_KEEP);
		
		// Create the log file for writing to
		try {
		Configuration.getLogFile().createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error starting WildLog; the log file could not be created.");
			return;
		}

		// Create and begin our log thread that will receive logs over a socket.
		System.out.println("Starting WildLog.");
		new MessageHandlerThread(Configuration.PORT).start();
	}
}
