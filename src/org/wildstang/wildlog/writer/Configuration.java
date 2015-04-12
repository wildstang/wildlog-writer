package org.wildstang.wildlog.writer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * Populates the configuration fields with values from a configuration file.This configuration file is located in
 * {@code /etc/wildlog/wildlog.conf}.
 * <p>
 * Each line of the config file should have the format {@code PARAMETER_NAME=VALUE} where {@code PARAMETER_NAME} is the
 * name of the config parameter and {@code VALUE} is the desired value of the parameter in the appropriate format.
 * <p>
 * The following parameters are available:
 * <ul>
 * <li>{@code PORT}: the port to look for incoming connections on; will be parsed as an integer. Defaults to
 * {@code 1111}.</li>
 * <li>{@code OUTPUT_DIRECTORY}: The directory that logs should be placed in; should be a valid filepath. Defaults to ?</li>
 * <li>{@code OUTPUT_FILE_BASE_NAME}: The base name of the generated log files. For instance, if this parameter is
 * {@code wildstang}, the generated log files will be named {@code wildstang.data, wildstang-1.data, wildstang-2.data},
 * etc. Defaults to {@code log}.</li>
 * <li>{@code OUTPUT_FILE_EXTENSION}: The extension of the generated log files. Defaults to {@code .data}.
 * <li>{@code USE_FILE_EXTENSION}: Whether or not log files should be created with an extension; should be either
 * {@code true} or {@code false}. Defaults to true.</li>
 * <li>{@code MAX_LOGS_TO_KEEP}: The maximum number of logs that should be kept. Defaults to 0.</li>
 * </ul>
 * The log reader will not attempt to parse lines that begin with a {@code #}; they can be used to mark a line as a
 * comment.
 * 
 * @author Nathan
 *
 */

public class Configuration {

	public static int PORT;
	public static String OUTPUT_DIRECTORY;
	public static String OUTPUT_FILE_BASE_NAME;
	public static String OUTPUT_FILE_EXTENSION;
	public static boolean USE_FILE_EXTENSION;
	public static int MAX_LOGS_TO_KEEP;

	public static void readConfigurationFromFile(File file) {
		StringTokenizer st;
		String configLine;

		// Begin with all parameters initialized to their default values
		popualteDefaultValues();

		// If the file doesn't exist, don't attempt to read it
		if (!file.exists()) {
			return;
		}

		// Begin reading the config file
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
			while ((configLine = br.readLine().trim()) != null) {
				if (!(configLine.startsWith("#")) && (configLine.length() > 0)) {
					st = new StringTokenizer(configLine.trim(), "=");
					if (st.countTokens() >= 2) {
						String paramName = st.nextToken().trim();
						String paramValue = st.nextToken().trim();
						switch (paramName) {
						case "PORT":
							try {
								int port = Integer.parseInt(paramValue);
								PORT = port;
							} catch (NumberFormatException e) {
								// Leave the port as the default value
							}
							break;
						case "OUTPUT_DIRECTORY":
							try {
								File outputDirectory = new File(paramValue);
								if (!outputDirectory.exists()) {
									outputDirectory.createNewFile();
								}
								if (!outputDirectory.isDirectory()) {
									// Not a valid directory. Use the default.
									break;
								}
								OUTPUT_DIRECTORY = paramValue;
							} catch (IOException e) {
								// There was an error finding or creating this directory. Leave it as the default.
							}
							break;
						case "OUTPUT_FILE_BASE_NAME":
							OUTPUT_FILE_BASE_NAME = paramValue;
							break;
						case "OUTPUT_FILE_EXTENSION":
							OUTPUT_FILE_EXTENSION = paramValue;
							break;
						case "USE_FILE_EXTENSION":
							// Anything other than "true" will parse as false.
							USE_FILE_EXTENSION = Boolean.parseBoolean(paramValue);
							break;
						case "MAX_LOGS_TO_KEEP":
							try {
								int maxLogs = Integer.parseInt(paramValue);
								if (maxLogs <= 0) {
									// Don't allow a number of logs less than 1.
									break;
								}
								MAX_LOGS_TO_KEEP = maxLogs;
							} catch (NumberFormatException e) {
								// Not a valid number. Use the default.
								break;
							}
							break;
						default:
							System.out.println("Unknown parameter found: " + configLine);
						}
					} else {
						// Bad line; skip it.
						continue;
					}
				}
			}
		} catch (Throwable ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Initializes all of this class's fields to appropriate default values.
	 */
	private static void popualteDefaultValues() {
		PORT = 1111;
		OUTPUT_DIRECTORY = File.listRoots()[0].toString();
		OUTPUT_FILE_BASE_NAME = "log";
		OUTPUT_FILE_EXTENSION = "data";
		USE_FILE_EXTENSION = true;
		MAX_LOGS_TO_KEEP = 5;
	}

	/**
	 * Gets the log file that logs should be written to. If {@code USE_FILE_EXTENSION} is {@code true}, this file will
	 * be {@code OUTPUT_DIRECTORY/OUTPUT_FILE_BASE_NAME.OUTPUT_FILE_EXTENSION}. If {@code USE_FILE_EXTENSION} is
	 * {@code false}, the file extension will not be appended to the end of the file name.
	 * 
	 * @return the appropriate file
	 * @see Configuration#getNumberedLogFile(int)
	 */
	public static File getLogFile() {
		String fileName;
		if (USE_FILE_EXTENSION) {
			fileName = OUTPUT_FILE_BASE_NAME + "." + OUTPUT_FILE_EXTENSION;
		} else {
			fileName = OUTPUT_FILE_BASE_NAME;
		}
		return new File(OUTPUT_DIRECTORY + File.separator + fileName);
	}

	/**
	 * Gets the specified numbered log file; this is mostly useful for when logs need to be rotated. If
	 * {@code USE_FILE_EXTENSION} is {@code true}, this file will be
	 * {@code OUTPUT_DIRECTORY/OUTPUT_FILE_BASE_NAME-number.OUTPUT_FILE_EXTENSION}. If {@code USE_FILE_EXTENSION} is
	 * {@code false}, the file extension will not be appended to the end of the file name.
	 * <p>
	 * Note that if {@code number <= 0}, {@code getLogFile()} will be used instead.
	 * 
	 * @param number
	 *            the number of the file that should be returned
	 * @return the appropriate file
	 * @see Configuration#getLogFile()
	 */
	public static File getNumberedLogFile(int number) {
		if (number <= 0) {
			return getLogFile();
		}
		String fileName;
		if (USE_FILE_EXTENSION) {
			fileName = OUTPUT_FILE_BASE_NAME + "-" + number + "." + OUTPUT_FILE_EXTENSION;
		} else {
			fileName = OUTPUT_FILE_BASE_NAME + "-" + number;
		}
		return new File(OUTPUT_DIRECTORY + File.separator + fileName);
	}

}
