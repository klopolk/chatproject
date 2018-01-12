package com.company.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server
{
    private ServerSocket server=null;
    private Socket soc = null;
    private Vector <ClientHandler> clients ;
    private final int PORT = 2355;
    
    private AuthService authService ;
    public AuthService getAuthService ()
    {
    	return authService ;
    }
    public Server()
    {
        try
        {
            server = new ServerSocket(PORT);
            clients=new Vector<ClientHandler>();
            authService = new BaseAuthService ();
            authService . start ();

            new Thread(()->
            {
                while(true)
	            {
	                try
	                {
	                    System.out.println("Wait server...");
	                    soc = server.accept();
	                    System.out.println("New client connect ");
	                    new Thread( new ClientHandler(this, soc)).start();

	                }
	                catch (IOException e)
	                {
	                    System.out.println(e.getStackTrace());
	                }
	            }         
            }).start();
            BufferedReader readerFromKey = new BufferedReader(new InputStreamReader(System.in));
            while(true)
            {
            	String str= readerFromKey.readLine();
            	sendMessageToAllClients("Server: "+ str);
            }
        }
        catch(IOException e)
        {
            System.out.println("Error server");
        }
        finally
        {
            try
            {
                server.close();
                soc.close();
            }
            catch(IOException e)
            {
                System.out.println(e.getStackTrace());
            }
        }
    }
    public synchronized void subscribe(ClientHandler client)
    {
        clients.add(client);
    }
    public synchronized void unsubscribe(ClientHandler client)
    {
        clients.remove(client);
    }

    public synchronized void sendMessageToAllClients(String str)
    {
        if(clients.size()>0)
        {
            for(ClientHandler client: clients)
            {
                client.sendMessageToClient(str);
            }
        }
        else
        {
            System.out.println("No clients");
        }
    }
    public synchronized void sendMessageToOneClient(String str, String toNick, String fromNick)
    {
        for(ClientHandler client: clients)
        {
            if(client.getName().equals(toNick) || client.getName().equals(fromNick))
            {
                client.sendMessageToClient("Сообщение от "+fromNick+": "+str);
            }
        }
    }

    public synchronized boolean isNickBusy ( String nick )
    {
    	for ( ClientHandler o : clients )
    	{
            if ( o . getName (). equals ( nick ))
    		return true ;
    	}
    	return false ;
    }
    public synchronized String getActiveClients()
    {
        String activClients="";
        for(ClientHandler o : clients)
        {
            activClients = activClients + " "+ o.getName();
        }
        return "/activClients "+activClients;
    }
}
