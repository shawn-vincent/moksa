/*
 * Util: various util libraries, by me, under the GPL.
 * Copyright (C) 1999  Shawn P. Vincent (svincent@svincent.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * -------------------------------------------------------------------------
 *
 * NestableException.java 
 * 
 */

package com.svincent.util;

import java.io.PrintWriter;
import java.io.PrintStream;

/**
 * <p>A NestableException is an exception which can contain a nested 
 * exception.</p>
 *
 * <p>This is generally useful in a very common code idiom, in which
 * some code catches an exception, then re-throws a differently typed
 * exception.</p>
 *
 * For example,
<pre>
     try {
       foo ();
     } catch (SomeRandomException ex) {
       throw new NestableException ("Could not foo()", ex);
     }
</pre>
 * It will be common to wish to subclass NestableException for all
 * your friends and neighbors, as so:
<pre>
     public class WilsonException extends NestableException {
       public WilsonException () { super (); }
       public WilsonException (String msg) { super (msg); }
       public WilsonException (String msg, Throwable ex) { super (msg, ex); }
     }
</pre>
 * 
 * <p>When a stack trace for a NestableException is printed, the stack
 * trace of the nested exception is also printed, seperated by the
 * string "--- Nested Exception ---".  In this way, a stack of stack
 * traces are printed, for every exception to the root cause of the
 * error.  The stack trace dump is terminated by the string 
 * "--- No Nested Exception ---"</p>
 * 
 * An example stack trace follows:
 *
 * <pre>
 * com.engulfco.StartupException: Could not start the system!
 *         at com.engulfco.Starter.startup(Starter.java:129)
 *         at com.engulfco.Starter.main(Starter.java:116)
 * --- Nested Exception ---
 * com.engulfco.ProtocolFailure: Could not read initialization file!
 *         at com.engulfco.Protocol.initialize(Protocol.java:136)
 *         at com.engulfco.Starter.startup(Starter.java:126)
 *         at com.engulfco.Starter.main(NestableException.java:116)
 * --- No Nested Exception ---
 * </pre>
 *
 * <p>In this simple stack trace, you can see that the Starter
 * class was unable to run due to a StartupException.  However, we
 * also discover that the StartupException was caused by a ProtocolFailure!
 * In other exception handling schemes, the choice must be made whether to
 * throw a new exception (which hides the original), or to let the underlying
 * exception through (which mostly gives super-specific information
 * which is also unneccesary).  NestableException gives the best of both
 * worlds.</p>
 *
 * @see NestableRuntimeException
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class NestableException extends Exception {
  Throwable nested = null;

  public NestableException () { super (); }
  public NestableException (String msg) { super (msg); }
  public NestableException (String msg, Throwable ex) 
  { super (msg); nested = ex; }

  public Throwable getNestedException () { return nested; }

  /**
   * A convenience method: returns the stack trace of this
   * exception as a string.
   */
  public String getStackTraceAsString ()
  {
    StringPrintWriter out = new StringPrintWriter ();
    printStackTrace (out);
    return out.toString ();
  }

  /**
   * Print the stack trace of this exception to the given writer.
   */
  public void printStackTrace (PrintWriter out)
  {
    super.printStackTrace (out);
    if (nested != null)
      {
	out.println ("--- Nested Exception ---");
	nested.printStackTrace (out);
      }
    else
      {
	out.println ("--- No Nested Exception ---");
      }
  }

  /**
   * Print the stack trace of this exception to the given PrintStream.
   */
  public void printStackTrace (PrintStream out)
  {
    super.printStackTrace (out);
    if (nested != null)
      {
	out.println ("--- Nested Exception ---");
	nested.printStackTrace (out);
      }
    else
      {
	out.println ("--- No Nested Exception ---");
      }
  }

  // -------------------------------------------------------------------------
  // ---- Test Code ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /** Test code */
  public static void main (String[] args)
  {
    try {
      testFoo ();
    } catch (NestableException ex) {
      ex.printStackTrace (Util.out);
    }
  }
  
  /** Test code */
  protected static void testFoo () throws NestableException
  {
    try {
      testBar ();
    } catch (NestableException ex) {
      throw new NestableException ("Failure in foo!", ex);
    }
  }

  /** Test code */
  protected static void testBar () throws NestableException
  { throw new NestableException ("Failure in bar!!"); }
}
