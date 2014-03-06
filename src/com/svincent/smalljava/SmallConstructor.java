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
 * SmallConstructor.java 
 *
 * A Constructor, in SmallJava.
 *
 */

package com.svincent.smalljava;

import java.lang.reflect.*;
import java.util.*;

import com.svincent.util.*;

/**
 * Represents a constructor declaration.  To create new constructor
 * declaration, use one of the constructor() methods in SmallClass.
 *
 * @see SmallClass
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a> 
 **/
public class SmallConstructor extends SmallMethod
{
  // -------------------------------------------------------------------------
  // ---- Constructors -------------------------------------------------------
  // -------------------------------------------------------------------------

  public SmallConstructor (SmallType.MethodType _type, 
                           String[] _argNames)
    throws SmallJavaBuildingException
  { this (Modifier.PUBLIC, _type, _argNames); }

  public SmallConstructor (int _modifiers, SmallType.MethodType _type, 
                           String[] _argNames)
    throws SmallJavaBuildingException
  { this (_modifiers, _type.descriptorToString (), _type, _argNames); }

  public SmallConstructor (String _descriptor, String[] _argNames)
    throws SmallJavaBuildingException
  { this (Modifier.PUBLIC, _descriptor, _argNames); }

  public SmallConstructor (int _modifiers, String _descriptor, 
                           String[] _argNames)
    throws SmallJavaBuildingException
  { 
    this (_modifiers, _descriptor, 
          SmallJavaUtil.parseTypeFromMethodDescriptor (_descriptor, 
                                                      _argNames.length), 
          _argNames); 
  }

  public SmallConstructor (int _modifiers, String _descriptor, 
                           SmallType.MethodType _type, String[] _argNames)
    throws SmallJavaBuildingException
  {
    super (_modifiers, "<init>", _descriptor, _type, _argNames);
  }


  // -------------------------------------------------------------------------
  // ---- Java code generation -----------------------------------------------
  // -------------------------------------------------------------------------

  public void writeAsJava (IndentPrintWriter out)
  {
    out.print (Modifier.toString (modifiers));
    out.print (' ');
    out.print (getDeclaringClass ().getName ());
    out.print (' ');
    out.print ('(');
    for (int i=0; i<getArgCount (); i++)
      {
        if (i>0) out.print (", ");
        getArgType (i).writeAsJava (out);
        out.print (' ');
        out.print (getArgName (i));
      }
    out.println (')');

    writeBodyAsJava (out);
  }
}
