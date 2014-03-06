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
 * Rule.java
 *
 */
package com.svincent.moksa;

import java.io.PrintWriter;

/**
 * The abstract base class for all Rules.
 */
public abstract class Rule extends WamObject 
{
  Rule prevAlternative;
  Rule nextAlternative;

  /**
   * Make a new instance of this Rule.  Rules are Singletons: there is
   * one instance of any particular Rule class.
   **/
  public Rule () { super (); }

  /** Return the name of this Rule in &quot;atom/arity&quot;
      notation. */
  public abstract String getName ();

  /** Return the arity of this Rule (i.e. - the number of parameters
      it expects). */
  public abstract int getArity ();

  /** 
   * Return the Prolog term corresponding to this Rule.  The term
   * returned is invariably an instance of <code>:-/2</code>: this
   * <em>is</em> a <code>Rule</code>, after all!
   **/
  public abstract PrologTerm getTerm (PrologFactory factory);

  // XXX Would be nice to get a number for these things...

  /** true iff this is the only registered alternative. */
  public boolean isOnlyAlternative () 
  { return isFirstAlternative () && isLastAlternative (); }
  
  /** true iff this is the first registered alternative. */
  public boolean isFirstAlternative () { return prevAlternative == null; }

  /** true iff this is the last registered alternative. */
  public boolean isLastAlternative () { return nextAlternative == null; }

  /** 
   * Set this Rule's next alternative.  Rules are stored in a
   * Linked List structure inside AlternativeSets, in the WAM.
   **/
  void setNextAlternative (Rule _nextAlternative)
  { nextAlternative = _nextAlternative; }

  /** 
   * Set this Rule's previous alternative.  Rules are stored in a
   * Linked List structure inside AlternativeSets, in the WAM.
   **/
  void setPrevAlternative (Rule _prevAlternative)
  { prevAlternative = _prevAlternative; }

  /**
   * Invoke this Rule.  Does various WAM setup, then calls
   * <code>invokeRule</code>.
   *
   * @see #invokeRule
   **/
  public Continuation invoke (Wam wam) throws PrologException
  {
	/*
    if (wam.engine.debug)
      wam.engine.log ().println ("Doing choicepoint stuff for rule "+tag ());
    */
	  
    // --- do choicepoint setup.
    if (isOnlyAlternative ())
      ; // these guys don't need choicepoint handling.
    else if (isFirstAlternative ())
      wam.tryMeElse (this, getArity (), nextAlternative);
    else if (isLastAlternative ())
      wam.trustMe ();
    else // is typical alternative.
      wam.retryMeElse (nextAlternative);

    if (wam.engine.debug)
      wam.engine.log ().println ("Invoking rule "+tag ());
    
    // --- actually invoke the Rule.
    return invokeRule (wam);
  }

  /**
   * Contains the code which defines what this Rule does.  Called by
   * <code>invoke</code>.
   *
   * @see #invoke
   **/
  public abstract Continuation invokeRule (Wam wam) throws PrologException;

  /**
   * Returns a short, human-readable String describing this Rule.
   **/
  public void tag (PrintWriter out) { out.write (getName ()); }
}

