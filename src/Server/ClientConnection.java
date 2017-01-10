package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Runnable Class to handle individual Client connection and requests. Able to List files from local directory and send individual files to the client  
 * @author Martin Repicky
 *
 */

public class ClientConnection implements Runnable{
	private Socket clientSocket;
    private BufferedReader inComing = null;
    private PrintStream cMessage;
    private static volatile boolean stop = false;
    private static BlockingQueue<String> servLog = new ArrayBlockingQueue<String>(10);
	private String clientIp;
    private String path;
    private static ArrayList<String> directoryList;
    
    public ClientConnection(Socket client, BlockingQueue<String> connections, String path) {
        this.clientSocket = client; 
    	ClientConnection.servLog = connections;
        this.path = path;
    }

    /**
     * Runnable Method (main for Thread) to deal with clients requests. 3 commands "dir" , "getFile" , "#QuIt#" 
     * 
     */
    
    @Override
    public void run() {
    	String clientSelection, outGoingFileName="";
    	clientIp = clientSocket.getRemoteSocketAddress().toString();
    	try {
	            while (!stop) 
	            {
	            	inComing = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	            	cMessage = new PrintStream(clientSocket.getOutputStream());
	            	clientSelection = inComing.readLine();
	                switch (clientSelection) 
	                {
		                case "dir":
		                	directoryList = ls();
		                	if(!directoryList.isEmpty())
		                	{
		                		cMessage.println("dir");
		                		sendList();
		                	}
		                	else cMessage.print("NoFilesFound");
		                	break;
		                case "getFile":	//Client signals it is ready to receive file                        
		                	cMessage.println("Getting file");
		                	outGoingFileName = inComing.readLine();
		                	sendFile(outGoingFileName);
		                	break;
						case "#QuIt#":
							servLog.add("Session terminated");
							return;
						default:
						    break;
	                	}
	            }
    		} catch (IOException ex) {
            servLog.add("[WARNING] Client: "+clientIp+" "+ex.toString());
        }
    }
    
    /**
     * Method to send the list of local files to client. List is populated by method ls().
     */
    
    public void sendList()
    {
    	servLog.add(clientIp+" requested list of files");
    	try {
			PrintStream cMessage = new PrintStream(clientSocket.getOutputStream());
			for(String dFile: directoryList) cMessage.print(dFile+System.getProperty("line.separator"));
			cMessage.println("#EnDoFlIsT#");
		} catch (IOException e) {
			servLog.add("[Error] Client: "+clientIp+" "+e.toString());
		}
    }
    
    /**
     * Method to populate ArrayList with file names from local directory
     * @return ArrayList
     */
    
    public ArrayList<String> ls()
	{
    	ArrayList<String> dirFiles = new ArrayList<String>();
		File[] files = new File(path).listFiles();
		for (File file : files) if (file.isFile()) dirFiles.add(file.getName());
		return dirFiles;
	}
    
    /**
     * Method to send file to client 
     * @param fileName String
     * 
     */
    
    public void sendFile(String fileName) {
        try {
            //handle file read
            File myFile = new File(path,fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);
            //handle file send over socket
            OutputStream os = clientSocket.getOutputStream();
            //Sending file name and file size to the server
            DataOutputStream dos = new DataOutputStream(os);
            cMessage.println(myFile.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            dis.close();
            servLog.add("File: "+fileName+" sent to client: "+clientIp);
        } catch (Exception e) {
            servLog.add("[ERROR] Client: "+clientIp+" requested file: "+fileName +" unsuccesfully");
            cMessage.println("#NoSuChAfIlE#");
        } 
    }
    
    /**
     * Static Method which triggers all running Clients Thread to orderly finish. 
     */
    
    public static void stopSession()
    {
    	stop=true;
    }
}
