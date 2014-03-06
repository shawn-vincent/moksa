
package com.svincent.util.smath;

import java.io.*;

import com.svincent.util.*;

/**
 * A class which can perform arithmetic on distributions.
 */
public abstract class Distribution {
  
  /**
   * Compose this distribution with the other given distribution,
   * using the given functor to do the math.
   */
  public abstract Distribution compose (Distribution other, Functor f)
    throws SMathException;

  public Distribution add (Distribution other) throws SMathException
  { return compose (other, Functor.AddFunctor); }

  public Distribution subtract (Distribution other) throws SMathException
  { return compose (other, Functor.SubtractFunctor); }

  public Distribution multiply (Distribution other) throws SMathException
  { return compose (other, Functor.MultiplyFunctor); }

  public Distribution divide (Distribution other) throws SMathException
  { return compose (other, Functor.DivideFunctor); }

  public void dump (PrintWriter out) { out.print (toString ()); }

  public String tag ()
  {
    StringPrintWriter pw = new StringPrintWriter ();
    dump (pw);
    pw.flush ();
    return pw.toString ();
  }
}
