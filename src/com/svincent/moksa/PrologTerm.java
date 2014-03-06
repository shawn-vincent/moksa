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
 * PrologTerm.java
 *
 */
package com.svincent.moksa;

import java.io.*;
import java.util.*;

import com.svincent.util.*;

/**
 * <p>Represents a WAM first-order term (i.e. - a Variable or a
 * CompoundTerm). </p>
 *
 * <p>Every term has one of the following types</p>
 *
 * <ul>
 *
 *   <li><strong>variable</strong> - implemented by the subclass
 *   Variable.</li>
 *
 *   <li><strong>integer</strong> - implemented by the subclass
 *   WamInteger.</li>
 *
 *   <li><strong>floating-point</strong> - implemented by the subclass
 *   WamFloat.<//li>
 *
 *   <li><strong>atom</strong> - implemented by the subclass
 *   CompoundTerm, with arity 0.</li>
 *
 *   <li><strong>compound term</strong> - implemented by the subclass
 *   CompoundTerm, with arity greater than 0.</li>
 *
 * </ul>
 *
 * <p>Note that a term of type <strong>integer</strong>,
 * <strong>floating-point</strong>, or <strong>atom</strong> is a
 * <strong>constant</strong>.  This condition can be tested with the
 * <code>isConstant</code> method.</p>
 **/
public abstract class PrologTerm extends WamObject implements Trailable {

  /** A pointer to our associated Engine. */
  PrologEngine engine;

  /** A static empty array of PrologTerms. */
  public static final PrologTerm[] EmptyArray = new PrologTerm[0];

  // -------------------------------------------------------------------------
  // ---- Constructors -------------------------------------------------------
  // -------------------------------------------------------------------------

  public PrologTerm (PrologEngine _engine) { engine = _engine; }

  // -------------------------------------------------------------------------
  // ---- Accessors ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /* Note: you must typically override both of these, or neither of them.  */
  public int getPriority () { return 0; }
  public void setPriority (int _priority) { Util.assertTrue (_priority == 0); }

  public abstract String getName ();

  /** Retrieve the arity of this term.  All terms other than compound
      terms have an arity of 0.*/
  public int getArity () { return 0; }

  public PrologTerm getNameConstant ()
  { return engine.getFactory ().makeCompoundTerm (getName ()); }

  /**
   * <p>Returns this PrologTerm's integral value.  Only certain
   * prologTerm types can be viewed this way: the others throw
   * exceptions. </p>
   **/
  public int intValue ()
  { 
    throw new PrologRuntimeException 
      ("PrologTerm of type "+getClass ().getName ()+
       " cannot be observed as an integer"); 
  }

  /**
   * <p>Returns this PrologTerm's floating-point value.  Only certain
   * prologTerm types can be viewed this way: the others throw
   * exceptions. </p>
   **/
  public double floatValue ()
  { 
    throw new PrologRuntimeException 
      ("PrologTerm of type "+getClass ().getName ()+
       " cannot be observed as an floating-point value"); 
  }

  /** Allow PrologTermVisitor to access this PrologTerm. */
  public Object accept (PrologTermVisitor v, Object parm) 
  { return v.visitPrologTerm (this, parm); }

  // -------------------------------------------------------------------------
  // ---- Type identification ------------------------------------------------
  // -------------------------------------------------------------------------

  /** Returns true iff this is a Variable. */
  public boolean isVariable () { return false; }

  /** Returns true iff this is a Java object wrapper. */
  public boolean isJavaObject () { return false; }

  /** Return true iff this is a CompoundTerm or an Atom */
  public boolean isStructure () { return false; }

  /** Returns true iff this is a CompoundTerm. */
  public boolean isCompoundTerm () { return false; }

  /** Returns true iff this is a List. */
  public boolean isList () { return false; }
  public boolean isEmptyList () { return false; }
  public boolean isNonEmptyList () { return false; }

  /** Returns true iff this is a integer constant. */
  public boolean isInteger () { return false; }

  /** Returns true iff this is a floating-point constant. */
  public boolean isFloat () { return false; }

  /** Returns true iff this is an Atom. */
  public boolean isAtom () { return false; }

  /** Returns true iff this is an constant: that is, if it is an Atom,
      an integer constant, or a floating-point constant. */
  public boolean isConstant () { return false; }

  /** Returns true iff this is a Stream Identifier. */
  public boolean isStreamId () { return false; }

  // -------------------------------------------------------------------------
  // ---- Implementation -----------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Dereferences this PrologTerm, retrieving the PrologTerm that it
   * represents.  This has no effect except for Variables. <p>
   *
   * Unbound variables and all other PrologTerms return themselves. <p>
   **/
  public PrologTerm deref () { return this; }

  /**
   * Unification algorithm.  Can cause variables to become
   * bound. (There is a rollback mechanism to undo these bindings.)
   * <p>
   *
   * @return true iff the two PrologTerms successfully unify.
   **/
  public boolean unify (PrologTerm that)
  {
    if (engine.debugUnify)
      {
        engine.log ().indent ("    ");
        engine.log ().println ("Unifying "+this.deref ().tag ()+" and "+
                            that.deref ().tag ());
        engine.log ().indent ("  ");
      }

    boolean retval;
    if (unify (that, true))
      {
        if (engine.debugUnify)
          engine.log ().println ("Unify succeeded.");
        retval = true; 
      }
    else
      { 
        retval = false; 
      }

    if (engine.debugUnify)
      {
        engine.log ().outdent ();
        engine.log ().outdent ();
      }

    return retval;
  }

  public boolean unifyWithoutBindings (PrologTerm that)
  { return unify (that, false); }

  /**
   * Unification algorithm.
   *
   * @param doBindings Pass in true if you want matched variables to
   * be bound.
   *
   * @return true iff the two PrologTerms successfully unify.
   **/
  public abstract boolean unify (PrologTerm that, boolean doBindings);

  public PrologTerm clonePrologTerm () 
  { return clonePrologTerm (new HashMap<PrologTerm,PrologTerm> ()); }

  public abstract PrologTerm clonePrologTerm (Map<PrologTerm,PrologTerm> objs);

  /** An unfortunate hack because the parser's gross.  <sigh>... */
  PrologTerm uniqueVariables ()
  { return uniqueVariables (new HashMap<String,Variable> ()); }

  /** An unfortunate hack because the parser's gross.  <sigh>... */
  PrologTerm uniqueVariables (Map<String,Variable> vars) { return this; }

  /**
   * Evaluates this term as an expression.
   **/
  public PrologTerm evaluateExpression () throws PrologException
  { 
    throw new PrologException 
      ("Can't evaluate term of type "+getClass ().getName ()+
       " as an expression"); 
  }


  // -------------------------------------------------------------------------
  // ---- Backtracking behavior ----------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Default behavior for untrailing is to do nothing.</p>
   *
   * <p>Note that since most PrologTerms are never placed on the trail
   * stack, this method will not be called for many PrologTerm types
   * (CompoundTerms and Numbers come to mind)</p>
   **/
  public void untrail ()
  {}

  // -------------------------------------------------------------------------
  // ---- Print Behavior -----------------------------------------------------
  // -------------------------------------------------------------------------

  public void printVariables (PrintWriter out)
  {
    Collection<?> c = VariableCollector.getVariables (this);
    Iterator<?> i = c.iterator ();
    while (i.hasNext ())
      {
        Variable v = (Variable)i.next ();
        out.println (v.getName ()+" = "+v.getValue ().deref ().tag ());
      }
  }
}
