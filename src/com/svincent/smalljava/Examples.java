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
 * Examples.java 
 * 
 */

package com.svincent.smalljava;

import java.io.*;

import com.svincent.util.*;

/**
 * Provides a number of informative examples of
 * how to use the Smalljava framework. 
 */
public class Examples extends BaseObject {

  // -------------------------------------------------------------------------
  // ---- Hello World Example ------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Generates (in bytecode and source format), and runs, a
   * simple HelloWorld program.
   */
  public static class HelloWorld extends BaseObject {
    /**
     * Call this Main method to run the HelloWorld example.
     */
    public static void main (String[] args)
    {
      // --- print an informative banner.
      SmallJavaUtil.printBanner (Util.out);

      // --- create the hello world class.
      SmallClass c;
      try {
        c = makeHelloWorldClass ();
      } catch (SmallJavaBuildingException ex) {
        Util.out.println ("Error: could not build HelloWorld class!");
        ex.printStackTrace ();
        return;
      }

      // --- Finalize the class.
      try {
        c.finalize ();
      } catch (SmallJavaValidationException ex) {
        Util.out.println ("Error: could not finalize HelloWorld class!");
        ex.printStackTrace ();
        return;
      }

      // --- dump the class to a .class file.
      try {
        c.writeAsBytecodes ("HelloWorld.class");
      } catch (IOException ex) {
        Util.out.println ("Error: could not write out 'HelloWorld.class'!");
        ex.printStackTrace ();
        return;
      }

      // --- dump the class to a .java source file.
      try {
        c.writeAsJava ("HelloWorld.java");
      } catch (IOException ex) {
        Util.out.println ("Error: could not write out 'HelloWorld.java'!");
        ex.printStackTrace ();
        return;
      }

      // --- run the class, using a new ClassLoader.
      SmallClassLoader loader = new SmallClassLoader ();
      Class loadedClass = loader.loadClass (c);
      try {
        ReflectUtil.callMain (loadedClass, Util.EmptyStringArray);
      } catch (ReflectException ex) {
        Util.out.println ("Error: could not call 'main'!");
        ex.printStackTrace ();
        return;
      }
    }

    /**
     * Actually build a HelloWorld class.
     */
    public static SmallClass makeHelloWorldClass ()
      throws SmallJavaBuildingException
    {
      // --- make a new class called 'HelloWorld'.
      SmallClass helloWorldClass = new SmallClass ("HelloWorld");

      // --- make a new main method for it.
      SmallMethod main = 
        helloWorldClass.staticMethod ("main([Ljava.lang.String;)V", 
                                      new String[] {"args"});

      // --- This generates 
      //   -   Util.out.println ("Hello, world.");
      //   - Note the static get (Util.out), the method call (println),
      //   - and the constant String.
      main.add (new Expr.Call (new Expr.GetStatic ("Ljava.io.PrintWriter;",
                                                   "com.svincent.util.Util", 
                                                   "out"), 
                               "java.io.PrintWriter",
                               "println(Ljava.lang.String;)V",
                               new Expr[] 
                                   {new Expr.StringConst ("Hello, world.")}));
      main.add (new Expr.Return ());

      // --- NOTE:
      //   - Since printing things this way all the time is tedious,
      //   - the class SmallMacro has a helper that does the same thing.
      //   - The above could have been written.
      //   -    main.add (SmallMacro.println ("Hello, world."));

      // --- That's it: we have a HelloWorld program, now.
      return helloWorldClass;
    }
  }

  // -------------------------------------------------------------------------
  // ---- Simple Loop Example ------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Generates (in bytecode and source format), and runs, a
   * simple counter loop program.
   */
  public static class Loop extends BaseObject {
    /**
     * Call this Main method to run the HelloWorld example.
     */
    public static void main (String[] args)
    {
      // --- print an informative banner.
      SmallJavaUtil.printBanner (Util.out);

      // --- create the class.
      SmallClass c;
      try {
        c = makeLoopClass ();
      } catch (SmallJavaBuildingException ex) {
        Util.out.println ("Error: could not build loop class!");
        ex.printStackTrace ();
        return;
      }

      // --- Finalize the class.
      try {
        c.finalize ();
      } catch (SmallJavaValidationException ex) {
        Util.out.println ("Error: could not finalize loop class!");
        ex.printStackTrace ();
        return;
      }

      // --- dump the class to a .class file.
      try {
        c.writeAsBytecodes ("Loop.class");
      } catch (IOException ex) {
        Util.out.println ("Error: could not write out 'Loop.class'!");
        ex.printStackTrace ();
        return;
      }

      // --- dump the class to a .java source file.
      try {
        c.writeAsJava ("Loop.java");
      } catch (IOException ex) {
        Util.out.println ("Error: could not write out 'Loop.java'!");
        ex.printStackTrace ();
        return;
      }

      // --- run the class, using a new ClassLoader.
      SmallClassLoader loader = new SmallClassLoader ();
      Class loadedClass = loader.loadClass (c);
      try {
        ReflectUtil.callMain (loadedClass, Util.EmptyStringArray);
      } catch (ReflectException ex) {
        Util.out.println ("Error: could not call 'main'!");
        ex.printStackTrace ();
        return;
      }
    }

    /**
     * Build the loop class.
     */
    public static SmallClass makeLoopClass ()
      throws SmallJavaBuildingException
    {
      // --- make a new class called 'Loop'.
      SmallClass loopClass = new SmallClass ("Loop");

      // --- make a new main method for it.
      SmallMethod main = 
        loopClass.staticMethod ("main([Ljava.lang.String;)V", 
                                new String[] {"args"});

      // --- create a new local variable, like
      //   -     int i = 0;
      main.local (SmallType.Int, "i", new Expr.IntConst (0));

      // --- this hairy expression creates:
      //   -
      //   -    while (i < 20) 
      //   -      {
      //   -        Util.out.println (i);
      //   -        Util.out.println ();
      //   -        i = i + 2;
      //   -      }
      //   -
      main.add (new Expr.While (new Expr.LT (new Expr.GetLocal ("i"),
                                             new Expr.IntConst (20)),
                                new Expr.Begin ().
                                add (SmallMacro.printLocal (main, "i")).
                                add (SmallMacro.println ()).
                                add (new Expr.SetLocal 
                                     ("i",
                                      new Expr.Plus 
                                      (new Expr.GetLocal ("i"),
                                       new Expr.IntConst (2))))));
      main.add (new Expr.Return ());

      // --- That's it: we have a loop program, now.
      return loopClass;
    }
  }

}
