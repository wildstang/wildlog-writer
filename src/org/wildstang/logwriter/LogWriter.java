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

public class LogWriter implements Runnable
{
	List<Map<String, Object>> maps = new ArrayList<>();
	
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		new LogWriter();
	}

	public LogWriter()
	{
		try
		{
			for(int i = 0; true; i++)
			{
				Socket socket = new Socket("roboRIO-111.local", 1111);
			    
			    InputStream yourInputStream = null;
			    while((yourInputStream = socket.getInputStream()) == null){}
			    ObjectInputStream mapInputStream = new ObjectInputStream(yourInputStream);
			    maps.add((Map) mapInputStream.readObject());
			    mapInputStream.close();
			    if(i % 10 == 0)
			    {
					Thread t = new Thread();
					t.start();
			    }
			}
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
	
	@Override
	public void run()
	{
		try
		{
			FileOutputStream fileOut = new FileOutputStream("save.data");
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

}
