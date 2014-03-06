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
 * PrologTermVisitor.java
 *
 */
package com.svincent.moksa;

import com.svincent.util.*;

/**
 * A Visitor pattern implementation for PrologTerms.  This implementation
 * has two slight extensions: first, inheritance is simulated.
 * Second, the methods take an Object parameter and return an Object
 * value. <p>
 **/
public class PrologTermVisitor extends WamObject {

  // -------------------------------------------------------------------------
  // ---- Visit Methods ------------------------------------------------------
  // -------------------------------------------------------------------------

  public Object visitPrologTerm (PrologTerm v, Object parm)
  { return null; }

  public Object visitVariable (Variable v, Object parm)
  { return visitPrologTerm (v, parm); }

  public Object visitCompoundTerm (CompoundTerm v, Object parm)
  { return visitPrologTerm (v, parm); }

  public Object visitWamInteger (WamInteger v, Object parm)
  { return visitPrologTerm (v, parm); }

  public Object visitWamFloat (WamFloat v, Object parm)
  { return visitPrologTerm (v, parm); }

  public Object visitJavaTerm (JavaTerm v, Object parm)
  { return visitPrologTerm (v, parm); }

  // -------------------------------------------------------------------------
  // ---- Utility Methods ----------------------------------------------------
  // -------------------------------------------------------------------------

  public Object visitChildren (CompoundTerm v, Object parm)
  {
    Object retval = null;
    int count = v.getArity ();
    for (int i=0; i<count; i++)
      {
        PrologTerm child = v.getSubterm (i);
        retval = child.accept (this, parm);
      }
    return retval;
  }

  public static class WamVisitorException extends NestableRuntimeException {
	private static final long serialVersionUID = 1L;
	public WamVisitorException () { super (); }
    public WamVisitorException (String msg) { super (msg); }
    public WamVisitorException (String msg, Throwable ex) 
    { super (msg, ex); }
  }
}


