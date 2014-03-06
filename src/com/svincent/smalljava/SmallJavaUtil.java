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
 * SmallJavaUtil.java 
 *
 * Various utility methods.
 * 
 */

package com.svincent.smalljava;

import java.io.*;
import java.lang.reflect.*; // XXX move to ReflectUtil

import com.svincent.util.*;

/**
 * Various utility methods used by (and to help use) Smalljava.
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class SmallJavaUtil {

  private SmallJavaUtil () {}

  /**
   * Returns 'true' if the current Smalljava library being used is 
   * compatible with the given version.
   *
   * <p>For example,</p>
   * <pre>
   *   if (SmallJavaUtil.isCompatibleWith ("2.0"))
   *     // --- do some 2.0 specific things
   * </pre>
   **/
  public static boolean isCompatibleWith (String specVersion)
  {
    Package pkg = Package.getPackage ("com.svincent.smalljava");
    if (pkg == null) 
      {
        Util.out.println ("WARNING: No package information available.  "+
                          "Are you running Smalljava from a JAR file?");
        return false;
      }
    return pkg.isCompatibleWith (specVersion);
  }

  /**
   * Get the specification version of the current Smalljava package.
   * Returns a string containing decimal integers separated by "."'s.
   * This string may have leading zeros.
   **/
  public static String getSpecificationVersion ()
  {
    Package pkg = Package.getPackage ("com.svincent.smalljava");
    if (pkg == null)
      {
        Util.out.println ("WARNING: No package information available.  "+
                          "Are you running Smalljava from a JAR file?");
        return "?.?";
      }
    return pkg.getSpecificationVersion ();
  }

  /**
   * Get the implementation version of the current Smalljava package.
   **/
  public static String getImplementationVersion ()
  {
    Package pkg = Package.getPackage ("com.svincent.smalljava");
    if (pkg == null)
      {
        Util.out.println ("WARNING: No package information available.  "+
                          "Are you running Smalljava from a JAR file?");
        return "?";
      }
    return pkg.getImplementationVersion ();
  }
  
  /**
   * Prints an informative banner about Smalljava.
   */
  public static void printBanner (PrintWriter out)
  {
    out.println ("Smalljava V"+getSpecificationVersion ()+"-"+
                 getImplementationVersion ()+"");
    out.println ("Copyright (C) 1999 Shawn Vincent");
    out.println ("http://www.svincent.com/moksa/");
    out.println ("Smalljava is released under the GNU general public license");
    out.println ("See http://www.gnu.org/copyleft/gpl.html for details.");
  }

  /**
   * Process the given classname, replacing '.' characters with '/'
   * characters.  
   **/
  public static String getBytecodeClassname (String javaClassname)
  { return javaClassname.replace ('.', '/'); }

  public static void main (String[] args)
  { printBanner (Util.out); }

  // -------------------------------------------------------------------------
  // ---- Internal Utility APIs ----------------------------------------------
  // -------------------------------------------------------------------------

  static SmallType parseFieldDescriptor (String fieldDescriptor)
    throws SmallJavaBuildingException
  {
    try {
      return SmallType.parseFieldDescriptor (fieldDescriptor);
    } catch (InvalidDescriptorException ex) {
      throw new SmallJavaBuildingException 
        ("Could not parse field descriptor '"+fieldDescriptor+"'!", ex);
    }
  }

  static String parseNameFromMethodSignature (String signature)
    throws SmallJavaBuildingException
  {
    int openParen = signature.indexOf ('(');
    if (openParen == -1)
      throw new SmallJavaBuildingException 
        ("For method '"+signature+
         "': Unable to parse signature: couldn't find open paren!");

    return signature.substring (0, openParen);
  }
  static String parseDescriptorFromMethodSignature (String signature)
    throws SmallJavaBuildingException
  {
    int openParen = signature.indexOf ('(');
    if (openParen == -1)
      throw new SmallJavaBuildingException 
        ("For method '"+signature+
         "': Unable to parse signature: couldn't find open paren!");
    return signature.substring (openParen, signature.length ());
  }

  static SmallType.MethodType parseTypeFromMethodDescriptor (String desc,
                                                             int desiredLen)
    throws SmallJavaBuildingException
  {
    SmallType.MethodType type;

    // --- parse the type.
    try {
      type = SmallType.parseMethodDescriptor (desc);
    } catch (InvalidDescriptorException ex) {
      throw new SmallJavaBuildingException 
        ("For descriptor '"+desc+
         "': Unable to parse!", ex);
    }

    // --- ensure that there are the same number of args as there arg 
    //   - types.
    if (type.getArgCount () != desiredLen)
      throw new SmallJavaBuildingException 
        ("For descriptor '"+desc+
         "': Arg type count ("+type.getArgCount ()+
         ") and desired type count ("+desiredLen+") do not agree.");

    return type;
  }

}
