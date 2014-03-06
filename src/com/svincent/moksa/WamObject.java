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
 * WamObject.java
 *
 */
package com.svincent.moksa;

import java.io.*;

import com.svincent.util.*;

/**
 * Base class for stuff in the Wam package.  Provides useful general
 * object stuff...
 **/
public class WamObject {

  // -------------------------------------------------------------------------
  // ---- Print Behavior -----------------------------------------------------
  // -------------------------------------------------------------------------

  //public String toString () { return tag (); }

  /**
   * Retrieves a short human-readable representation of this object as
   * a String. <p>
   **/
  public String tag ()
  {
    StringPrintWriter out = new StringPrintWriter ();
    tag (out);
    return out.toString ();
  }

  /**
   * Writes a short human-readable representation of this object to
   * the given Writer. <p>
   **/
  protected void tag (PrintWriter out)
  {
    out.write (getClass ().getName ());
    out.write ('@');
    out.write (Integer.toHexString (System.identityHashCode (this)));
  }

}
