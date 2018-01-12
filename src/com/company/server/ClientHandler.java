package com.company.server;

import java.io.*;
import java.net.Socket;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

public class ClientHandler implements Runnable
{
    private Server server = null;
    private Socket socket =null;
    private BufferedReader readerFromNet;
    private PrintWriter writer;
    private String name;
    public ClientHandler (Server server, Socket socket)
    {
        this.server = server;
        this.socket = socket;
        try
        {
            readerFromNet = new BufferedReader(new InputStreamReader(this.socket.getInputStream()) );
            writer = new PrintWriter(this.socket.getOutputStream());
        }
        catch(IOException e)
        {
            System.out.println(e.getStackTrace());
        }
    }
    public void run()
    {
        Thread timeOut = new Thread(new Runnable()  // timeOut for autorization
        {
            @Override
            public void run()
            {
                for(int i = 0;i<120;i++)
                {
                    try
                    {
                        sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        return;
                    }
                }

                System.out.println("Time for autorization is empty ");
                sendMessageToClient ( "Time for autorization is empty" );
                try
                {
                    socket.close();
                }
                catch (IOException e)
                {
                    System.out.println(e.getStackTrace());
                }
            }
        });
        timeOut.start();

        try
        {
            while(true)
            {
                String str = readerFromNet.readLine();
                String[] elements = str.split(" ");

                if(str.startsWith("/auth"))
                {
                    String [] parts = str . split ( " " );
                    String nick = server . getAuthService (). getNickByLoginPass ( parts [ 1 ], parts [ 2 ]);

                    if ( nick != null )
                    {
                        if (! server . isNickBusy ( nick )) {
                            sendMessageToClient("/authok");
                            name = nick ;
                            timeOut.interrupt();
                            server.subscribe ( this );
                            sendMessageToClient("/name "+name);
                            server.sendMessageToAllClients( name + " is connect" );
                            server.sendMessageToAllClients( server.getActiveClients() );

                            break ;
                        }
                        else sendMessageToClient ( "Nick is buzy" );
                    }
                }
                else
                {
                    sendMessageToClient("You need autorized");
                }
            }
            while(true)
            {
                String str = readerFromNet.readLine();
                if(str.startsWith("/"))
                {
                    if(str.equalsIgnoreCase("/end"))
                    {
                        break;
                    }
                    if(str.startsWith("/w"))
                    {
                        String nickAndMessage=str.substring(3);
                        String toNick=nickAndMessage.substring(0,nickAndMessage.indexOf(' '));
                        String message = nickAndMessage.substring(nickAndMessage.indexOf(' ')+1);
                        server.sendMessageToOneClient(message,toNick,name);
                        continue;
                    }
                }
                System.out.println(name+ ": " + str);
                server.sendMessageToAllClients(name + ": "+str);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            server.unsubscribe(this);
            try
            {
                socket.close();
            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    public void sendMessageToClient(String str)
    {
        writer.println(str);
        writer.flush();
    }
    public String getName()
    {    	
        return name;
    }
}
