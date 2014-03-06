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
 * Expr.java 
 * 
 */

package com.svincent.smalljava;

import com.svincent.util.*;
import java.util.*;

import com.svincent.smalljava.rhino.*;

/**
 * Expr represents an executable expression.  Most of the expression
 * types you've come to know and love are here: ifs, whiles, arithmetic,
 * field manipulation, etc.  They're all right here, in Texas.
 *
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public abstract class Expr extends BaseObject {

  /** Set to true to debug typecheck algorithms */
  public static final boolean debugTypecheck = false;

  /** A convenient empty Expression array. */
  public static final Expr[] EmptyArray = new Expr[0];

  /** The cached result type of this expression. */
  SmallType resultType = null;

  /** 
   * Not to be called by users.  Evaluates the type of this
   * expression, and caches the value, so that 'getResultType' can work.
   *
   * @see #getResultType()
   **/
  protected SmallType evalType (SmallMethod m) 
    throws SmallJavaValidationException
  {
//      Util.out.println ("Deriving type of "+this.tag ());
    resultType = deriveType (m);
    return resultType;
  }

  /**
   * Actually do the work.
   * deriveType should call 'evalType' for recursion, and super.deriveType
   * to repeat parent work.  
   * These simple rules for you and me will result in a happy world where
   * the type returned is always cached appropriately.
   *
   * @see #evalType(SmallMethod)
   * @see #getResultType()
   */
  protected abstract SmallType deriveType (SmallMethod m)
    throws SmallJavaValidationException;

  /**
   * Get the type of the result of this expression.
   * Can only be called after the declaring class has been finalized.
   */
  public SmallType getResultType ()
  {
    Util.assertTrue (resultType != null,
                 "Do not call getResultType until after SmallClass.finalize "+
                 "(was called on "+getClass ().getName ()+")");
    return resultType;
  }

  public String tag ()
  {
    StringPrintWriter out = new StringPrintWriter ();
    IndentPrintWriter iout = new IndentPrintWriter (out);
    writeAsJava (iout);
    iout.flush ();
    return out.toString ();
  }

  /** 
   * Write this expression out as Java source code. 
   */
  public abstract void writeAsJava (IndentPrintWriter out);

  /** 
   * Generate a list of JVM instructions for this expression node. 
   */
  protected abstract void generateInstructions (ClassFileWriter out,
                                                VariableTable vars);

  /** 
   * Returns true if, after printing this expression as a statement,
   * there should be a trailing semicolon printed.  Notable exceptions
   * are {...} constructs, and ifs and whiles.
   **/
  protected boolean printTrailingSemi () { return true; }

  // -------------------------------------------------------------------------
  // ---- Constructing new instances -----------------------------------------
  // -------------------------------------------------------------------------

  /** 
   * Construct a new instance of the named class. 
   */
  public static class New extends Expr {
    String className;
    String descriptor;
    Expr[] args;

    SmallType.MethodType type;

    public New (String _className, String _descriptor, Expr[] _args)
      throws SmallJavaBuildingException
    { 
      className = _className; descriptor = _descriptor; args = _args; 
      parseSignature ();
    }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      return new SmallType.ObjectType (className);
    }

    private void parseSignature () throws SmallJavaBuildingException
    {
      type = 
        SmallJavaUtil.parseTypeFromMethodDescriptor (descriptor, args.length);
    }

    public void writeAsJava (IndentPrintWriter out)
    {
      out.print ("new ");
      out.print (className);
      out.print ('(');
      for (int i=0; i<args.length; i++)
        {
          out.print ('(');
          type.getArgType (i).writeAsJava (out);
          out.print (')');
          
          args[i].writeAsJava (out);
        }
      out.print (')');
    }

    protected void generateInstructions (ClassFileWriter out,
                                         VariableTable vars)
    {
      String classDesc = className.replace ('.', '/');

      // --- do the actual NEW instruction
      out.add (ByteCode.NEW, classDesc);

      // --- duplicate the new reference... why is this necessary????
      out.add (ByteCode.DUP);

      // --- run each arg.
      for (int i=0; i<args.length; i++)
        args[i].generateInstructions (out, vars);

      // --- append the INVOKESPECIAL.
      out.add (ByteCode.INVOKESPECIAL, 
               classDesc, 
               "<init>", 
               type.argsTypeDescriptor (), 
               type.returnTypeDescriptor ());
    }
  }

  /** 
   * call the null-arg super constructor.. 
   */
  public static class SuperConstructor extends Expr {

    String superClassName;

    public SuperConstructor ()
    { 
    }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      // XXX ensure is first instruction in method, other constraints...
      if (!(m instanceof SmallConstructor))
        throw new SmallJavaValidationException 
          ("super() can only be called in constructor!");
      SmallClass declaringClass = m.getDeclaringClass ();
      superClassName = declaringClass.getSuperClassName ();
      return SmallType.Void;
    }

    public void writeAsJava (IndentPrintWriter out)
    {
      out.print ("super ()");
    }

    protected void generateInstructions (ClassFileWriter out,
                                         VariableTable vars)
    {
      // --- load 'this'.
      out.add (ByteCode.ALOAD_0);

      String classDesc = superClassName.replace ('.', '/');
      // --- append the INVOKESPECIAL.
      out.add (ByteCode.INVOKESPECIAL, 
               classDesc, 
               "<init>", 
               "()", 
               "V");
    }
  }


  /** 
   * Construct a new array instance for containing the given type
   *
   * (Only makes single-dimensional arrays, for now)
   *
   * Also ignores types of initializer things.  So can generate bad code.
   */
  public static class NewArray extends Expr {
    SmallType arrayType;
    Expr size;
    Expr[] initializer;

    /** 
     * Makes a new array like:
     *     new Type[size]
     **/
    public NewArray (SmallType _arrayType, Expr _size) 
    { 
      arrayType = _arrayType; 
      size = _size;
      initializer = Expr.EmptyArray;
    }

    /** 
     * Makes a new array like:
     *     new Type[] {initializer}
     **/
    public NewArray (SmallType _arrayType, Expr[] _initializer) 
    { 
      arrayType = _arrayType; 
      size = new IntConst (_initializer.length);
      initializer = _initializer;
    }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      SmallType sizeType = size.evalType (m);
      if (sizeType != SmallType.Int)
        throw new SmallJavaValidationException 
          ("array constructor size must be an expression of type INT");
      
      for (int i=0; i<initializer.length; i++)
        // XXX shouldn't ignore return here.
        initializer[i].evalType (m);

      return SmallType.array (arrayType); 
    }

    public void writeAsJava (IndentPrintWriter out)
    {
      out.print ("new ");
      arrayType.writeAsJava (out);
      out.print ('[');
      if (initializer.length == 0)
        size.writeAsJava (out);
      out.print (']');

      if (initializer.length > 0)
        {
          out.print (" {");
          for (int i=0; i<initializer.length; i++)
            {
              if (i>0) out.print (", ");
              initializer[i].writeAsJava (out);
            }
          out.print ('}');
        }
    }

    protected void generateInstructions (ClassFileWriter out,
                                         VariableTable vars)
    {
      // --- push the size.
      size.generateInstructions (out, vars);

      // --- do the actual instruction.
      arrayType.addArrayNew (out);

      // --- now the stack has the new array.  Maybe process the initializer.
      for (int i=0; i<initializer.length; i++)
        {
          // --- duplicate the stack, it gets eaten by array store.
          out.add (ByteCode.DUP);

          // --- push the index
          out.addLoadConstant (i);

          // --- push the value
          initializer[i].generateInstructions (out, vars);

          // --- do array store
          arrayType.addArrayStore (out);
        }
    }
  }

  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Return a value from a method.
   **/
  public static class Return extends Expr {
    Expr returnExpr;

    /** For void returns. */
    public Return () { returnExpr = null; }

    /** For non-void returns. */
    public Return (Expr _returnExpr) { returnExpr = _returnExpr; }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    {
      if (returnExpr != null)
        return returnExpr.evalType (m); 
      else
        return SmallType.Void;
    }
    
    /** 
     * Write this expression out as Java source code. 
     */
    public void writeAsJava (IndentPrintWriter out)
    {
      out.print ("return");
      if (returnExpr != null)
        {
          out.print (' ');
          returnExpr.writeAsJava (out);
        }
    }

    /** Generate a list of JVM instructions for this expression node. */
    protected void generateInstructions (ClassFileWriter out,
                                         VariableTable vars)
    {
      if (returnExpr != null)
        {
          returnExpr.generateInstructions (out, vars);
          SmallType thisType = getResultType ();
          thisType.addReturn (out);
        }
      else
        out.add (ByteCode.RETURN);
    }

  }

  // -------------------------------------------------------------------------
  // ---- Calling Methods ----------------------------------------------------
  // -------------------------------------------------------------------------

  /** 
   * Call a method.
   *
   * Based on the constructor, either calls a static method or an
   * instance method.  
   **/
  public static class Call extends Expr {
    Expr self;
    String className;
    String signature;
    Expr[] args;

    String name;
    String descriptor;
    SmallType.MethodType type;

    public Call (String _className,String _signature, Expr[] _args)
      throws SmallJavaBuildingException
    { this (null, _className, _signature, _args); }

    public Call (Expr _self, String _className,String _signature, Expr[] _args)
      throws SmallJavaBuildingException
    { 
      self = _self; className = _className;
      signature = _signature; args = _args; 
      parseSignature ();
    }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      // XXX make sure all these types are correct!!!
      if (self != null) self.evalType (m);
      for (int i=0; i<args.length; i++) args[i].evalType (m);

      return type.getReturnType (); 
    }

    private void parseSignature () throws SmallJavaBuildingException
    {
      name = 
        SmallJavaUtil.parseNameFromMethodSignature (signature);
      descriptor =
        SmallJavaUtil.parseDescriptorFromMethodSignature (signature);
      type = 
        SmallJavaUtil.parseTypeFromMethodDescriptor (descriptor, args.length);
    }

    public void writeAsJava (IndentPrintWriter out)
    {
      if (self == null)
        out.print (className);
      else
        self.writeAsJava (out);
      out.print ('.');
      out.print (name);
      out.print ('(');
      for (int i=0; i<args.length; i++)
        {
          if (i>0) out.print (", ");
          out.print ('(');
          type.getArgType (i).writeAsJava (out);
          out.print (')');
          
          args[i].writeAsJava (out);
        }
      out.print (')');
    }

    protected void generateInstructions (ClassFileWriter out,
                                         VariableTable vars)
    {
      // --- run self.
      if (self != null)
        self.generateInstructions (out, vars);

      // --- run each arg.
      for (int i=0; i<args.length; i++)
        args[i].generateInstructions (out, vars);

      // --- figure out which opcode to generate.
      byte opcode = 
        (self == null) ? ByteCode.INVOKESTATIC : ByteCode.INVOKEVIRTUAL;

      // --- append the opcode.
      out.add (opcode, 
               className.replace ('.', '/'),
               name, 
               type.argsTypeDescriptor (), 
               type.returnTypeDescriptor ());
    }
  }

  // -------------------------------------------------------------------------
  // ---- Accessing and modifying state --------------------------------------
  // -------------------------------------------------------------------------

  /** 
   * Assign a value to an instance field 
   */
  public static class SetField extends Expr {
    Expr self;
    String className;
    String descriptor;
    String fieldName;
    Expr rvalue;

    SmallType type;

    public SetField (String _className, String _descriptor,
                        String _fieldName, Expr _rvalue)
      throws SmallJavaBuildingException
    { this (new This (), _className, _descriptor, _fieldName, _rvalue); }

    public SetField (Expr _self, String _className, String _descriptor,
                        String _fieldName, Expr _rvalue)
      throws SmallJavaBuildingException
    { 
      self = _self; className = _className; descriptor = _descriptor;
      fieldName = _fieldName; rvalue = _rvalue; 

      type = SmallJavaUtil.parseFieldDescriptor (_descriptor);
    }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      self.evalType (m);
      SmallType rvalType = rvalue.evalType (m);
      if (!rvalType.descriptorToString ().equals (type.descriptorToString ()))
        throw new SmallJavaValidationException 
          ("Type error: attempt to assign value of type "+rvalType.tag ()+
           " into field of type "+type.tag ()+"");
      return SmallType.Void; 
    }

    public void writeAsJava (IndentPrintWriter out)
    {
      self.writeAsJava (out);
      out.print ('.');
      out.print (fieldName);
      out.print (" = ");
      rvalue.writeAsJava (out);
    }

    protected void generateInstructions (ClassFileWriter out,
                                         VariableTable vars)
    {
      // --- run self.
      self.generateInstructions (out, vars);
      
      // --- push the value onto the stack.
      rvalue.generateInstructions (out, vars);

      // --- append the PUTFIELD.
      out.add (ByteCode.PUTFIELD, 
               className.replace ('.', '/'), 
               fieldName, 
               type.descriptorToString ());
    }

  }

  /** 
   * Assign a value to a local variable 
   */
  public static class SetLocal extends Expr {
    String fieldName;
    Expr rvalue;

    SmallType fieldType;

    public SetLocal (String _fieldName, Expr _rvalue)
      throws SmallJavaBuildingException
    { fieldName = _fieldName; rvalue = _rvalue; }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      fieldType = m.getLocal (fieldName).getType ();
      SmallType rvalType = rvalue.evalType (m);
      if (!rvalType.descriptorToString ().
          equals (fieldType.descriptorToString ()))
        throw new SmallJavaValidationException 
          ("Type error: attempt to assign value of type "+rvalType.tag ()+
           " into field of type "+fieldType.tag ()+"");
      return SmallType.Void; 
    }

    public void writeAsJava (IndentPrintWriter out)
    {
      out.print (fieldName);
      out.print (" = ");
      rvalue.writeAsJava (out);
    }

    protected void generateInstructions (ClassFileWriter out,
                                         VariableTable vars)
    {
      // --- calculate the variable index.
      LocalVariable local = vars.get (fieldName);

      if (local == null)
        Util.assertTrue (false, "Huh?  Could not find field of name "+
                     fieldName+" in method: got variable table: "+vars.tag ());

      int index = local.getIndex ();

      // --- push the value onto the stack
      rvalue.generateInstructions (out, vars);
      
      // --- make the STORE instruction.
      fieldType.addStore (out, index);
    }
  }

  /** 
   * Assign a value to an array slot 
   */
  public static class SetArray extends Expr {
    Expr array;
    Expr idx;
    Expr newValue;

    SmallType arrayType;

    public SetArray (Expr _array, Expr _idx, Expr _newValue)
      throws SmallJavaBuildingException
    { array = _array; idx = _idx; newValue = _newValue; }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      if (SmallType.Int != idx.evalType (m))
        throw new SmallJavaValidationException 
          ("Array indexes must be of type 'int'");

      SmallType t = array.evalType (m);
      if (!(t instanceof SmallType.ArrayType))
        throw new SmallJavaValidationException
          ("Attempt to access non-array entity as array!");

      arrayType = ((SmallType.ArrayType)t).getElementType ();

      t = newValue.evalType (m);
      // XXX write 'type equals' method.
      if (t != arrayType)
        throw new SmallJavaValidationException
          ("Bad rvalue type!");

      return SmallType.Void; 
    }

    public void writeAsJava (IndentPrintWriter out)
    {
      array.writeAsJava (out);
      out.print ('[');
      idx.writeAsJava (out);
      out.print ("] = ");
      newValue.writeAsJava (out);
    }

    protected void generateInstructions (ClassFileWriter out,
                                         VariableTable vars)
    {
      // --- push the array.
      array.generateInstructions (out, vars);

      // --- push the index.
      idx.generateInstructions (out, vars);

      // --- push the value.
      newValue.generateInstructions (out, vars);

      // --- fire an ASTORE variant.
      arrayType.addArrayStore (out);
    }
  }


  /** 
   * Get the value from an instance field  
   */
  public static class GetField extends Expr {
    Expr self;
    String className;
    String descriptor;
    String fieldName;

    /** parsed from descriptor */
    SmallType type = null;

    public GetField (String _className, String _descriptor, String _fieldName) 
      throws SmallJavaBuildingException
    { this (new This (), _className, _descriptor, _fieldName); }

    public GetField (Expr _self, String _className,
                     String _descriptor, String _fieldName) 
      throws SmallJavaBuildingException
    { 
      self = _self; className = _className;
      descriptor = _descriptor; fieldName = _fieldName; 
      type = SmallJavaUtil.parseFieldDescriptor (descriptor);
    }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { return type; }

    public void writeAsJava (IndentPrintWriter out)
    {
      self.writeAsJava (out);
      out.print (".");
      out.print (fieldName);
    }

    protected void generateInstructions (ClassFileWriter out,
                                         VariableTable vars)
    {
      // --- run self.
      self.generateInstructions (out, vars);

      // --- append the GETFIELD.
      out.add (ByteCode.GETFIELD, 
               className.replace ('.', '/'), 
               fieldName, 
               type.descriptorToString ());
    }
  }

  /** 
   * Get the value from a static field 
   */
  public static class GetStatic extends Expr {
    String descriptor;
    String className;
    String fieldName;

    SmallType type;

    public GetStatic (String _descriptor,
                      String _className, String _fieldName)
      throws SmallJavaBuildingException
    { 
      descriptor = _descriptor; 
      className = _className; fieldName = _fieldName; 
      try {
        type = SmallType.parseFieldDescriptor (descriptor);
      } catch (InvalidDescriptorException ex) {
        throw new SmallJavaBuildingException 
          ("Could not parse descriptor for static reference to "+className+
           "."+fieldName, ex);
      }
    }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { return type; }

    public void writeAsJava (IndentPrintWriter out)
    {
      out.print (className);
      out.print (".");
      out.print (fieldName);
    }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      // --- append the GETSTATIC.
      out.add (ByteCode.GETSTATIC, 
               className.replace ('.', '/'), 
               fieldName, 
               type.descriptorToString ());
    }
  }

  /** 
   * Get the value from a local variable 
   */
  public static class GetLocal extends Expr {
    String fieldName;
    SmallType fieldType;

    public GetLocal (String _fieldName) 
    { fieldName = _fieldName; }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      SmallMethod.Local local = m.getLocal (fieldName);
      fieldType = local.getType ();
      return fieldType;
    }

    public void writeAsJava (IndentPrintWriter out) { out.print (fieldName); }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      // --- calculate the variable index.
      LocalVariable local = vars.get (fieldName);
      int index = local.getIndex ();

      // --- execute an appropriately-typed LOAD instruction
      fieldType.addLoad (out, index);
    }
  }

  /** 
   * Access the magic variable 'this'.  Only valid within nonstatic methods.
   */
  public static class This extends Expr {
    public This () {}

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      // --- XXX make sure method is nonstatic.
      String className = m.getDeclaringClass ().getName ();
      return new SmallType.ObjectType (className);
    }

    public void writeAsJava (IndentPrintWriter out)
    { out.print ("this"); }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      out.add (ByteCode.ALOAD_0);
    }
  }

  // -------------------------------------------------------------------------
  // ---- Constants ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Return the given integer constant.
   */
  public static class IntConst extends Expr {
    int value;
    public IntConst (int _value) { value = _value; }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { return SmallType.Int; }

    public void writeAsJava (IndentPrintWriter out)
    { out.print (value); }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      out.addLoadConstant (value);
    }
  }

  /**
   * Return the given double constant.
   */
  public static class DoubleConst extends Expr {
    double value;
    public DoubleConst (double _value) { value = _value; }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { return SmallType.Double; }

    // XXX is this ok?
    public void writeAsJava (IndentPrintWriter out) { out.print (value); }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      out.addLoadConstant (value);
    }
  }

  /**
   * Return the null constant..
   */
  public static class NullConst extends Expr {
    public NullConst () { }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      // XXX ?
      return new SmallType.ObjectType ("null"); 
    }

    public void writeAsJava (IndentPrintWriter out)
    { out.print ("null"); }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      out.add (ByteCode.ACONST_NULL);
    }
  }

  /**
   * Return the given boolean constant.
   */
  public static class BooleanConst extends Expr {
    boolean value;
    public BooleanConst (boolean _value) { value = _value; }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { return SmallType.Boolean; }

    public void writeAsJava (IndentPrintWriter out)
    { out.print (value); }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      out.addLoadConstant (value ? 1 : 0);
    }
  }

  /**
   * Return the given String constant.
   */
  public static class StringConst extends Expr {
    String value;
    public StringConst (String _value) { value = _value; }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      return new SmallType.ObjectType ("java.lang.String"); 
    }

    public void writeAsJava (IndentPrintWriter out)
    { 
      out.print ('"');
      // XXX quote string.
      out.print (value); 
      out.print ('"');
    }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      out.addLoadConstant (value);
    }
  }

  // -------------------------------------------------------------------------
  // ---- Control Structures -------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Execute a series of expressions, one after another.
   */
  public static class Begin extends Expr {
    List body = new LinkedList ();

    public Begin () { }

//      {
//        stuff;
//        stuff;
//        stuff;
//        stuff;
//        stuff;
//      }

    protected boolean printTrailingSemi () { return false; }

    // --- XXX maybe check for errors here??
    public Begin add (Expr e) { body.add (e); return this; }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      SmallType result = SmallType.Void;
      Iterator i = body.iterator ();
      while (i.hasNext ())
        {
          Expr e = (Expr)i.next ();
          result = e.evalType (m);
        }
      return result;
    }

    public void writeAsJava (IndentPrintWriter out)
    { 
      out.println ('{');
      out.indent ();

      writeContentsAsJava (out);

      out.outdent ();
      out.println ('}');
    }

    public void writeContentsAsJava (IndentPrintWriter out)
    {
      Iterator i = body.iterator ();
      while (i.hasNext ())
        {
          Expr e = (Expr)i.next ();
          e.writeAsJava (out);
          if (e.printTrailingSemi ())
            out.println (';');
        }
    }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      Iterator i = body.iterator ();
      while (i.hasNext ())
        {
          Expr e = (Expr)i.next ();
          e.generateInstructions (out, vars);
        }
    }
  }

  /**
   * Loop around, executing instructions until the given condition
   * evaluates to 'true'.
   */
  public static class While extends Expr {
    Expr cond;
    Expr body;

    public While (Expr _cond) { cond = _cond; }
    public While (Expr _cond, Expr _body) { this (_cond); body = _body; }

    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      SmallType condType = cond.evalType (m);
      if (condType != SmallType.Boolean)
        throw new SmallJavaValidationException 
          ("The condition expression for a while loop must be boolean "+
           "(got "+condType.tag ()+")!");

      body.evalType (m);

      return SmallType.Void;
    }

    protected boolean printTrailingSemi () { return false; }

    public void setBody (Expr _body) { body = _body; }

