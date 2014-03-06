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
 * SmallType.java 
 * 
 */

package com.svincent.smalljava;

import java.io.*;
import java.util.*;

import com.svincent.util.*;

import com.svincent.smalljava.rhino.*;

/**
 * <p>The SmallJava API uses two internal representations for types.
 * The first, which is used by most of the APIs for succinctness, is a
 * slightly modified syntax of standard JVM type descriptors.</p>
 *
 * <p>The syntax for fields is as follows (This table, of course,
 * looks much nicer in an HTML form):</p>
 *
 * <table>
 * <tr> <th>production</th> <th>syntax</th> <th>description</th> </tr>
 * <tr><td>FieldType </td><td>BaseType|ComplexType|ArrayType</td><td>A basic field</td></tr>
 * <tr><td>BaseType   </td><td><b>B</b></td><td>signed byte (byte)</td></tr>
 * <tr><td>           </td><td><b>C</b></td><td>character (char)</td></tr>
 * <tr><td>           </td><td><b>D</b></td><td>double precision (double)</td></tr>
 * <tr><td>           </td><td><b>F</b></td><td>single precision (float)</td></tr>
 * <tr><td>           </td><td><b>I</b></td><td>integer (int)</td></tr>
 * <tr><td>           </td><td><b>J</b></td><td>long integer (long)</td></tr>
 * <tr><td>           </td><td><b>S</b></td><td>short integer (short)</td></tr>
 * <tr><td>           </td><td><b>Z</b></td><td>boolean value (boolean)</td></tr>
 * <tr><td>VerboseType</td><td><b>Lbyte;</b></td><td>signed byte (byte)</td></tr>
 * <tr><td>           </td><td><b>Lchar;</b></td><td>character (char)</td></tr>
 * <tr><td>           </td><td><b>Ldouble;</b></td><td>double precision (double)</td></tr>
 * <tr><td>           </td><td><b>Lfloat;</b></td><td>single precision (float)</td></tr>
 * <tr><td>           </td><td><b>Lint;</b></td><td>integer (int)</td></tr>
 * <tr><td>           </td><td><b>Llong;</b></td><td>long integer (long)</td></tr>
 * <tr><td>           </td><td><b>Lshort;</b></td><td>short integer (short)</td></tr>
 * <tr><td>           </td><td><b>Lboolean;</b></td><td>boolean value (boolean)</td></tr>
 * <tr><td>           </td><td><b>L</b>className<b>;</b></td><td>boolean value (boolean)</td></tr>
 * <tr><td>ArrayType</td><td><b>[</b>FieldType</td><td>Array of type</td></tr>
 * </table>
 *
 * <p>The syntax for fields is as follows (Again, this table looks
 * much nicer in an HTML form):</p>
 
 * <table>
 * <tr> <th>production</th> <th>syntax</th> <th>description</th> </tr>
 * <tr><td>MethodType </td><td><b>(</b>FieldType*<b>)</b>ReturnDescriptor</td><td>Method type</td></tr>
 * <tr><td>ReturnType</td><td>FieldType</td><td>standard field type</td></tr>
 * <tr><td>          </td><td><b>V</b></td><td>void (no return)</td></tr>
 * </table>
 *
 * <p>The following are some examples of field type specifiers, and
 * their equivalent Java types.</p>
 *
 * <pre>
 *    I                      int
 *    [I                     int[]
 *    Ljava.lang.String;     java.lang.String
 *    [[I                    int[][]
 *    [Z                     boolean[]
 *    Lint;                  int
 *    [Lint;                 int[]
 *    [Ljava.lang.String;    java.lang.String[]
 * </pre>
 *
 * <p>The following are some examples of method type specifiers, and
 * their equivalent Java types.</p>
 *
 * <pre>
 *    ()V                    void foo ()
 *    (I)I                   int foo (int)
 *    (II)I                  int foo (int, int)
 *    (Ljava.lang.String)V   void foo (java.lang.String)
 *    ([Lint;)V              void foo (int[])
 *    ([I[[I)Z               boolean foo (int[], int[][])
 * </pre>
 *
 * <p>An alternative to using these type descriptors is to use the
 * SmallType API to build them for you.  An example of this follows.</p>
 *
 * <pre>
 * {
 *   // --- Make a field type
 *
 *   // --- make a type using a combination of static methods & static
 *   //   - instances.
 *   SmallType myType =
 *     SmallType.array (SmallType.array (SmallType.Int));
 *
 *   // --- print as java (outputs "int[][]")
 *   myType.writeAsJava (Util.out);
 *
 *   // --- print as type descriptor (outputs "[[I")
 *   myType.writeDescriptor (Util.out);
 *
 *
 *   // --- Make a method type
 *   SmallType.MethodType methodType =
 *      new SmallType.MethodType (myType, 
 *                                      new SmallType[] {SmallType.Int});
 *
 *   // --- print as java (outputs "(int)->int[][]")
 *   methodType.writeAsJava (Util.out);
 *
 *   // --- print as type descriptor (outputs "(I)[[I")
 *   methodType.writeDescriptor (Util.out);
 * }
 * </pre>
 *
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 **/
public abstract class SmallType extends BaseObject {
  /** Debug flag for the parsing algorithm. */
  public static final boolean debug = false;

