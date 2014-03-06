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
 * MiniPrologParser.java
 *
 */
package com.svincent.moksa;

import java.io.*;
import java.util.*;

/**
 * <p>Parses a subset of the Prolog language, that I call 'Mini-Prolog'.</p>
 *
 * <p>Moksa includes two Prolog parsers: this one, written entirely in
 * Java, and another, which supports the entire ISO Prolog grammar,
 * written in Prolog.</p>
 *
 * <p>This parser may one day be extended to parse all of Prolog.</p>
 *
 * <p>Mini-Prolog is defined as follows:</p>
 *
 * <dl>
 *   <dt>Clause</dt>
 *           <dd>Head <strong>:-</strong> Body<strong>.</strong></dd>
 *   <dt>Clause</dt>
 *           <dd><strong>:-</strong> Body<strong>.</strong></dd>
 *   <dt>Clause</dt>
 *           <dd>Head<strong>.</strong></dd>
 *   <dt>Head</dt>
 *           <dd>Term</dd>
 *   <dt>Body</dt>
 *           <dd>TermList</dd>
 *   <dt>TermList</dt>
 *           <dd>Term (<strong>,</strong> Term)*</dd>
 *   <dt>Term</dt>
 *           <dd><strong>(</strong>Term<strong>)</strong></dd>
 *   <dt>Term</dt>
 *           <dd>Const</dd>
 *   <dt>Term</dt>
 *           <dd>Variable</dd>
 *   <dt>Term</dt>
 *           <dd>CompoundTerm</dd>
 *   <dt>Term</dt>
 *           <dd>List</dd>
 *   <dt>CompoundTerm</dt>
 *           <dd>ID <strong>(</strong>TermList<strong>)</strong></dd>
 *   <dt>List</dt>
 *           <dd><strong>[</strong>TermList(<strong>|</strong>Term)?<strong>]</strong></dd>
 *   <dt>Const</dt>
 *           <dd>ID | INTEGER | REAL | STRING</dd>
 *   <dt>Variable</dt>
 *           <dd>VARIABLE</dd>
 * </dl>
 *
 * <p>A pleasant property of this language is that it is LA(1).  It
 * never has to peek ahead more than one token.</p>
 **/
public class MiniPrologParser extends PrologParser {
  Io io;
  PrologEngine engine; // copied from Io for direct access.
  PrologFactory factory; // copied from Io for direct access.
  PrologTokenizer tokenizer; // copied from Io for direct access.
  
  CompoundTerm Comma;
  CompoundTerm Neck;
  CompoundTerm End;
  CompoundTerm Open;
  CompoundTerm Close;
  CompoundTerm OpenList;
  CompoundTerm CloseList;
  CompoundTerm HtSep;

  public MiniPrologParser (Io _io) 
  {
    io = _io;
    engine = io.engine; 
    factory = engine.factory; 
    tokenizer = io.tokenizer;

    Comma = factory.makeCompoundTerm ("name", factory.makeAtom (","));
    Neck = factory.makeCompoundTerm ("name", factory.makeAtom (":-"));

    End = tokenizer.End;
    Open = tokenizer.Open;
    Close = tokenizer.Close;
    OpenList = tokenizer.OpenList;
    CloseList = tokenizer.CloseList;
    HtSep = tokenizer.HtSep;
  }

  // -------------------------------------------------------------------------
  // ---- Public API ---------------------------------------------------------
  // -------------------------------------------------------------------------

  public PrologTerm parseClause (Io.PrologInput in) 
    throws IOException, PrologParseException
  {
    if (tokenizer.peek (in).getName ().equals ("end_of_file")) return null;
    return parseClause (in, new HashMap<String,PrologTerm> ()).uniqueVariables (); 
  }

  public PrologTerm parseTerm (Io.PrologInput in) 
    throws IOException, PrologParseException
  { 
    if (tokenizer.peek (in).getName ().equals ("end_of_file")) return null;
    return parseTerm (in, new HashMap<String,PrologTerm> ()).uniqueVariables (); 
  }

  // -------------------------------------------------------------------------
  // ---- Grammar ------------------------------------------------------------
  // -------------------------------------------------------------------------

  PrologTerm parseClause (Io.PrologInput in, Map<String,PrologTerm> vars) 
    throws IOException, PrologParseException
  {
    try {
      CompoundTerm firstToken = tokenizer.peek (in);
      if (firstToken.unify (Neck, false))
        {
          // --- query
          tokenizer.consume (in, Neck);
          // --- just do the body.
          CompoundTerm retval = 
            factory.makeCompoundTerm (":-", parseTermList (in, vars));
          tokenizer.consume (in, End);
          return retval;
        }

      // --- head
      PrologTerm head = parseTerm (in, vars);
      CompoundTerm nextToken = tokenizer.peek (in);
      if (!nextToken.unify (Neck, false)) 
        {
          tokenizer.consume (in, End);
          return head;
        }

      // --- optional body
      tokenizer.consume (in, Neck);
      PrologTerm tail = parseTermList (in, vars);
      tokenizer.consume (in, End);

      return factory.makeCompoundTerm (":-", head, tail);

    } catch (PrologParseException ex) {
      throw ex;
    }
  }