//      {
//        while (true)
//          {
//            stuff;
//          }
//      }

    public void writeAsJava (IndentPrintWriter out)
    { 
      out.print ("while (");
      cond.writeAsJava (out);
      out.println (')');

      out.indent ();
      body.writeAsJava (out);
      if (body.printTrailingSemi ())
        out.println (';');
      out.outdent ();
    }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      // start:
      //   <cond>
      //   IFNE end
      //   <body>
      //   GOTO start
      // end:
      //   NOP

      Util.assertTrue (false, "Control structures currently not supported.");

//        InstructionHandle start = cond.generateInstructionList (cp, il);

//        BranchHandle branch = il.append (new IFEQ (null));

//        body.generateInstructionList (cp, il);

//        il.append (new GOTO (start));

//        InstructionHandle end = il.append (new NOP ());

//        branch.setTarget (end); 

//        return start;
    }

  }

  /**
   * If condition evaluates to true, evaluate the then clase.
   * otherwise, evaluate the else clasue.
   */
  public static class If extends Expr {
    Expr cond;
    Expr thenClause;
    Expr elseClause;

    public If (Expr _cond) { cond = _cond; }
    public If (Expr _cond, Expr _thenClause) 
    { this (_cond); thenClause = _thenClause; }
    public If (Expr _cond, Expr _thenClause, Expr _elseClause) 
    { this (_cond, _thenClause); elseClause = _elseClause; }

    protected boolean printTrailingSemi () { return false; }

    public void setThen (Expr _thenClause) { thenClause = _thenClause; }
    public void setElse (Expr _elseClause) { elseClause = _elseClause; }

    /** */
    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      SmallType condType = cond.evalType (m);
      if (condType != SmallType.Boolean)
        throw new SmallJavaValidationException 
          ("The condition expression for a if statement must be boolean "+
           "(got "+condType.tag ()+")!");

      if (thenClause == null)
        throw new SmallJavaValidationException
          ("No 'then' clause specified for if statement!");
      thenClause.evalType (m);

      if (elseClause != null)
        elseClause.evalType (m);

      return SmallType.Void;
    }


