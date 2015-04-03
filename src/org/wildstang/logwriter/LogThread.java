package org.wildstang.logwriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogThread extends Thread
{

	@Override
	public void run()
	{
		File reading;
		File writing;
		String line = "";
		BufferedWriter bw;
		BufferedReader br;
		File toWrite;
		while (true)
		{
			toWrite = new File("/robot-logs/log.data");
			try
			{

				System.out.println("LogThread started!");
				List<File> files = new ArrayList<>();
				files.add(new File("/robot-logs/log-5.data"));
				files.add(new File("/robot-logs/log-4.data"));
				files.add(new File("/robot-logs/log-3.data"));
				files.add(new File("/robot-logs/log-2.data"));
				files.add(new File("/robot-logs/log-1.data"));
				files.add(new File("/robot-logs/log.data"));
				for (int i = 0; i + 1 < files.size(); i++)
				{
					reading = files.get(i + 1);
					writing = files.get(i);
					if(reading.exists())
					{
						if(!writing.exists())
						{
							writing.createNewFile();
						}
						bw = new BufferedWriter(new FileWriter(writing));
						br = new BufferedReader(new FileReader(reading));
						line = "";
						while ((line = br.readLine()) != null)
						{
							bw.write(line);
						}
						bw.close();
						br.close();
					}
				}
				
				bw = new BufferedWriter(new FileWriter(toWrite));
				bw.write("");
				bw.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			boolean running = true;
			try
			{
				Map<String, Object> map = new HashMap<String, Object>();
				ServerSocket logSocket = new ServerSocket(1111);
				Socket socket = logSocket.accept();
				InputStream yourInputStream;
				ObjectInputStream mapInputStream;
				System.out.println("Starting loop...");
				Date date = new Date(System.currentTimeMillis());
				DateFormat format = new SimpleDateFormat("HH-mm-ss");
				String time = format.format(date);
				FileOutputStream fileOut = new FileOutputStream(toWrite);
				ObjectOutputStream out;
				out = new ObjectOutputStream(fileOut);
				yourInputStream = null;
				System.out.println("Waiting for InputStream...");
				while ((yourInputStream = socket.getInputStream()) == null)
				{
				}
				System.out.println("Data found!");
				mapInputStream = new ObjectInputStream(yourInputStream);
				while (running)
				{
					while ((map = (Map) mapInputStream.readObject()) == null)
					{
					}
					running = (Boolean) map.get("enabled");
					System.out.println("Writing");
					out.writeObject(map);
					out.flush();
				}
				out.close();
				fileOut.close();
				mapInputStream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
}
