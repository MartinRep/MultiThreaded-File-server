package Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerMain
{
	private static ServerSocket serverSocket;
	private static Socket clientSocket = null;
	private static String logFile = "server.log";
	private static BlockingQueue<String> servLog = new ArrayBlockingQueue<String>(10);
	private static ArrayList<Thread> connections = new ArrayList<Thread>();
	private static String POISON = "massMedia";
	private static volatile boolean stop = false;
	private static String path=".";
	static List<String> dirFiles = new ArrayList<String>();
	
	public static void main(String[] args) throws IOException {
		
		//Sets defaults if no parameters given		
		int port=8781;
		//main program API handling
		if(args.length != 0) 
		{
			port = Integer.parseInt(args[0]);
			path = args[1];
		}
		if(path.equals(".")) path = System.getProperty("user.dir");
		else if(path.charAt(0) == '.') path = System.getProperty("user.dir")+path;
		path.replaceAll("\\u0020", "%20");
		System.out.println("Setting up working directory: "+path);
		//Log file path setting up
		System.out.println("Configuring Server to port "+port+"...");
		try {
				serverSocket = new ServerSocket(port);
	            System.out.println("Server started.");
	        } catch (Exception e) {
	            System.err.println("Error! Port already in use.");
	            System.exit(1);
	        }
		System.err.println("PRESS ENTER ANYTIME TO SHUTDOWN SERVER");
		 //Starts incoming connection listener
        Thread theListener = new Thread(new Runnable() {
        	public void run() {
					listener();
        	}
        });
        theListener.start();
		//Starts the logger thread (logs to console & file)    
        Thread dLogger = new Thread(new Runnable() {
        	public void run() {
        		try {
					logger();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        });
        dLogger.start();
		//Waits for Return key pressed
		System.in.read(); 
		servLog.add("Shutting down all services, please wait...");
		//Signal to orderly finish listener thread
        stop=true;
        //Stop all the connections threads
        ClientConnection.stopSession();
        //Wait for all the connection threads to finish max wait 1 second per thread
        for(Thread temp: connections)
			try {
				temp.join(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        //Close server socket
        serverSocket.close();
        //Waits for logger and listener threads to finish
        try {
			theListener.join();
	        //Signal via poisonous pill to orderly finish logger thread
	        servLog.add(POISON);
			dLogger.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Thank you for using Fl Server © 2017 by Martin Repicky.");
		System.exit(0);
	    }
	
	public static void listener()
	{
		System.out.println("Accepting connection now...");
		while (!stop) {
        	try {
                clientSocket = serverSocket.accept();
                servLog.put("Accepted connection from " + clientSocket.getRemoteSocketAddress().toString());
                //Creates separate thread for each client
                connections.add(new Thread(new ClientConnection(clientSocket, servLog, path)));
                connections.get(connections.size()-1).start();
	            } catch (Exception e) {
	                if(e.toString().equals("java.net.SocketException: socket closed")) servLog.add("Socket Closed.");
	                else servLog.add("[Error] Connection failed. "+e.toString());
	                
	            }
	        }
		servLog.add("[Warning] New connection Service down.");
	}
	
	public static void logger() throws InterruptedException
	{
		
		String log = new StringBuilder().append("Server Started").append(System.getProperty("line.separator")).append("==============================").toString();		
		 
		//Creates Logger for log file management
		Logger logger = Logger.getLogger("ServerLog");
		 logger.setUseParentHandlers(true);
		    FileHandler fh;  
		    try {  
		        //This block configure the logger with handler and formatter  
		        fh = new FileHandler(path+logFile,true);  
		        logger.addHandler(fh);
		        SimpleFormatter formatter = new SimpleFormatter();
		        fh.setFormatter(formatter);  
		    } catch (SecurityException e) {  
		        e.printStackTrace();  
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    }  
			//Allows to orderly finish thread. Waits for "poison pill" to know when to stop 
			while(!log.equals(POISON))
					{
						logger.info(log);
						log = servLog.take();
					}
			log = "[Warning] Logging service down.";
			logger.warning(log);
	}

}