  /** A constant empty array of SmallTypes.  Useful on rainy nights. */
  public static final SmallType[] EmptyArray = new SmallType[0];

  /** Returns 'true' if this type is a primitive type, such as 'int'. */
  public boolean isPrimitive () { return false; }

  /** Returns a short description of this object, suitable for printing. */
  public String tag ()
  {
    StringPrintWriter out = new StringPrintWriter ();
    writeAsJava (out);
    out.flush ();
    return out.toString ();
  }

  /** 
   * Returns this type's type descriptor as a String. 
   * @see #writeDescriptor
   */
  public String descriptorToString ()
  {
    StringPrintWriter out = new StringPrintWriter ();
    writeDescriptor (out);
    out.flush ();
    return out.toString ();
  }

  /**
   * Write this type out as Java code, if possible.
   * If not possible, do your best.
   */
  public abstract void writeAsJava (PrintWriter out);

  /**
   * Write this type's type descriptor to the given PrintWriter.
   */
  public abstract void writeDescriptor (PrintWriter out);

  // -------------------------------------------------------------------------
  // ---- Bytecode generation ------------------------------------------------
  // -------------------------------------------------------------------------

  /** make an appropriately typed LOAD operator (ILOAD, ALOAD, etc) */
  protected void addLoad (ClassFileWriter out, int index)
  { out.add (ByteCode.ALOAD, index); }

  /** make an appropriately typed STORE operator (ISTORE, ASTORE, etc) */
  protected void addStore (ClassFileWriter out, int index)
  { out.add (ByteCode.ASTORE, index); }

  /** make an appropriately typed ASTORE operator (IASTORE, AASTORE, etc) */
  protected void addArrayStore (ClassFileWriter out)
  { out.add (ByteCode.AASTORE); }

  /** make an appropriately typed RETURN operator (IRETURN, LRETURN, etc) */
  protected void addReturn (ClassFileWriter out)
  { out.add (ByteCode.ARETURN); }

  /** make an appropriately typed ADD operator (IADD, FADD, etc) */
  protected void addAdd (ClassFileWriter out)
  { Util.assertTrue (false, "Cannot, in general, add values."); }

  /** make an appropriately typed SUB operator (ISUB, FSUB, etc) */
  protected void addSub (ClassFileWriter out)
  { Util.assertTrue (false, "Cannot, in general, sub values."); }

  /** make an appropriately typed CMP operator (ISUB, LCMP, etc) */
  protected void addCmp (ClassFileWriter out)
  { Util.assertTrue (false, "Cannot, in general, compare values."); }

  /** make an appropriately typed NEWARRAY instruction */
  protected void addArrayNew (ClassFileWriter out)
  { Util.assertTrue (false, "Cannot, in general, make arrays of any type ("+
                 toString ()+")."); }

