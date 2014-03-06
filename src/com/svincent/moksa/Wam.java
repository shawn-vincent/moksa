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
 * Wam.java
 *
 */
package com.svincent.moksa;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.svincent.util.Util;

/**
 * <p>An implementation of a Warren's Abstract Machine, as documented
 * in <em>Warren's Abstract Machine: a Tutorial Reconstruction</em>,
 * by Hassan Ait-Kaci. </p>
 *
 * <p>Some variations may exist between my implementation and Hassan's
 * description. </p>
 *
 * <p>There are a number of instructions in Warren's machine that we
 * don't implement here.  In particular, since we are representing
 * Prolog terms as Java compoundTerms, and have no explicit heap, most of the
 * instructions described in chapter 2 (up until section 2.3) of
 * Hassan's book are unneccesary.</p>
 *
 * <p>Most of them are replaced by <code>new</code> calls and calls to
 * <code>PrologTerm.unify</code>. </p>
 **/
public class Wam extends WamObject {

  PrologEngine engine;

  /** A very common continuation */
  public Continuation Fail;

  /** 
   * The initial register count.  Should be big enough for even the
   * hungriest man.  However, the registers will grow if necessary.
   **/
  static final int InitialRegisterCount = 32;

  /**
   * The initial choicepoint stack size.
   **/
  static final int InitialChoicePointsSize = 64;

  /**
   * The initial trail stack size.
   **/
  static final int InitialTrailSize = 64;

  /** The machine registers, used for all sorts of things. */
  PrologTerm[] registers = new PrologTerm[InitialRegisterCount];

  /**
   * <p>In the WAM book, this register is known as B0.</p>2
   *
   * <p>Stores the appropriatee choice point to return to after
   * backtracking over a cut.</p>
   **/
  int currentCut = 0;

  /** The continuation register, passed into instructions when run. */
  Continuation continuation = null;

  /** The map of rules. (i.e. - the Prolog Dictionary) */
  Map<String, AlternativeSet> rules = new HashMap<String, AlternativeSet> ();


  /** The choicepoint stack. */
  ChoicePoint[] choicePoints = new ChoicePoint[InitialChoicePointsSize];

  /** The current index into the choicepoint stack. */
  int lastChoicePoint = -1;

  /** The trail stack. */
  Trailable[] trail = new Trailable[InitialTrailSize];

  /** The current index into the trail stack. */
  int tr = 0;

  /** The current time, for timestamping things. */
  long time = 1; // XXX start at 0, 1, what???


  public Wam (PrologEngine _engine)
  {
    engine = _engine;
  }

  public PrologEngine getEngine () { return engine; }
  public PrologFactory getFactory () { return engine.factory; }
  public Io getIo () { return engine.io; }
  
  // -------------------------------------------------------------------------
  // ---- Accessors ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /** Retrieve a timestamp.  Note that this increments the timestamp. */
  public long getTimestamp () 
  { 
    // XXX should not happen, but if it does, I want to know about it.
    if (time >= Long.MAX_VALUE) Util.assertTrue (false, "Error: time overflowed.");
    return time++; 
  }

  /** Get the current continuation. */
  public Continuation getContinuation () { return continuation; }

  /** Set the value of continuation register. */
  public void setContinuation (Continuation _continuation)
  { continuation = _continuation; }

  /** Set the machine registers to the new array. **/
  void setRegisters (PrologTerm[] newRegisters) 
  { registers = newRegisters; }

  /** Get the value of register 'idx'. */
  public PrologTerm getRegister (int idx) { return registers[idx]; }

  /** Set the value of register 'idx'. */
  public void setRegister (int idx, PrologTerm v) 
  { 
    // --- if we don't have enough space, grow.
    if (idx >= registers.length)
      {
        //     Note: this gets called very rarely.  
        //           Careful about introducing bugs.

        // --- grow to next power of two greater than 'idx'.
        int newSize = registers.length << 1;
        while (newSize <= idx) newSize <<= 1;

        // --- should happen exceedingly rarely.
        engine.log ().println ("Warning: growing registers to "+newSize);

        // --- copy over the data.
        PrologTerm[] newRegisters = new PrologTerm[newSize];
        System.arraycopy (registers, 0, newRegisters, 0, registers.length);
        registers = newRegisters;
      }
    // --- assign.
    registers[idx] = v; 
  }

