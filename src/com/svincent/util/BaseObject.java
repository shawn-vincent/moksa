/*
 * BaseObject.java
 *
 */

package com.svincent.util;

import java.io.*;

/**
 * BaseObject is a common base object for things in the system that
 * don't already have some other base object.  This class provides useful
 * patterns useful in general, such as dumping an object to a writer.
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class BaseObject extends Object {

  // -------------------------------------------------------------------------
  // ---- User Overrides -----------------------------------------------------
  // -------------------------------------------------------------------------

  public String tag () { return toString (); }
  public void dump (PrintWriter out) { out.print (tag ()); }

  // -------------------------------------------------------------------------
  // ---- Utility Methods ----------------------------------------------------
  // -------------------------------------------------------------------------

  public void dump () { dump (Util.out); }

  public String dumpToString () 
  {
    StringPrintWriter out = new StringPrintWriter ();
    dump (out);
    return out.toString ();
  }

}
