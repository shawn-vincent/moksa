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
 * Builtin.java
 *
 * Definitions of general builtin Prolog primitives that need to be
 * implemented in Java.
 *
 */
package com.svincent.moksa;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a number of useful primitive predicates.
 **/
public class Builtin {

  /** Do not construct. */
  private Builtin () {}

  /**
   * All of the builtin primitive rules subclass BuiltinRule.
   **/
  public static abstract class BuiltinRule extends Rule {
    PrologTerm cachedTerm;
    
    public PrologTerm getTerm (PrologFactory factory)
    { 
      if (cachedTerm == null) cachedTerm = makeTerm (factory); 
      return cachedTerm;
    }

    public PrologTerm makeTerm (PrologFactory factory)
    { return factory.makeCompoundTerm ("builtin", 
                                       factory.makeAtom (getName ())); }
  }

  /**
   * <p>Calls a goal (7.8.3)</p>
   *
   * <p>XXX when called goal contains !, wierd things happen.  Note
   * spec.</p>
   **/
  public static class Call_1 extends BuiltinRule {
    public String getName () { return "call/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      PrologTerm goal = wam.getRegister (0).deref ();

      if (goal.isVariable ()) 
        return wam.getFactory ().callThrowInstantiationError ();

      if (!goal.isStructure ()) 
        return wam.getFactory ().callThrowTypeError ("callable", goal);

      // --- Note that call doesn't have to actually call the goal
      //   - itself, as that is done in the main loop.

      // --- construct continuation.  
      Continuation continuation =
        Continuation.make (wam.engine, goal, wam.getContinuation ());

      return continuation;
    }
  }

  /**
   * 
   **/
  public static class Cut_1 extends BuiltinRule
  {
    public Cut_1 () {}

    public String getName () { return "!/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException
    { 
      int cutPoint = wam.getRegister (0).deref ().intValue ();
      wam.cut (cutPoint); 
      return wam.getContinuation ();
    }

    public void tag (PrintWriter out) { out.write ("!"); }
  }

  /**
   * <p>Calls a goal with an exception handler.</p>
   **/
  public static class Catch_3 extends BuiltinRule {
    public String getName () { return "catch/3"; }
    public int getArity () { return 3; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get our parameters.
      PrologTerm goal = wam.getRegister (0).deref ();
      PrologTerm exception = wam.getRegister (1).deref ();
      PrologTerm handler = wam.getRegister (2).deref ();
      
      Continuation next = wam.getContinuation ();

      // --- make and trail a new exception handler.
      Wam.ExceptionHandler exceptionHandler = 
        new Wam.ExceptionHandler 
          (wam, goal, exception, 
           Continuation.make (wam.engine, handler, next));
      wam.trail (exceptionHandler);

      // --- now do the same thing as call does.

      // --- Note that call doesn't have to actually call the goal
      //   - itself, as that is done in the main loop.

      // --- construct continuation.  
      Continuation continuation =
        Continuation.make (wam.engine, goal, wam.getContinuation ());

      return continuation;
    }
  }

  /**
   * <p>Throws an exception to the nearest matching exception handler,
   * or halts the machine, if neccessary.</p>
   **/
  public static class Throw_1 extends BuiltinRule {
    public String getName () { return "throw/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      PrologTerm exception = wam.getRegister (0).deref ();

      // keep untrailing until we hit an exception handler or pop off
      // the world, I guess.  Also erase choicepoints as we go.

      // --- when we throw an exception, we drop our continuation on
      //     the floor, unless we want to implement 'resume'.

      Continuation exceptionHandler = wam.findExceptionHandler (exception);

      // --- Is it ok to throw an exception from here?
      // --- Would be nice to have a Prolog stack trace of 
      //   - some sort here, as well.
      if (exceptionHandler == null)
        throw new PrologException.UnhandledPrologException (exception);

      return exceptionHandler;
    }
  }

  /**
   * <p>Always fails</p>
   **/
  public static class Fail_0 extends BuiltinRule {
    public String getName () { return "fail/0"; }
    public int getArity () { return 0; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      return wam.backtrack ();
    }
  }