  /**
   * Called when facts are processing their parameters.
   *
   * @return true iff the parm is bad.
   **/
  public boolean badparm (int parmIdx, PrologTerm desiredValue) 
    throws PrologException
  {
    PrologTerm parmPattern = getRegister (parmIdx).deref ();
    PrologTerm gotValue = desiredValue.deref ();
    return !parmPattern.unify (gotValue); 
  }

  /** Get the first alternative rule for the given name. */
  public Rule getRule (String name) 
  { 
    AlternativeSet alternativeSet = getAlternativeSet (name);
    if (alternativeSet.isEmpty ()) return null;
    Rule rule = alternativeSet.getFirstAlternative (); 

    return rule;
  }

  AlternativeSet getAlternativeSet (String ruleName)
  {
    AlternativeSet alternativeSet = (AlternativeSet)rules.get (ruleName);
    if (alternativeSet == null)
      {
        alternativeSet = new AlternativeSet ();
        rules.put (ruleName, alternativeSet);
      }
    return alternativeSet;
  }


  /** Invoke a goal. */
  public boolean invoke (PrologTerm _goal) throws PrologException
  {
    CompoundTerm goal = (CompoundTerm)_goal;

    // --- set up the initial choice point (has no rule, note, 'cause
    //   - we're outside of any rule: we're a query)
    tryMeElse (null, 0, null);

    // --- construct an continuation to account for the rule.
    Continuation continuation = Continuation.make (engine, goal, null);
    
    // --- then loop until complete.
    Continuation lastContinuation = null;
    while (continuation != null)
      {
        if (engine.debug)
          engine.log ().println ("Invoking continuation \""+
                                 continuation.tag ()+"\"");
        lastContinuation = continuation;
        continuation = continuation.exec (this);

        // --- if the last continuation was not used in a choicepoint,
        //   - return it to the pool for re-use (saves on GC time)
        if (!lastContinuation.notReclaimable)
          getFactory ().returnToPool (lastContinuation);
      }

    // --- all done.  Pop the initial choicepoint.
    if (engine.debug) engine.log ().println ("Done.");
    trustMe ();

    // XXX note that this last continuation will not have been re-used.
    return lastContinuation != Fail;
  }

  Rule lookupRule (PrologTerm ruleSpec) throws PrologException
  {
    Rule rule = getRule (ruleSpec.getName () + "/" + ruleSpec.getArity ());

    // --- complain if the rule doesn't exist.
    if (rule == null) 
      throw new NoSuchRuleException ("No rule found for term "+ruleSpec.tag());

    return rule;
  }

  // -------------------------------------------------------------------------
  // ---- Choicepoint/Trail management ---------------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Allocates a new choice point frame on the stack setting its
   * next clause field to 'next'. </p>
   *
   * <p>This should be called before the first clause of any
   * multi-clause rule.  Subsequent clauses, except for the last
   * clause, should call <code>retryMeElse</code>. </p>
   *
   * <p>The last clause should call <code>trustMe</code>. </p>
   *
   * @param arity the arity of the procedure offering alternatives.
   * Needed so that the Wam knows how many argument registers to save.
   *
   * @param next a pointer to the next choicepoint continuation.
   *
   * @see #retryMeElse(Rule)
   * @see #trustMe()
   **/
  public void tryMeElse (Rule rule, int arity, Rule next)
  { 
    makeChoicePoint (rule, arity, next); 
  }

  /**
   * <p>We just backtracked to the current choicepoint.  Setup the
   * next possible choice. </p>
   *
   * <p>One way of thinking about this is: if there are N choicepoints
   * (choice1..choiceN), then <code>retryMeElse</code> should be
   * called before executing choice2 through choice(N-1). </p>
   *
   * @param arity the arity of the procedure offering alternatives.
   * Needed so that the Wam knows how many argument registers to save.
   *
   * @param next a pointer to the next choicepoint continuation.
   *
   * @see #tryMeElse(Rule, int, Rule)
   **/
  public void retryMeElse (Rule next)
  {
    ChoicePoint cp = getLastChoicepoint ();
    // --- set the next choicepoint.
    cp.setNextAlternative (next);
  }

