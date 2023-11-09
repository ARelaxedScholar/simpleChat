package edu.seg2105.edu.server.backend;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 


import edu.seg2105.client.common.ChatIF;
import edu.seg2105.ocsf.server.AbstractServer;
import edu.seg2105.ocsf.server.ConnectionToClient;

import java.io.IOException;

import static java.lang.Integer.parseInt;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 */
public class EchoServer extends AbstractServer {
  //Class variables *************************************************
  ChatIF serverUI;
  //!_! is a message from admin flag. Used to allow special handling on the client side.
  final static String FLAG_FOR_MESSAGE_FROM_ADMIN = "!_!";
  final static String FLAG_FOR_COMMAND = "#";


  //Constructors ****************************************************

  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port, ChatIF serverUI) {
    super(port);
    this.serverUI = serverUI;
    try {
      listen();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  //Instance methods ************************************************

  /**
   * This method handles any messages received from the client.
   *
   * @param msg    The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient
  (Object msg, ConnectionToClient client) {
    System.out.println("Message received: " + msg + " from " + client.getInfo("userID"));
    if (msg.toString().startsWith(FLAG_FOR_COMMAND)) {
      String command = msg.toString().substring(FLAG_FOR_COMMAND.length());
      handleLoginRequest(command, client);
      return;
    }
    this.sendToAllClients(client.getInfo("userID") + "> "+ msg.toString());
  }


  public void handleMessageFromServerUI(String message) {
    if (message.startsWith(FLAG_FOR_COMMAND)) {
      String command = message.substring(1);
      handleCommandFromServerUI(command);
      return;
    }

    message = FLAG_FOR_MESSAGE_FROM_ADMIN+message;
    serverUI.display(message.substring(FLAG_FOR_MESSAGE_FROM_ADMIN.length()));
    sendToAllClients(message);
  }
  private void handleLoginRequest(String loginRequest, ConnectionToClient client){
    String[] loginRequestArr = loginRequest.split(" ");
    boolean invalidLoginRequest = false;
    //Basic sanity check
    if (client.getInfo("authenticated") != null){
      try {
        //
        client.sendToClient("You attempted to login as you were already authenticated. Your connection will terminate.");
        client.close();
        invalidLoginRequest = true;
      } catch (IOException e) {
        serverUI.display("Attempt at closing connection to client failed.");
      }
    }
    else if (loginRequestArr.length != 2){
      try {
        //
        client.sendToClient("Your login request does not follow the format \"#login userID\", hence you cannot be logged in. Your connection will terminate.");
        client.close();
        invalidLoginRequest = true;
      } catch (IOException e) {
        serverUI.display("Attempt at closing connection to client failed.");
      }
    }
    else if (!loginRequestArr[0].equals("login")){
      try {
        //
        client.sendToClient("The command you passed was not found. Your connection will terminate.");
        client.close();
        invalidLoginRequest = true;
      } catch (IOException e) {
        serverUI.display("Attempt at closing connection to client failed.");
      }
    }

    //Then

    if (invalidLoginRequest){
      return;
    }
    client.setInfo("userID", loginRequestArr[1]);
    client.setInfo("authenticated", true);
    try {
      client.sendToClient("SERVER MSG> You were successfully logged in. (only you can see this)");
      System.out.println("<" + client.getInfo("userID") + ">" +" has logged in.");
    } catch (IOException e) {
      serverUI.display("Attempt at sending status update to client failed.");
    }
  }
  private void handleCommandFromServerUI(String command) {
    String[] fullCommandArr = command.split(" ");
    String operation = fullCommandArr[0];

    switch (operation) {
      case "quit":
        sendToAllClients(FLAG_FOR_MESSAGE_FROM_ADMIN+"The server will terminate shortly.");
        serverUI.display("Server will terminate shortly. (Clients have been informed)");
        try {
          close();
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(-1);
        }
        System.exit(0);
        break; //useless but ...

      case "stop":
        stopListening();
        break;
      case "close":
        try {
          close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        break;
      case "setport":
        if (isListening()) {
          System.out.println("You cannot change the port while the server is listening.");
        }
        try {
          setPort(parseInt(fullCommandArr[1]));
          System.out.println("Port number was updated.");
        } catch (NumberFormatException e) {
          System.out.println("Please make sure to input a valid port number.");
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println("Please make sure to input a port number in this format, #set port-number");
        } catch (Exception e) {
          System.out.println("Something went wrong");
          e.printStackTrace();
        }
        break;
      case "start":
        if (isListening()) {
          System.out.println("The server is already waiting for connections.");
        }
        try {
          listen();
          serverUI.display("The server is now listening for connections.");
        } catch (IOException e) {
          System.out.println("Initialization of the server failed, please try again.");
        }
        break;
      case "getport":
        System.out.println("The port in is use is: " + getPort());
        break;
      default:
        serverUI.display("This command does not exist.");

    }
  }


  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted() {
    System.out.println("Server listening for connections on port " + getPort());
  }

  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped() {
    System.out.println("Server has stopped listening for connections.");
  }

  public static String getFlagForMessageFromAdmin(){
    return FLAG_FOR_MESSAGE_FROM_ADMIN;
  }

  @Override
  protected void clientConnected(ConnectionToClient client) {
    System.out.println("<" + client.getInfo("userID") + ">" +  " connected");
  }

  @Override
  synchronized protected void clientDisconnected(ConnectionToClient client) {
    System.out.println("<" + client.getInfo("userID") + ">" + " has disconnected");
  }


}
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */

//End of EchoServer class
