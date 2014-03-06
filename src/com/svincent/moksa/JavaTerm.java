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
 * JavaTerm.java
 *
 */
package com.svincent.moksa;

import java.io.*;
import java.util.*;

/**
 * A subclass of PrologTerm which contains a reference to a Java object.
 *
 * @see JavaInterface
 **/
public class JavaTerm extends PrologTerm {

  Object o;

  public JavaTerm (PrologEngine _engine, Object _o) 
  { super (_engine); o = _o; }

  public boolean isJavaObject () { return true; }

  public Object getObject () { return o; }

  public PrologTerm clonePrologTerm (Map<PrologTerm,PrologTerm> objs)
  {
    PrologTerm retval = (PrologTerm)objs.get (this);
    if (retval == null)
      {
        retval = engine.factory.wrapObject (o);
        objs.put (this, retval);
      }
    return retval; 
  }

  public String getName () 
  { 
    if (o == null)
      return "null";
    else
      return 
        o.getClass ().getName ()+
        "@"+
        Integer.toHexString (System.identityHashCode (o)); 
  }

  /** Allow PrologTermVisitor to access this PrologTerm. */
  public Object accept (PrologTermVisitor v, Object parm)
  { return v.visitJavaTerm (this, parm); }

  public boolean unify (PrologTerm _that, boolean bindVars)
  {
    if (_that.isVariable ()) return _that.unify (this, bindVars);

    if (engine.debug)
      engine.log ().println ("Unifying "+this.tag ()+" and "+_that.tag ());
    
    if (!_that.isJavaObject ()) return false;

    JavaTerm that = (JavaTerm)_that;

    return this.getObject () == that.getObject ();
  }

  public void tag (PrintWriter out) 
  { 
    if (o instanceof String)
      {
        out.write ('"');
        out.write ((String)o);
        out.write ('"');
        out.write ('@');
        out.write (Integer.toHexString (System.identityHashCode (o)));
      }
    else
      out.write (getName ()); 
  }
}

