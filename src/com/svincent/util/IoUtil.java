/*
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
 * IoUtil.java
 */
package com.svincent.util;

import java.io.*;
import java.util.*;
import java.net.*;


/**
 * Implements various I/O utility methods.  In particular, given an
 * URL, it will open either a Reader or an InputStream on that URL,
 * isolating you from knowledge of whether it's local or remote, etc.
 **/
public class IoUtil {
  
  /**
   * Normalize an URI (turn into a valid URI)
   **/
  public static String normalizeUri (String uri)
  {
    // --- remove back-slashes.
    uri = uri.replace ('\\', '/');

    // --- do bad things on a Windows platform.
    //   - We have something of the form:  
    //   -     X:blahblah
    //   - Convert it to:
    //   -     file:///X:blahblah
    if (uri.charAt (1) == ':')
      uri = "file:///"+uri.charAt (0)+":"+uri.substring (2);

    // --- modern JDKs don't allow file URIs to not have "file:" at
    // --- the beginning.  XXX This is probably broken: we should do
    // --- something fancier with relative URIs here and a reasonable
    // --- base.  Not for today.  HACK HACK
    if (uri.charAt (0) == '/')
      uri = "file:"+uri;

    // XXX do hacky stuff.
    return uri;
  }

  /**
   * Open an InputStream on the given URI.
   **/
  public static InputStream openInputUri (String uriStr) throws IOException
  {
    String normalized = normalizeUri (uriStr); 
    //Util.out.println ("normalized == "+normalized);
    URL uri = new URL (normalized);
    return uri.openStream ();
  }

  /**
   * Open an OutputStream on the given URI.
   **/
  public static OutputStream openOutputUri (String uriStr, boolean append) 
    throws IOException
  {
    String normalized = normalizeUri (uriStr); 

    if (!normalized.startsWith ("file://"))
      throw new IOException ("Currently only support writing to file: URIs.");

    String fileName = normalized.substring ("file://".length ());
    return new FileOutputStream (fileName);
  }

  public static void main (String[] args)
  {
    try {
      IoUtil.openInputUri ("c:\\users\\svincent\\cable.txt");
    } catch (Throwable ex) {
      ex.printStackTrace ();
    }
  }

}
