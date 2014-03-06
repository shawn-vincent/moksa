/*
 * 
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
 * PrologException.java
 *
 */
package com.svincent.moksa;

import java.io.*;

import com.svincent.util.*;

/**
 * Superclass of all exceptions thrown from the WAM.
 **/
public class PrologException extends NestableException {
  private static final long serialVersionUID = 1L;

  public PrologException () { super (); }
  public PrologException (String msg) { super (msg); }
  public PrologException (String msg, Throwable ex) { super (msg, ex); }

  public void printStackTrace (PrintStream out)
  { this.printStackTrace (new PrintWriter (out, true)); }

  public static class UnhandledPrologException extends PrologException
  {
	private static final long serialVersionUID = 1L;

    PrologTerm exception;

    public UnhandledPrologException (PrologTerm _exception) 
    { super (); exception = _exception; }

    public String getMessage () { return exception.tag (); }
    public PrologTerm getPrologException () { return exception; }

    public void printStackTrace (PrintWriter out)
    {
      super.printStackTrace (out);
      printJavaException (out);
    }

    public void printJavaException (PrintWriter out)
    {
      if (!exception.isCompoundTerm ()) return;

      PrologTerm firstArg = ((CompoundTerm)exception).getSubterm (0);

      Util.out.println ("First arg == "+firstArg);

      if (firstArg.isJavaObject () &&
          ((JavaTerm)firstArg).getObject () instanceof Throwable)
        {
          out.println ("--- Java Exception ---");
          Throwable ex = (Throwable)((JavaTerm)firstArg).getObject ();
          ex.printStackTrace (out);
        }
    }
  }
}
