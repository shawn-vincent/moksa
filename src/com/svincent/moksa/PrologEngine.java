/*
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
 * PrologEngine.java
 *
 */
package com.svincent.moksa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.svincent.util.IndentPrintWriter;
import com.svincent.util.Util;

/**
 * Global context for everything, plus a public interface for users.
 **/
public class PrologEngine extends WamObject {
  /**
   * The Warren's Abstract Machine which does all the Prolog stuff.
   **/
  Wam wam;

  /**
   * The Term factory.
   **/
  PrologFactory factory;

  /** Debug flags inlined for speed and comfort. */
  public boolean trace = false;
  public boolean debug = false;
  public boolean debugUnify = false;

  /** The I/O subsystem. */
  Io io;

  /** A Rule compiler, for dynamically compiling Rules. */
  Prologc.PrologRuleCompiler ruleCompiler;

  /** The package manager to use for managing packages. */
  PrologPackageManager packageManager;

  /** The debug trace log. */
  IndentPrintWriter log;

  /**
   * Make a new PrologEngine.
   **/
  public PrologEngine ()
  {
    wam = new Wam (this);
    factory = new PrologFactory (this);
    io = new Io (this, System.in, System.out);
    ruleCompiler = new Prologc.PrologRuleCompiler (this);
    packageManager = new PrologPackageManager (this);
    log = new IndentPrintWriter (Util.out);

    // XXX ?? where should these go??  Probably in Prolog somewhere.
    installBuiltins ();
    installArithmeticHandlers ();

    // XXX this is a bad place for this.  Think more about Fail.
    wam.Fail = factory.makeContinuation (wam.getRule ("fail/0"), 
                                         PrologTerm.EmptyArray, null);
    wam.Fail.notReclaimable = true;
  }

  /**
   * Retrieve the new object Factory used in this PrologEngine.
   **/
  public PrologFactory getFactory () { return factory; }

  /**
   * Retrieve the current output log.
   **/
  public IndentPrintWriter log () { return log; }

  // -------------------------------------------------------------------------
  // ---- Interface to Prolog Engine. ----------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Asserts a new clause before all clauses of the definition of the
   * given predicate.
   **/
  public void asserta (PrologTerm term) throws PrologException
  { asserta (compileRule (term)); }

  /**
   * Asserts a new clause after all clauses of the definition of the
   * given predicate.
   **/
  public void assertz (PrologTerm term) throws PrologException
  { assertz (compileRule (term)); }

  /**
   * Asserts a new clause before all clauses of the definition of the
   * given predicate.
   **/
  public void asserta (Rule rule) { wam.asserta (rule); }

  /**
   * Asserts a new clause after all clauses of the definition of the
   * given predicate.
   **/
  public void assertz (Rule rule) { wam.assertz (rule); }

  /**
   * Utility method from outside.
   *
   * solves 'goal':
   * <ul>
   *    <li>On success, returns array of bound variables from goal</li>
   *    <li>On failure, returns null</li>
   * </ul>
   **/
  public Variable[] solve (CompoundTerm goal) throws PrologException
  {
    boolean success = invoke (goal);
    if (success)
      {
        Collection<Variable> c = VariableCollector.getVariables (goal);
        return (Variable[])c.toArray (new Variable[c.size ()]);
      }
    else
      return null;
  }

  /**
   * Utility method from outside.
   *
   * solves 'goal':
   * <ul>
   *    <li>On success, returns true</li>
   *    <li>On failure, returns false</li>
   * </ul>
   **/
  public boolean invoke (CompoundTerm goal) throws PrologException
  { return wam.invoke (goal); }