  /**
   * <p>We just backtracked to the last choicepoint.  No more choices,
   * so we've got to trust this fellow.</p>
   *
   * @see #tryMeElse(Rule, int, Rule)
   **/
  public void trustMe ()
  {
    popChoicePoint ();
  }

  void makeChoicePoint (Rule rule, int arity, Rule next)
  {
    if (engine.debug)
      {
        engine.log ().indent ("   > ");
      }

    // --- make a new choicepoint.
    if (lastChoicePoint+1 >= choicePoints.length)
      {
        ChoicePoint[] newChoicePoints = new ChoicePoint[choicePoints.length*2];
        System.arraycopy 
          (choicePoints, 0, newChoicePoints, 0, choicePoints.length);
        choicePoints = newChoicePoints;
      }

    choicePoints[++lastChoicePoint] = 
      new ChoicePoint (this, rule, arity, next);

    if (engine.debug)
      engine.log ().println ("Added choice point "+
                             choicePoints[lastChoicePoint].tag ());

  }

  /**
   * Backtrack to the next alternative.
   **/
  Continuation backtrack ()
  {
    ChoicePoint cp = getLastChoicepoint ();
    
    if (engine.debug)
      engine.log ().println ("Backtracking to choicepoint "+cp.tag ());

    // --- restore the registers.
    cp.restoreRegisters ();

    // --- unwind the trail
    unwindTrail ();

    // --- reset the cut register so that if a cut gets executed, this
    //   - choicepoint will go away.
    currentCut = lastChoicePoint - 1;

    Rule nextAlternative = cp.getNextAlternative ();

    if (nextAlternative == null)
      // termination case.  No more rules to run, so everything's all
      // done.
      return null;
    else
      return getFactory ().makeContinuation (cp.getNextAlternative (), 
                                             cp.savedArgRegisters,
                                             cp.savedContinuation);
  }

  /**
   * Remove choice points off the stack until you get to the given one.
   **/
  void cut (int cutPoint)
  {
    // --- do a bunch of pops until we get to the appropriate place.
    while (lastChoicePoint != cutPoint)
      {
        //ChoicePoint cp = getLastChoicepoint ();
        //log ().println ("Cutting choice point "+cp.tag ());
        popChoicePoint ();
      }

    // --- reset the current cut point to someplace sane.
    currentCut = lastChoicePoint;
  }

  void popChoicePoint ()
  {
    if (engine.debug)
      engine.log ().outdent ();

    if (engine.debug)
      engine.log ().println ("Dropped choice point "+
                      choicePoints[lastChoicePoint].tag ());

    // --- drop the choicepoint on the floor.
    choicePoints[lastChoicePoint--] = null;
  }

  /** Retrieve the current trail pointer. XXX empty? */
  protected ChoicePoint getLastChoicepoint ()
  { return choicePoints[lastChoicePoint]; }



  /** Retrieve the current trail pointer. */
  protected int getTrailPointer () { return tr; }

  /**
   * Places this object on the trail stack, marking it to be undone
   * when the trail is unwound.
   **/
  public void trail (Trailable obj)
  {
    // one of the most common methods in the system (called 500,000
    // times in one of my recent tests):  don't do anything too slow here.
    //Util.assert (obj != null, "Can't trail null!");
	  /*
    if (engine.debug)
      engine.log ().println ("Trailing obj "+obj);
      */

    // --- increment the trail top register.
    int pos = ++tr;

    // --- grow if necessary.
    if (pos >= trail.length)
      {
        Trailable[] newTrail = new Trailable[trail.length*2];
        System.arraycopy (trail, 0, newTrail, 0, trail.length);
        trail = newTrail;
      }

    // --- make a new trail entry.
    trail[pos] = obj;
  }

  /**
   * <p>Unwinds the trail stack, which contains things that need to be
   * undone when a choicepoint occurs. </p>
   *
   * <p>This version of the method unwinds the trail to the last
   * ChoicePoint. </p>
   **/
  protected void unwindTrail ()
  {
    ChoicePoint cp = getLastChoicepoint ();
    int trailPointer = cp.getTrailPointer ();

    /*
    if (engine.debug) 
      {
        engine.log ().println ("Untrailing until "+trailPointer);
        engine.log ().indent ();
      }
      */
    
    // --- untrail all PrologTerms on the trail stack, popping them off the
    //   - trail stack as we go.
    while (tr > trailPointer)
      popTrail ();

    /*
    if (engine.debug) 
      {
        engine.log ().outdent ();
      }
      */
  }

