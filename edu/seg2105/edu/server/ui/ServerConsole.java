package edu.seg2105.edu.server.ui;

import edu.seg2105.client.common.ChatIF;
import edu.seg2105.edu.server.backend.EchoServer;

import java.util.Scanner;

public class ServerConsole implements ChatIF {
    final public static int DEFAULT_PORT = 5555;
    public static int portInUse = 0;
    Scanner fromConsole;
    EchoServer myServerInstance;
    ServerConsole myConsole;

    public ServerConsole(int port)
    {
        myServerInstance= new EchoServer(port, this);
        // Create scanner object to read from console
        fromConsole = new Scanner(System.in);
    }
    public void display(String message)
    {
        System.out.println("SERVER MSG> " + message);
    }

    public void accept()
    {
        try
        {

            String message;

            while(true)
            {
                message = fromConsole.nextLine();
                myServerInstance.handleMessageFromServerUI(message);
            }
        }
        catch (Exception ex)
        {
            System.out.println("Unexpected error while reading from console!");
            ex.printStackTrace();
        }
    }




    public static void main(String[] args)
    {
        portInUse = 0; //Port to listen on
        ServerConsole myConsole;

        try
        {
            portInUse = Integer.parseInt(args[0]); //Get port from command line
        }
        catch(Throwable t)
        {
            portInUse = DEFAULT_PORT; //Set port to 5555
        }
        myConsole = new ServerConsole(portInUse);


        myConsole.accept();
    }
}