  /** 
   * Reprsents a primitive type, such as int or boolean. 
   * Users cannot create new primitive types: use one of the
   * static final members on Type instead (such as 
   * {@link SmallType#Int Int} or {@link SmallType#Void Void}.
   */
  protected static class PrimitiveType extends SmallType {
    String typeName;
    char typeChar;
    protected PrimitiveType (String _typeName, char _typeChar)
    { 
      typeName = _typeName; typeChar = _typeChar; 
    }
    /** Returns 'true' if this type is a primitive type, such as 'int'. */
    public boolean isPrimitive () { return true; }
    public void writeAsJava (PrintWriter out) { out.print (typeName); }
    public void writeDescriptor (PrintWriter out) { out.print (typeChar); }

    // --- most of the primitive types are actually 'int' internally,
    //   - so the defaults here are good.

    protected void addAdd (ClassFileWriter out) { out.add (ByteCode.IADD); }
    protected void addSub (ClassFileWriter out) { out.add (ByteCode.ISUB); }
    protected void addCmp (ClassFileWriter out) { out.add (ByteCode.ISUB); }

    protected void addLoad (ClassFileWriter out, int index)
    { out.add (ByteCode.ILOAD, index); }
    protected void addStore (ClassFileWriter out, int index)
    { out.add (ByteCode.ISTORE, index); }
    protected void addArrayStore (ClassFileWriter out)
    { out.add (ByteCode.IASTORE); }
    protected void addReturn (ClassFileWriter out)
    { out.add (ByteCode.IRETURN); }
    protected void addArrayNew (ClassFileWriter out)
    { out.add (ByteCode.NEWARRAY, ByteCode.T_INT); }

    public String toString () { return "PrimitiveType ("+typeName+")"; }
  }

  /** The Void type: only allowed as the return type of methods. */
  public static final SmallType Void = 
    new PrimitiveType ("void", 'V')
    {
      protected void addReturn (ClassFileWriter out)
      { out.add (ByteCode.RETURN); }
    };

  /** A boolean value: true or false. */
  public static final SmallType Boolean = 
    new PrimitiveType ("boolean", 'Z')
    {
      protected void addArrayNew (ClassFileWriter out)
      { out.add (ByteCode.NEWARRAY, ByteCode.T_BOOLEAN); }
    };

  /** A signed byte value. */
  public static final SmallType Byte = 
    new PrimitiveType ("byte", 'B')
    {
      protected void addArrayNew (ClassFileWriter out)
      { out.add (ByteCode.NEWARRAY, ByteCode.T_BYTE); }
    };

  /** An unsigned Unicode character. */
  public static final SmallType Char = 
    new PrimitiveType ("char", 'C')
    {
      protected void addArrayNew (ClassFileWriter out)
      { out.add (ByteCode.NEWARRAY, ByteCode.T_CHAR); }
    };

  /** A signed short integer. */
  public static final SmallType Short = 
    new PrimitiveType ("short", 'S')
    {
      protected void addArrayNew (ClassFileWriter out)
      { out.add (ByteCode.NEWARRAY, ByteCode.T_SHORT); }
    };

  /** A signed integer */
  public static final SmallType Int = 
    new PrimitiveType ("int", 'I')
    {
      protected void addArrayNew (ClassFileWriter out)
      { out.add (ByteCode.NEWARRAY, ByteCode.T_INT); }
    };

  /** A signed long integer */
  public static final SmallType Long = 
    new PrimitiveType ("long", 'J')
  {
    protected void addAdd (ClassFileWriter out) { out.add (ByteCode.LADD); }
    protected void addSub (ClassFileWriter out) { out.add (ByteCode.LSUB); }
    protected void addCmp (ClassFileWriter out) { out.add (ByteCode.LCMP); }

    protected void addLoad (ClassFileWriter out, int index)
    { out.add (ByteCode.LLOAD, index); }
    protected void addStore (ClassFileWriter out, int index)
    { out.add (ByteCode.LSTORE, index); }
    protected void addArrayStore (ClassFileWriter out)
    { out.add (ByteCode.LASTORE); }
    protected void addReturn (ClassFileWriter out)
    { out.add (ByteCode.LRETURN); }

    protected void addArrayNew (ClassFileWriter out)
    { out.add (ByteCode.NEWARRAY, ByteCode.T_LONG); }
  };

