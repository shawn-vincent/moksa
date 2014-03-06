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
 * SmallClass.java 
 *
 * A Class, in SmallJava.
 * 
 */

package com.svincent.smalljava;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

import com.svincent.util.*;

import com.svincent.smalljava.rhino.*;

/**
 * Represents a class in SmallJava.
 *
 * <p>To generate any code with the SmallJava library, you must create an
 * instance of SmallClass, add a number of methods and fields to it,
 * call finalize (), and finally, either generate Java code or JVM
 * bytecodes using an appropriate method.</p>
 *
 * <p>An example follows.</p>
 *
 * <pre>
 *   {
 *     // --- make a new SmallClass: by default, is public and subclasses
 *     //   - java.lang.Object
 *     SmallClass myClass = new SmallClass ("MyClass");
 *
 *     // --- add a protected instance field, (int age)
 *     myClass.instanceField ("I", "age");
 *
 *     // --- add a method (single int parameter, named myInt
 *     SmallMethod foo = myClass.method ("foo(I)V", new String[] {"myInt"})
 *     
 *     // --- add a println (using a convenience method)
 *     foo.add (Expr.println ("Hello, world!"));
 *
 *     // --- finalize the class
 *     myClass.finalize ();
 *
 *     // --- write the class as java to MyClass.java
 *     myClass.writeAsJava ("MyClass.java");
 *     
 *     // --- write the class as JVM bytecodes to MyClass.class
 *     myClass.writeAsBytecodes ("MyClass.class");
 *   }
 * </pre>
 *
 * <p>Note the use of JVM type descriptors.  One can also use the
 * {@link SmallType Type} class to create type descriptors.  See that
 * class's documentation also on documentation on how to write type
 * descriptors.<p> 
 *
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a> 
 **/
public class SmallClass extends BaseObject
{
  String name;
  String superclassName;
  int modifiers;
  String[] interfaces;
  String sourceFileName;

  Map constructorsByName = new Hashtable ();
  Map methodsByName = new Hashtable ();
  Map fieldsByName = new Hashtable ();

  boolean finalized = false;
  
  // -------------------------------------------------------------------------
  // ---- Constructors -------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Make a new SmallClass given just a classname.  By default, the
   * SmallClass is public, is a subclass of java.lang.Object, and has
   * the source file name "[generated]"
   *
   * @see #SmallClass(String,String,int,String[],String)
   **/
  public SmallClass (String className)
  { this (className, "java.lang.Object"); }

  /**
   * Make a new SmallClass with a specified name and superclass.
   *
   * @see #SmallClass(String,String,int,String[],String)
   **/
  public SmallClass (String className, String superClassName)
  { 
    this (className, superClassName, Modifier.PUBLIC/*|Modifier.SUPER*/,null); 
  }

  /**
   * <p>Make a new SmallClass with a specified name, superclass,
   * permissions (as defined on java.lang.reflect.Modifier), and
   * interfaces.</p>
   * 
   * <p>'interfaces' can be specified as null or an empty string
   * array, if there are no interfaces implemented by the generated
   * class.</p>
   *
   * @see #SmallClass(String,String,int,String[],String)
   **/
  public SmallClass (String className, String superClassName,
                     int accessFlags, String[] interfaces)
  { this (className, superClassName, accessFlags, interfaces, "[generated]"); }

  /**
   * <p>The mother of all constructors: specifies everything.</p>
   *
   * <p>The parameters are largely self-explanatory</p>
   * 
   * <p>'interfaces' can be specified as null or an empty string array, 
   * if there are no interfaces implemented by the generated class.</p>
   */
  public SmallClass (String _className, String _superClassName,
                     int _accessFlags, String[] _interfaces,
                     String _sourceFileName)
  {
    name = _className; superclassName = _superClassName;
    modifiers = _accessFlags; 
    if (_interfaces == null) _interfaces = Util.EmptyStringArray;
    interfaces = _interfaces;
    sourceFileName = _sourceFileName;
  }

  // -------------------------------------------------------------------------
  // ---- Accessors ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /** Returns true iff this class has been finalized. */
  public boolean isFinalized () { return finalized; }

  /** Returns the name of this class. */
  public String getName () { return name; }

  /** Returns the name of this class, stripped of package information. */
  public String getRelativeName () 
  { 
    int dotIdx = name.lastIndexOf ('.');
    if (dotIdx == -1) return name;
    else return name.substring (dotIdx+1);
  }

  /** Returns the package naem of this class, or null if there is none */
  public String getPackageName () 
  { 
    int dotIdx = name.lastIndexOf ('.');
    if (dotIdx == -1) return null;
    else return name.substring (0, dotIdx);
  }