//    {
//        if (true)
//            // do this
//        else
//            // do that
//    }
    public void writeAsJava (IndentPrintWriter out)
    { 
      out.print ("if (");
      cond.writeAsJava (out);
      out.println (")");

      // --- thenClause
      out.indent ();
      thenClause.writeAsJava (out);
      if (thenClause.printTrailingSemi ())
        out.println (';');
      out.outdent ();

      // --- elseClause
      if (elseClause != null)
        {
          out.println ("else");
          out.indent ();
          elseClause.writeAsJava (out);
          if (elseClause.printTrailingSemi ())
            out.println (';');
          out.outdent ();
        }
    }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      // --- output the condition
      cond.generateInstructions (out, vars);

      // --- make a label for the IFEQ (so we can set its target later on)
      int ifeqLabel = out.acquireLabel ();
      out.add (ByteCode.IFEQ, ifeqLabel);

      // --- output the THEN clause.
      thenClause.generateInstructions (out, vars);


      if (elseClause != null)
        {
          // --- add a GOTO to skip the ELSE clause if we run off the
          //   - end of the THEN clause.
          int gotoLabel = out.acquireLabel ();
          out.add (ByteCode.GOTO, gotoLabel);

          // --- the IFEQ jumps to the ELSE clause.
          out.markLabel (ifeqLabel);

          // --- generate the ELSE clause.
          elseClause.generateInstructions (out, vars);

          // --- the GOTO skips the ELSE clause
          out.markLabel (gotoLabel);
        }
      else
        {
          // --- if no else clause, the IFEQ jumps to the end.
          out.markLabel (ifeqLabel);
        }
    }
  }

  // -------------------------------------------------------------------------
  // ---- Arithmetic and Comparisons -----------------------------------------
  // -------------------------------------------------------------------------

  /**
   * An expression with two halves.
   */
  private abstract static class BinaryExpr extends Expr {
    Expr l;
    Expr r;

    public BinaryExpr (Expr _l, Expr _r) { l = _l; r = _r; }

    public Expr getLhs () { return l; }
    public Expr getRhs () { return r; }

    public abstract String getOpName ();
    protected abstract void addInstruction (ClassFileWriter out);

    /** */
    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      SmallType lType = l.evalType (m);
      SmallType rType = r.evalType (m);

      if (!lType.equals (rType))
        throw new SmallJavaValidationException 
          ("Sorry: SmallJava isn't smart enough to do type promotions.  "+
           "Type '"+lType.tag ()+"' and '"+rType.tag ()+"' were found to "+
           "be different in '"+getOpName ()+"' operation:  "+
           "you're out of luck.");

      if (debugTypecheck)
        Util.out.println ("Got "+getOpName ()+" of type "+lType.tag ());

      return lType;
    }

    public void writeAsJava (IndentPrintWriter out)
    { 
      l.writeAsJava (out);
      out.print (' '); 
      out.print (getOpName ());
      out.print (' '); 
      r.writeAsJava (out);
    }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      l.generateInstructions (out, vars);
      r.generateInstructions (out, vars);
      addInstruction (out);
    }
  }

  /**
   * The normal + binary operator.
   */
  public static class Plus extends BinaryExpr {
    public Plus (Expr _l, Expr _r) { super (_l, _r); }
    public String getOpName () { return "+"; }
    protected void addInstruction (ClassFileWriter out) 
    {
      SmallType thisType = getResultType ();
      thisType.addAdd (out);
    }
  }

  /**
   * The normal - binary operator.
   */
  public static class Minus extends BinaryExpr {
    public Minus (Expr _l, Expr _r) { super (_l, _r); }
    public String getOpName () { return "-"; }
    protected void addInstruction (ClassFileWriter out) 
    {
      SmallType thisType = getResultType ();
      thisType.addSub (out);
    }
  }

  /**
   * Binary operators which evaluate to 'true' or 'false'.
   */
  private abstract static class CompareExpr extends BinaryExpr {
    public CompareExpr (Expr _l, Expr _r) { super (_l, _r); }

    /** */
    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      super.deriveType (m);

      return SmallType.Boolean;
    }