  PrologTerm parseTermList (Io.PrologInput in, Map<String,PrologTerm> vars) 
    throws IOException, PrologParseException
  {
    PrologTerm list = parseTerm (in, vars);
    try {
      CompoundTerm peek = tokenizer.peek (in);
      while (peek.unify (Comma, false))
        {
          tokenizer.consume (in, Comma);
          PrologTerm next = parseTerm (in, vars);
          list = appendToTermList (list, next);
          peek = tokenizer.peek (in);
        }

      //Util.out.println ("Parsed term list "+list.tag ());
    } catch (PrologParseException ex) {
      throw ex;
    }
    return list;
  }

  /**
   * Appends to the end of a term list so that it obeys the 'XFY
   * infix' notation that Prolog requires.
   *
   * Note that this is not correct in general, as it ignores the
   * priority of the terms it finds.
   **/
  CompoundTerm appendToTermList (PrologTerm termList, PrologTerm next)
  {
    if (!termList.getName ().equals (",") || termList.getArity () != 2)
      return factory.makeCompoundTerm (",", termList, next);
    else
      {
        CompoundTerm list = (CompoundTerm)termList;
        PrologTerm first = list.getSubterm (0);
        PrologTerm rest = list.getSubterm (1);
        return factory.makeCompoundTerm (",", first, 
                                         appendToTermList (rest, next));
      }
  }

  PrologTerm[] parseTermListArray (Io.PrologInput in, Map<String,PrologTerm> vars) 
    throws IOException, PrologParseException
  {
    List<PrologTerm> list = new ArrayList<PrologTerm> ();
    list.add (parseTerm (in, vars));
    CompoundTerm peek = tokenizer.peek (in);
    try {
      while (peek.unify (Comma, false))
        {
          tokenizer.consume (in, Comma);
          list.add (parseTerm (in, vars));
          peek = tokenizer.peek (in);
        }
    } catch (PrologParseException ex) {
      throw ex;
    }
    return (PrologTerm[])list.toArray (new PrologTerm[list.size ()]);
  }

  PrologTerm parseTerm (Io.PrologInput in, Map<String,PrologTerm> vars) 
    throws IOException, PrologParseException
  {
    CompoundTerm token = tokenizer.readToken (in);
    if (token.getName ().equals ("open_list"))
      return parseList (in, vars);
    else if (token.getName ().equals ("open"))
      {
        // --- parenthesized terms.
        PrologTerm t = parseTerm (in, vars);
        tokenizer.consume (in, Close);
        return t;
      }
    else if (token.getName ().equals ("integer"))
      return token.getSubterm (0);
    else if (token.getName ().equals ("float"))
      return token.getSubterm (0);
    else if (token.getName ().equals ("string"))
      return token.getSubterm (0);
    else if (token.getName ().equals ("variable"))
      {
        String name = token.getSubterm (0).getName ();
        PrologTerm retval = (PrologTerm)vars.get (name);
        if (retval == null)
          {
            retval = factory.makeVariable (name);
            vars.put (name, retval);
          }
        return retval;
      }
    else if (token.getName ().equals ("name"))
      {
        // at least an atom.
        if (!tokenizer.peek (in).getName ().equals ("open"))
          return token.getSubterm (0);
        else
          {
            String name = token.getSubterm (0).getName ();
            tokenizer.consume (in, Open);
            PrologTerm[] subterms = parseTermListArray (in, vars);
            tokenizer.consume (in, Close);
            return factory.makeCompoundTerm (name, subterms);
          }
      }
    else
      {
        throw new PrologParseException ("Unexpected token "+token.tag ());
      }
  }

  // -------------------------------------------------------------------------
  // ---- List parsing -------------------------------------------------------
  // -------------------------------------------------------------------------


  PrologTerm parseList (Io.PrologInput in, Map<String,PrologTerm> vars) 
    throws IOException, PrologParseException
  {
    CompoundTerm list = 
      factory.makeCompoundTerm (".", 
                                parseTerm (in, vars), 
                                engine.factory.makeEmptyList ());
    try {
      CompoundTerm peek = tokenizer.peek (in);
      while (peek.unify (Comma, false))
        {
          tokenizer.consume (in, Comma);
          PrologTerm next = parseTerm (in, vars);
          list = list.append (next);
          peek = tokenizer.peek (in);
        }

      if (peek.unify (HtSep, false))
        {
          tokenizer.consume (in, HtSep);
          PrologTerm next = parseTerm (in, vars);
          list = list.completeList (next);
        }

      tokenizer.consume (in, CloseList);

    } catch (PrologParseException ex) {
      throw ex;
    }

    return list;
  }


}