  /** A single-precision IEEE 754 float */
  public static final SmallType Float = 
    new PrimitiveType ("float", 'F')
  {
    protected void addAdd (ClassFileWriter out) { out.add (ByteCode.FADD); }
    protected void addSub (ClassFileWriter out) { out.add (ByteCode.FSUB); }
    protected void addCmp (ClassFileWriter out) { out.add (ByteCode.FCMPL); }

    protected void addLoad (ClassFileWriter out, int index)
    { out.add (ByteCode.FLOAD, index); }
    protected void addStore (ClassFileWriter out, int index)
    { out.add (ByteCode.FSTORE, index); }
    protected void addArrayStore (ClassFileWriter out)
    { out.add (ByteCode.FASTORE); }
    protected void addReturn (ClassFileWriter out)
    { out.add (ByteCode.FRETURN); }

    protected void addArrayNew (ClassFileWriter out)
    { out.add (ByteCode.NEWARRAY, ByteCode.T_FLOAT); }
  };

  /** A double-precision IEEE 754 float */
  public static final SmallType Double = 
    new PrimitiveType ("double", 'D')
  {
    protected void addAdd (ClassFileWriter out) { out.add (ByteCode.DADD); }
    protected void addSub (ClassFileWriter out) { out.add (ByteCode.DSUB); }
    protected void addCmp (ClassFileWriter out) { out.add (ByteCode.DCMPL); }

    protected void addLoad (ClassFileWriter out, int index)
    { out.add (ByteCode.DLOAD, index); }
    protected void addStore (ClassFileWriter out, int index)
    { out.add (ByteCode.DSTORE, index); }
    protected void addArrayStore (ClassFileWriter out)
    { out.add (ByteCode.DASTORE); }
    protected void addReturn (ClassFileWriter out)
    { out.add (ByteCode.DRETURN); }

    protected void addArrayNew (ClassFileWriter out)
    { out.add (ByteCode.NEWARRAY, ByteCode.T_DOUBLE); }
  };

  /**
   * An instance of a class.  In some ways, ObjectType represents
   * the "Type" quality of a class.
   */
  public static class ObjectType extends SmallType {
    String className;
    public ObjectType (String _className) { className = _className; }
    public void writeAsJava (PrintWriter out) { out.print(className); }
    public void writeDescriptor (PrintWriter out) 
    { 
      String descriptorClassName = className.replace ('.', '/');
      out.print ('L');
      out.print (descriptorClassName);
      out.print (';');
    }

    protected void addArrayNew (ClassFileWriter out)
    { out.add (ByteCode.ANEWARRAY, className.replace ('.', '/')); }
  }

  /** 
   * Make a new array of the given type.  If you pass an array type
   * into this method, you will get a multidimensional array!
   */
  public static ArrayType array (SmallType t) { return new ArrayType (t); }

  /** 
   * A type which represents an array type.  ArrayTypes can be nested,
   * which allows multidimensional arrays.
   * To make an ArrayType, use the method 
   * {@link SmallType#array SmallType.array }
   *
   * @see SmallType#array
   */
  public static class ArrayType extends SmallType {
    /** The type of our elements. */
    SmallType subType;

    /** Make a new ArrayType. */
    protected ArrayType (SmallType _subType) { subType = _subType; }

    public SmallType getElementType () { return subType; }

    /** Returns the number of dimensions this array has. */
    public int getDimensionCount ()
    {
      // --- if our subtype is an array, our dimension count is > 1.
      if (subType instanceof ArrayType)
        return ((ArrayType)subType).getDimensionCount () + 1;
      else
        // --- not an array: single-dimensioned array.
        return 1;
    }

    /** Write out this type as Java code, if possible. */
    public void writeAsJava (PrintWriter out) 
    { 
      subType.writeAsJava (out);
      out.print ("[]");
    }

    /** Write out this type's type descriptor. */
    public void writeDescriptor (PrintWriter out) 
    { 
      out.print ('[');
      subType.writeDescriptor (out);
    }
  }

  /**
   * Represents a type for a method.
   * Note that since in Java methods are not first-class, a method's
   * type cannot be printed as Java code.  
   * writeAsJava is implemented, however, to print in the form:
   *    (parmType1, parmType2, ... parmTypeN)->returnType
   */
  public static class MethodType extends SmallType {
    SmallType returnType;
    SmallType[] argTypes;

