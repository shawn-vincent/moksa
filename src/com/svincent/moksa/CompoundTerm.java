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
 * CompoundTerm.java
 *
 * Basic implementation of a standard Prolog functor (i.e. - foo (3, 4, bar). )
 *
 */
package com.svincent.moksa;

import java.io.*;
import java.util.*;

import com.svincent.util.*;

/**
 * <p>A compoundTerm is a Prolog tree constant (normally of the form
 * <code>f(x<sub>1</sub>, x<sub>2</sub>, ..., x<sub>n</sub>)</code>,
 * where <code>f</code> is known as the compoundTerm's <em>functor</em>,
 * and <code>x<sub>1</sub></code> through <code>x<sub>n<sub></code>
 * are known as the term's <em>subterms</em>. </p>
 *
 * <p>The value <code>n</code> is known as the compoundTerm's
 * <em>arity</em>. </p>
 **/
public class CompoundTerm extends PrologTerm {

  public static final CompoundTerm[] EmptyArray = new CompoundTerm[0];

  /** Reference to CompoundTerm's name. **/
  String name;

  /** Reference to CompoundTerm's subterms. **/
  PrologTerm[] subterms;

  /** Our priority. */
  int priority;

  // -------------------------------------------------------------------------
  // ---- Constructors -------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Builds a new CompoundTerm with the given name.  Note that CompoundTerms
   * are intended to be used as constants, so no building API is
   * provided. <p>
   **/
  public CompoundTerm (PrologEngine _engine, String _name, 
                       PrologTerm[] _subterms, int _priority)
  {
    super (_engine);
    name = _name;
    subterms = _subterms;
    priority = _priority;
  }

  // -------------------------------------------------------------------------
  // ---- Accessors ----------------------------------------------------------
  // -------------------------------------------------------------------------

  public int getPriority () { return priority; }

  /** Retrieves this CompoundTerm's name. */
  public String getName () { return name; }

  public PrologTerm getNameConstant ()
  { 
    // XXX slow.
    String name = getName ();

    try {
      return engine.factory.makeInteger (Integer.parseInt (name));
    } catch (NumberFormatException ex) {
    }

    try {
      return engine.factory.makeFloat (Double.parseDouble (name));
    } catch (NumberFormatException ex) {
    }

    return engine.factory.makeCompoundTerm (getName ()); 
  }

  /** Retrives this CompoundTerm's arity. */
  public int getArity () { return subterms.length; }

  /** Retrives this CompoundTerm's arity, as a constant. */
  public WamInteger getArityConstant () 
  { 
    // XXX maybe cache this information.
    return engine.factory.makeInteger (getArity ()); 
  }

  /** Return true iff this is a CompoundTerm or an Atom */
  public boolean isStructure () { return true; }

  /** Returns true iff this is a CompoundTerm. */
  public boolean isCompoundTerm () { return getArity () > 0; }

  /** Returns true iff this is an Atom. */
  public boolean isAtom () { return getArity () == 0; }

  /** Returns true iff this is a constant: i.e. an Atom, an integer
      constant, or a floating-point constant. */
  public boolean isConstant () { return isAtom (); }


  /** 
   * Retrieves this CompoundTerm's value. <p>
   *
   * @param idx - the index of the subterm to retrieve: must be in the
   * range <code>0</code> through <code>getArity () - 1</code>,
   * inclusively.
   **/
  public PrologTerm getSubterm (int idx) 
  { 
    try {
      return subterms[idx]; 
    } catch (ArrayIndexOutOfBoundsException ex) {
      throw new IndexOutOfBoundsException 
        ("Illegal attempt to get subterm at index "+idx+" from functor "+
         getName ()+" with arity "+getArity ());
    }
  }

  public PrologTerm getSubtermDeref (int idx) 
  { return getSubterm (idx).deref (); }

  /** Allow PrologTermVisitor to access this PrologTerm. */
  public Object accept (PrologTermVisitor v, Object parm) 
  { return v.visitCompoundTerm (this, parm); }

