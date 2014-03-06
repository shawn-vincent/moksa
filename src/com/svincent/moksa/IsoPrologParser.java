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
 * IsoPrologParser.java
 *
 */
package com.svincent.moksa;

import java.io.IOException;

/**
 * <p>Parses ISO Prolog source files.</p>
 **/
public class IsoPrologParser extends PrologParser {
  PrologEngine engine; // copied from Io for direct access.

  public IsoPrologParser (PrologEngine _engine) { engine = _engine; }

  // -------------------------------------------------------------------------
  // ---- Public API ---------------------------------------------------------
  // -------------------------------------------------------------------------

  public PrologTerm parseClause (Io.PrologInput in) 
    throws IOException, PrologParseException
  { return parseTerm (in); }

  public PrologTerm parseTerm (Io.PrologInput in) 
    throws IOException, PrologParseException
  { 
    if (in.peekChar () == -1) return null;

    Variable retval = 
      engine.getFactory ().makeVariable ("Retval");
    CompoundTerm doParse = 
      engine.getFactory ().makeCompoundTerm ("read_term", in, retval);

    Variable[] vars;
    try {
      vars = engine.solve (doParse);
    } catch (PrologException ex) {
      throw new PrologParseException ("", ex);
    }

    if (vars == null)
      throw new PrologParseException 
        ("Was unable to parse clause: read_term/2 failed");

    PrologTerm t = retval.deref ();
    if (t.getName ().equals ("end_of_file"))
      return null;

    t = t.uniqueVariables ();

    return t;
  }


}