    public MethodType (SmallType _returnType, SmallType[] _argTypes)
    { returnType = _returnType; argTypes = _argTypes; }

    public SmallType getReturnType () { return returnType; }
    public SmallType[] getArgTypes () { return argTypes; }
    public int getArgCount () { return argTypes.length; }
    public SmallType getArgType (int i) { return argTypes[i]; }

    /** 
     * Note: not really Java code, as method descriptors have 
     * no real correspondence in Java.
     */
    public void writeAsJava (PrintWriter out)
    {
      out.print ("(");
      for (int i=0; i<argTypes.length; i++)
        {
          if (i>0) out.print (", ");
          argTypes[i].writeAsJava (out);
        }
      out.print (")->");
      returnType.writeAsJava (out);
    }

    public void writeDescriptor (PrintWriter out) 
    { 
      out.print (argsTypeDescriptor ());
      out.print (returnTypeDescriptor ());
    }

    public String argsTypeDescriptor () 
    {
      StringPrintWriter out = new StringPrintWriter ();
      out.print ('(');
      SmallType[] args = getArgTypes ();
      for (int i=0; i<args.length; i++)
        args[i].writeDescriptor (out);
      out.print (')');
      return out.toString ();
    }

    public String returnTypeDescriptor () 
    { 
      return getReturnType ().descriptorToString ();
    }
  }

  // -------------------------------------------------------------------------
  // ---- Parsing field and method descriptors -------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Parse the given field descriptor into a SmallType.  Will not
   * work on method descriptors.  Reasonably efficient
   * implementation.</p>
   *
   * <p>Meaningful exceptions are thrown, including integer positions,
   * if an error is found.</p>
   **/
  public static SmallType parseFieldDescriptor (String desc)
    throws InvalidDescriptorException
  {
    char[] chars = desc.toCharArray ();
    return parseFieldDescriptor (chars, 0, chars.length, false);
  }
   
  /**
   * <p>Parses a method descriptor into a SmallType.MethodType
   * structure.</p>
   *
   * <p>Will, of course, not work on fields (as they cannot be
   * represented with MethodTypes).  Reasonably efficient
   * implementation.</p>
   *
   * <p>Meaningful exceptions are thrown, including integer positions, if
   * an error is found.</p>
   **/
  public static MethodType parseMethodDescriptor (String desc)
    throws InvalidDescriptorException
  {
    // --- suck the chars out of the desc string.
    char[] chars = desc.toCharArray ();
    return parseMethodDescriptor (chars);
  }

  /**
   * Parses a method descriptor contained in the given character
   * array.  
   */
  private static MethodType parseMethodDescriptor (char[] desc)
    throws InvalidDescriptorException
  {
    // --- start pos at the beginning.
    int pos = 0;

    // --- make sure the string starts like we expect.
    if (desc[pos] != '(')
      throw new InvalidDescriptorException 
        ("No '(' found in method descriptor!", 0);

    // --- start one past the open roundie.
    pos++;

    // --- loop, finding each parm descriptor.
    List argTypesList = new LinkedList ();

    // --- start the lookahead var at pos.
    int seek = pos;

    // --- loop
    boolean done = false;
    while (!done)
      {
        // --- push seek ahead until we find the end of a parameter token.
        seek = findStartOfNextToken (desc, seek);

        // --- if we hit the end of the string before a ')', die.
        if (seek == desc.length)
          throw new InvalidDescriptorException 
            ("Hit premature end of method descriptor(expected ',' or ')'", 
             seek);

        if (debug)
          Util.out.println ("Found field descriptor pos == "+pos+
                            ", end == "+seek);

        // --- we're done when we hit a ')'.
        if (desc[pos] == ')') break;

        // --- if we aren't at the end of the list, try to parse this
        //   - token.
        SmallType t = parseFieldDescriptor (desc, pos, seek, false);

        if (debug)
          Util.out.println ("Got arg type "+t.tag ());

        argTypesList.add (t);

        // --- increment and iterate.
        //seek++;
        pos = seek;
      }

    pos++;
    
    if (debug)
      Util.out.println ("Found retval descriptor pos == "+pos+
                        ", end == "+desc.length);

    // --- parse the return type descriptor.
    SmallType returnType = parseFieldDescriptor (desc, pos, desc.length, true);

    // --- create a SmallType[] containing the args we just found.
    SmallType[] argTypes = 
      (SmallType[])argTypesList.toArray (SmallType.EmptyArray);

    // --- return a new method descriptor
    return new MethodType (returnType, argTypes);
  }

