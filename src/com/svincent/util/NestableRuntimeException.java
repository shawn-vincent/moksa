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
 * NestableRuntimeException.java 
 * 
 */

package com.svincent.util;

import java.io.PrintWriter;
import java.io.PrintStream;

/**
 * A runtime version of NestableException.
 * @see NestableException
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class NestableRuntimeException extends RuntimeException {
  Throwable nested = null;

  public NestableRuntimeException () { super (); }
  public NestableRuntimeException (String msg) { super (msg); }
  public NestableRuntimeException (String msg, Throwable ex) 
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
	out.println ("---- Nested Exception");
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

}
