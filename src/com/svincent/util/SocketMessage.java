package com.svincent.util;

import java.net.*;
import java.io.*;

/**
 * Construct & run one of these puppies.
 * 
 * They send a string to a socket.
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class SocketMessage {

  String host;
  int port;

  public SocketMessage (String _host, int _port)
  {
    host = _host;
    port = _port;
  }

  /**
   * Returns 'true' if successful.
   */
  public boolean send (String msg)
  {
    try {
      Socket s = new Socket (host, port);
      OutputStream os = s.getOutputStream ();

      PrintWriter out = new PrintWriter (new OutputStreamWriter (os));

      Util.out.println (msg);
      out.println (msg);
      out.flush ();

      os.close ();

      return true;

    } catch (IOException e) {
      e.printStackTrace (Util.out);
      return false;
    }
  }

  public static void main (String[] args)
  {
    try {
      SocketMessage out = 
	new SocketMessage (args[0], Integer.parseInt (args[1]));
      for (int i=2; i<args.length; i++)
	if (!out.send (args[i])) break;
    } catch (Throwable t) {
      Util.out.println ("Usage: SocketMessage host port (msg)*");
    }
  }
}