  /**
   * Finds the index of the next token (a token being a parameter
   * declaration in a method descriptor.  Works like String.indexOf,
   * in that it uses 'seek' as the first character to look at in its search.
   */
  private static int findStartOfNextToken (char[] desc, int seek)
  {
    switch (desc[seek])
      {
      case ')':
        // --- end of parameters
        return seek;

      case 'B': case 'C': case 'D': case 'F': case 'I': case 'J': 
      case 'S': case 'Z': case 'V': 
        // --- primitive type
        return seek+1;

      case 'L':
        // --- complex type
        while (seek < desc.length &&
               desc[seek] != ';' &&
               desc[seek] != ')')
          seek++;
        if (desc[seek] == ';') return seek+1;
        return seek;

      case '[':
        // --- array type
        return findStartOfNextToken (desc, seek+1);
      }

    return seek;
  }

  /**
   * Parses a field descriptor found in the given character array.
   * The descriptor is assumed to start at 'pos', and end at 'end'.
   * If allowVoid is true, then the Void type is allowed (normally
   * only allowed as a return value of a method in Java)
   */
  private static SmallType parseFieldDescriptor (char[] desc, 
                                                 int pos, int end,
                                                 boolean allowVoid)
    throws InvalidDescriptorException
  {
    if (debug)
      Util.out.println ("Parsing field descriptor: (pos == "+pos+
                        ", end == "+end+")");

    switch (desc[pos])
      {
      case 'B': return Byte;
      case 'C': return Char;
      case 'D': return Double;
      case 'F': return Float;
      case 'I': return Int;
      case 'J': return Long;
      case 'S': return Short;
      case 'Z': return Boolean;
      case 'V': return makeVoid (allowVoid, pos);
      case '[': return array (parseFieldDescriptor (desc, pos+1, end, false));
      case 'L': return parseObjectTypeDescriptor(desc, pos+1, end-1,allowVoid);
      default:
        throw new InvalidDescriptorException 
          ("Unknown descriptor char: '"+desc[pos]+"'", pos);
      }
  }

  /**
   * Parses a type descriptor of the form 'Ltypename;'
   */
  private static SmallType parseObjectTypeDescriptor (char[] desc, 
                                                      int stringBegin, 
                                                      int stringEnd,
                                                      boolean allowVoid)
    throws InvalidDescriptorException
  {
    if (desc[stringEnd] != ';')
      throw new InvalidDescriptorException 
        ("classname must end with a semicolon! (found '"+desc[stringEnd]+
         "')", stringEnd);

    int stringLength = stringEnd - stringBegin;

    // --- a string can only contain the characters
    //   -   [a-z][A-Z][0-9].$/
    //   - any other character is illegal.  Check for this.
    ensureValidClassname (desc, stringBegin, stringEnd);

    String className = new String (desc, stringBegin, stringLength);
    if (className.equals ("byte"))  return Byte;
    if (className.equals ("char"))  return Char;
    if (className.equals ("double"))  return Double;
    if (className.equals ("float"))  return Float;
    if (className.equals ("int"))  return Int;
    if (className.equals ("long"))  return Long;
    if (className.equals ("short"))  return Short;
    if (className.equals ("boolean"))  return Boolean;
    if (className.equals ("void")) return makeVoid(allowVoid,stringBegin);
    
    className = className.replace ('/', '.');
    return new ObjectType (className);
  }

  /**
   * Make a Void type.  Also check to ensure that void is allowed, 
   * and, if not, throw a big exception.
   */
  private static SmallType makeVoid (boolean allowVoid, int pos)
    throws InvalidDescriptorException
  {
    if (allowVoid) return Void;
    else throw new InvalidDescriptorException 
           ("Void type is not allowed here.  "+
            "Void is allowed only as a method return value.", pos);
  }