//      protected abstract BranchInstruction makeBranch ();

    protected void addInstruction (ClassFileWriter out) 
    {
      SmallType thisType = getResultType ();
      thisType.addCmp (out);
    }

    protected void generateInstructions (ClassFileWriter out, 
                                         VariableTable vars)
    {
      Util.assertTrue (false, "Comparison expressions not supported yet!");
//        InstructionHandle ih = l.generateInstructionList (cp, il);
//        r.generateInstructionList (cp, il);

//        // XXX this is very inefficient, but very easy to write.
//        // Example:  Greater than

//        //   push l
//        //   push r
//        //   sub       % l - r    // a comparision opcode
//        //   ifgt goto other      // ifgt/ifge/etc
//        //   push 0
//        //   goto end
//        // elseClause:
//        //   push 1
//        // end:
//        //   nop

//        il.append (makeInstruction ()); // probably ISUB

//        BranchHandle branch = il.append (makeBranch ()); // ex: IFGT
//        il.append (new PUSH (cp, 0));
//        BranchHandle gotoEnd = il.append (new GOTO (null));
//        InstructionHandle elseClause = il.append (new PUSH (cp, 1));
//        branch.setTarget (elseClause);
//        InstructionHandle end = il.append (new NOP ());
//        gotoEnd.setTarget (end);

//        return ih;
    }
  }

  /**
   * The normal == binary operator.
   */
  public static class EQ extends CompareExpr {
    public EQ (Expr _l, Expr _r) { super (_l, _r); }
    public String getOpName () { return "=="; }

//      protected BranchInstruction makeBranch () { return new IFEQ (null); }
  }

  /**
   * The normal != binary operator.
   */
  public static class NE extends CompareExpr {
    public NE (Expr _l, Expr _r) { super (_l, _r); }
    public String getOpName () { return "!="; }

//      protected BranchInstruction makeBranch () { return new IFNE (null); }
  }

  /**
   * The normal &gt; binary operator.
   */
  public static class GT extends CompareExpr {
    public GT (Expr _l, Expr _r) { super (_l, _r); }
    public String getOpName () { return ">"; }

//      protected BranchInstruction makeBranch () { return new IFGT (null); }
  }

  /**
   * The normal &gt;= binary operator.
   */
  public static class GE extends CompareExpr {
    public GE (Expr _l, Expr _r) { super (_l, _r); }
    public String getOpName () { return ">="; }

//      protected BranchInstruction makeBranch () { return new IFGE (null); }
  }

  /**
   * The normal &lt; binary operator.
   */
  public static class LT extends CompareExpr {
    public LT (Expr _l, Expr _r) { super (_l, _r); }
    public String getOpName () { return "<"; }

//      protected BranchInstruction makeBranch () { return new IFLT (null); }
  }

  /**
   * The normal &lt;= binary operator.
   */
  public static class LE extends CompareExpr {
    public LE (Expr _l, Expr _r) { super (_l, _r); }
    public String getOpName () { return "<="; }

//      protected BranchInstruction makeBranch () { return new IFLE (null); }
  }


  // -------------------------------------------------------------------------
  // ---- Comment ------------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * A comment.  Generates no instructions, but prints prettily.
   *
   * Designed to be used inside of methods.
   */
  public static class Comment extends Expr {
    String text;

    public Comment (String _text) { text = _text; }

    /** */
    protected SmallType deriveType (SmallMethod m)
      throws SmallJavaValidationException
    { 
      // huh?
      return SmallType.Void;
    }

    public void writeAsJava (IndentPrintWriter out)
    { 
      if (text.indexOf ('\n') != -1)
        {
          out.println ("/*");
          out.indent ();
          out.print (text);
          out.outdent ();
          out.println ("*/");
        }
      else
        {
          out.print ("// ");
          out.println (text);
        }
    }

    protected boolean printTrailingSemi () { return false; }

    protected void generateInstructions (ClassFileWriter out,
                                         VariableTable vars)
    {
      // --- no code generated for comments.
    }
  }

}
