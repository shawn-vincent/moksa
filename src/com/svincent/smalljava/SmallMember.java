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
 * SmallMember.java 
 *
 */

package com.svincent.smalljava;

import java.lang.reflect.*;
import java.util.*;

import com.svincent.util.*;


/**
 * <p>Represents a member of a class.</p>
 *
 * @see SmallClass
 * @see SmallConstructor
 * @see SmallMethod
 * @see SmallField
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a> 
 **/
public abstract class SmallMember extends BaseObject
{
  /** Declaring class: set when added to a SmallClass object.*/
  SmallClass declaringClass = null;

  int modifiers;

  // -------------------------------------------------------------------------
  // ---- Constructors -------------------------------------------------------
  // -------------------------------------------------------------------------

  protected SmallMember (int _modifiers) { modifiers = _modifiers; }

  // -------------------------------------------------------------------------
  // ---- Accessors ----------------------------------------------------------
  // -------------------------------------------------------------------------

  public boolean isStatic () { return Modifier.isStatic (modifiers); }

  /** 
   * Set the declaring class.  
   * Called when a member is added to a SmallClass. 
   **/
  protected void setDeclaringClass (SmallClass _declaringClass) 
  { declaringClass = _declaringClass; }

  /** 
   * Returns the 'declaring' class: that is, the class this member is 
   * declared within. 
   **/
  public SmallClass getDeclaringClass () { return declaringClass; }

  protected void finalize () 
    throws SmallJavaValidationException
  {}

  // -------------------------------------------------------------------------
  // ---- Bytecode Generation ------------------------------------------------
  // -------------------------------------------------------------------------

  // member-specific.

  // -------------------------------------------------------------------------
  // ---- Java code generation -----------------------------------------------
  // -------------------------------------------------------------------------

  /** Writes this field declaration as Java source code. */
  public abstract void writeAsJava (IndentPrintWriter out);
}
