
package com.svincent.util.smath;

import java.io.*;
import java.math.*;

import com.svincent.util.*;


public class ZanyDistribution extends Distribution
{
  HistogramIntDistribution fastDist = null;
  HistogramBignumDistribution bigDist = null;

  // --- creates an invalid zany distribution: for internal use only.
  protected ZanyDistribution () {}

  public ZanyDistribution (long[] values, long[] weights)
  { fastDist = new HistogramIntDistribution (values, weights); }

  public ZanyDistribution (BigInteger[] values, BigInteger[] weights)
  { bigDist = new HistogramBignumDistribution (values, weights); }

  public static ZanyDistribution dice (int rolls, int sides)
    throws SMathException
  {
    ZanyDistribution retval = new ZanyDistribution ();
    
    try {
      retval.fastDist = HistogramIntDistribution.dice (rolls, sides);
    } catch (SMathOverflow ex) {
      retval.bigDist = HistogramBignumDistribution.dice (rolls, sides);
    }

    return retval;
  }

  public static ZanyDistribution singleton (long value) throws SMathException
  {
    ZanyDistribution retval = new ZanyDistribution ();
    retval.fastDist = HistogramIntDistribution.singleton (value);
    return retval;
  }

  public static ZanyDistribution singleton (BigInteger value)
    throws SMathException
  {
    ZanyDistribution retval = new ZanyDistribution ();
    retval.bigDist = HistogramBignumDistribution.singleton (value);
    return retval;
  }

  public boolean isBig () { return bigDist != null; }
  public boolean isFast () { return fastDist != null; }

  /**
   * Compose this distribution with the other given distribution,
   * using the given functor to do the math.
   */
  public Distribution compose (Distribution _other, Functor f)
    throws SMathException
  {
    try {
      ZanyDistribution retval = new ZanyDistribution ();
      ZanyDistribution other = (ZanyDistribution)_other;
      // --- make sure they're both compatible.
      //   - (I'd rather not do this (self ISSUE) -- see about 
      //   -   performing arithmetic on BigInts and these guys simultaneously)
      if (isBig () || other.isBig ()) { bloat (); other.bloat (); }

      // --- first case is most common: everything's fast.
      if (isFast ())
	{
	  try {
	    // --- try with the fast distribution first.
	    retval.fastDist = fastDist.composeInt (other.fastDist, f);
	    return retval;
	  } catch (SMathOverflow ex) {
	    // --- if that fails, try with the bloaty one.
	    bloat (); other.bloat ();
	    retval.bigDist = bigDist.composeBig (other.bigDist, f);
	    return retval;
	  }
	}
      else
	{
	  // --- this will never fail (2 biggies)
	  retval.bigDist = bigDist.composeBig (other.bigDist, f);
	  return retval;
	}

    } catch (ClassCastException ex) {
      throw new SMathException ("Error: can't compose ZanyDistribution and "+
				_other.getClass ().getName ());
    }
  }

  /** Makes this grow and grow. */
  public void bloat () throws SMathException
  {
    if (isBig ()) return;
    HistogramIntDistribution fast = fastDist;
    fastDist = null;
    bigDist = fast.bloat ();
  }

  public void grease () throws SMathException
  {
    if (isFast ()) return;
    HistogramBignumDistribution big = bigDist;
    fastDist = big.grease ();
    bigDist = null;
  }

  public void dump (PrintWriter out)
  {
    if (isBig ())
      bigDist.dump (out);
    else if (isFast ())
      fastDist.dump (out);
    else
      Util.assertTrue (false, "Internal error in ZanyDistribution null/null");
  }

  public static void main (String[] args)
  {
    Distribution d1 = ZanyDistribution.dice (3, 6);
    Util.out.println ("3d6 == "+d1.tag ());

    Distribution d2 = ZanyDistribution.dice (1, 4);
    Util.out.println ("1d4 == "+d2.tag ());

    Distribution d3 = d1.add (d2);
    Util.out.println ("sum == "+d3.tag ()+", "+d3);

    Distribution d4 = ZanyDistribution.singleton (1);
    Util.out.println ("Unit == "+d4.tag ()+", "+d4);

    Distribution d5 = d3.add (d4);
    Util.out.println ("sum+1 == "+d5.tag ()+", "+d5);
  }
}
