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
import java.util.Map;

/**
 * This class is in charge of accepting incoming connections and handling all messages passed over that connection. At
 * present, all messages will consist of Java objects written to a {@link ObjectOutputStream}, although in the future
 * this may change to a more generic representation of data such as JSON in order to make WildLog cross-platform.
 * 
 * When a message is received, this class will decide how to act on it based on the received object's class. Currently,
 * two classes are supported:
 * <ul>
 * <li>{@code String}: interpreted as a command</li>
 * <li>{@code Map<?,?>}: interpreted as a log</li>
 * </ul>
 * A combination of commands and logs will be used to make WildLog function. A typical command/log sequence will look
 * like this:
 * 
 * <pre>
 * "startlog" (command)
 * {@code Map<?,?>} (log)
 * {@code Map<?,?>} (log)
 * {@code Map<?,?>} (log)
 * .
 * .
 * .
 * "endlog" (command)
 * </pre>
 * 
 * The {@code startlog} command signifies that this thread should rotate the logs and open the main log file for
 * writing. Any subsequent Maps will be written to this file.
 * <p>
 * The {@code endlog} command signifies that this thread should stop writing to the log file and reject any subsequent
 * logs until a {@code startlog} command is received again.
 */
public class MessageHandlerThread extends Thread {

	// Possible commands
	private static final String COMMAND_START_LOG = "startlog";
	private static final String COMMAND_END_LOG = "endlog";

	private int port;

	/**
	 * Constructs a new instance of LogWriterThread. This thread will run in the background and receive data over a
	 * socket.
	 * 
	 * @param port
	 *            The port to listen for incoming connections on
	 */
	public MessageHandlerThread(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		System.out.println("Logging thread started!");
		boolean logStarted = false;
		int currentLog = 0;
		// Placed in a loop so that we will continually try to make a connection until one is established.
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
					// Read incoming objects until we lose the connection or something goes horribly wrong.
					while (true) {

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
								if (fileOutput != null) {
									fileOutput.close();
								}
								// Rotate the logs
								rotateLogs();
								// Create a new log file
								fileOutput = new ObjectOutputStream(new FileOutputStream(Configuration.getLogFile()));
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
								if (logStarted && fileOutput != null) {
									fileOutput.writeChars(toWrite);
								}
								System.out.println("wrote string " + toWrite);
							}
						}

						// Only accept logs if we've started a file
						if (logStarted) {
							if (object instanceof Map<?, ?>) {

								// We received a map; this is a container of log data. Write this to the file.
								System.out.println("Writing new log");
								if (fileOutput != null) {
									fileOutput.writeObject(object);
									fileOutput.flush();
									assert true;
								}
							}
						}
						// Try writing to the socket. If we lost comm, this will throw an exception.
						socket.getOutputStream().write(0);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Lost connection. Retrying...");
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

	/**
	 * Performs a rotation of the log files to prepare for a new one to be written.
	 * <p>
	 * The log-rotation algorithm begins with the numbered log file specified by {@link Configuration.MAX_LOGS_TO_KEEP}.
	 * If the file exists, the contents of the next lowest-numbered log file are copied to that log file. If it does not
	 * exist, the algorithm checks for the subsequent files until it finds one that exists.
	 * <p>
	 * After iterating though all the log files, the base log file, which will soon be written to, is cleared of any
	 * previously-existing content.
	 */
	private static void rotateLogs() {
		File reading;
		File writing;
		String line = "";
		BufferedWriter bw;
		BufferedReader br;
		try {
			System.out.println("Rotating logs!");

			for (int i = Configuration.MAX_LOGS_TO_KEEP; i > 0; i--) {
				reading = Configuration.getNumberedLogFile(i - 1);
				writing = Configuration.getNumberedLogFile(i);

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

			// Ensure that the log file that will be written to is empty
			bw = new BufferedWriter(new FileWriter(Configuration.getLogFile()));
			bw.write("");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