  /** Returns the name of this class's superclass. */
  public String getSuperClassName () { return superclassName; }

  // -------------------------------------------------------------------------
  // ---- Builder API --------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Adds a new constructor to this class.
   *
   * @see SmallConstructor
   */
  public SmallConstructor addConstructor (SmallConstructor constructor)
    throws SmallJavaBuildingException
  {
    finalized = false;
    constructorsByName.put (constructor.getDescriptor (), constructor);
    constructor.setDeclaringClass (this);
    return constructor;
  }

  /**
   * <p>Adds a new constructor with the given descriptor and argnames to
   * this class.  Note that the descriptor of a constructor is simply
   * its type.  For example, the constructor </p>
   *
   * <pre>
   *   public MyClass (int i) { ... }
   * </pre>
   *
   * <p>would have the descriptor</p>
   *
   * <pre>
   *   (I)V
   * </pre>
   * 
   * <p>Note that constructors ALWAYS have a void return type</p>
   *
   * @see SmallConstructor
   */
  public SmallConstructor constructor (String descriptor, String[] argNames)
    throws SmallJavaBuildingException
  { return addConstructor (new SmallConstructor (descriptor, argNames)); }

  /**
   * Adds a default public nullarg constructor to this class.
   */
  public void addNullArgConstructor () throws SmallJavaBuildingException
  {
    SmallConstructor constructor = constructor ("()V", Util.EmptyStringArray);
    constructor.add (new Expr.SuperConstructor ());
    constructor.add (new Expr.Return ());
  }

  /** 
   * <p>Adds a new method to this class, returning the parameter for
   * convenience.</p>
   *
   * @see #method
   **/
  public SmallMethod addMethod (SmallMethod method)
    throws SmallJavaBuildingException
  {
    finalized = false;
    methodsByName.put (method.getName (), method);
    method.setDeclaringClass (this);
    return method;
  }

  /**
   * <p>Adds a new public instance method to this class.  'signature' is the
   * concatenation of the methods name and its method descriptor.</p>
   *
   * <p>For example, the method</p>
   * <pre>
   *   void foo (int[] sax) { ... }
   * </pre>
   * <p>would have the signature</p>
   * <pre>
   *   foo([I)V
   * </pre>
   * 
   * @see SmallMethod
   */
  public SmallMethod method (String signature, String[] argNames)
    throws SmallJavaBuildingException
  { return addMethod (new SmallMethod (signature, argNames)); }

  /**
   * <p>Adds a new public static method to this class.  'signature' is the
   * concatenation of the methods name and its method descriptor.</p>
   *
   * <p>For example, the method</p>
   * <pre>
   *   void foo (int[] sax) { ... }
   * </pre>
   * <p>would have the signature</p>
   * <pre>
   *   foo([I)V
   * </pre>
   *
   * @see SmallMethod
   */
  public SmallMethod staticMethod (String signature, String[] argNames)
    throws SmallJavaBuildingException
  { 
    return addMethod (new SmallMethod (Modifier.STATIC|Modifier.PUBLIC, 
                                       signature, 
                                       argNames)); 
  }

  /**
   * Add the given field to this class.
   *
   * @see SmallField
   */
  public SmallField addField (SmallField field)
    throws SmallJavaBuildingException
  {
    finalized = false;
    fieldsByName.put (field.getName (), field);
    field.setDeclaringClass (this);
    return field;
  }

  /**
   * Creates a new protected instance field, and adds it to this class.
   *
   * @see SmallField
   */
  public SmallField field (String signature, String name)
    throws SmallJavaBuildingException
  { return addField (new SmallField (signature, name)); }

  // -------------------------------------------------------------------------
  // ---- Primary interface --------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Does a final typecheck pass, and fills in required missing information
   * with defaults (add null-arg constructors, etc)
   */
  public void finalize () throws SmallJavaValidationException
  {
    // --- add a null-arg constructor, if there isn't one already.
    if (getConstructorCount () == 0)
      {
        try {
          addNullArgConstructor ();
        } catch (SmallJavaBuildingException ex) {
          throw new SmallJavaValidationException 
            ("Could not add null-arg public constructor to class "+getName (), 
             ex);
        }
      }

    Iterator fi = fields ();
    while (fi.hasNext ())
      {
        SmallField f = (SmallField)fi.next ();
        f.finalize ();
      }

    Iterator ci = constructors ();
    while (ci.hasNext ())
      {
        SmallConstructor c = (SmallConstructor)ci.next ();
        c.finalize ();
      }

    Iterator mi = methods ();
    while (mi.hasNext ())
      {
        SmallMethod m = (SmallMethod)mi.next ();
        m.finalize ();
      }

    finalized = true;
  }

