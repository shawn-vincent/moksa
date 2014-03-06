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
 * PrologFactory.java
 */
package com.svincent.moksa;

import java.util.*;

/**
 * <p>Responsible for creating instances of various Prolog entities. </p>
 *
 * <p>Having a factory allows the rest of the system to be somewhat
 * abstracted away from concrete types.  Plus, object pooling is
 * possible, as all object creation is centralized. </p>
 **/
public class PrologFactory {
  PrologEngine engine;

  /**
   * Make a new PrologFactory for the given PrologEngine.
   **/
  public PrologFactory (PrologEngine _engine) { engine = _engine; }

  // -------------------------------------------------------------------------
  // ---- PrologTerm factory -------------------------------------------------
  // -------------------------------------------------------------------------

  int varId = 1;

  public JavaTerm wrapObject (Object o)
  { return new JavaTerm (engine, o); }

  public Variable makeTemporaryVariable ()
  { return makeTemporaryVariable (null);}

  public Variable makeTemporaryVariable (String name)
  {
    if (name == null)
      return makeVariable ("_"+(varId++)); 
    else
      return makeVariable (name+"_"+(varId++)); 
  }

  public Variable makeVariable (String name)
  { return new Variable (engine, name); }

  public WamInteger makeInteger (int value)
  { return new WamInteger (engine, value); }

  public WamFloat makeFloat (double value)
  { return new WamFloat (engine, value); }

  public CompoundTerm makeCompoundTerm (String name, PrologTerm[] subterms, 
                                        int priority)
  { return new CompoundTerm (engine, name, subterms, priority); }

  public CompoundTerm makeCompoundTerm (String name, PrologTerm[] subterms)
  { return makeCompoundTerm (name, subterms, 0); }

  /** Convenience constructor for making Atoms. */
  public CompoundTerm makeAtom (String _name)
  { return makeCompoundTerm (_name); }

  /** Convenience constructor for making Lists. */
  public CompoundTerm makeList (PrologTerm[] elements)
  {
    CompoundTerm retval = makeEmptyList ();
    for (int i=elements.length-1; i>=0; i--)
      retval = makeCompoundTerm (".", elements[i], retval);
    return retval;
  }

  /** Convenience constructor for making Lists. */
  public CompoundTerm makeEmptyList ()
  { return makeAtom ("[]"); }

  /** Convenience constructor for making Curlies. */
  public CompoundTerm makeEmptyCurlies ()
  { return makeAtom ("{}"); }

  /** Convenience constructor for 0 subterms */
  public CompoundTerm makeCompoundTerm (String _name)
  { return makeCompoundTerm (_name, PrologTerm.EmptyArray); }

  /** Convenience constructor for 1 subterm */
  public CompoundTerm makeCompoundTerm (String _name, PrologTerm sub1)
  { return makeCompoundTerm (_name, new PrologTerm[]{sub1}); }

  /** Convenience constructor for 2 subterms */
  public CompoundTerm makeCompoundTerm (String _name, PrologTerm sub1, 
                                        PrologTerm sub2)
  { return makeCompoundTerm (_name, new PrologTerm[]{sub1, sub2}); }

  /** Convenience constructor for 3 subterms */
  public CompoundTerm makeCompoundTerm (String _name, PrologTerm sub1, 
                                        PrologTerm sub2, PrologTerm sub3)
  { return makeCompoundTerm (_name, new PrologTerm[]{sub1, sub2, sub3}); }

  /** Convenience constructor for 4 subterms */
  public CompoundTerm makeCompoundTerm (String _name, PrologTerm sub1, 
                                        PrologTerm sub2, PrologTerm sub3, 
                                        PrologTerm sub4)
  { return makeCompoundTerm (_name, new PrologTerm[]{sub1, sub2, sub3, sub4});}

  /** Convenience constructor for 5 subterms */
  public CompoundTerm makeCompoundTerm (String _name, PrologTerm sub1, 
                                        PrologTerm sub2, PrologTerm sub3, 
                                        PrologTerm sub4, PrologTerm sub5)
  { return makeCompoundTerm (_name, new PrologTerm[]{sub1, sub2, sub3, 
                                                     sub4, sub5}); }

  // -------------------------------------------------------------------------
  // ---- Continuations ------------------------------------------------------
  // -------------------------------------------------------------------------

  List<Continuation> continuationPool = new ArrayList<Continuation> ();

  /**
   * Make a new Continuation.
   **/
  public Continuation makeContinuation (Rule rule, PrologTerm[] registers, 
                                        Continuation next)
  { return new Continuation (rule, registers, next); }


  // XXX no longer called.  The lifespans of Continuations is too difficult.
  // XXX Choicepoints save old Continuations around.  Weak references 
  //     also seem not to work.
  public void returnToPool (Continuation continuation)
  {
    synchronized (continuationPool) {

      continuation.rule = null;
      continuation.registers = null;
      continuation.next = null;
      continuation.dead = true;

      continuationPool.add (continuation);
    }
  }

  // -------------------------------------------------------------------------
  // ---- Prolog Exception Continuations -------------------------------------
  // -------------------------------------------------------------------------

  public Continuation callThrowInstantiationError ()
  { return callThrow (makeAtom ("instantiation_error")); }

  public Continuation callThrowTypeError (String desiredType, 
                                          PrologTerm offendingTerm)
  { 
    return callThrow (makeCompoundTerm ("type_error", makeAtom (desiredType), 
                                        offendingTerm)); 
  }

  public Continuation callThrowDomainError (String desiredDomain, 
                                            PrologTerm offendingTerm)
  { 
    return callThrow (makeCompoundTerm ("domain_error", 
                                        makeAtom (desiredDomain), 
                                        offendingTerm)); 
  }

  public Continuation callThrowPermissionError (String desiredPermission, 
                                                PrologTerm offendingTerm)
  { 
    return callThrow (makeCompoundTerm ("permission_error", 
                                        makeAtom (desiredPermission), 
                                        offendingTerm)); 
  }

  public Continuation callThrowPermissionError (String desiredPermission,
                                                String auxilliaryAtom,
                                                PrologTerm offendingTerm)
  { 
    return callThrow (makeCompoundTerm ("permission_error", 
                                        makeAtom (desiredPermission), 
                                        makeAtom (auxilliaryAtom), 
                                        offendingTerm)); 
  }

  public Continuation callThrowExistenceError (String desiredExistor, 
                                               PrologTerm offendingTerm)
  { 
    return callThrow (makeCompoundTerm ("existence_error", 
                                        makeAtom (desiredExistor), 
                                        offendingTerm)); 
  }

  public Continuation callThrow (PrologTerm exception)
  {
    new Throwable ("Throwing Prolog exception: "+exception.tag ()).
      printStackTrace ();
    return makeContinuation (engine.wam.getRule ("throw/1"), 
                             new PrologTerm[] {exception}, null);
  }


}