  /**
   * <p>Halts the machine</p>
   **/
  public static class Halt_0 extends BuiltinRule {
    public String getName () { return "halt/0"; }
    public int getArity () { return 0; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      return null;
    }
  }

  // -------------------------------------------------------------------------
  // ---- Term Unification ---------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Implements the <code>=/2</code> predicate (8.2.1).</p>
   *
   * <p><code>'='(?term, ?term)</code></p>
   *
   * <p>If <code>X</code> and <code>Y</code> are prolog terms, then
   * <code>'='(X, Y)</code> is true iff <code>X</code> and
   * <code>Y</code> are unifiable.</p>
   *
   * <p>Note that <code>=/2</code> doesn't implement the occurs-check.
   * Another unifier will be available for those times when you need
   * this functionality.</p>
   **/
  public static class Unify_2 extends BuiltinRule {
    public String getName () { return "=/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the two parameters.
      PrologTerm x = wam.getRegister (0).deref ();
      PrologTerm y = wam.getRegister (1).deref ();

      // --- if they unify, succeed.
      if (!x.unify (y)) return wam.Fail;

      return wam.getContinuation ();
    }
  }


  // -------------------------------------------------------------------------
  // ---- Type Testing -------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Tests whether its argument is a variable (8.3.1).</p>
   *
   * <p><code>var(@term)</code></p>
   *
   * <p><code>var(X)</code> is true iff <code>X</code> is a
   * variable.</p>
   **/
  public static class Var_1 extends BuiltinRule {
    public String getName () { return "var/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameter.
      PrologTerm x = wam.getRegister (0).deref ();

      // --- if its a variable, succeed.
      if (!x.isVariable ()) return wam.Fail;

      return wam.getContinuation ();
    }
  }

  /**
   * <p>Tests whether its argument is an atom (8.3.2).</p>
   *
   * <p><code>atom(@term)</code></p>
   *
   * <p><code>atom(X)</code> is true iff <code>X</code> is an atom.</p>
   **/
  public static class Atom_1 extends BuiltinRule {
    public String getName () { return "atom/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameter.
      PrologTerm x = wam.getRegister (0).deref ();

      // --- succeed only if our parameter is an Atom.
      if (!x.isAtom ()) return wam.Fail;

      return wam.getContinuation ();
    }
  }

  /**
   * <p>Tests whether its argument is an integer (8.3.3).</p>
   *
   * <p><code>integer(@term)</code></p>
   *
   * <p><code>integer(X)</code> is true iff <code>X</code> is an
   * integer.</p>
   **/
  public static class Integer_1 extends BuiltinRule {
    public String getName () { return "integer/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameter.
      PrologTerm x = wam.getRegister (0).deref ();

      // --- if its an integer, succeed.
      if (!x.isInteger ()) return wam.Fail;

      return wam.getContinuation ();
    }
  }

  /**
   * <p>Tests whether its argument is a floating-point value (8.3.4).</p>
   *
   * <p><code>real(@term)</code></p>
   *
   * <p><code>real(X)</code> is true iff <code>X</code> is a floating
   * point value.</p>
   *
   * <p><strong>Note:</strong> the name 'real' is something of a
   * misnomer: there are no real numbers in standard prolog: only
   * floats.</p>
   **/
  public static class Real_1 extends BuiltinRule {
    public String getName () { return "real/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameter.
      PrologTerm x = wam.getRegister (0).deref ();

      // --- if its a float, succeed.
      if (!x.isFloat ()) return wam.Fail;

      return wam.getContinuation ();
    }
  }

  /**
   * <p>Tests whether its argument is atomic (8.3.5).</p>
   *
   * <p><code>atomic(@term)</code></p>
   *
   * <p><code>real(X)</code> is true iff <code>X</code> is atomic
   * (that is, an atom, an integer, or a floating point value</p>
   **/
  public static class Atomic_1 extends BuiltinRule {
    public String getName () { return "atomic/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameter.
      PrologTerm x = wam.getRegister (0).deref ();

      boolean success = false;

      // --- atoms succeed.
      if (x.isAtom ())
        success = true;
      // --- integers succeed.
      else if (x.isInteger ())
        success = true;
      // --- floats succeed.
      else if (x.isFloat ())
        success = true;

      if (!success) return wam.Fail;

      return wam.getContinuation ();
    }
  }