  /** Returns an iterator over all fields (type SmallField) in this class */
  public Iterator fields () { return fieldsByName.values ().iterator (); }

  /** Returns an iterator over all methods (type SmallMethod) in this class */
  public Iterator methods () { return methodsByName.values ().iterator (); }

  /** Returns iterator over this class's constructors (type SmallConstructor)*/
  public Iterator constructors () 
  { return constructorsByName.values ().iterator (); }

  /** Returns the number of construtors this class has defined. */
  public int getFieldCount () 
  { return fieldsByName.values ().size (); }

  /** Returns the number of construtors this class has defined. */
  public int getMethodCount () 
  { return methodsByName.values ().size (); }

  /** Returns the number of construtors this class has defined. */
  public int getConstructorCount () 
  { return constructorsByName.values ().size (); }

  // -------------------------------------------------------------------------
  // ---- Java code generation -----------------------------------------------
  // -------------------------------------------------------------------------

  /** 
   * Writes a new text file with the given filename, containing the
   * generated Java code from this SmallJava class. 
   */
  public void writeAsJava (String fileName) throws IOException
  { 
    writeAsJava (new IndentPrintWriter (new FileWriter (fileName), 2)); 
  }

  /** 
   * Writes the generated Java code from this SmallJava class to the
   * given IndentPrintWriter.  
   *
   * XXX maybe allow other people to pass in stuff for the comment
   *     at the top of the file??
   */
  public void writeAsJava (Writer _out)
  {
    IndentPrintWriter out;
    if (_out instanceof IndentPrintWriter)
      out = (IndentPrintWriter)_out;
    else
      out = new IndentPrintWriter (_out);

    // --- print package information.
    String packageName = getPackageName ();
    if (packageName != null)
      {
        out.println ("package "+packageName+";");
        out.println ();
      }

    // --- header
    out.println("/*");
    out.indent (" * ");
    out.println ("This file was automatically generated by Smalljava.");
    out.println ();
    SmallJavaUtil.printBanner (out);
    out.println ();
    out.println ("File generated: "+Util.dateAndTimeToString ());
    out.outdent ();
    out.println(" */");

    // --- definition clause
    out.print (Modifier.toString (modifiers));
    out.print (" class ");
    out.println (name);

    out.indent ();

    // --- extends clause
    out.print ("extends ");
    out.println (superclassName);

    // --- implements clause
    if (interfaces.length > 0)
      {
        out.print ("implements ");
        for (int i=0; i<interfaces.length; i++)
          {
            if (i>0) out.print (", ");
            out.print (interfaces[i]);
          }
        out.println ();
      }

    out.outdent ();

    out.println ("{");
    out.indent ();

    Iterator fi = fields ();
    while (fi.hasNext ())
      {
        SmallField f = (SmallField)fi.next ();
        f.writeAsJava (out);
        out.println ();
      }

    Iterator ci = constructors ();
    while (ci.hasNext ())
      {
        SmallConstructor c = (SmallConstructor)ci.next ();
        c.writeAsJava (out);
        out.println ();
      }

    Iterator mi = methods ();
    while (mi.hasNext ())
      {
        SmallMethod m = (SmallMethod)mi.next ();
        m.writeAsJava (out);
        out.println ();
      }

    out.outdent ();
    out.println ("}");
  }

  // -------------------------------------------------------------------------
  // ---- Bytecode generation ------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Writes a standard Java classfile to a new file with the given name.
   */
  public void writeAsBytecodes (String fileName) throws IOException
  { writeAsBytecodes (new FileOutputStream (fileName)); }

