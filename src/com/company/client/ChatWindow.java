package com.company.client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ChatWindow extends JFrame
{
    Socket socket;
    PrintWriter writer;
    BufferedReader readerFromNet;
    final int PORT = 2355;
    JTextArea areaForHistoryMassage;
    JTextArea areaForOutputMassage;
    JTextArea areaForActiveClients;
    JPanel panelForPass;
    JPanel panelForSend;
    boolean isAuthorized;

    public ChatWindow()
    {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500,600);
        setResizable(false);
        setTitle("My Chat. authorization");

        areaForHistoryMassage = new JTextArea();
        areaForHistoryMassage.setBackground(Color.getHSBColor(169,215,153));
        areaForHistoryMassage.setFont(new Font("serif",Font.BOLD,20 ));
        areaForHistoryMassage.setLineWrap(true);
        areaForHistoryMassage.setEditable(false);
        add(BorderLayout.CENTER , areaForHistoryMassage);
        JScrollPane scrollPane = new JScrollPane(areaForHistoryMassage);
        add(scrollPane);

        areaForActiveClients=new JTextArea();
        areaForActiveClients.setBackground(Color.getHSBColor(169,215,153));
        areaForActiveClients.setLineWrap(true);
        areaForActiveClients.setEditable(false);
        add(BorderLayout.WEST , areaForActiveClients);
        JScrollPane scrollPaneActiveUsers = new JScrollPane(areaForActiveClients);
        add(BorderLayout.EAST, scrollPaneActiveUsers);

        panelForSend = new JPanel();
        panelForSend.setBackground(Color.GRAY);
        getContentPane().add(BorderLayout.SOUTH , panelForSend);

        areaForOutputMassage = new JTextArea(3,25);
        areaForOutputMassage.setLineWrap(true);
        areaForOutputMassage.setFont(new Font("serif",Font.CENTER_BASELINE,15));
        areaForOutputMassage.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e)
            {
                if( e.getKeyCode() == KeyEvent.VK_ENTER )
                {
                    String strForSend=areaForOutputMassage.getText();
                    writer.println(strForSend);
                    writer.flush();
                    areaForOutputMassage.setText("");
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        panelForSend.add(BorderLayout.WEST, areaForOutputMassage);

        JScrollPane scrollPane1 = new JScrollPane(areaForOutputMassage);
        panelForSend.add(scrollPane1);

        JButton button = new JButton();
        button.setFont(new Font("serif",Font.BOLD,16 ));
        button.setText("Send");
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String strForSend=areaForOutputMassage.getText();
                writer.println(strForSend);
                writer.flush();
                areaForOutputMassage.setText("");
            }
        });
        panelForSend.add( BorderLayout.EAST,button);
        panelForSend.setVisible(false);
        ///authorization panel
        panelForPass = new JPanel();
        getContentPane().add(BorderLayout.NORTH , panelForPass);

        JTextField fieldLogin = new JTextField(12);
        panelForPass.add(BorderLayout.WEST,fieldLogin);

        JTextField fieldPass = new JTextField(12);
        panelForPass.add(BorderLayout.CENTER,fieldPass);

        JButton buttonLoginPas = new JButton();

        buttonLoginPas.setFont(new Font("serif",Font.BOLD,16 ));
        buttonLoginPas.setText("CheckLogin");
        buttonLoginPas.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String strForSend="/auth "+fieldLogin.getText()+" "+fieldPass.getText();

                writer.println(strForSend);
                writer.flush();
                fieldLogin.setText("");
                fieldPass.setText("");
            }
        });
        panelForPass.add(BorderLayout.EAST,buttonLoginPas);
        addWindowListener(new WindowAdapter()         
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
                try{
                    writer.print("/end");
                    writer.flush();
                    socket.close();
                }catch(IOException e1)
                {
                    e1.printStackTrace();
                    setClientAuthorized(false);
                }
            }
        });
        this.start();

        setVisible(true);
    }

    void start()
    {
        try
        {
            socket = new Socket("localhost",PORT);
            System.out.println("We connected");
            readerFromNet = new BufferedReader(new InputStreamReader(socket.getInputStream()) );
            writer = new PrintWriter(socket.getOutputStream());

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run()
                {
                    try
                    {
                        while (true)
                        {
                            if (readerFromNet.ready())
                            {
                                String inputStr = readerFromNet.readLine();
                                if (inputStr.equals("/authok"))
                                {
                                    setClientAuthorized(true);
                                    break;
                                }
                            }
                        }
                        while (true)
                        {
                            if (readerFromNet.ready())
                            {
                                String inputStr = readerFromNet.readLine();
                                System.out.println(inputStr);

                                if(inputStr.startsWith("/"))
                                {
                                    if(inputStr.startsWith("/activClients"))
                                    {
                                        String[] activClients = inputStr.split(" ");
                                        areaForActiveClients.setText("");
                                        for(int i=1; i<activClients.length ; i++ )
                                            areaForActiveClients.append(activClients[i]+"\n");
                                    }
                                    if(inputStr.startsWith("/name"))
                                    {
                                        String str=inputStr.substring(6);
                                        setTitle("My Chat. "+str);
                                    }
                                }
                                else
                                {
                                    areaForHistoryMassage.setText(areaForHistoryMassage.getText() + "\n" + inputStr);
                                }

                            }
                        }
                    }
                    catch (IOException e)
                    {
                        System.out.println(e.getStackTrace());
                    }
                }
            });
            thread.start();
        }
        catch(IOException e)
        {
            System.out.println("Connect wrong");
        }
    }
    void setClientAuthorized(boolean authorized)
    {
        isAuthorized=authorized;
        panelForPass.setVisible(!isAuthorized);
        panelForSend.setVisible(authorized);
    }   
}