  public Trailable popTrail ()
  {
    if (engine.debug) engine.log ().println ("Untrailing at index "+tr);

    Trailable top = trail[tr];
    top.untrail ();
    trail[tr] = null; // for GC.
    tr--;

    // possibly pop a choicepoint as well, here.  I dunno.  Or maybe a
    // fixup pass afterwards.

    return top;
  }


  /**
   * Represents a choice point.
   **/
  public static class ChoicePoint extends WamObject {

    /** The enclosing WAM.  Note that this class could be nonstatic,
        but I have some methods with the same names, so it's more
        pleasant to do this.  Safer, too! */
    Wam wam;

    /** The rule which created this choice point.  Not used in the
        backtracking algorithms. */
    Rule rule;

    /** The saved argument registers. */
    PrologTerm[] savedArgRegisters;

    /** The saved continuation (i.e. - program counter) */
    Continuation savedContinuation;

    /** The arity of the procedure offering alternatives. */
    int arity;

    /** A pointer to the next alternative. */
    Rule nextAlternative;

    /** The creation timestamp of this choicepoint. */
    long timeStamp;

    /** A pointer into the trail to the point where we should stop
        unwinding for this choicepoint. */
    int trailPointer;

    /** Make a new choice point. */
    public ChoicePoint (Wam _wam, Rule _rule, int _arity, Rule next)
    {
      wam = _wam;

      rule = _rule;

      arity = _arity;
      nextAlternative = next;

      // --- save the registers.
      savedArgRegisters = new PrologTerm[arity];
      for (int i=0; i<arity; i++) savedArgRegisters[i] = wam.getRegister (i);

      // --- save the continuation.
      savedContinuation = wam.getContinuation ();

      // --- tell the continuation and all chained continuations that
      //   - it has been saved in a choicepoint so that the pool reclamation 
      //   - stuff doesn't try to eat it.
      Continuation c = savedContinuation;
      while (c != null && !c.notReclaimable)
        {
          c.notReclaimable = true;
          c = c.next;
        }
      
      // --- save away a timestamp.
      timeStamp = wam.getTimestamp ();

      // --- save the current trail pointer.
      trailPointer = wam.getTrailPointer ();
    }

    public int getTrailPointer () { return trailPointer; }

    public long getTimestamp () { return timeStamp; }

    /** Restore the WAM registers that are saved in this ChoicePoint. */
    public void restoreRegisters ()
    { 
      wam.setRegisters (savedArgRegisters);
      //for (int i=0; i<arity; i++) wam.setRegister (i, savedArgRegisters[i]); 
      wam.setContinuation (savedContinuation);
    }

    /** Set this choice point's next alternative. */
    public void setNextAlternative (Rule next)
    { nextAlternative = next; }

    /** Get this choice point's next alternative. */
    public Rule getNextAlternative () { return nextAlternative; }

    public void tag (PrintWriter out)
    {
      out.write ("ChoicePoint(");
      if (rule == null)
        out.write ("QUERY");
      else
        out.write (rule.getName ());
      out.write (", next == ");
      if (savedContinuation == null)
        out.write ("NONE");
      else
        out.write (savedContinuation.tag ());
      out.write (")");
    }
  }

  // -------------------------------------------------------------------------
  // ---- Exception Handling Stuff -------------------------------------------
  // -------------------------------------------------------------------------

