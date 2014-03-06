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
 * VariableCollector.java
 *
 */
package com.svincent.moksa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Collects all the variables out of an PrologTerm compoundTerm. <p>
 *
 * For debugging purposes.  XXX Maybe move off to subpackage. <p>
 **/
public class VariableCollector extends PrologTermVisitor {

  public static final VariableCollector Inst = new VariableCollector ();

  /**
   * Primary interface method.
   **/
  public static Collection<Variable> getVariables (PrologTerm a)
  {
    Map<?, Variable> map = getVariableMap (a);
    return map.values ();
  }

  public static Map<String,Variable> getVariableMap (PrologTerm a)
  {
    Map<String, Variable> l = new HashMap<String, Variable> ();
    a.accept (Inst, l);
    return l;
  }

  // -------------------------------------------------------------------------
  // ---- Implementation: visit methods --------------------------------------
  // -------------------------------------------------------------------------
  
  public Object visitVariable (Variable v, Object _l)
  { 
    @SuppressWarnings("unchecked")
	Map<String, Variable> l = (Map<String, Variable>)_l;
    //Util.out.println ("Found ("+v+") "+v.tag ());
    l.put (v.getName (), v);
    return null;
  }

  public Object visitCompoundTerm (CompoundTerm v, Object parm)
  { 
    //Util.out.println ("Found "+v.tag ());
    //Util.out.println (">>> visiting children");
    visitChildren (v, parm);
    //Util.out.println ("<<< done visiting children");
    return null;
  }

}
