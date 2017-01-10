package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import org.w3c.dom.*;
import javax.xml.parsers.*;


public class Client {

	private static Socket sock;
	private static BufferedReader inStream;
    private static PrintStream sMessage;
    private static String filePath;
    private static String hostAdress = "127.0.0.1";
    private static int hostSocket = 7777;

    //Setting up Server parsing config.xml file
    public static void init()
    {
    	try{
    		File inputFile = new File("config.xml");
            DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("client-config");
            Node nNode = nList.item(0);
            Element eElement = (Element) nNode;
            
            hostAdress = eElement.getElementsByTagName("server-host").item(0).getTextContent();
            System.out.println("Host: "+eElement.getElementsByTagName("server-host").item(0).getTextContent());
            
            hostSocket = Integer.parseInt(eElement.getElementsByTagName("server-port").item(0).getTextContent());
            System.out.println("Port: "+eElement.getElementsByTagName("server-port").item(0).getTextContent());
            
            filePath = eElement.getElementsByTagName("download-dir").item(0).getTextContent();
            System.out.println("Path: "+eElement.getElementsByTagName("download-dir").item(0).getTextContent());
    	} catch(Exception e) {
    		System.err.println("Error reading config XML file. Using defaults ("+hostAdress+","+hostSocket+")\n"+e.toString());
    		//e.printStackTrace();
    	}finally
    	{
    		System.out.println("Server set to "+hostAdress+":"+hostSocket);
    	}
    }
    
    public static void main(String[] args) throws IOException {
    	int choice;
    	boolean connection = false;
    	
    	init();
    	
        do{
        	@SuppressWarnings("resource")
			Scanner console = new Scanner(System.in);
        	System.out.println("\nMain Menu\n===================================");
        	System.out.println("1.Connect to Server");
        	System.out.println("2.List remote files");
        	System.out.println("3.Download file");
        	System.out.println("4.Exit");
        	System.out.println("\nEnter your choice: ");
        	choice = console.nextInt();
        	switch(choice)
        	{
        	case 1:
        		//Open the connection
            	connection = connect();
        		break;
        	case 2:
        		if(connection) listFiles();
        		else System.err.println("Connect to Server First!");
        		break;
        	case 3:	
        		if(connection) receiveFile();
        		else System.err.println("Connect to Server First!");
        		break;
        	default:
        		break;
        	}
        	
        }while(choice != 4);
        System.out.println("Thank you for using Fl Client © 2017 by Martin Repicky.");
        if(connection)
        {
            sMessage.println("#QuIt#");
            sock.close();
        }
    }

    public static boolean connect()
    {
    	try {
            sock = new Socket(hostAdress, hostSocket);
            inStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            sMessage = new PrintStream(sock.getOutputStream());
            return true;
        } catch (Exception e) {
            System.out.println("Cannot connect to the server, try again later.");
            return false;
        }
    }
    
    public static void listFiles() 
    {
    	String cmMessage;
    	try
    	{
    		
        	//Send request to server
    		sMessage.println("dir");
        	cmMessage = inStream.readLine();
        	//Check for correct response from Server
        	if(!cmMessage.equals("dir")) 
        	{
        		System.err.println("No files to list");
        		return;
        	}
        	//List the files
        	cmMessage = "List of files:\n===================================";
        	//Prints out the file list
        	do{
        		System.out.println(cmMessage);
        		cmMessage = inStream.readLine();
        	} 	while(!cmMessage.equalsIgnoreCase("#EnDoFlIsT#"));
    	} catch(IOException ex) {
    		System.out.println("Error recovering list of files.");
    		ex.printStackTrace();
    	}
    }
    
    public static void receiveFile() {
        String cMessage;
    	try {
            int bytesRead;
            String file, fileName;
            @SuppressWarnings("resource")
			Scanner console = new Scanner(System.in);
            sMessage.println("getFile");
            cMessage = inStream.readLine();
            if(!cMessage.equals("Getting file"))
            {
            	System.err.println("No files to list");
        		return;
            }
            System.out.print("Enter file to download: ");
    		file = console.nextLine();
    		sMessage.println(file);
    		//Income data            
    		InputStream stream = sock.getInputStream();
            DataInputStream cData = new DataInputStream(stream);
            //Check if file exists
            fileName = inStream.readLine();
            if(!file.equals(fileName))
            {
            	System.err.println("[Error] Server says file "+file+" not found!");
            	return;
            }
            //Set filename to include path
            String fullName = new StringBuilder().append(filePath).append(fileName).toString();
            //opens the file
            OutputStream outFile = new FileOutputStream(fullName);
            long size = cData.readLong();
            byte[] buffer = new byte[1024];
            //Writes data to local file
            while (size > 0 && (bytesRead = cData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) 
            {
                outFile.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            //Close connection & save file 
            outFile.close();
            System.out.println("File: "+fileName+" received from Server and saved to local drive.");
        } catch (IOException ex) {
        	System.err.println("[Error] Unexpected error downloading file.");
        	//ex.printStackTrace();
        }
    }

}
