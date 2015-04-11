package org.wildstang.wildlog.writer;

import java.io.File;
import java.io.IOException;

public class LogWriter {

	public static void main(String[] args) {
		File file = new File("log.data");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Starting threads...");
		
		// Log thread
		new LogWriterThread(1111).start();
	}

}
