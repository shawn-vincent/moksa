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
 * WamInteger.java
 *
 */
package com.svincent.moksa;

import java.io.PrintWriter;
import java.util.Map;

/**
 * <p>A WamInteger is a Prolog constant for an integral number.</p>
 **/
public class WamInteger extends PrologTerm {

  /** The integer's value. */
  int value;

  // -------------------------------------------------------------------------
  // ---- Constructors -------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Builds a new WamInteger with the given name.  Note that WamIntegers
   * are intended to be used as constants, so no building API is
   * provided. <p>
   **/
  public WamInteger (PrologEngine _engine, int _value)
  {
    super (_engine);
    value = _value;
  }

  // -------------------------------------------------------------------------
  // ---- Accessors ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /** Retrieves this WamInteger's name. */
  public String getName () { return String.valueOf (intValue ()); }

  /** Retrieves this WamInteger's name as a term. */
  public PrologTerm getNameConstant () { return this; }

  /** Returns true iff this is a WamInteger. */
  public boolean isInteger () { return true; }

  /** Returns true iff this is a constant. */
  public boolean isConstant () { return true; }

  /** Returns this Integer's integral value. */
  public int intValue () { return value; }

  /** Allow PrologTermVisitor to access this PrologTerm. */
  public Object accept (PrologTermVisitor v, Object parm)
  { return v.visitWamInteger (this, parm); }

  // -------------------------------------------------------------------------
  // ---- Implementation -----------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Unification algorithm.  Can cause variables to become bound. (We
   * will eventually have a rollback mechanism to undo these
   * bindings.) <p>
   *
   * @return true iff the two PrologTerms successfully unify.
   **/
  public boolean unify (PrologTerm _that, boolean doBindings) 
  {
    if (engine.debugUnify)
      engine.log ().println ("Unifying "+this.tag ()+" and "+_that.tag ());

    // --- variables are easy to deal with.
    if (_that.isVariable ())
      return _that.unify (this, doBindings);

    if (!_that.isInteger ())
      {
        if (engine.debugUnify)
          engine.log ().println ("Got attempt to unify integer with "+
                              _that.getClass ().getName ());
        return false;
      }

    //Integer that = (Integer)_that;
    if (this.intValue () == _that.intValue ()) return true;

    if (engine.debugUnify)
      engine.log ().println ("Nonequal values");
    return false;
  }
  
  public PrologTerm clonePrologTerm (Map<PrologTerm,PrologTerm> objs)
  {
    /** XXX is this ok?  Should be: there is never any reason to want
        a different immutable integer. */
    return this;
  }

  /**
   * Evaluates this term as an expression.
   **/
  public PrologTerm evaluateExpression () throws PrologException
  { return this; }

  public void tag (PrintWriter out)
  {
    out.write (String.valueOf (intValue ()));
  }
}
