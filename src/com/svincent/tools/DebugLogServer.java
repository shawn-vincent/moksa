/*
 * DebugLogServer.java
 */

package com.svincent.tools;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;


/**
 * Debug log server.
 *
 */
class DebugLogServer {
  static final int DEFAULT_DEBUG_PORT = 2000;
  
  void run (int port)
  {
    ServerSocket ss;

    try {
      ss = new ServerSocket (port);
    } catch (IOException x) {
      System.out.println ("Got exception "+x);
      x.printStackTrace (System.out);
      return;
    }

    while (true)
      {
	Socket s;

	// --- Wait for incoming connection
	System.out.println ("Waiting for Connection");
	try {
	  s = ss.accept ();
	} catch (IOException x) {
	  System.out.println ("accept failed : "+x.getMessage ());
	  try {
	    Thread.sleep (1000);
	  } catch (InterruptedException e) {}
	  continue;
	}
	
	for (int i=0; i<100; i++) System.out.println ();
	System.out.println ("Got connection : "+s);

	// --- Copy stream input to stdout until EOF, then close
	try {
	  InputStream is = s.getInputStream ();
	  byte[] inbuf = new byte[4096];
	  int gotbytes;
	  while ((gotbytes = is.read (inbuf)) != -1)
	    {
	      System.out.write (inbuf, 0, gotbytes);
	      System.out.flush ();
	    }
	  s.close ();

	} catch (IOException x) {
	  System.out.println ("Connection terminated ("+x.getMessage ()+").");
	}

	// ---
	System.out.println ("Done.");
      }
  }

  public static int parseInt (String str, int radix, int defaultValue)
  {
    if (str == null) return defaultValue;
    try {
      return Integer.parseInt (str, radix);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static void main (String[] args)
  {
    int debugPort = DEFAULT_DEBUG_PORT;

    for (int i=0; i<args.length; i++)
      {
	if (args[i].equals ("-port") && i+1<args.length)
	  debugPort = parseInt (args[++i], 10, debugPort);
	else
	  System.out.println ("Unknown arg : "+args[i]);
      }

    DebugLogServer srv = new DebugLogServer ();
    srv.run (debugPort);
  }
}


/*
	// --- Send reply
	try {
	  System.out.println ("Replying");
	  byte[] reply = html.getHtml ();
	  s.getOutputStream().write (reply, 0, reply.length);
	} catch (IOException x) {
	  error = x.getMessage ();
	  if (error == null) error = "unknown error";
	}
	
 */  

