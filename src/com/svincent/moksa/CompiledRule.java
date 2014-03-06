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
 * CompiledRule.java
 *
 * Base class for all rules that have been compiled into Java code
 */
package com.svincent.moksa;

import com.svincent.util.*;

/**
 * <p>Provides a superclass for compiled rules. </p>
 **/
public abstract class CompiledRule extends Rule {
  PrologTerm cachedTerm;
  public abstract String getName ();
  public abstract int getArity ();

  public PrologTerm getTerm (PrologFactory factory)
  { 
    try {
      if (cachedTerm == null) cachedTerm = makeTerm (factory); 
      return cachedTerm; 
    } catch (PrologException ex) {
      Util.assertTrue (false, "XXX fix me.");
      return null;
    }
  }

  public abstract Continuation invokeRule (Wam wam) throws PrologException;

  public abstract PrologTerm makeTerm (PrologFactory factory) 
    throws PrologException;
}
