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
 * PrologParser.java
 *
 */
package com.svincent.moksa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A parser for the Prolog language.  There are two subclasses,
 * MiniPrologParser, and ISOPrologParser, which parse part/all of the
 * Prolog language, respectively. </p>
 *
 * <p>This is an interface so that builtin terms and Moksa tools can
 * parse Prolog source code. </p>
 **/
public abstract class PrologParser extends WamObject {

  /**
   * <p>Parse a Prolog clause.  This ends at the next end ('.')
   * character, and typically contains something of the form "Term :-
   * Term, Term." </p>
   *
   * <p>Returns null on EOF.</p>
   **/
  public abstract PrologTerm parseClause (Io.PrologInput in)
    throws IOException, PrologParseException;

  /**
   * <p>Parse a Prolog term.  This is the primitive term stuff we're
   * all familiar with: atoms, integers, compound terms, floats,
   * etc. </p>
   *
   * <p>Returns null on EOF.</p>
   **/
  public abstract PrologTerm parseTerm (Io.PrologInput in)
    throws IOException, PrologParseException;

  /**
   * Parses a Prolog clause, given a String.
   **/
  public PrologTerm parseClause (Io io, String term) 
    throws IOException, PrologParseException
  {
    Io.PrologInput in = io.openString (term);
    return parseClause (in);
  }

  /**
   * Parses a Prolog term, given a String.
   **/
  public PrologTerm parseTerm (Io io, String term) 
    throws IOException, PrologParseException
  {
    Io.PrologInput in = io.openString (term);
    return parseTerm (in);
  }

  /**
   * Parses an entire file, quickly and succinctly, into a list of clauses
   **/
  public PrologTerm[] parseFile (Io.PrologInput in)
    throws IOException, PrologParseException
  {
    List<PrologTerm> clauses = new ArrayList<PrologTerm> ();
    PrologTerm clause = parseClause (in);
    while (clause != null)
      {
        clauses.add (clause);
        clause = parseClause (in);
      }

    return (PrologTerm[])clauses.toArray (new PrologTerm [clauses.size ()]);
  }

}

