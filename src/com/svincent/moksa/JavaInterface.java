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
 * JavaInterface.java
 *
 * The interface to Java code.
 *
 */
package com.svincent.moksa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.svincent.util.ReflectException;
import com.svincent.util.ReflectUtil;
import com.svincent.util.Util;

/**
 * Provides an interface to Java code.
 *
 * @see JavaTerm
 */
public class JavaInterface {

  /** Utility class: do not create. */
  private JavaInterface () {}

  // -------------------------------------------------------------------------
  // ---- Builtin Predicates -------------------------------------------------
  // -------------------------------------------------------------------------

  public abstract static class JavaRule extends Builtin.BuiltinRule {
  }

  /**
   * <p>Constructs a new Java object of a given class.</p>
   *
   * <p><code>java_constructor (+full_class_name(args), -instance)</code></p>
   *
   * <p>The idea and syntax for this predicate came from Prolog
   * Cafe</p>
   **/
  public static class Java_constructor_2 extends JavaRule 
  {
    public String getName () { return "java_constructor/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      PrologTerm classAndArgs = wam.getRegister (0).deref ();
      PrologTerm instance = wam.getRegister (1).deref ();

      if (!instance.isVariable ()) 
        return wam.getFactory ().callThrowInstantiationError ();

      if (!classAndArgs.isStructure ()) 
        return wam.getFactory ().callThrowTypeError ("compound", classAndArgs);

      String className = classAndArgs.getName ();
      int argCount = classAndArgs.getArity ();
      Object[] args;
      if (argCount == 0)
        args = Util.EmptyObjectArray;
      else
        {
          args = new Object[argCount];
          CompoundTerm term = (CompoundTerm)classAndArgs;
          for (int i=0; i<argCount; i++)
            args[i] = term.getSubterm (0).deref ();
        }

      // --- perhaps do fancier stuff here to cast arguments.
      // --- also perhaps search CLASSPATH.
      Object newInstance;

      try {
        newInstance = ReflectUtil.construct (className, args);
      } catch (ReflectException ex) {
        return wam.getFactory ().
          callThrow (makeJavaError (wam.getFactory (), ex));
      }

      if (!instance.unify (wam.getFactory ().wrapObject (newInstance))) 
        return wam.Fail;

      return wam.getContinuation ();
    }
  }

  public static PrologTerm makeJavaError (PrologFactory factory, Throwable ex)
  { return factory.makeCompoundTerm ("java_error", factory.wrapObject (ex)); }

  public static PrologTerm makeJavaError (PrologFactory factory, String atom)
  { return factory.makeCompoundTerm ("java_error", factory.makeAtom (atom)); }

  public static PrologTerm makeJavaError (PrologFactory factory, String atom1,
                                          String atom2)
  { return factory.makeCompoundTerm ("java_error", 
                                     factory.makeAtom (atom1),
                                     factory.makeAtom (atom2)); }

  /**
   * <p>Calls a method on a class or instance..</p>
   *
   * <p><code>java_method (+class_or_instance, +method, -return_value)
   * </code></p>
   *
   * <p>Here's a data conversion chart.  The list of conversions will
   * grow when I get a chance.</p>
   *
   * <pre>
   *     Prolog                  Java
   *     -------------------------------------------------
   *     integer         --      java.lang.Integer or int
   *     atom            --      java.lang.String
   *     list            --      java.lang.Object[]
   *     java term       --      object
   * </pre>
   *
   * <p>The idea and syntax for this predicate came from Prolog
   * Cafe</p>
   **/
  public static class Java_method_3 extends JavaRule 
  {
    public String getName () { return "java_method/3"; }
    public int getArity () { return 3; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      PrologTerm classOrInstance = wam.getRegister (0).deref ();
      PrologTerm method = wam.getRegister (1).deref ();
      PrologTerm returnValue = wam.getRegister (2).deref ();
     
      PrologFactory factory = wam.getFactory ();
 
      // --- figure out 'type' and 'self'.
      Class<?> type;
      Object self;
      if (classOrInstance.isJavaObject ())
        {
          self = ((JavaTerm)classOrInstance).getObject ();
          type = self.getClass ();
        }
      else
        {
          if (!classOrInstance.isAtom ())
            return factory.callThrowTypeError ("atom_or_obj",classOrInstance); 
          try {
            type = Class.forName (classOrInstance.getName ());
          } catch (ClassNotFoundException ex) {
            return factory.callThrow (makeJavaError (factory, ex));
          }
          self = null;
        }

      // --- find the method object.
      if (!method.isStructure ())
        return factory.callThrowTypeError ("compound", method);
      
      // XXX cache this somehow.

      // --- find all methods,  
      int arity = method.getArity ();

      Method foundMethod = null;

      Method[] methods = type.getMethods ();
      for (int i=0; i<methods.length; i++)
        {
          Method m = methods[i];
          if (m.getName ().equals (method.getName ()))
            {
              Class<?>[] parmTypes = m.getParameterTypes ();
              if (parmTypes.length == arity)
                {
                  // --- for each method, attempt to match up types.
                  //   - in the long term, use a fancier algorithm here.
                  //   - Maybe steal the method resolution stuff out of 
                  //   - BeanShell?  (www.beanshell.org)

                  // for now, this is good enough.
                  foundMethod = m;
                  break;
                }
            }
        }

      if (foundMethod == null)
        return factory.callThrow
          (makeJavaError (factory, 
                          "java_error", "method_not_found"));

      // --- prepare parameters.

      Object[] parms;
      if (arity == 0)
        parms = Util.EmptyObjectArray;
      else
        {
          CompoundTerm term = (CompoundTerm)method;
          parms = new Object[arity];
          for (int i=0; i<parms.length; i++)
            parms[i] = convertToJava (term.getSubterm (i).deref ());
        }


      // --- call the method.
      Object ret;
      try {
        ret = foundMethod.invoke (self, parms);

      } catch (IllegalAccessException ex) { 
        // if the underlying method is inaccessible.
        return factory.callThrow (makeJavaError (factory, ex));
      } catch (IllegalArgumentException ex) { 
        // if the number of actual and formal parameters differ, or if
        // an unwrapping conversion fails.
        return factory.callThrow (makeJavaError (factory, ex));
      } catch (InvocationTargetException ex) { 
        // if the underlying method throws an exception.
        return factory.callThrow (makeJavaError (factory, 
                                                 ex.getTargetException ()));
      } catch (NullPointerException ex) { 
        // if the specified object is null and the method is an
        // instance method.
        return factory.callThrow (makeJavaError (factory, ex));
      } catch (ExceptionInInitializerError ex) { 
        // if the initialization provoked by this method fails.
        return factory.callThrow (makeJavaError (factory, ex.getException ()));
      }

      // --- unify return value with 'returnValue' parameter.
      JavaTerm wrapped = factory.wrapObject (ret);
      if (wam.engine.debug)
        wam.engine.log ().println 
          ("Called method, got return value "+wrapped.tag ());

      if (!wrapped.unify (returnValue)) return wam.Fail;

      // --- <pant, pant>... success!
      return wam.getContinuation ();
    }

    public static Object convertToJava (PrologTerm o) throws PrologException
    {
      if (o.isInteger ())
        return new Integer (o.intValue ());
      if (o.isFloat ())
        return new Double (o.floatValue ());
      if (o.isAtom ())
        return o.getName ();
//        if (o.isList ())
//          return ;
      if (o.isJavaObject ())
        return ((JavaTerm)o).getObject ();

      else
        // XXX huh?
        return o;
    }
  }
}