  // -------------------------------------------------------------------------
  // ---- Implementation -----------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Unification algorithm.  Can cause variables to become bound. (We
   * will eventually have a rollback mechanism to undo these
   * bindings.) <p>
   *
   * @return true iff the two PrologTerms successfully unify.
   **/
  public boolean unify (PrologTerm _that, boolean doBindings) 
  {
    // --- variables are easy to deal with.
    if (_that.isVariable ())
      return _that.unify (this, doBindings);

    if (!_that.isStructure ())
      {
        if (engine.debugUnify)
          engine.log ().println 
            ("Unify Failed: Got attempt to unify compoundTerm with "+
             _that.getClass ().getName ());
        return false;
      }

    // --- other things ain't so easy.

    CompoundTerm that = (CompoundTerm)_that;

    // --- first, arities must be the same.  Otherwise, no dice.
    int count = this.getArity ();
    if (that.getArity () != count) 
      {
        if (engine.debugUnify)
          engine.log ().println 
            ("Unify Failed: Got bad arity: wanted "+this.tag ()+
             " ("+this.getArity ()+
             "), got "+that.tag ()+" ("+that.getArity ()+")");
        return false;
      }

    // --- second, name must be the same.  Again, otherwise, no dice.
    // XXX somehow unique these names.
    if (!this.getName ().equals (that.getName ()))
      {
        if (engine.debugUnify)
          engine.log ().println 
            ("Unify Failed: Got bad name: wanted "+this.getName ()+
             ", got "+that.getName ());
        return false;
      }

    if (engine.debugUnify) engine.log ().indent ();

    // --- third, all subterms must unify.
    for (int i=0; i<count; i++)
      {
        PrologTerm thisSubterm = this.getSubterm (i).deref ();
        PrologTerm thatSubterm = that.getSubterm (i).deref ();

        // --- no dice if *any* of them fail.
        if (!thisSubterm.unify (thatSubterm, doBindings))
          {
            if (engine.debugUnify)
              engine.log ().println 
                ("Unify Failed: Subterm failed to unify:  wanted "+
                 thisSubterm.tag ()+", got "+
                 thatSubterm.tag ());

            if (engine.debugUnify) engine.log ().outdent ();
            return false;
          }
      }

    if (engine.debugUnify) engine.log ().outdent ();

    // --- finally, everything seems to agree.  Finis.
    return true;
  }

  public PrologTerm clonePrologTerm (Map<PrologTerm,PrologTerm> objs)
  {
    PrologTerm retval = (PrologTerm)objs.get (this);
    if (retval == null)
      {
       int arity = getArity ();
       PrologTerm[] clonedSubterms = new PrologTerm[arity];
       for (int i=0; i<arity; i++)
         clonedSubterms[i] = getSubterm(i).clonePrologTerm (objs);
       retval = engine.factory.makeCompoundTerm (getName (), clonedSubterms, 
                                                 getPriority ());
       objs.put (this, retval);
      }
    return retval;
  }

  PrologTerm uniqueVariables (Map<String,Variable> vars)
  {
    int arity = getArity ();
    PrologTerm[] clonedSubterms = new PrologTerm[arity];
    for (int i=0; i<arity; i++)
      clonedSubterms[i] = getSubterm(i).uniqueVariables (vars);

    return engine.factory.makeCompoundTerm (getName (), clonedSubterms, 
                                            getPriority ());
  }

  /**
   * Evaluates this term as an expression.
   **/
  public PrologTerm evaluateExpression () throws PrologException
  {
    if (isAtom ()) return this;

    // --- lookup the appropriate handler for this structure.
    PrologEngine.ArithmeticHandler ah = 
      engine.lookupArithmeticHandler (getName ()+"/"+getArity ());

    // XXX wrong error.
    if (ah == null)
      throw new PrologException ("type error: number, "+this.tag ());

    return ah.evaluate (subterms); 
  }


  // -------------------------------------------------------------------------
  // ---- Print Behavior -----------------------------------------------------
  // -------------------------------------------------------------------------

  void writePrintableName (PrintWriter out)
  {
    String realName = getName ();
    for (int i=0; i<realName.length (); i++)
      {
        if (!isPrintable (realName.charAt (i)))
          {
            out.write ('\'');
            out.write (realName);
            out.write ('\'');
            return;
          }
      }
    out.write (realName);
  }

  boolean isPrintable (char c)
  { return Character.isJavaIdentifierPart (c); }

  // XXX fix to do something fancier.
  public boolean isInfix () 
  { return false; } //getName ().equals (":-") || getName ().equals (","); }

  // XXX fix to do something fancier.
  public boolean isPrefix () { return false; }

  // XXX fix to do something fancier.
  public boolean isPostfix () { return false; }

  public boolean isList () 
  { return isEmptyList () || isNonEmptyList (); }

  public boolean isEmptyList () 
  { return getArity () == 0 && getName ().equals ("[]"); }

  public boolean isNonEmptyList () 
  { return getArity () == 2 && getName ().equals ("."); }

  /**
   * Writes a short human-readable representation of this object to
   * the given Writer. <p>
   **/
  protected void tag (PrintWriter out)
  {
    if (isInfix () && getArity () == 2)
      {
        // infix is implicitly 2 parameter.
        out.write ('(');
        getSubterm (0).deref ().tag (out);
        out.write (')');
        out.write (' ');
        out.write (name);
        out.write (' ');
        out.write ('(');
        getSubterm (1).deref ().tag (out);
        out.write (')');
      }
    else if (isPrefix () && getArity () == 1)
      {
        // prefix is implicitly 1 parameter.
        out.write (name);
        out.write (' ');
        getSubterm (0).deref ().tag (out);
      }
    else if (isPostfix () && getArity () == 1)
      {
        // postfix is implicitly 1 parameter.
        getSubterm (0).deref ().tag (out);
        out.write (' ');
        out.write (name);
      }
    else if (isList ())
      {
        listTag (out);
      }
    else if (isAtom ())
      {
        out.write (getName ());
      }
    else
      {
        writePrintableName (out);

        int count = getArity ();
        if (count > 0)
          {
            out.write ('(');

            for (int i=0; i<count; i++)
              {
                if (i>0) out.write (", ");
                getSubterm (i).deref ().tag (out);
              }

            out.write (')');
          }
      }

  }

  void listTag (PrintWriter out)
  {
    out.write ('[');
    listBodyTag (out);
    out.write (']');
  }

  void listBodyTag (PrintWriter out)
  {
    PrologIterator i = asList ().prologIterator ();
    while (i.hasNext ())
      {
        PrologTerm a = i.nextPrologTerm ().deref ();
        a.tag (out);
        if (i.hasNext ()) out.write (", ");
      }

    PrologTerm terminator = i.terminator ();

    if (terminator != null)
      {
        out.write (" | ");
        terminator.deref ().tag (out);
      }
  }

  // -------------------------------------------------------------------------
  // ---- Manipulation as List -----------------------------------------------
  // -------------------------------------------------------------------------

  public ListWrapper asList ()
  {
    if (!isList ()) return null;
    return new ListWrapper (this);
  }

  CompoundTerm append (PrologTerm obj)
  {
    Util.assertTrue (isNonEmptyList ());

    PrologFactory factory = engine.factory;

    // .(a, [])
    //    -->
    // .(a, .(obj, []))
    if (!getSubterm (1).isNonEmptyList ())
      {
        return factory.makeCompoundTerm 
          (getName (), 
           new PrologTerm[] {
             getSubterm (0),
             factory.makeCompoundTerm (".",
                                       new PrologTerm[] {
                                         obj, 
                                         getSubterm (1)
                                       },
                                       getPriority ())
           },
           getPriority ());
      }
    // .(a, l)
    //    -->
    // .(a, l.append (obj))
    else 
      {
        return factory.makeCompoundTerm
          (getName (),
           new PrologTerm[] {
             getSubterm (0),
             ((CompoundTerm)getSubterm (1)).append (obj)
           },
           getPriority ());
      }
  }

  /**
   * We are
   *    .(a, .(b, .(c, [])))
   * We want to be
   *    .(a, .(b, .(c, obj)))
   *
   */
  CompoundTerm completeList (PrologTerm obj)
  {
    Util.assertTrue (isNonEmptyList ());

    PrologFactory factory = engine.factory;

    // .(a, [])
    //    -->
    // .(a, obj)
    if (!getSubterm(1).isNonEmptyList ())
      {
        return factory.makeCompoundTerm (getName (),
                                         new PrologTerm[] {
                                           getSubterm (0),
                                           obj
                                         },
                                         getPriority ());
      }
    // .(a, l)
    //    -->
    // .(a, l.completeList (obj))
    else 
      {
        return factory.makeCompoundTerm 
          (getName (),
           new PrologTerm[] {
             getSubterm (0),
             ((CompoundTerm)getSubterm(1)).completeList(obj)
           },
           getPriority ());
      }
  }

  /**
   * <p>A wrapper for a CompoundTerm as a list.</p>
   *
   * <p>There is a caveat: since Prolog can have lists like this:
   * <code>[term, term, term | term]</code>, this list implementation
   * ignores the last term (as it is not part of the list proper).  If
   * you want to see it, use the <code>prologIterator</code> method to
   * get a <code>PrologIterator</code>, which will provide you with
   * this information.</p>
   **/
  public static class ListWrapper extends AbstractList<PrologTerm> {
    CompoundTerm compoundTerm;

    public ListWrapper (CompoundTerm _compoundTerm) 
    { compoundTerm = _compoundTerm; }

    public PrologIterator prologIterator () 
    { return new PrologIterator (compoundTerm); }

    /**
     * <p>Finds the first term in this list which can unify (without
     * variable binding) with the given key. </p>
     *
     * <p>For example, in the list:</p>
     *
     * <p><code>[foo(3), bar (4)]</code></p>
     *
     * <p><code>findUnifiable (bar (X))</code> would result in
     * <code>bar(4)</code></p>
     *
     * <p>If no such term is found, <code>null</code> is returned.</p>
     **/
    public PrologTerm findUnifiable (PrologTerm key)
    {
      PrologIterator i = prologIterator ();
      while (i.hasNext ())
        {
          PrologTerm t = i.nextPrologTerm ();
          if (t.unifyWithoutBindings (key)) return t;
        }
      return null;
    }

    public String getOption (PrologTerm key)
    {
      CompoundTerm value = (CompoundTerm)findUnifiable (key);
      return value == null ? null : value.getSubterm (0).getName ();
    }

    /**
     * Part of java.util.List implementation.  Return an iterator over
     * the elements of this list.
     **/
    public Iterator<PrologTerm> iterator () 
    { return prologIterator (); }

    /**
     * Part of java.util.List implementation.  Return the element of
     * this list at the specified index.
     **/
    public PrologTerm get (int index)
    { 
      Iterator<PrologTerm> i = iterator ();
      int count = 0;
      while (i.hasNext ()) 
        { 
          PrologTerm o = i.next (); 
          if (count == index) return o;
          count++; 
        }
      throw new IndexOutOfBoundsException 
        ("Tried to access index "+i+" of a list: size of list is only "+count);
    }

    /**
     * Part of java.util.List implementation.  Return the size of this
     * list.
     **/
    public int size ()
    { 
      Iterator<PrologTerm> i = iterator ();
      int count = 0;
      while (i.hasNext ()) { i.next (); count++; }
      return count;
    }

    public String tag () { return compoundTerm.tag (); }

  }

  /**
   * 
   */
  public static class PrologIterator extends BaseObject implements Iterator<PrologTerm> {
    boolean hasNext;
    CompoundTerm current;
    PrologTerm terminator;

    /*
      .(a, .(b, [])) -> a, b, terminator=null

      .(a, .(b, c)) -> a, b, terminator=c
     */

    public PrologIterator (CompoundTerm head)
    { 
      current = (CompoundTerm)head.deref (); 
      hasNext = !current.isEmptyList ();
      //Util.out.println ("current == "+current.getName ()+", hasNext == "+hasNext);
    }

    public boolean hasNext ()
    { return hasNext; }

    public PrologTerm nextPrologTerm ()
    { 
      if (!hasNext) throw new NoSuchElementException ();
      PrologTerm retval = current.getSubterm (0).deref (); 
      advance (); 
      return retval;
    }

    public PrologTerm terminator () { return terminator; }

    public PrologTerm next () { return nextPrologTerm (); }
    public void remove () { throw new UnsupportedOperationException (); }

    void advance ()
    {
      if (current == null) return;

      PrologTerm tail = current.getSubterm (1).deref ();
      if (tail.isNonEmptyList ())
        {
          //Util.out.println ("Got current == "+tail.getName ());
          current = (CompoundTerm)tail;
          return;
        }
      else
        {
          current = null;
          hasNext = false;
          //Util.out.println ("Oops, got tail  == "+tail.getName ());
          if (tail.isEmptyList ())
            terminator = null;
          else
            terminator = tail;
        }
    }
  }
}
