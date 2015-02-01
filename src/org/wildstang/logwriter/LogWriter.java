package org.wildstang.logwriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class LogWriter
{

	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		Socket kkSocket = new Socket("roboRIO-111.local", 1111);
	    
	    InputStream yourInputStream = kkSocket.getInputStream();
	    ObjectInputStream mapInputStream = new ObjectInputStream(yourInputStream);
	    Map <String, Object> hm = (Map) mapInputStream.readObject();
	    
	    mapInputStream.close();
	    
		//kkSocket = new Socket("", 1112);
		
		FileOutputStream fileOut = new FileOutputStream("save.data");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(hm);
		out.close();
		fileOut.close();
	}
}
