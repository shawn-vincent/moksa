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
 * Represents a method declaration.  To create new method declaration,
 * use one of the method() methods in SmallClass.
 *
 * @see SmallClass
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a> 
 **/
public class SmallMethod extends SmallMember
{
  String[] argNames;

  Expr.Begin body = new Expr.Begin ();
  Map locals = new HashMap ();

  List throwsList = new ArrayList ();

  int nextLocalVarIndex = 0;

  String name = null;
  String descriptor = null;
  SmallType.MethodType type = null;

  // -------------------------------------------------------------------------
  // ---- Constructors -------------------------------------------------------
  // -------------------------------------------------------------------------

  public SmallMethod (String _signature, String[] _argNames)
    throws SmallJavaBuildingException
  { this (Modifier.PUBLIC, _signature, _argNames); }

  public SmallMethod (int _modifiers, String _signature, String[] _argNames)
    throws SmallJavaBuildingException
  {
    this (_modifiers, 
          SmallJavaUtil.parseNameFromMethodSignature (_signature),
          SmallJavaUtil.parseDescriptorFromMethodSignature (_signature),
          _argNames);
  }

  protected SmallMethod (int _modifiers, String _name, 
                         String _descriptor, String[] _argNames)
    throws SmallJavaBuildingException
  {
    this (_modifiers,
          _name,
          _descriptor,
          SmallJavaUtil.parseTypeFromMethodDescriptor (_descriptor, 
                                                      _argNames.length),
          _argNames);
  }


  public SmallMethod (String _name, SmallType.MethodType _type, 
                      String[] _argNames)
    throws SmallJavaBuildingException
  { this (Modifier.PUBLIC, _name, _type, _argNames); }

  public SmallMethod (int _modifiers, String _name,
                      SmallType.MethodType _type, String[] _argNames)
    throws SmallJavaBuildingException
  {
    this (_modifiers, _name, _type.descriptorToString (), _type, _argNames);
  }

  protected SmallMethod (int _modifiers, String _name, String _descriptor,
                         SmallType.MethodType _type, String[] _argNames)
    throws SmallJavaBuildingException
  {
    super (_modifiers);
    name = _name;
    descriptor = _descriptor;
    type = _type;
    argNames = _argNames;

    // --- figure out the next var index to allocate.

    // --- one for 'this'
    if (!Modifier.isStatic (modifiers))
      nextLocalVarIndex++;

    // --- one for each parameter.
    for (int i=0; i<getArgCount (); i++)
      addParameter (getArgType (i), getArgName (i));
  }

  // -------------------------------------------------------------------------
  // ---- Accessors ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /** Return the name of this method (not including its descriptor) */
  public String getName () { return name; }

  /** Return this method's type descriptor. */
  public String getDescriptor () { return descriptor; }

  /** Return the number of parameters this function has. */
  public int getArgCount () { return argNames.length; }

  /** Get the type of argument 'i'. */
  public SmallType getArgType (int i) { return type.getArgType (i); }
  /** Get the types of all arguments. */
  public SmallType[] getArgTypes () { return type.getArgTypes (); }

  /** Return the name of argument 'i' */
  public String getArgName (int i) { return argNames[i]; }
  /** Return the names of all arguments */
  public String[] getArgNames () { return argNames; }

  /** Return the type of the return value of this method.*/
  public SmallType getReturnType () { return type.getReturnType (); }

  /** Return the type of this method.*/
  public SmallType.MethodType getType () { return type; }

  /**
   * Add a throws clause to this method for the given exception class name.
   **/
  public void addThrows (String exClassName) { throwsList.add (exClassName); }

  // -------------------------------------------------------------------------
  // ---- Local Variable support ---------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Get the local variable with the given name.
   */
  public Local getLocal (String name)
  { 
    Local retval = (Local)locals.get (name); 
    Util.assertTrue (retval != null, 
                 "Could not find local variable declaration for '"+name+"'");
    return retval;
  }

  /**
   * Return an iterator over all local variables of this method.
   */
  public Iterator locals () { return locals.values ().iterator (); }

  /**
   * Add a new parameter to this method: called from the constructor.
   */
  protected void addParameter (SmallType type, String name)
    throws SmallJavaBuildingException
  {
    Parameter l = new Parameter (this, type, name);
    locals.put (name, l);
  }

  /**
   * When a new local variable is added, its index is assigned using
   * this method.
   */
  protected int allocVarIndex () { return nextLocalVarIndex++; }

  /** 
   * Represents a local variable. 
   **/
  public abstract static class Local extends BaseObject
  {
    SmallMethod method;
    SmallType type;
    String name;

    int index;

    protected Local (SmallMethod _method, SmallType _type, String _name)
      throws SmallJavaBuildingException
    { 
      method = _method; type = _type; name = _name;
      index = method.allocVarIndex ();
      Util.assertTrue(index>=0, "Got index == "+index);
    }

