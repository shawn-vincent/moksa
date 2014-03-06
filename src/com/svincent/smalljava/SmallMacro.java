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
 * SmallMacro.java 
 * 
 */

package com.svincent.smalljava;

import com.svincent.util.*;
import java.util.*;

/**
 * A small library of useful expression generators for SmallJava.
 *
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class SmallMacro extends BaseObject {
  
  private SmallMacro () {}

  // -------------------------------------------------------------------------
  // ---- High-level control structures --------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Builds a for loop.
   * <p>
   * Builds 
   * <pre>
   *   for ([varName]=[start]; [varName]<[end]; [varName]++)
   *     [body]
   * </pre>
   * into the given method.<p>
   *
   * Note that the local int variable must be defined before calling
   * this method, like so:
   *
   * <pre>
   *   // --- print the integers between 0 and 9, inclusive.
   *   method.local ("I", "i", new Expr.IntConst (0));
   *   method.add (SmallMacro.forLoop (method, "i", 0, 10, 
   *                                   SmallMacro.printLocal (method, "i")));
   * </pre>
   */
  public static Expr forLoop (SmallMethod method, String varName,
                              int start, int end, Expr body)
    throws SmallJavaBuildingException
  {
    Expr.Begin b = new Expr.Begin ();

    // int [varName] = [start]
    b.add (new Expr.SetLocal (varName, new Expr.IntConst (start)));
    
    // while ([varName] < [end])
    Expr.While whileBlock = 
      new Expr.While (new Expr.LT (new Expr.GetLocal (varName), 
                                   new Expr.IntConst (end)));
     
    Expr.Begin begin = new Expr.Begin ();
    whileBlock.setBody (begin);
     
    begin.add (body);
    begin.add (new Expr.SetLocal (varName, 
                                  new Expr.Plus(new Expr.GetLocal(varName),
                                                new Expr.IntConst (1))));

    b.add (whileBlock);

    return b;
  }

  // -------------------------------------------------------------------------
  // ---- Printing various values --------------------------------------------
  // -------------------------------------------------------------------------

  /** Calls Util.out.println (); */
  public static Expr println ()
    throws SmallJavaBuildingException
  {
    return new Expr.Call (new Expr.GetStatic ("Ljava.io.PrintWriter;",
                                              "com.svincent.util.Util", 
                                              "out"), 
                          "java.io.PrintWriter",
                          "println()V",
                          Expr.EmptyArray);
  }

  /** Calls Util.out.println ("msg"); */
  public static Expr println (String msg)
    throws SmallJavaBuildingException
  {
    return new Expr.Call (new Expr.GetStatic ("Ljava.io.PrintWriter;",
                                              "com.svincent.util.Util", 
                                              "out"), 
                          "java.io.PrintWriter",
                          "println(Ljava.lang.String;)V",
                          new Expr[] {new Expr.StringConst (msg)}
                          );
  }

  /** Calls Util.out.print, printing the given field reference. */
  public static Expr printField (String className, String descriptor, 
                                 String fieldName)
    throws SmallJavaBuildingException
  {
    return new Expr.Call (new Expr.GetStatic ("Ljava.io.PrintWriter;",
                                              "com.svincent.util.Util", 
                                              "out"), 
                          "java.io.PrintWriter",
                          "print("+getAppropriatePrintType (descriptor)+")V",
                          new Expr[] {new Expr.GetField (className, 
                                                         descriptor,
                                                         fieldName)});
  }

  /** Calls Util.out.print, printing the given local field reference. */
  public static Expr printLocal (SmallMethod method, String fieldName)
    throws SmallJavaBuildingException
  {
    SmallMethod.Local local = method.getLocal (fieldName);
    String descriptor = local.getType ().descriptorToString ();
    return new Expr.Call (new Expr.GetStatic ("Ljava.io.PrintWriter;",
                                              "com.svincent.util.Util", 
                                              "out"), 
                          "java.io.PrintWriter",
                          "print("+getAppropriatePrintType (descriptor)+")V",
                          new Expr[] {new Expr.GetLocal (fieldName)});
  }

  /**
   * Does fancy algorithms to determine, given a field descriptor,
   * the appropriate parameter descriptor for a PrintWriter's print()
   * or println() call that would be good for printing that field.
   */
  private static String getAppropriatePrintType (String descriptor)
    throws SmallJavaBuildingException
  {
    SmallType t = SmallJavaUtil.parseFieldDescriptor (descriptor);
    String fieldTypeDesc;
    if (t.isPrimitive ())
      fieldTypeDesc = t.descriptorToString ();
    else
      fieldTypeDesc = "Ljava.lang.Object;";
    return fieldTypeDesc;
  }
}
