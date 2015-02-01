package org.wildstang.logwriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogThread extends Thread
{
	@Override
	public void run()
	{
		try
		{
			List<Map<String, Object>> maps = new ArrayList<>();
			Socket logSocket = new Socket("roboRIO-111.local", 1111);
			InputStream yourInputStream;
			ObjectInputStream  mapInputStream;
			for(int i = 0; true; i++)
			{
				yourInputStream = null;
				while((yourInputStream = logSocket.getInputStream()) == null){}
				mapInputStream = new ObjectInputStream(yourInputStream);
				maps.add((Map) mapInputStream.readObject());
				mapInputStream.close();
				if(i % 10 == 0)
				{
					Thread thread = new Thread(){
						public void run()
						{
							try
							{
								FileOutputStream fileOut = new FileOutputStream("log.data");
								ObjectOutputStream out;
								out = new ObjectOutputStream(fileOut);
								out.writeObject(maps);
								out.close();
								fileOut.close();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
				    };
					thread.start();
				}
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
