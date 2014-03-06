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
 * SmallClassLoader.java 
 *
 * A ClassLoader for SmallClasses: allows code to be dynamically executed.
 * 
 */

package com.svincent.smalljava;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

import com.svincent.util.*;

/**
 * Allows dynamic loading and execution of Smalljava code.
 *
 * <p>Beware.  JDK 1.2 added new rules regarding the garbage
 * collection of classes.  In particular,</p>
 *
 * <p><em>"A class or interface may be unloaded if and only if its
 * class loader is unreachable (the definition of unreachable is given
 * in JLS 12.6.1). Classes loaded by the bootstrap loader may not be
 * unloaded."</em></p>
 *
 * <p>Here is Sun's 
 * <a href="http://java.sun.com/docs/books/jls/unloading-rationale.html">
 * technical rationale</a> for this decision</p>
 *
 * <p>This means that you must be careful about how you use
 * dynamically generated classes.  One model that is guaranteed to
 * work is to create a ClassLoader, define and execute a class using
 * it, and then drop the ClassLoader on the floor.  This works for
 * some applications, but not all.</p>
 *
 * <p>Fancier schemes are possible, and may be documented here later.
 * It is possible that a fancy scheme using weak references and many
 * ClassLoaders may be implemented in Smalljava.  If that happens,
 * these facilities will be made available as part of the Smalljava
 * API.</p>
 *
 * <p>There may be issues with this ClassLoader, particularly with
 * respect to redefining classes again and again.  This has not been
 * thoroughly tested.</p>
 *
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a> 
 **/
public class SmallClassLoader extends ClassLoader {

  /**
   * The map of currently installed and cached classes.
   */
  Map classesByName = new HashMap ();

  /**
   * Requests the immediate loading of the given SmallClass object.
   */
  public Class loadClass (SmallClass smallClass) 
  {
    if (!smallClass.isFinalized ())
      Util.assertTrue (false, "Attempt to load unfinalized SmallJava class '"+
                   smallClass.getName ()+"'");

    installClass (smallClass);
    try {
      return loadClass (smallClass.getName ());
    } catch (ClassNotFoundException ex) {
      // eh?  we just INSTALLED it!
      Util.assertTrue (false, "Unusual: installing class "+smallClass.getName ()+
                   " seems to have failed!", ex);
      return null;
    }
  }

  /**
   * Install the given SmallClass into this ClassLoader.
   */
  public void installClass (SmallClass smallClass)
  {
    String name = smallClass.getName ();
    byte[] data = smallClass.toByteArray ();

    Class c = defineClass (name, data, 0, data.length);
    classesByName.put (name, c);
  }

  /**
   * Finds a class, trying the current installed classes first, 
   * and then the system classloader, if all else fails.
   */
  public Class findClass (String name) throws ClassNotFoundException
  {
    Class c = (Class)classesByName.get (name);
    if (c == null)
      {
        ClassLoader systemClassLoader = getSystemClassLoader ();
        c = systemClassLoader.loadClass (name);
        classesByName.put (name, c);
      }
    return c;
  }
}
