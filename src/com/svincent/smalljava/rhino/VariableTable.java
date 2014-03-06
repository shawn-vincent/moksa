/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * The contents of this file are subject to the Netscape Public License
 * Version 1.0 (the "NPL"); you may not use this file except in
 * compliance with the NPL.  You may obtain a copy of the NPL at
 * http://www.mozilla.org/NPL/
 *
 * Software distributed under the NPL is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the NPL
 * for the specific language governing rights and limitations under the
 * NPL.
 *
 * The Initial Developer of this code under the NPL is Netscape
 * Communications Corporation.  Portions created by Netscape are
 * Copyright (C) 1997-1999 Netscape Communications Corporation.  All Rights
 * Reserved.
 *
 * This file was originally distributed with Rhino.  I have copied it
 * into SmallJava to avoid enforcing a dependency on Rhino being
 * installed (and to avoid conflicting with installed versions of
 * Rhino) -- all hail Java's extraordinarily bad package system!
 */

package com.svincent.smalljava.rhino;

import java.io.*;
import java.util.*;

// XXX SPV: changed name->pos mapping to a name->var mapping.  The
// XXX other was broken anyway.
public class VariableTable {

    public int size()
    {
        return itsVariables.size();
    }

    public int getParameterCount()
    {
        return varStart;
    }
    
    public LocalVariable createLocalVariable(String name, boolean isParameter) 
    {
        return createLocalVariable (name, isParameter, "Ljava/lang/Object;");
    }

    public LocalVariable createLocalVariable(String name, boolean isParameter,
                                             String typeDescriptor) 
    {
        return new LocalVariable(name, isParameter, typeDescriptor);
    }

    public LocalVariable get(int index)
    {
        return (LocalVariable)(itsVariables.elementAt(index));
    }

    public LocalVariable get(String name)
    {
        return (LocalVariable)itsVariableNames.get(name);
    }

    public int getOrdinal(String name) {
        LocalVariable var = (LocalVariable)itsVariableNames.get(name);
        if (var == null) return -1;

        for (int i = 0; i < itsVariables.size(); i++) {
            LocalVariable lVar = (LocalVariable)(itsVariables.elementAt(i));
            if (lVar == var) return i;
        }
        // XXX huh?  Shouldn't happen.
        throw new RuntimeException ("Assertion failed");
    }

    public String getName(int index)
    {
        return ((LocalVariable)(itsVariables.elementAt(index))).getName();
    }

    public void establishIndices()
    {
        establishIndices (false);
    }

    public void establishIndices(boolean insertExtra)
    {
        for (int i = 0; i < itsVariables.size(); i++) {
            LocalVariable lVar = (LocalVariable)(itsVariables.elementAt(i));
            lVar.setIndex(i+(insertExtra ? 1 : 0));
        }
    }

    public void addParameter(String pName)
    { addParameter (pName, "Ljava/lang/Object;"); }

    public void addParameter(String pName, String typeDescriptor)
    {
        LocalVariable existingVar = (LocalVariable)itsVariableNames.get(pName);
        if (existingVar != null && existingVar.isParameter()) {
            System.err.println ("XXX duplicate parameter: "+pName);
        }
        int curIndex = varStart++;
        LocalVariable lVar = createLocalVariable(pName, true, typeDescriptor);
        itsVariables.insertElementAt(lVar, curIndex);
        itsVariableNames.put(pName, lVar);
    }

    public void addLocal(String vName)
    { addLocal (vName, "Ljava/lang/Object;"); }

    public void addLocal(String vName, String typeDescriptor)
    {
        LocalVariable existingVar = (LocalVariable)itsVariableNames.get(vName);
        if (existingVar != null) {
            if (existingVar.isParameter()) {
                // this is o.k. the parameter subsumes the variable def.
            }
            else {
                return;
            }
        }
        int index = itsVariables.size();
        LocalVariable lVar = createLocalVariable(vName, false, typeDescriptor);
        itsVariables.addElement(lVar);
        itsVariableNames.put(vName, lVar);
    }

    public String tag () { return itsVariables.toString (); }
    
    // a list of the formal parameters and local variables
    protected Vector itsVariables = new Vector();    

    // mapping from name to variables in list
    protected Hashtable itsVariableNames = new Hashtable(11);   

    protected int varStart;               // index in list of first variable

}
