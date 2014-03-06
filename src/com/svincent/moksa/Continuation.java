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
 * Continuation.java
 *
 */
package com.svincent.moksa;

import java.io.*;
import java.util.*;

import com.svincent.util.*;

/**
 * <p>This is the Continuation concept, made explicit, for clarity.
 * It would be possible to use PrologTerms to build Continuations, or
 * use Instructions directly as their own continuations (I tried
 * both), but this seems the least confusing, particularly for
 * somebody like me: with relatively little experience working on
 * systems written in C-P style. </p>
 *
 **/
public class Continuation extends WamObject {
  Rule rule;
  PrologTerm[] registers;

  Continuation next;

  boolean dead = false;

  boolean notReclaimable = false;

  /**
   * <p>Construct a new Continuation which will call the given goal,
   * then proceed on to the 'next' continuation. </p>
   *
   * <p>Make Continuations using the static
   * <code>Continuation.make</code> methods.</p>
   **/
  protected Continuation (Rule _rule, PrologTerm[] _registers,
                          Continuation _next)
  {
    rule = _rule;
    registers = _registers;
    next = _next;

    Util.assertTrue (rule != null, 
                 "Huh!? Got null rule after rigourous constructor!");
  }

  /**
   * Execute this continuation.
   **/
  public Continuation exec (Wam wam) throws PrologException
  {
    if (dead)
      Util.assertTrue (false, "Hola!  Continuation ("+this+
                   ") for rule "+rule.tag ()+
                   " was returned, but is now being re-used!");

    if (wam.engine.trace)
      {
        Util.out.println (" -> Invoking rule "+rule.tag ());
        Util.out.println ("     Continuation: "+this);
        Util.out.println ("     Rule Def: "+
                          rule.getTerm (wam.getFactory ()).tag ());
        Util.out.println ("     "+rule.getArity ()+" Registers follow:");
        for (int i=0; i<rule.getArity (); i++) 
          Util.out.println ("         register["+i+"] == "+
                            registers[i].deref ().tag ());
        Util.out.println ("         continuation == "+
                          (next == null ? "NONE" : next.tag ()));
      }
    

    // --- set up registers
    wam.registers = registers;
    //    wam.setRegisters (registers);
//      for (int i=0; i<rule.getArity (); i++) 
//        {
//          //Util.out.println ("setting register "+i+" to "+registers[i]);
//          wam.setRegister (i, registers[i]);
//        }

    // --- set up continuation
    wam.continuation = next;
    //wam.setContinuation (next);

    // --- call the beast.
    return rule.invoke (wam);
  }

  /**
   * <p>Make a new Continuation for the given (possibly compound) goal
   * 'goal'.  Use 'next' as the terminating Continuation. </p>
   **/
  public static Continuation make (PrologEngine engine, 
                                   PrologTerm goal, Continuation next)
    throws PrologException
  {
    // XXX slow and bad.  Fix this up.
    PrologTerm[] flat = flatten (goal);

    // --- "call" body terms by returning them in our continuation.
    for (int i=flat.length-1; i>=0; i--)
      {
        PrologTerm bodyTerm = flat[i];
        next = continuationFromTerm (engine, bodyTerm, next);
      }

    return next;
  }

  /**
   * <p>Construct a new Continuation which will call the given goal,
   * then proceed on to the 'next' continuation. </p>
   **/
  static Continuation continuationFromTerm (PrologEngine engine, 
                                            PrologTerm ruleSpec, 
                                            Continuation next)
    throws PrologException
  {
    Wam wam = engine.wam;
    PrologFactory factory = engine.factory;

    Rule rule;
    PrologTerm[] registers;

    // XXX this is a gross thing.  Instead, make a callback into Rules
    // so that any rule can, if it chooses, add virtual registers to itself.
    if (ruleSpec.isAtom () && ruleSpec.getName ().equals ("!"))
      {
        // --- wierdo special case cut handling.
        rule = wam.getRule ("!/1");
        registers =
          new PrologTerm[] { factory.makeInteger (wam.currentCut) };
      }
    else
      {
        try {

          rule = wam.lookupRule (ruleSpec);
          if (ruleSpec.isCompoundTerm ())
            // XXX watch out here: we aren't making a copy.
            registers = ((CompoundTerm)ruleSpec).subterms;
          else
            registers = PrologTerm.EmptyArray;

        } catch (NoSuchRuleException ex) {

          Util.out.println ("Could not find rule named "+ruleSpec.getName ()+
                            "/"+ruleSpec.getArity ()+"");

          // --- do various things based on value of flag 'undefined_predicate'
          //   - (1) 'error'
          rule = wam.getRule ("throw/1");
          registers = new PrologTerm[] {
            factory.makeCompoundTerm ("existence_error", 
                                      factory.makeAtom ("procedure"),
                                      ruleSpec)
          };
          //   - (2) 'warning' - implementation-dependent warning
          //   - (3) 'fail' - replace 'rule' with 'fail'.
        }
      }

    return factory.makeContinuation (rule, registers, next);
  }

  // -------------------------------------------------------------------------
  // ---- Utility Methods ----------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Turn a comma list into an array.  XXX slow.
   **/
  private static PrologTerm[] flatten (PrologTerm _body)
  {
    // --- do the simple cases first.
    if (_body == null) return PrologTerm.EmptyArray;
    if (!_body.isCompoundTerm ()) return new PrologTerm[] {_body};

    CompoundTerm body = (CompoundTerm)_body;

    List<PrologTerm> collector = new LinkedList<PrologTerm> ();
    while (body.getName ().equals (","))
      {
        PrologTerm head = body.getSubterm (0);
        PrologTerm tail = body.getSubterm (1);
        collector.add (head);

        body = (CompoundTerm)tail;
      }
    collector.add (body);

    //Util.out.println ("Collector == "+collector);

    return 
      (PrologTerm[])collector.toArray (new PrologTerm[collector.size ()]);
  }

  /**
   * Retrieve a human-readable String for this object.
   **/
  public void tag (PrintWriter out)
  {
    out.write ("Continuation(");
    out.write ("rule == ");
    out.write (rule.tag ());
    if (dead)
      {
        out.write ("***DEAD***");
      }
    else
      {
        out.write (", args == [");

        for (int i=0; i<registers.length; i++)
          {
            if (i>0) out.write (", ");

            PrologTerm arg = registers[i];
            if (arg == null)
              out.write ("null");
            else
              arg.tag (out);
          }

        out.write ("], next == ");
        if (next == null)
          out.write ("NONE");
        else
          out.write (next.tag ());
      }
    out.write (")");
    out.write ('@');
    out.write (Integer.toHexString (System.identityHashCode (this)));
  }
  
}