  /**
   * Ensure that the characters between 'stringBegin' and 'stringEnd'
   * constitute a valid classname.  Note that this is not entirely
   * correct, as it accepts beasts like
   *    com.svincent.32223
   * and 
   *    334434
   * This is not the end of the world, however, and it does
   * reject trailing dots and slashes, as well as any character not
   * allowed to be part of a java identifier, such as '&'.
   */
  private static void ensureValidClassname (char[] str, int stringBegin, 
                                            int stringEnd)
    throws InvalidDescriptorException
  {
    for (int i=stringBegin; i<stringEnd; i++)
      {
        char c = str[i];
        switch (c)
          {
          case '/':
          case '.':
            // --- these two characters separate parts of a Java
            //   - classname in descriptors.

            // --- the string must not end before at least one other
            //   - character is found.
            if (i+1 >= stringEnd)
              throw new InvalidDescriptorException 
                ("'"+c+"' found terminating classname: more tokens expected", 
                 i);

            // --- the character following must be a Java start char.
            if (!Character.isJavaIdentifierStart (str[i+1]))
              throw new InvalidDescriptorException 
                ("'"+str[i+1]+
                 "' not allowed as the first character of a java classname "+
                 "component.", i+1);
              
            break; // otherwise, good.
          default:
            if (!Character.isJavaIdentifierPart (c))
              throw new InvalidDescriptorException 
                ("The character '"+c+"' is never allowed in a Java classname.",
                 i);
          }
      }
  }

  /**
   * A non-exhaustive test of the field and method descriptor
   * parsing mechanism.  Many cases are dealt with, but rigorous
   * tests have yet to be performed.
   */
  public static void main (String[] args)
  {
    PrintWriter out = Util.out;

    out.println ("--- ones we expect to succeed ---");

    testField (out, "I");
    testField (out, "Ljava/lang/String;");
    testField (out, "[Ljava.lang.String;");
    testField (out, "[[Ljava.lang.String;");
    testField (out, "[[Z");
    testField (out, "Lcom.svincent.util.Util$1;");

    testField (out, "Lint;");
    testField (out, "[Ldouble;");
    testField (out, "[[[[Lshort;");

    testMethod (out, "([Ljava/lang/String;I)V");
    testMethod (out, "(II)V");
    testMethod (out, "(Z)[Ljava.lang.String;");
    testMethod (out, "(IZI)[Ljava.lang.String;");
    testMethod (out, "(ZD[[Ljava.lang.String;)[Ljava.lang.String;");
    testMethod (out, "()V");

    testMethod (out, "(Lint;Ldouble;[Lint;)[Ljava.lang.String;");

    out.println ("--- ones we expect to fail ---");
    testField (out, "V");
    testField (out, "Ljava.lang.String");
    testField (out, "Ljava.lang.String;;");

    testMethod (out, "(V)V");
    testMethod (out, "([Ljava/lang/StringLjava/util/Date;)V");

  }

  /**
   * Test method for testing the parsing of field descriptors
   */
  public static void testField (PrintWriter out, String fieldDesc)
  {
    try {
      SmallType t = parseFieldDescriptor (fieldDesc);
      out.print ("'");
      out.print (fieldDesc);
      out.print ("'");
      out.print (" parsed as ");
      out.print ("'");
      out.print (t.tag ());
      out.print ("'");
      out.println ();

      out.println ("Descriptor is '"+t.descriptorToString ()+"'");

      out.println ();
    } catch (InvalidDescriptorException ex) {
      out.println ("'"+fieldDesc+"' failed: "+ex.getMessage ());
      out.println ();
    }
  }

  /**
   * Test method for testing the parsing of method descriptors
   */
  public static void testMethod (PrintWriter out, String methodDesc)
  {
    try {
      SmallType t = parseMethodDescriptor (methodDesc);
      out.print ("'");
      out.print (methodDesc);
      out.print ("'");
      out.print (" parsed as ");
      out.print ("'");
      out.print (t.tag ());
      out.print ("'");
      out.println ();

      out.println ("Descriptor is '"+t.descriptorToString ()+"'");

      out.println ();
    } catch (InvalidDescriptorException ex) {
      out.println ("'"+methodDesc+"' failed: "+ex.getMessage ());
      out.println ();
    }
  }

}
