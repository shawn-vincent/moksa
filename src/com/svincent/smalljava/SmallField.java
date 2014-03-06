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
 * SmallMethod.java 
 *
 * A Method, in SmallJava.
 *
 */

package com.svincent.smalljava;

import java.lang.reflect.*;
import java.util.*;

import com.svincent.util.*;

import com.svincent.smalljava.rhino.*;

/**
 * Represents a field on a class.
 *
 * <p>Normally, SmallFields are created implicitly, when {@link
 * SmallClass#field SmallClass.field()} is called while building a
 * class.</p>
 *
 * @see SmallClass#field
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a> 
 **/
public class SmallField extends SmallMember
{
  String descriptor;
  String name;

  /* parsed from descriptor */

  SmallType type = null;

  // -------------------------------------------------------------------------
  // ---- Constructors -------------------------------------------------------
  // -------------------------------------------------------------------------

  public SmallField (SmallType _type, String _name)
    throws SmallJavaBuildingException
  { this (Modifier.PROTECTED, _type, _name); }

  public SmallField (int _modifiers, SmallType _type, String _name)
    throws SmallJavaBuildingException
  { this (_modifiers, _type.descriptorToString (), _type, _name); }

  public SmallField (String _descriptor, String _name)
    throws SmallJavaBuildingException
  { this (Modifier.PROTECTED, _descriptor, _name); }

  public SmallField (int _modifiers, String _descriptor, String _name)
    throws SmallJavaBuildingException
  { 
    this (_modifiers, _descriptor,
          SmallJavaUtil.parseFieldDescriptor (_descriptor), _name); 
  }

  protected SmallField (int _modifiers, String _descriptor, 
                        SmallType _type, String _name)
  { super (_modifiers); descriptor = _descriptor; name = _name; type = _type; }

  // -------------------------------------------------------------------------
  // ---- Accessors ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /** Return the name of this field. */
  public String getName () { return name; }

  /** Return the type of this field. */
  public SmallType getType () { return type; }

  // -------------------------------------------------------------------------
  // ---- Bytecode Generation ------------------------------------------------
  // -------------------------------------------------------------------------

  protected void writeAsBytecodes (ClassFileWriter out)
  {
    out.addField (getName (), 
                  getType ().descriptorToString (),
                  (short)modifiers);
  }

  // -------------------------------------------------------------------------
  // ---- Java code generation -----------------------------------------------
  // -------------------------------------------------------------------------

  /** Writes this field declaration as Java source code. */
  public void writeAsJava (IndentPrintWriter out)
  {
    out.print (Modifier.toString (modifiers));
    out.print (' ');
    type.writeAsJava (out);
    out.print (' ');
    out.print (name);
    out.println (';');
  }


}