  /**
   * <p>Tests whether its argument is a compound term (8.3.6).</p>
   *
   * <p><code>compound(@term)</code></p>
   *
   * <p><code>compound(X)</code> is true iff <code>X</code> is an
   * compound term.</p>
   **/
  public static class Compound_1 extends BuiltinRule {
    public String getName () { return "compound/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameter.
      PrologTerm x = wam.getRegister (0).deref ();

      if (!x.isCompoundTerm ()) return wam.Fail;

      return wam.getContinuation ();
    }
  }

  /**
   * <p>Tests whether its argument is not a variable (8.3.7).</p>
   *
   * <p><code>nonvar(@term)</code></p>
   *
   * <p><code>nonvar(X)</code> is true iff <code>X</code> is NOT a
   * variable.</p>
   **/
  public static class Nonvar_1 extends BuiltinRule {
    public String getName () { return "nonvar/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameter.
      PrologTerm x = wam.getRegister (0).deref ();

      // --- if its a variable, fail.
      if (x.isVariable ()) return wam.Fail;

      return wam.getContinuation ();
    }
  }

  /**
   * <p>Tests whether its argument is a number (8.3.8).</p>
   *
   * <p><code>number(@term)</code></p>
   *
   * <p><code>number(X)</code> is true iff <code>X</code> is a number
   * (that is, an integer or a floating point value</p>
   **/
  public static class Number_1 extends BuiltinRule {
    public String getName () { return "number/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameter.
      PrologTerm x = wam.getRegister (0).deref ();

      boolean success = false;

      // --- integers succeed.
      if (x.isInteger ())
        success = true;
      // --- floats succeed.
      else if (x.isFloat ())
        success = true;

      if (!success) return wam.Fail;

      return wam.getContinuation ();
    }
  }

  // -------------------------------------------------------------------------
  // ---- Term Comparison ----------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Tests whether its arguments are identical terms (8.4.1).</p>
   *
   * <p><code>'=='(@term, @term)</code></p>
   *
   * <p><code>'=='(X,Y)</code> is true iff <code>X</code> and
   * <code>Y</code> are identical </p>
   *
   * XXX broken for many types.  Should actually do a deep compare.
   **/
  public static class Identical_2 extends BuiltinRule {
    public String getName () { return "==/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameters.
      PrologTerm x = wam.getRegister (0).deref ();
      PrologTerm y = wam.getRegister (1).deref ();

      if (x != y) return wam.Fail;

      return wam.getContinuation ();
    }
  }

  // -------------------------------------------------------------------------
  // ---- Term creation and decomposition ------------------------------------
  // -------------------------------------------------------------------------
  
  /**
   * <p>Allows a term to be split into its components (8.5.1)</p>
   *
   * <p><code>functor(-nonvar, +constant, +integer)</code></p>
   * <p><code>functor(@nonvar, ?constant, ?integer)</code></p>
   *
   * <p><code>functor(Term, Name, Arity)</code></p>
   *
   **/
  public static class Functor_3 extends BuiltinRule {
    public String getName () { return "functor/3"; }
    public int getArity () { return 3; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameters.
      PrologTerm _term = wam.getRegister (0).deref ();
      PrologTerm _name = wam.getRegister (1).deref ();
      PrologTerm _arity = wam.getRegister (2).deref ();

      if (_term.isCompoundTerm ())
        {
          CompoundTerm term = (CompoundTerm)_term;
          if (!term.getNameConstant ().unify (_name)) return wam.Fail;
          if (!term.getArityConstant ().unify (_arity)) return wam.Fail;
        }
      else if (_term.isConstant ())
        {
          if (!_term.getNameConstant ().unify (_name)) return wam.Fail;
          if (!wam.getFactory ().makeInteger (0).unify (_arity)) 
            return wam.Fail;
        }
      else if (_term.isVariable ())
        {
          Variable term = (Variable)_term;

          if (_name.isVariable () || _arity.isVariable ())
            return wam.getFactory ().callThrowInstantiationError ();

          // --- so name must be a constant.
          if (!_name.isConstant ())
            return wam.getFactory ().callThrowTypeError ("constant", _name);

          // --- plus, arity must be an integer.
          if (!_arity.isInteger ())
            return wam.getFactory ().callThrowTypeError ("integer", _arity);

          WamInteger arity = (WamInteger)_arity;
          int arityIntValue = arity.intValue ();
          if (arityIntValue < 0)
            return wam.getFactory ().callThrowDomainError ("not_less_than_zero", arity); 

          PrologTerm newFunctor;
          if (arityIntValue == 0)
            {
              newFunctor = _name;
            }
          else
            {
              // --- make the new functor.
              PrologTerm[] subterms = new PrologTerm[arityIntValue];
              for (int i=0; i<arityIntValue; i++)
                subterms[i] = wam.getFactory ().makeVariable (null);

              newFunctor = 
                wam.getFactory ().makeCompoundTerm (_name.getName(), subterms);
            }

          if (!term.unify (newFunctor)) return wam.Fail;
        }


      return wam.getContinuation ();
    }
  }