  // -------------------------------------------------------------------------
  // ---- Install builtin rules ----------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Install all the builtin rules.</p>
   *
   * <p>There are big issues with this, particularly w.r.t. memory
   * consumption.  Rather, I should load things dynamically (maybe
   * have a lookup table of modules that can be loaded by function
   * name or something...)</p>
   *
   * <p>XXX when we do modules, there can be a dynamic module called
   * 'java', which allows builtin classes to be dynamically loaded.
   * Much nicer. </p>
   **/
  public void installBuiltins ()
  {
    asserta (new Builtin.Call_1 ());
    asserta (new Builtin.Fail_0 ());
    asserta (new Builtin.Cut_1 ());
    asserta (new Builtin.Halt_0 ());

    asserta (new Builtin.Catch_3 ());
    asserta (new Builtin.Throw_1 ());

    asserta (new Builtin.Unify_2 ());

    asserta (new Builtin.Var_1 ());
    asserta (new Builtin.Atom_1 ());
    asserta (new Builtin.Integer_1 ());
    asserta (new Builtin.Real_1 ());
    asserta (new Builtin.Atomic_1 ());
    asserta (new Builtin.Compound_1 ());
    asserta (new Builtin.Nonvar_1 ());
    asserta (new Builtin.Number_1 ());

    asserta (new Builtin.Identical_2 ());

    asserta (new Builtin.Functor_3 ());
    asserta (new Builtin.Arg_3 ());
    asserta (new Builtin.Univ_2 ());
    asserta (new Builtin.Copy_term_2 ());

    asserta (new Builtin.Is_2 ());
    asserta (new Builtin.Equals_2 ());
    asserta (new Builtin.NotEquals_2 ());
    asserta (new Builtin.LessThan_2 ());
    asserta (new Builtin.LessThanEquals_2 ());
    asserta (new Builtin.GreaterThan_2 ());
    asserta (new Builtin.GreaterThanEquals_2 ());

    asserta (new Builtin.Set_prolog_flag_2 ());

    asserta (new Builtin.Set_var_nameXXX_2 ());

    asserta (new JavaInterface.Java_constructor_2 ());
    asserta (new JavaInterface.Java_method_3 ());


    asserta (new Io.Open_4 ());
    asserta (new Io.Close_2 ());
    asserta (new Io.Flush_output_1 ());
    asserta (new Io.Current_input_1 ());
    asserta (new Io.Current_output_1 ());
    asserta (new Io.Put_char_2 ());
    asserta (new Io.Nl_1 ());
    asserta (new Io.Get_char_2 ());
    asserta (new Io.Write_2 ());

    asserta (new Io.Get_prolog_token_2 ());
  }

  // -------------------------------------------------------------------------
  // ---- Arithmetic Handling (Primitive right now) --------------------------
  // -------------------------------------------------------------------------

  Map<String, ArithmeticHandler> arithmeticHandlers = new HashMap<String, ArithmeticHandler> ();

  public void installArithmeticHandlers ()
  {
    addArithmeticHandler (new AddHandler ());
    addArithmeticHandler (new SubtractHandler ());
    addArithmeticHandler (new NegateHandler ());
  }

  public void addArithmeticHandler (ArithmeticHandler ah)
  { arithmeticHandlers.put (ah.getName (), ah); }

  public ArithmeticHandler lookupArithmeticHandler (String name)
  { 
    //log ().println ("looking up arithmetic handler for "+name);
    return (ArithmeticHandler)arithmeticHandlers.get (name); 
  }

  /**
   * 
   **/
  public static abstract class ArithmeticHandler extends WamObject {
    public abstract String getName ();
    public abstract PrologTerm evaluate (PrologTerm[] operands) 
      throws PrologException;
  }

  /**
   * 
   **/
  public class AddHandler extends ArithmeticHandler {
    public String getName () { return "+/2"; }
    public PrologTerm evaluate (PrologTerm[] operands) throws PrologException
    {
      WamInteger x = (WamInteger)operands[0].deref ().evaluateExpression ();
      WamInteger y = (WamInteger)operands[1].deref ().evaluateExpression ();

      return factory.makeInteger (x.intValue () + y.intValue ());
    }
  }

  /**
   * 
   **/
  public class NegateHandler extends ArithmeticHandler {
    public String getName () { return "-/1"; }
    public PrologTerm evaluate (PrologTerm[] operands) throws PrologException
    {
      WamInteger x = (WamInteger)operands[0].deref ().evaluateExpression ();

      return factory.makeInteger (-x.intValue ());
    }
  }

  /**
   * 
   **/
  public class SubtractHandler extends ArithmeticHandler {
    public String getName () { return "-/2"; }
    public PrologTerm evaluate (PrologTerm[] operands) throws PrologException
    {
      WamInteger x = (WamInteger)operands[0].deref ().evaluateExpression ();
      WamInteger y = (WamInteger)operands[1].deref ().evaluateExpression ();

      return factory.makeInteger (x.intValue () - y.intValue ());
    }
  }

  // -------------------------------------------------------------------------
  // ---- Interface to compiler ----------------------------------------------
  // -------------------------------------------------------------------------

  public Rule compileRule (PrologTerm term) throws PrologException
  {
    // XXX maybe save the result somewhere?
    return ruleCompiler.compileRule (term);
  }

  // -------------------------------------------------------------------------
  // ---- Loading Modules ----------------------------------------------------
  // -------------------------------------------------------------------------

  public void loadModule (String moduleName) throws PrologException
  { packageManager.loadModule (this, moduleName); }

}
