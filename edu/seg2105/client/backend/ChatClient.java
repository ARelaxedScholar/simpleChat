// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.backend;

import edu.seg2105.client.common.ChatIF;
import edu.seg2105.ocsf.client.AbstractClient;

import java.io.IOException;

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
  private final int MAX_ATTEMPTS_AT_RECONNECTION = 5;
  private final long TIME_TO_WAIT_BETWEEN_ATTEMPTS = 2000; //in ms

  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
  
  public ChatClient(String host, int port, ChatIF clientUI) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
    openConnection();
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