  /**
   * <p>Allows arguments of a functor to be retrieved and
   * set. (8.5.2)</p>
   *
   * <p><code>arg(+integer, +compound_term, ?term)</code></p>
   *
   * <p><code>arg(N, Term, Arg)</code> is true iff the
   * <code>N</code>-th argument of <code>Term</code> is
   * <code>Arg</code>.</p>
   **/
  public static class Arg_3 extends BuiltinRule {
    public String getName () { return "arg/3"; }
    public int getArity () { return 3; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameters.
      PrologTerm _n = wam.getRegister (0).deref ();
      PrologTerm _term = wam.getRegister (1).deref ();
      PrologTerm _arg = wam.getRegister (2).deref ();

      if (_n.isVariable ())
        return wam.getFactory ().callThrowInstantiationError ();

      if (_term.isVariable ())
        return wam.getFactory ().callThrowInstantiationError ();

      if (!_term.isCompoundTerm ()) 
        return wam.getFactory ().callThrowTypeError ("compound", _term);

      if (!_n.isInteger ())
        return wam.getFactory ().callThrowTypeError ("compound", _n);

      WamInteger n = (WamInteger)_n;
      CompoundTerm term = (CompoundTerm)_term;

      // --- according to the spec, "arguments are numbered from 1".
      int argNum = n.intValue () - 1;

      // XXX this first case should perhaps throw a number exception
      // of some sort?
      if (argNum < 0) return wam.Fail;

      if (argNum >= term.getArity ()) return wam.Fail;

      PrologTerm argN = term.getSubterm (argNum);

      //Util.out.println ("Unifying "+argN.tag ()+" with "+_arg.tag ());

      // --- do the actual unification.
      if (!_arg.unify (argN)) return wam.Fail;

      // --- success!
      return wam.getContinuation ();
    }
  }  

