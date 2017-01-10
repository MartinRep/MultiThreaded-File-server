# **MultiThreaded-File-server**

## *Basic MultiThreaded File Server & Client.*

## Instalation guide:
Server doesn't need any additional files. Just compile:

>Server.java

Run it:

>java -cp oop.jar Server.ServerMain 7777 /

where 7777 is port to run the server and second argument is download folder.

Clientside reads config.xml configuration file on start. If not present or corrupted will sets parameters to default port 7777 and Adress 127.0.0.1

## Tutorial

## Server

Serverside has no User Interface to say as it only runs in console and logs and display logs.
After initial setup it will accept new connection. All notified console. No additional interaction is necessery.

Server can be safely terminated by pressing #RETURN# key, which will initialize shutdown of all connections, listening and logging threads.
Delay timer for thread termination is 1 second. Which should be more then enough.


## Client

ClientSide start by setting up Server address and port from config.xml file. If not pressent, it will set port to 7777 and address to localHost.
Main menu let you connect to Server, which must be done prior to any other operation. List remote files option will display List of remote files available to download.
Download file option will allow you to download specific file. Manually type in full file name and press enter. if succesfull the file will be downloaded and saved to predefined folder.


## Under the hood

File server runs as multiThread, that means, that main method only triggers 2 runnebles (Logger and listener) and waits for ENTER key to trigger shutting down of services.

Listener Thread method runs in infinite loop and listens to socket for new connections. If new connection is triggered new ClientServer class Thread is created and places in thread ArrayList
to deal with specific connection(Client).

Logger Method runs in infinite loop and collects data from Blocking Queue, which deals with multiple inputs(ClientServer classes) and one output (Logger method). Consumer-producers model.
Logger class takes care of file log and console display of logs.