  /**
   * Returns this class as a byte array.
   */
  public byte[] toByteArray ()
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream ();
    try {
      writeAsBytecodes (out);
      out.flush ();
    } catch (IOException ex) {
      // ??? huh ???  What can happen in a ByteArrayOutputStream??
      Util.assertTrue (false, 
                   "Did not expect IOException from ByteArrayOutputStream", 
                   ex);
    }
    return out.toByteArray ();
  }

  /**
   * Writes the compiled JVM classfile corresponding to this SmallJava class
   * to the given output stream.
   */
  public void writeAsBytecodes (OutputStream out) throws IOException
  {
    //Util.out.println ("Writing class "+name+" as bytecodes");

    // XXX what about modifiers & interfaces????
    ClassFileWriter classFileWriter = 
      new ClassFileWriter (name, superclassName, sourceFileName);

    Iterator fi = fields ();
    while (fi.hasNext ())
      {
        SmallField f = (SmallField)fi.next ();
        f.writeAsBytecodes (classFileWriter);
      }

    Iterator ci = constructors ();
    while (ci.hasNext ())
      {
        SmallConstructor c = (SmallConstructor)ci.next ();
        c.writeAsBytecodes (classFileWriter);
      }

    Iterator mi = methods ();
    while (mi.hasNext ())
      {
        SmallMethod m = (SmallMethod)mi.next ();
        m.writeAsBytecodes (classFileWriter);
      }

    classFileWriter.write (out);
  }

  // -------------------------------------------------------------------------
  // ---- Test Code ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Test: generate a class called 'Person', which has some methods and
   * code within.  Dump as a Java source file and as a class file.
   */
  public static void main (String[] args) throws Exception
  {
    SmallJavaUtil.printBanner (Util.out);

    // public class Person extends java.lang.Object
    SmallClass c = new SmallClass ("Person");

    // int i;
    c.field ("I", "i");

    // int j;
    c.field ("Z", "j");

    // String s;
    c.field ("Ljava.lang.String;", "s");

    // static void main (String[] args)
    {
      SmallMethod main = 
        c.staticMethod ("main([Ljava.lang.String;)V", new String[] {"args"});


      // Util.out.println ("Hello!")
      main.add (new Expr.Call (new Expr.GetStatic ("Ljava.io.PrintWriter;",
                                                   "com.svincent.util.Util", 
                                                   "out"), 
                               "java.io.PrintWriter",
                               "println(Ljava.lang.String;)V",
                               new Expr[] {new Expr.StringConst ("Hello!")}));

      // new Person ().foo ();
      main.add (new Expr.Call (new Expr.New ("Person", "()V", new Expr[] {}),
                               "Person",
                               "foo()V",
                               new Expr[] {}));

    }

    // void foo ()
    {
      SmallMethod foo = 
        c.method ("foo()V", new String[] {});

      // Util.out.println ("Hello!")
      foo.add (new Expr.Call (new Expr.GetStatic ("Ljava.io.PrintWriter;",
                                                   "com.svincent.util.Util", 
                                                   "out"), 
                               "java.io.PrintWriter",
                               "println(Ljava.lang.String;)V",
                               new Expr[] 
                              {new Expr.StringConst ("Hi from foo!")}));
      //   baz ()
      foo.add (new Expr.Call ("Person", "baz()V", new Expr[] {}));
      //   baz ()
      foo.add (new Expr.Call ("Person", "baz()V", new Expr[] {}));
      //   bar ()
      foo.add (new Expr.Call ("Person", "bar(I)V", 
                              new Expr[] {new Expr.IntConst (42)}));
    }


    // void bar ()
    {
      SmallMethod bar = c.method ("bar(I)V", new String[] {"arg1"});

      bar.add (SmallMacro.println ("Hello from bar!"));
      bar.add (SmallMacro.printField ("Person", "I", "i"));

      bar.add (SmallMacro.printLocal (bar, "arg1"));


      // i = 10;
      bar.add (new Expr.SetField ("Person", "I", "i",
                                     new Expr.Plus 
                                     (new Expr.IntConst (10),
                                      new Expr.GetField ("Person","I","i"))));

      Expr.If ifExpr = new Expr.If (new Expr.GT 
                                    (new Expr.GetField ("Person", "I", "i"),
                                     new Expr.IntConst (20)));

      // Util.out.println (this.i);
      ifExpr.setThen (SmallMacro.printField ("Person", "I", "i"));

      ifExpr.setElse (SmallMacro.println ("Too Small!"));

      bar.add (ifExpr);

      Expr.If ifExpr2 = new Expr.If (new Expr.BooleanConst (true));
      ifExpr2.setThen (SmallMacro.println ("Too Small!"));
      bar.add (ifExpr2);


      bar.add (SmallMacro.println ());
    }

    // void baz ()
    {
      SmallMethod baz = c.method ("baz()V", new String[] {});

      baz.add (SmallMacro.println ("Hello from baz!"));
      baz.add (SmallMacro.printField ("Person", "I", "i"));
      
      baz.local ("I", "idx", new Expr.IntConst (0));

      // --- loop from 0 to 20, run body.
      // println (hello, world)
      // print (idx)
      baz.add (SmallMacro.forLoop (baz, "idx", 0, 20, 
                                   new Expr.Begin ().
                                   add (SmallMacro.println ("Hello World!")).
                                   add (SmallMacro.printLocal (baz, "idx")).
                                   add (SmallMacro.println ())));
    }

    c.finalize ();

    c.writeAsBytecodes ("Person.class");
    c.writeAsJava ("Person.java");

    SmallClassLoader loader = new SmallClassLoader ();
    Class personClass = loader.loadClass (c);

    ReflectUtil.callMain (personClass, Util.EmptyStringArray);
  }
}