  /**
   * <p>Allows a compound term to be constructed from a list</p>
   *
   * <p><code>'=..'(+nonvar, ?list)</code></p>
   * <p><code>'=..'(-nonvar, +list)</code></p>
   *
   **/
  public static class Univ_2 extends BuiltinRule {
    public String getName () { return "=../2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameters.
      PrologTerm term = wam.getRegister (0).deref ();
      PrologTerm list = wam.getRegister (1).deref ();

      PrologFactory factory = wam.getFactory ();

      if (term.isVariable () && list.isVariable ())
        return factory.callThrowInstantiationError ();

      if (term.isConstant ())
        {
          // --- make a singleton list.
          CompoundTerm desiredList = 
            factory.makeCompoundTerm (".", term, factory.makeEmptyList ());
          if (!list.unify (desiredList)) return wam.Fail;

          return wam.getContinuation ();
        }

      if (term.isCompoundTerm ())
        {
          // --- make the desired list.
          CompoundTerm ct = (CompoundTerm)term;
          CompoundTerm desiredList = factory.makeList (ct.subterms);
          desiredList = 
            factory.makeCompoundTerm (".", ct.getNameConstant (), desiredList);
          if (!list.unify (desiredList)) return wam.Fail;

          return wam.getContinuation ();
        }

      if (!list.isList ())
        return factory.callThrowTypeError ("list", list);

      // XXX deal with more errors here.

      if (list.isEmptyList ()) return wam.Fail;
      CompoundTerm cList = (CompoundTerm)list;

      //Util.out.println ("cList == "+cList.tag ());

      PrologTerm head = cList.getSubterm (0).deref ();
      PrologTerm tail = cList.getSubterm (1).deref ();

      if (!tail.isList ())
        return factory.callThrowTypeError ("list", list);

      if (tail.isEmptyList ())
        {
          if (!term.unify (head)) return wam.Fail;
          return wam.getContinuation ();
        }

      List<PrologTerm> bodyList = new ArrayList<PrologTerm> ();
      CompoundTerm.PrologIterator i = 
        ((CompoundTerm)tail).asList ().prologIterator ();
      while (i.hasNext ())
        {
          PrologTerm t = i.nextPrologTerm ();
          //Util.out.println ("Adding term "+t.tag ());
          bodyList.add (t);
        }
      if (i.terminator () != null)
        return factory.callThrowTypeError ("list", list);

      PrologTerm[] subterms = 
        (PrologTerm[])bodyList.toArray (new PrologTerm[bodyList.size ()]); 

      PrologTerm desiredTerm = factory.makeCompoundTerm (head.getName (),
                                                         subterms);

      if (!term.unify (desiredTerm)) return wam.Fail;
      return wam.getContinuation ();
    }
  }  

  /**
   *
   **/
  public static class Set_var_nameXXX_2 extends BuiltinRule {
    public String getName () { return "set_var_nameXXX/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      PrologTerm name = wam.getRegister (0).deref ();
      PrologTerm var = wam.getRegister (1).deref ();

      if (!var.isVariable ())
        return wam.getFactory ().callThrowTypeError ("variable", var);

      // XXX INCREDIBLY CREEPY.  RETHINK IMMEDIATELY.
      ((Variable)var).name = name.getName ();

      return wam.getContinuation ();
    }
  }

  /**
   * <p><code>copy_term(?Term1, ?Term2)</code> is true iff Term2
   * unifies with a renamed copy of Term1. (8.5.4)</p>
   **/
  public static class Copy_term_2 extends BuiltinRule {
    public String getName () { return "copy_term/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      PrologTerm term1 = wam.getRegister (0).deref ();
      PrologTerm term2 = wam.getRegister (1).deref ();
      
      // --- trivial success.
      if (term1.isVariable () && term2.isVariable ())
        return wam.getContinuation ();

      // --- make a renamed copy, try to unify it.
      PrologTerm renamedCopy = term1.clonePrologTerm ();
      if (!renamedCopy.unify (term2)) return wam.Fail;

      return wam.getContinuation ();
    }
  }

  // -------------------------------------------------------------------------
  // ---- Flags --------------------------------------------------------------
  // -------------------------------------------------------------------------

  public static class Set_prolog_flag_2 extends BuiltinRule {
    public String getName () { return "set_prolog_flag/2"; }
    public int getArity () { return 2; }
    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameter.
      PrologTerm flagName = wam.getRegister (0).deref ();
      PrologTerm value = wam.getRegister (1).deref ();

      // XXX doesn't work for zooming through all the flags yet.

      // XXX hack-fest
      if (flagName.getName ().equals ("trace"))
        {
          if (value.getName ().equals ("true"))
            wam.engine.trace = true;
          else if (value.getName ().equals ("false"))
            wam.engine.trace = false;
        }

      // XXX hack-fest
      else if (flagName.getName ().equals ("debug"))
        {
          if (value.getName ().equals ("true"))
            wam.engine.debug = true;
          else if (value.getName ().equals ("false"))
            wam.engine.debug = false;
        }

      // XXX hack-fest
      else if (flagName.getName ().equals ("debugUnify"))
        {
          if (value.getName ().equals ("true"))
            wam.engine.debugUnify = true;
          else if (value.getName ().equals ("false"))
            wam.engine.debugUnify = false;
        }

      return wam.getContinuation ();
    }
  }

  // -------------------------------------------------------------------------
  // ---- Clause creation and destruction ------------------------------------
  // -------------------------------------------------------------------------

