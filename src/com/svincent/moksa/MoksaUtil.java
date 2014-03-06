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
 * MoksaUtil.java
 *
 */
package com.svincent.moksa;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.lang.reflect.*; // XXX move to ReflectUtil

import com.svincent.util.*;

/**
 * Contains static utility methods to do a bunch of random stuff that
 * doesn't belong elsewhere.
 **/
public class MoksaUtil extends WamObject {

  /**
   * Prints a copyright/version information banner for Moksa.
   **/
  public static void printBanner (PrintWriter out)
  {
    out.println ();
    out.println ("Moksa V0.1 - http://www.svincent.com/moksa/");
    out.println ("   Copyright (C) 1999 Shawn Vincent - "+
                 "http://www.svincent.com/shawn/");
    out.println ("   Moksa is released under the "+
                 "GNU General Public License");
    out.println ("   See http://www.gnu.org/copyleft/gpl.html for details.");
  }

  /**
   * Returns 'true' if the current Moksa subsystem being used is
   * compatible with the given version.
   *
   * <p>For example,</p>
   * <pre>
   *   if (MoksaUtil.isCompatibleWith ("2.0"))
   *     // --- do some 2.0 specific things
   * </pre>
   **/
  public static boolean isCompatibleWith (String specVersion)
  {
    return isCompatibleWith (specVersion, getSpecificationVersion ());
  }

  // XXX not tested.
  static boolean isCompatibleWith (String desiredVersion, String actualVersion)
  {
    StringTokenizer desired = new StringTokenizer (desiredVersion, ".");
    StringTokenizer actual = new StringTokenizer (desiredVersion, ".");

    while (desired.hasMoreTokens ())
      {
        if (!actual.hasMoreTokens ()) return false;
        String dTok = desired.nextToken ();
        String aTok = actual.nextToken ();

        int dInt;
        int aInt;

        try {
          dInt = Integer.parseInt (dTok);
          aInt = Integer.parseInt (aTok);
        } catch (NumberFormatException ex) {
          Util.assertTrue (false, "Got bad version strings: "+aTok+", "+dTok+
                       ": wanted Integers");
          return false;
        }

        if (aInt > dInt) return true;
        if (dInt > aInt) return true;

        // otherwise, loop.
      }

    return true;
  }

  /**
   * Get the specification version of the current Moksa package.
   * Returns a string containing decimal integers separated by "."'s.
   * This string may have leading zeros.
   **/
  public static String getSpecificationVersion ()
  {
    Package pkg = Package.getPackage ("com.svincent.moksa");
    if (pkg == null) return getManifestEntry ("Specification-Version");
    String retval = pkg.getSpecificationVersion ();
    if (retval == null) retval = getManifestEntry ("Specification-Version");
    return stripQuotes (retval);
  }

  /**
   * Get the implementation version of the current Moksa package.
   **/
  public static String getImplementationVersion ()
  {
    Package pkg = Package.getPackage ("com.svincent.moksa");
    if (pkg == null) return getManifestEntry ("Package-Version");
    String retval = pkg.getImplementationVersion ();
    if (retval == null) retval = getManifestEntry ("Package-Version");
    return stripQuotes (retval);
  }

  static String stripQuotes (String s)
  {
    if (s.charAt (0) == '"') s = s.substring (1);
    if (s.charAt (s.length ()-1) == '"') s = s.substring (0, s.length ()-1);
    return s;
  }

  /**
   * Read an entry out of our Manifest.  Required if we're not in a
   * JAR file.
   **/
  public static String getManifestEntry (String key)
  { return getManifestEntry ("/com/svincent/moksa/moksa.manifest", key); }

  /**
   * Read an entry out of some Manifest.  Required if we're not in a
   * JAR file.
   **/
  public static String getManifestEntry (String manifestName, String key)
  {
    try {
      // --- find the manifest file.
      InputStream _in = MoksaUtil.class.getResourceAsStream (manifestName);
      if (_in == null)
        {
          Util.err.println 
            ("Warning: Expected to get manifest at "+manifestName);
          return null;
        }

      // --- read the Manifest attributes
      Manifest manifest = new Manifest (_in);
      Attributes attributes = manifest.getMainAttributes ();

      // --- return the desired one.
      return attributes.getValue (key);

    } catch (IOException ex) {
      Util.err.println ("Did not expect IOException from Manifest reading");
      return null;
    }
  }

  public static void main (String[] args)
  { Util.out.println (getSpecificationVersion ()); }

}