    /** 
     * Return the index of this local variable.  In SmallJava, index 0
     * is reserved for 'this' in non-static methods, 1 (or 0) to n are
     * reserved for the method's n parameters, and subsequent indexes are 
     * unused.
     */
    public int getIndex () { return index; }

    /**
     * Returns the name of this local variable.
     */
    public String getName () { return name; }

    /**
     * Returns the type of this local variable.
     */
    public SmallType getType () { return type; }

    /**
     * Write this variable declaration as a Java statement, like "int i;".
     */
    public abstract void writeVarDeclAsJava (IndentPrintWriter out);

  }

  /**
   * A parameter local variable.
   */
  private static class Parameter extends Local {
    Parameter (SmallMethod _method, SmallType _type, String _name)
      throws SmallJavaBuildingException
    { super (_method, _type, _name); }

    public void writeVarDeclAsJava (IndentPrintWriter out)
    {
      // do nothing.
    }
  }

  /**
   * A non-parameter local variable.
   */
  private static class UserLocal extends Local {
    UserLocal (SmallMethod _method, SmallType _type, String _name)
      throws SmallJavaBuildingException
    { super (_method, _type, _name); }

    public void writeVarDeclAsJava (IndentPrintWriter out)
    {
      this.type.writeAsJava (out);
      out.print (' ');
      out.print (this.name);
      out.println (';');
    }
  }

  // -------------------------------------------------------------------------
  // ---- Builder API --------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Add a new expression to this method, and returns, for your
   *  convenience, the SmallMethod again, so a convenient and
   *  wonderful method.add ().add ()... form of programming.
   **/
  public SmallMethod add (Expr expr) { body.add (expr); return this; }

  /**
   * Add a new local variable to this method, with the given type descriptor
   * and initializer.
   **/
  public void local (String descriptor, String name, Expr initializer)
    throws SmallJavaBuildingException
  {
    local (SmallJavaUtil.parseFieldDescriptor (descriptor), 
           name, initializer);
  }

  /**
   * Add a new local variable to this method, with the given type and
   * initializer.  
   */
  public void local (SmallType type, String name, Expr initializer)
      throws SmallJavaBuildingException
  { addLocal (new UserLocal (this, type, name), initializer); }

  /**
   * Adds a new local variable to this method.  It is usually more
   * convenient to call {@link #local(String,String,Expr) local}
   * instead.
   *
   * @see #local(String,String,Expr)
   * @see #local(SmallType,String,Expr)
   **/
  public void addLocal (Local l, Expr initializer)
    throws SmallJavaBuildingException
  {
    locals.put (l.getName (), l);
    add (new Expr.SetLocal (l.getName (), initializer));
  }

  protected void finalize () throws SmallJavaValidationException
  {
    body.evalType (this);
  }

  // -------------------------------------------------------------------------
  // ---- Bytecode Generation ------------------------------------------------
  // -------------------------------------------------------------------------

  protected void writeAsBytecodes (ClassFileWriter out)
  {
    out.startMethod (getName (), 
                     getType ().descriptorToString (),
                     (short)modifiers);

    VariableTable vars = new VariableTable ();

    // --- generate locals
    Iterator li = locals ();
    while (li.hasNext ())
      {
        Local l = (Local)li.next ();
        if (l instanceof Parameter)
          vars.addParameter (l.getName (), l.getType ().descriptorToString ());
        else
          vars.addLocal (l.getName (), l.getType ().descriptorToString ());
      }

    // --- fixup the vars list.
    vars.establishIndices (!isStatic ());

    // --- generate instructions.  Whoo-hoo!
    body.generateInstructions (out, vars);

    // --- finish up the method.
    //   - 1+ here for 'this' pointer.
    out.stopMethod ((short)(1+locals.size ()), vars);

    // --- all done.
  }

  // -------------------------------------------------------------------------
  // ---- Java code generation -----------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Write this method out as Java source code.
   */
  public void writeAsJava (IndentPrintWriter out)
  {
    out.print (Modifier.toString (modifiers));
    out.print (' ');
    getReturnType ().writeAsJava (out);
    out.print (' ');
    out.print (name);
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
    if (!throwsList.isEmpty ())
      {
        out.print ("  throws ");
        Iterator i = throwsList.iterator ();
        while (i.hasNext ())
          {
            String exClassName = (String)i.next ();
            
            out.println (exClassName);
            if (i.hasNext ()) out.print (", ");
          }
      }

    writeBodyAsJava (out);
  }

  /**
   * Write the body of this method as Java source.
   */
  protected void writeBodyAsJava (IndentPrintWriter out)
  {
    out.println ('{');
    out.indent ();

    writeLocalVarDeclsAsJava (out);
    
    body.writeContentsAsJava (out);

    out.outdent ();
    out.println ('}');
  }

  /**
   * Write local variable declarations as Java source.
   */
  protected void writeLocalVarDeclsAsJava (IndentPrintWriter out)
  {
    Iterator i = locals ();
    while (i.hasNext ())
      {
        Local l = (Local)i.next ();

        l.writeVarDeclAsJava (out);
      }
  }

}