  public static class Asserta_1 extends BuiltinRule {
    public String getName () { return "asserta/1"; }
    public int getArity () { return 1; }
    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameter.
      PrologTerm x = wam.getRegister (0).deref ();

      wam.engine.asserta (x);

      return wam.getContinuation ();
    }
  }

  public static class Assertz_1 extends BuiltinRule {
    public String getName () { return "assertz/1"; }
    public int getArity () { return 1; }
    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameter.
      PrologTerm x = wam.getRegister (0).deref ();

      wam.engine.assertz (x);

      return wam.getContinuation ();
    }
  }

  // -------------------------------------------------------------------------
  // ---- I/O Builtins -------------------------------------------------------
  // -------------------------------------------------------------------------

  // -------------------------------------------------------------------------
  // ---- Arithmetic ---------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Evaluate expression (8.6.1)</p>
   *
   * <p><code>is (?nonvar, +nonvar)</code></p>
   *
   * <p><code>is (Result, Expression)</code> is true iff Result
   * unifies with the result of evaluating Expression as an
   * expression</p>
   **/
  public static class Is_2 extends BuiltinRule {
    public String getName () { return "is/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      PrologTerm result = wam.getRegister (0).deref ();
      PrologTerm expression = wam.getRegister (1).deref ();

      if (expression.isVariable ())
        return wam.getFactory ().callThrowInstantiationError ();

      PrologTerm answer = expression.evaluateExpression ();

      if (!result.unify (answer)) return wam.Fail;

      return wam.getContinuation ();
    }
  }

  /**
   * Base class for arithmetic comparison builtins.
   **/
  public abstract static class ArithmeticComparisonRule extends BuiltinRule {
    public abstract String getName ();
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      PrologTerm x = wam.getRegister (0).deref ();
      PrologTerm y = wam.getRegister (1).deref ();

      if (x.isVariable ())
        return wam.getFactory ().callThrowInstantiationError ();
      if (y.isVariable ()) 
        return wam.getFactory ().callThrowInstantiationError ();

      x = x.evaluateExpression ();
      y = y.evaluateExpression ();

      if (!compare (x, y)) return wam.Fail;

      return wam.getContinuation ();
    }

    public abstract boolean compare (PrologTerm x, PrologTerm y) 
      throws PrologException;
  }

  /** Equals */
  public static class Equals_2 extends ArithmeticComparisonRule {
    public String getName () { return "=:=/2"; }
    public boolean compare (PrologTerm x, PrologTerm y) throws PrologException
    { return ((WamInteger)x).intValue () == ((WamInteger)y).intValue (); }
  }

  /** Not equals */
  public static class NotEquals_2 extends ArithmeticComparisonRule {
    public String getName () { return "=\\=/2"; }
    public boolean compare (PrologTerm x, PrologTerm y) throws PrologException
    { return ((WamInteger)x).intValue () != ((WamInteger)y).intValue (); }
  }

  /** Less than */
  public static class LessThan_2 extends ArithmeticComparisonRule {
    public String getName () { return "</2"; }
    public boolean compare (PrologTerm x, PrologTerm y) throws PrologException
    { return ((WamInteger)x).intValue () < ((WamInteger)y).intValue (); }
  }

  /** Greater than */
  public static class GreaterThan_2 extends ArithmeticComparisonRule {
    public String getName () { return ">/2"; }
    public boolean compare (PrologTerm x, PrologTerm y) throws PrologException
    { return ((WamInteger)x).intValue () > ((WamInteger)y).intValue (); }
  }

  /** Less than/equals */
  public static class LessThanEquals_2 extends ArithmeticComparisonRule {
    public String getName () { return "=</2"; }
    public boolean compare (PrologTerm x, PrologTerm y) throws PrologException
    { return ((WamInteger)x).intValue () <= ((WamInteger)y).intValue (); }
  }

  /** Greater than/equals */
  public static class GreaterThanEquals_2 extends ArithmeticComparisonRule {
    public String getName () { return ">=/2"; }
    public boolean compare (PrologTerm x, PrologTerm y) throws PrologException
    { return ((WamInteger)x).intValue () >= ((WamInteger)y).intValue (); }
  }
}

