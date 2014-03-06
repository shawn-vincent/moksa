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
 * Variable.java
 *
 */
package com.svincent.moksa;

import java.io.*;
import java.util.*;

import com.svincent.util.*;

/**
 * Represents a Variable: a WAM term that contains (possibly) a
 * reference to another WAM term. <p>
 **/
public class Variable extends PrologTerm {

  Wam wam;

  /** Timestamp for this variable. */
  long timestamp;

  /** Reference to Variable's name. **/
  String name;

  /** Reference to Variable's value. **/
  PrologTerm value;

  // -------------------------------------------------------------------------
  // ---- Constructors -------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Constructs a new Variable with the given name.  Note that by
   * default, this Variable's value will not be null.  Rather, it
   * points to itself, by WAM convention. <p>
   *
   * (There are some good consequences that come out of this slightly
   * odd convention) <p>
   **/
  public Variable (PrologEngine _engine, String _name)
  {
    super (_engine);

    wam = engine.wam;
    Util.assertTrue (wam != null, "WAM is null in Variable constructor!");

    name = _name;

    timestamp = wam.getTimestamp ();

    // --- by WAM convention, variables are self-referential (see
    //   - Ait-Kaci, Chapter 2, page 6)
    setValue(this);
  }

  // -------------------------------------------------------------------------
  // ---- Accessors ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /** Retrieves this Variable's name. */
  public String getName () { return name; }

  /** Sets this Variable's value. */
  private void setValue (PrologTerm _value) { value = _value; }

  /** Retrieves this Variable's value. */
  public PrologTerm getValue () { return value; }

  /** Returns true iff this is a Variable. */
  public boolean isVariable () { return true; }

  /** Allow PrologTermVisitor to access this PrologTerm. */
  public Object accept (PrologTermVisitor v, Object parm) 
  { return v.visitVariable (this, parm); }

  // -------------------------------------------------------------------------
  // ---- Implementation -----------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Dereferences this PrologTerm, retrieving the PrologTerm that it represents.
   * This has no effect except for Variables. <p>
   *
   * Unbound variables and all other PrologTerms return themselves. <p>
   **/
  public PrologTerm deref () 
  { 
    if (getValue() == this) return this;
    return getValue().deref ();
  }


  /**
   * Binds this variable to the given new value. <p>
   *
   * @return true iff the binding was successful.
   **/
  public void bind (PrologTerm newValue)
  {
    Variable toBind;
    PrologTerm useValue;

    if (engine.debug)
      engine.log ().println ("Binding "+this.tag ()+" to "+newValue.tag ());

    // --- sort binding by time.
    if (newValue.isVariable ())
      {
        // --- always bind variables created later in time to
        //   - variables created earlier.
        Variable that = (Variable)newValue;
        if (this.timestamp >= that.timestamp)
          {
            toBind = this;
            useValue = that;
          }
        else
          {
            toBind = that;
            useValue = this;
          }
      }
    else
      {
        toBind = this;
        useValue = newValue;
      }

    // --- don't re-bind variables.
    if (toBind.getValue () != toBind)
      Util.assertTrue (false, "Attempt to re-bind variable "+toBind.tag ());

    // --- actually set the value.
    toBind.setValue(useValue);

    // --- only trail if we're a 'permanent' value: i.e. we are going
    //   - to exist when the current choicepoint is all done.
    if (toBind.timestamp < wam.getLastChoicepoint ().getTimestamp ())
      wam.trail (this);
    /*
    else
      if (engine.debug)
        engine.log ().println 
          ("Not trailing "+toBind.tag ()+
           " var time == "+toBind.timestamp+
           ", choicepoint time == "+wam.getLastChoicepoint ().getTimestamp ());
           */
  }

  /**
   * Unification algorithm.  Can cause variables to become bound. (We
   * will eventually have a rollback mechanism to undo these
   * bindings.) <p>
   *
   * @return true iff the two PrologTerms successfully unify.
   **/
  public boolean unify (PrologTerm that, boolean doBindings)
  { 
    if (engine.debugUnify)
      engine.log ().println ("Unify: Binding "+this.getName ()+
                             " = "+that.tag ());
    if (doBindings) bind (that);
    
    return true;
  }

  public PrologTerm clonePrologTerm (Map<PrologTerm, PrologTerm> objs)
  {
    PrologTerm retval = (PrologTerm)objs.get (this);
    if (retval == null)
      {
//          Util.out.println ("Cloning "+this.tag ()+"@"+
//                            System.identityHashCode (this));
//          dumpPrologTerms (Util.out, objs);
        retval = engine.factory.makeTemporaryVariable (getName ());
        objs.put (this, retval);
      }
    return retval;
  }

  PrologTerm uniqueVariables (Map<String,Variable> vars)
  {
    PrologTerm deref = deref ();
    Variable v = (deref instanceof Variable) ? (Variable)deref : this;

    String name = v.getName ();
//  Util.out.println ("Uniquing variable "+name+
//                    " (this == "+this.tag ()+", deref == "+deref.tag ()+")");
    if (name != null)
      {
        Variable unique = (Variable)vars.get (name);
        if (unique == null) 
          {
            unique = v;
            vars.put (name, unique);
          }
        return unique;
      }
    return v.clonePrologTerm (); 
  }

  void dumpPrologTerms (PrintWriter out, Map<?, ?> prologTerms)
  {
    Iterator<?> i = prologTerms.keySet ().iterator ();
    while (i.hasNext ())
      {
        PrologTerm a = (PrologTerm)i.next ();
        a.tag (out);
        out.print ("@"+System.identityHashCode (a));
        out.print (" = ");
        ((PrologTerm)prologTerms.get (a)).tag (out);
        out.println ();
      }
  }

  // -------------------------------------------------------------------------
  // ---- Backtracking behavior ----------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Unbinds the Variable</p>
   **/
  public void untrail () 
  {
	/*
    if (engine.debug) 
     engine.log ().println ("Unbinding variable "+tag ());
     */
    setValue(this); 
  }

  // -------------------------------------------------------------------------
  // ---- Print Behavior ----------------------------------------------------- 
  // -------------------------------------------------------------------------

  /**
   * Writes a short human-readable representation of this object to
   * the given Writer. <p>
   **/
  protected void tag (PrintWriter out)
  {

    String name = getName ();
    if (name == null)
      out.write ("_");
    else
      out.write (name);

//      out.write ('@');
//      out.write (Integer.toHexString (System.identityHashCode (this)));
    
//      PrologTerm value = deref ();
//      if (value != this)
//        {
//          out.write ("[");
//          value.tag (out);
//          out.write ("]");
//        }
  }
}
