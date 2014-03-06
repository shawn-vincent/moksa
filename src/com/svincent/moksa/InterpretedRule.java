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
 * InterpretedRule.java
 *
 */
package com.svincent.moksa;

import java.io.PrintWriter;

/**
 * A rule, but interpreted, rather than compiled.
 **/
public class InterpretedRule extends Rule 
{
  CompoundTerm ruleDef;

  String name;
  int arity;

  public InterpretedRule (CompoundTerm _ruleDef) 
  { 
    super ();

    ruleDef = _ruleDef;

    CompoundTerm head = getHead (ruleDef);

    arity = head.getArity ();
    name = head.getName () + '/' + head.getArity ();
  }

  public int getArity () { return arity; }
  public String getName () { return name; }

  public PrologTerm getTerm (PrologFactory factory) { return ruleDef; }

  public Continuation invokeRule (Wam wam) throws PrologException
  {
    CompoundTerm subst = (CompoundTerm)ruleDef.clonePrologTerm ();

    CompoundTerm head = getHead (subst);

    // --- verify parameters.
    int parmCount = head.getArity ();
    for (int i=0; i<parmCount; i++)
      if (wam.badparm (i, head.getSubterm (i))) 
        return wam.Fail;

    // XXX copy machine registers away??? 
    // XXX maybe pass them around on call stack?

    // --- get the body.
    CompoundTerm body = getBody (subst);

    // --- construct continuation.
    Continuation continuation =
      Continuation.make (wam.engine, body, wam.getContinuation ());
    
    // --- clear registers
    //for (int i=0; i<parmCount; i++) wam.setRegister (i, null);

    // --- return the continuation so that the top-level fellow can
    //   - call our body terms or the original continuation.
    return continuation;
  }

  private CompoundTerm getHead (CompoundTerm rule)
  {
    if (rule.getName ().equals (":-")) 
      return (CompoundTerm)rule.getSubterm (0);
    else return rule;
  }

  private CompoundTerm getBody (CompoundTerm rule)
  {
    if (rule.getName ().equals (":-")) 
      return (CompoundTerm)rule.getSubterm (1);
    else return null;
  }

  public void tag (PrintWriter out)
  {
    ruleDef.tag (out);
    out.write (".");
  }
}
