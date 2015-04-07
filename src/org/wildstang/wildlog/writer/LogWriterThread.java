package org.wildstang.wildlog.writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogWriterThread extends Thread {

	// Possible commands
	private static final String COMMAND_START_LOG = "startlog";
	private static final String COMMAND_END_LOG = "endlog";

	boolean running = true;
	private int port;
	private String fileSuffix;

	/**
	 * Constructs a new instance of LogWriterThread. This thread will run in the background and receive data over a
	 * socket.
	 * 
	 * The parameter fileSuffix will be appended to the current date/time when creating each file. For instance, a file
	 * suffix of "-debug.data" will produce files that are named "yyyy_MM_dd:HH-mm-ss-debug.data".
	 * 
	 * @param port
	 * @param fileSuffix
	 */
	public LogWriterThread(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		System.out.println("DebugThread started!");
		boolean logStarted = false;
		int currentLog = 0;
		while (true) {
			System.out.println("Starting loop again");
			ServerSocket logSocket = null;
			Socket socket = null;
			try {
				logSocket = new ServerSocket(port);
				socket = logSocket.accept();
				ObjectInputStream mapInputStream;
				System.out.println("Starting loop...");
				ObjectOutputStream fileOutput = null;
				System.out.println("Waiting for InputStream...");
				mapInputStream = new ObjectInputStream(socket.getInputStream());
				System.out.println("Data found!");

				try {
					while (running) {

						Object object = mapInputStream.readObject();

						if (object instanceof String) {
							// We received a raw String; this is some sort of command.
							// Commands should usually come in the format <command>:<data>
							String command = (String) object;
							System.out.println("Command received: " + command);
							if (command.equals(COMMAND_START_LOG)) {
								if (logStarted) {
									// Don't start a new one until we stop
									continue;
								}
								// The remote client is requesting us to start a new log file.
								logStarted = true;
								// Close the current log file if it exists and is open
								if(fileOutput != null) {
									fileOutput.close();
								}
								// Rotate the logs
								rotateLogs();
								// Create a new log file
								fileOutput = new ObjectOutputStream(new FileOutputStream("/robot-logs/log.data"));
								System.out.println("starting log file " + currentLog);
								currentLog++;
							} else if (command.equals(COMMAND_END_LOG)) {
								// Stop the current log file!
								logStarted = false;
								if (fileOutput != null) {
									fileOutput.close();
								}
								System.out.println("ending log file");
							} else if (command.startsWith("write:")) {
								String toWrite = command.replace("write:", "");
								if(logStarted && fileOutput != null) {
									fileOutput.writeChars(toWrite);
								}
							}
						}

						// Only accept logs if we've started a file
						if (logStarted) {
							if (object instanceof Map<?, ?>) {

								// We received a map; this is a container of log data. Write this to the file.
								System.out.println("Writing");
								if (fileOutput != null) {
									fileOutput.writeObject(object);
									fileOutput.flush();
								}
							}
						}
						// Try writing to the socket. If we lost comm, this will throw an exception.
						socket.getOutputStream().write(0);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				fileOutput.close();
				mapInputStream.close();
				logSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			logStarted = false;

		}
	}

	private void rotateLogs() {
		File reading;
		File writing;
		String line = "";
		BufferedWriter bw;
		BufferedReader br;
		File toWrite = new File("/robot-logs/log.data");
		try {

			System.out.println("LogThread started!");
			List<File> files = new ArrayList<>();
			files.add(new File("/robot-logs/log-5.data"));
			files.add(new File("/robot-logs/log-4.data"));
			files.add(new File("/robot-logs/log-3.data"));
			files.add(new File("/robot-logs/log-2.data"));
			files.add(new File("/robot-logs/log-1.data"));
			files.add(new File("/robot-logs/log.data"));
			for (int i = 0; i + 1 < files.size(); i++) {
				reading = files.get(i + 1);
				writing = files.get(i);
				if (reading.exists()) {
					if (!writing.exists()) {
						writing.createNewFile();
					}
					bw = new BufferedWriter(new FileWriter(writing));
					br = new BufferedReader(new FileReader(reading));
					line = "";
					while ((line = br.readLine()) != null) {
						bw.write(line);
					}
					bw.close();
					br.close();
				}
			}

			bw = new BufferedWriter(new FileWriter(toWrite));
			bw.write("");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