  public Continuation findExceptionHandler (PrologTerm exception)
    throws PrologException
  {
    while (tr > 0)
      {
        // --- get top trail entry
        Trailable trailable = popTrail ();

        // --- if top trail entry is an exception handler, then...
        if (trailable instanceof ExceptionHandler)
          {
            ExceptionHandler exceptionHandler = (ExceptionHandler)trailable;

            // --- Note that even though this next unify will bind
            // variables, we could just untrail them again, since
            // we're in this loop.

            // --- the alternative to this is to unify without
            // bindings until we find one that matches, then re-unify.
            // This is much cleaner, so it's the chosen
            // implementation.
           
            // --- if its exception expression matches 'exception', ...
            PrologTerm exceptionTemplate = exceptionHandler.getException ();
            if (exceptionTemplate.unify (exception, false))
              {
                if (engine.debug)
                  engine.log ().println 
                    ("Found handler handler == "+exceptionTemplate.tag ()+
                     " ex == "+exception.tag ());

                // --- remove intervening choicepoints, as well!
                while (lastChoicePoint > exceptionHandler.choicePointTop)
                  popChoicePoint ();

                // --- re-unify with bindings.  (should not fail!)
                if (!exceptionTemplate.unify (exception))
                  throw new PrologException 
                    ("Internal error: expected to unify but couldn't "+
                     "while looking for exception handler!");

                // --- return a continuation made from its continuation as well
                //   - as a call to its handler.
                return exceptionHandler.getHandler ();
              }
          }

        // --- otherwise, loop around until we hit the top of the stack.
      }

    // --- stop the machine: throw an exception..
    return null;
  }

  /**
   * <p>A registered exception handler for the current thread of
   * execution.</p>
   **/
  public static class ExceptionHandler extends WamObject implements Trailable 
  {
    Wam wam;

    PrologTerm goal;
    PrologTerm exception;

    // --- where to go on failure.
    Continuation handler;

    /** Saved choice point top so we can unroll choice points when
        handling exceptions. */
    int choicePointTop;

    public ExceptionHandler (Wam _wam, PrologTerm _goal, 
                             PrologTerm _exception, 
                             Continuation _handler)
    {
      wam = _wam;
      goal = _goal;
      exception = _exception;
      handler = _handler;

      choicePointTop = wam.lastChoicePoint;
    }

    public PrologTerm getException () { return exception; }
    public Continuation getHandler () { return handler; }

    /**
     * Called when the trail is being unwound.
     **/
    public void untrail ()
    {

    }
  }

  // -------------------------------------------------------------------------
  // ---- Definition of Rules ------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Contains the entire set of alternatives for a particular rule.
   **/
  static class AlternativeSet extends WamObject {

    /** rules are a singly linked compoundTerm */
    Rule firstAlternative = null;
    Rule lastAlternative = null;

    public AlternativeSet () {} 

    public boolean isEmpty () { return firstAlternative == null; }

    public void asserta (Rule newAlternative)
    {
      if (firstAlternative == null)
        {
          // --- empty list special case
          firstAlternative = newAlternative; 
          lastAlternative = newAlternative; 
          newAlternative.nextAlternative = null;
          newAlternative.prevAlternative = null;
        }
      else
        {
          // --- standard case.
          firstAlternative.setPrevAlternative (newAlternative);
          newAlternative.setPrevAlternative (null);
          newAlternative.setNextAlternative (firstAlternative);
          firstAlternative = newAlternative;
        }
    }

    public void assertz (Rule newAlternative)
    {
      if (lastAlternative == null)
        {
          // --- empty list special case
          firstAlternative = newAlternative; 
          lastAlternative = newAlternative; 
          newAlternative.nextAlternative = null;
          newAlternative.prevAlternative = null;
        }
      else
        {
          // --- standard case.
          lastAlternative.setNextAlternative (newAlternative);
          newAlternative.setPrevAlternative (lastAlternative);
          newAlternative.setNextAlternative (null);
          lastAlternative = newAlternative;
        }
    }

    public Rule getFirstAlternative () { return firstAlternative; }

    public void tag (PrintWriter out)
    {
      out.write ("{");
      Rule r = firstAlternative;
      while (r != null)
        {
          r.tag (out);
          if (!r.isLastAlternative ()) out.write (", ");
          r = r.nextAlternative;
        }
      out.write ("}");
    }
  }



  // -------------------------------------------------------------------------
  // ---- Database manipulation ----------------------------------------------
  // -------------------------------------------------------------------------

  /** Add a new alternative rule for the given name. */
  public void asserta (Rule rule) 
  { 
    AlternativeSet alternativeSet = getAlternativeSet (rule.getName ());
    alternativeSet.asserta (rule); 
  }

  /** Add a new alternative rule for the given name. */
  public void assertz (Rule rule) 
  { 
    AlternativeSet alternativeSet = getAlternativeSet (rule.getName ());
    alternativeSet.assertz (rule); 
  }

}
