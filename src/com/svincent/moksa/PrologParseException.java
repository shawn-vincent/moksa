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
 * PrologParseException.java
 *
 */
package com.svincent.moksa;

/**
 * Thrown when an exception occurs whilst parsing Prolog source.
 **/
public class PrologParseException extends PrologException {
  private static final long serialVersionUID = 1L;
  public PrologParseException () { super (); }
  public PrologParseException (String msg) { super (msg); }
  public PrologParseException (String msg, Throwable ex) { super (msg, ex); }
}

