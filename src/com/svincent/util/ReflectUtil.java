/*
 * Util: a utility library for Java programs.
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
 * ReflectUtil.java 
 * 
 */

package com.svincent.util;

import java.lang.reflect.*;


/**
 * Minimizes the tedium of Java reflection.
 *
 * <p>Java reflection is an exciting technology which enables a whole
 * new style of programming.  It's particularly exciting when coupled
 * with dynamically generated code, for langauge interpreters,
 * etc.</p>
 *
 * <p>Unfortunately, every time you do reflection, you're swamped with
 * exceptions.  SecurityExceptions, MethodNotFound exceptions, etc
 * etc.  It's enough to drive one batty.  It's good to know various
 * things happened, but most of the time, you just want to fail
 * gracefully on all of them.</p>
 *
 * <p>ReflectUtil is designed to minimize some of the horrors of
 * reflectively accessing java objects, by repackaging some
 * exceptions.</p>
 *
 * <p>XXX current issue:  ReflectException should be subclassed, so that
 * various exceptions can be managed, if desired.</p>
 **/
public class ReflectUtil extends BaseObject {

  private ReflectUtil () {}

  /**
   * Calls the main() method on the given class.
   */
  public static void callMain (Class c, String[] args) throws ReflectException
  {
    // --- get 'main'.
    Method m = getMethod (c, "main",  new Class[] {String[].class});

    Util.assertTrue (m != null, "Got null method from getMethod, without "+
                 "getting NoSuchMethodException!");

    // --- check modifiers.
    int modifiers = m.getModifiers ();

    // --- main must be static
    if (!Modifier.isStatic (modifiers))
      throw new ReflectException ("Found main(), but it should be static!");

    // --- main must be public
    if (!Modifier.isPublic (modifiers))
      throw new ReflectException ("Found main(), but it should be public!");

    // --- invoke the method.
    invoke (null, m, new Object[] { args });
  }

  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  // --- fix to examine args.
  public static Object construct (String className, Object[] args)
    throws ReflectException
  {
    try {
      Class c = Class.forName (className);
      return construct (c, args);
    } catch (ClassNotFoundException ex) {
      throw new ReflectException ("Could not find class '"+className+"'", ex);
    }
  }

  // --- fix to examine args.
  public static Object construct (Class c, Object[] args) 
    throws ReflectException
  {
    try {
      return c.newInstance ();
    } catch (IllegalAccessException ex) {
      // if the class or initializer is not accessible.
      throw new ReflectException 
        ("Class or no-arg constructor not accessible", ex);
    } catch (InstantiationException ex) {
      // if this Class represents an abstract class, an interface, an
      // array class, a primitive type, or void; or if the
      // instantiation fails for some other reason.
      throw new ReflectException 
        ("Can't construct '"+c.getName ()+"': it might be abstract, "+
         "an interface, or some other sort of bad thing.", ex);
    } catch (ExceptionInInitializerError ex) {
      // - if the initialization provoked by this method fails.
      throw new ReflectException 
        ("Can't construct '"+c.getName ()+
         "': an exception occured in its initializer", ex.getException ());
    } catch (SecurityException ex) {
      // - if there is no permission to create a new instance.
      throw new ReflectException 
        ("Can't construct '"+c.getName ()+
         "': no permission to create new instance", ex);
    }
  }

  /**
   * Return a method of 'c' with the given name and parameters.
   * Throw an exception if anything really bad happened.
   */
  public static Method getMethod (Class clazz, String name, Class[] parmTypes)
    throws ReflectException
  {
    try {
      return clazz.getMethod (name, parmTypes);
    } catch (NoSuchMethodException ex) {
      throw new ReflectException 
        ("Could not find method '"+getMethodSignature (name, parmTypes)+
         "' on class "+clazz.getName (), ex);
    } catch (SecurityException ex) {
      throw new ReflectException 
        ("Got SecurityException trying to get Method for '"+
         getMethodSignature (name, parmTypes)+"': "+
         "don't have permission to get members for class "+
         clazz.getName ()+"", ex);
    }
  }

  /**
   * Invokes the given method, wrapping exceptions up in
   * ReflectExceptions for ease of consumption.  
   */
  public static Object invoke (Object self, Method method, Object[] parms)
    throws ReflectException
  {
    try {
      return method.invoke (self, parms);
    } catch (NullPointerException ex) {
      throw new ReflectException 
        ("Got unexpected nullpointer exception from "+
         "invoking method '"+getMethodSignature (method)+"'", ex);
    } catch (IllegalAccessException ex) {
      throw new ReflectException 
        ("Could not access method '"+getMethodSignature (method)+
         "' (is declared "+
         Modifier.toString (method.getModifiers ())+")", ex);
    } catch (IllegalArgumentException ex) {
      throw new ReflectException 
        ("Bad parameters passed to method '"+
         getMethodSignature (method)+"'", ex);
    } catch (InvocationTargetException ex) {
      throw new ReflectException 
        ("Got exception while invoking '"+getMethodSignature (method)+
         "'", ex.getTargetException ());
    } catch (ExceptionInInitializerError ex) {
      throw new ReflectException 
        ("Got exception initializing some class while "+
         "invoking method '"+getMethodSignature (method)+"'", ex);
    }
  }

  // -------------------------------------------------------------------------
  // ---- I/O ----------------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Return a human-readable method signature for the method with
   * the given name and parameters.
   */
  public static String getMethodSignature (String name, Class[] parmTypes)
  {
    StringBuffer out = new StringBuffer ();

    out.append (name);
    out.append ('(');
    for (int i=0; i<parmTypes.length; i++)
      {
        if (i>0) out.append (", ");
        out.append (parmTypes[i].getName ());
      }
    out.append (')');

    return out.toString ();
  }

  /**
   * Return a human-readable method signature for the given method.
   */
  public static String getMethodSignature (Method method)
  { 
    return getMethodSignature (method.getName (), 
                               method.getParameterTypes ()); 
  }
}
