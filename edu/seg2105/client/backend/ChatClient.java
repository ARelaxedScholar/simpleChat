// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.backend;

import edu.seg2105.client.common.ChatIF;
import edu.seg2105.ocsf.client.AbstractClient;

import java.io.IOException;

import static java.lang.Integer.parseInt;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 */
public class ChatClient extends AbstractClient
{
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF clientUI;
  String userID;
  private final int MAX_ATTEMPTS_AT_RECONNECTION = 5;
  private final long TIME_TO_WAIT_BETWEEN_ATTEMPTS = 10000; //in ms
  final static String FLAG_FOR_COMMAND = "#";

  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
  
  public ChatClient(String id, String host, int port, ChatIF clientUI)
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
    this.userID = id;
    openConnection();
    sendToServer("#login " + userID);
  }

  
  //Instance methods ************************************************
    
  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
  public void handleMessageFromServer(Object msg) 
  {
    clientUI.display(msg.toString());
  }

  @Override
  protected void connectionClosed(){
    clientUI.display("Connection closed.");


  }

  @Override
  protected void connectionException(Exception exception) {
    super.connectionException(exception);
    // First we inform the user, connection was lost.
    clientUI.display("We have lost connection with the server, we will now attempt reconnection.");
    // Then we attempt to reconnect an arbitrary MAX_ATTEMPTS_AT_RECONNECTION times.
    for (int i = 1; i <= MAX_ATTEMPTS_AT_RECONNECTION; i++){
      try {
        openConnection();
        break;
      } catch (IOException e) {
        //If the connection failed
        try {
          //We wait between failed attempt to not spam the server, and make sure the server
          //actually has time to recover if it's a momentary loss.
          clientUI.display("Attempt " + i + "/" + MAX_ATTEMPTS_AT_RECONNECTION + " resulted in failure");
          Thread.sleep(TIME_TO_WAIT_BETWEEN_ATTEMPTS);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
    }
    //Then we stop trying to connect and inform the user of the loss of connection
    clientUI.display("Connection failed too many times, server is likely down. This process will terminate.");

    //Finally we can quit
    quit();
  }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  public void handleMessageFromClientUI(String message)
  {

      if (message.startsWith(FLAG_FOR_COMMAND)) {
        String command = message.substring(1);
        handleCommand(command);
        return;
      }

    try
    {
      sendToServer(message);
    }
    catch(IOException e)
    {
      clientUI.display
        ("Could not send message to server.  Terminating client.");
      quit();
    }
  }

  public String getUserID(){
    return userID;
  }

  private void handleCommand(String command){
    String[] fullCommandArr = command.split(" ");
    String operation = fullCommandArr[0];

    switch (operation) {
      case "quit":
        quit();
        break;

      case "logoff":
        try {
          closeConnection();
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
        break;
      case "sethost":
        if (isConnected()){
          clientUI.display("You cannot set a host if you're already connected, please disconnect first.");
        }
        try {
          setHost(fullCommandArr[2]);
        } catch (ArrayIndexOutOfBoundsException e){
          clientUI.display("Please make sure to input a port number in this format, #set hostname");
        } catch (Exception e){
          clientUI.display("Something went wrong");
          e.printStackTrace();
        }
        break;
      case "setport":
        if (isConnected()){
          clientUI.display("You cannot set a host if you're already connected, please disconnect first.");
        }
        try {
          setPort(parseInt(fullCommandArr[1]));
        } catch (NumberFormatException e){
          clientUI.display("Please make sure to input a valid port number.");
        } catch (ArrayIndexOutOfBoundsException e){
          clientUI.display("Please make sure to input a port number in this format, #set port-number");
        } catch (Exception e){
          clientUI.display("Something went wrong");
          e.printStackTrace();
        }
        break;
      case "login":
        try {
          //Since we want to let the server handle both cases.
          if (isConnected())
            sendToServer(FLAG_FOR_COMMAND + command);
          openConnection();
          sendToServer(FLAG_FOR_COMMAND + command);
        } catch (IOException e) {
          clientUI.display("Login request couldn't be sent to server.");
        }
        break;
      case "gethost":
        clientUI.display("The current host name in use is : " + getHost());
        break;
      case "getport":
        clientUI.display("The port in is use is: " + getPort());
        break;
      default:
        clientUI.display("This command does not exist.");

    }
  }
  /**
   * This method terminates the client.
   */
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {}
    System.exit(0);
  }
}
//End of ChatClient class
