	package org.wildstang.logwriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DebugThread extends Thread
{
	boolean running = true;
	
	@Override
	public void run()
	{
		while(true)
		{
			System.out.println("DebugThread started!");
			boolean running = true;
			try
			{
				Map<String, Object> map = new HashMap<String, Object>();
				ServerSocket logSocket = new ServerSocket(1112);
				Socket socket = logSocket.accept();
				InputStream yourInputStream;
				ObjectInputStream  mapInputStream;
				System.out.println("Starting loop...");
				Date date = new Date(System.currentTimeMillis());
				DateFormat format = new SimpleDateFormat("HH-mm-ss");
				String time = format.format(date);
				FileOutputStream fileOut = new FileOutputStream(time + "-debug.data");
				ObjectOutputStream out;
				out = new ObjectOutputStream(fileOut);
				yourInputStream = null;
				System.out.println("Waiting for InputStream...");
				while((yourInputStream = socket.getInputStream()) == null){}
				System.out.println("Data found!");
				mapInputStream = new ObjectInputStream(yourInputStream);
				while(running)
				{
					while((map = (Map) mapInputStream.readObject()) == null){}
					//running = (Boolean) map.get("enabled");
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

