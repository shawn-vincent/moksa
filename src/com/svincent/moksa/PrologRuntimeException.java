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
 * PrologRuntimeException.java
 *
 */
package com.svincent.moksa;

import java.io.*;

import com.svincent.util.*;

/**
 * Superclass of all runtime exceptions thrown from the WAM.
 **/
public class PrologRuntimeException extends NestableRuntimeException {
  private static final long serialVersionUID = 1L;

  public PrologRuntimeException () { super (); }
  public PrologRuntimeException (String msg) { super (msg); }
  public PrologRuntimeException (String msg, Throwable ex) { super (msg, ex); }

  public void printStackTrace (PrintStream out)
  { this.printStackTrace (new PrintWriter (out, true)); }
}
