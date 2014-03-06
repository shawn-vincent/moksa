
package com.svincent.util.smath;

import java.io.*;
import java.math.BigInteger;

import com.svincent.util.*;

/**
 * A class which can perform arithmetic on distributions.
 */
public class HistogramIntDistribution extends Distribution {

  static final long[] SingletonWeights = new long[] {1};
  
  long[] values = SMath.EmptyLongArray;
  long[] weights = SMath.EmptyLongArray;
  int size = 0;
  long total = 0;

  public HistogramIntDistribution () {}

  public HistogramIntDistribution (long[] _values, long[] _weights)
    throws SMathException
  {
    init (_values, _weights);
  }

  public void init (long[] _values, long[] _weights) throws SMathException
  {
    int newSize = SMath.min (_values.length, _weights.length);

    values = new long[newSize];
    System.arraycopy (_values, 0, values, 0, newSize);

    weights = new long[newSize];
    System.arraycopy (_weights, 0, weights, 0, newSize);

    size = newSize;
    updateTotal ();
  }

  public static HistogramIntDistribution dice (int rolls, int sides)
    throws SMathException
  {
    if (rolls == 1)
      return flat (sides);
    long[] weights = SMath.dice (rolls, sides);
    long[] values = new long[rolls*sides+1];
    for (int i=rolls; i<rolls*sides+1; i++)
      values[i-rolls] = i;
    return new HistogramIntDistribution (values, weights);
  }

  public static HistogramIntDistribution flat (int sides)
    throws SMathException
  {
    long[] values = new long[sides];
    long[] weights = new long[sides];
    for (int i=0; i<sides; i++)
      values[i] = i;
    for (int i=0; i<sides; i++)
      weights[i] = 1;
    return new HistogramIntDistribution (values, weights);
  }

  public static HistogramIntDistribution singleton (long value)
    throws SMathException
  {
    return new HistogramIntDistribution (new long[] {value}, SingletonWeights);
  }

  /**
   * Converts this histogram into one containing big integers.
   */
  public HistogramBignumDistribution bloat () throws SMathException
  {
    BigInteger[] bigValues = SMath.toBigIntegerArray (values);
    BigInteger[] bigWeights = SMath.toBigIntegerArray (weights);

    return new HistogramBignumDistribution (bigValues, bigWeights);
  }

  /**
   * Called when somebody wants the sum of the weights.
   */
  void updateTotal () throws SMathException
  { total = SMath.sum (weights, size); }

  /**
   * Compose this distribution with the other given distribution,
   * using the given functor to do the math.
   */
  public Distribution compose (Distribution _other, Functor f)
    throws SMathException
  {
    try {
      HistogramIntDistribution other = (HistogramIntDistribution)_other;
      return composeInt (other, f);
    } catch (ClassCastException ex) {
      throw new SMathException 
	("Type Error: cannot compose HistogramIntDistribution and "+
	 _other.getClass ().getName ());
    }
  }

  public HistogramIntDistribution composeInt (HistogramIntDistribution other, 
					      Functor f)
    throws SMathException
  {
    HistogramIntDistribution retval = new HistogramIntDistribution ();
    // --- now we must do a pairwise composition of each element in the
    //   - distribution.
    for (int i=0; i<size; i++)
      {
	for (int j=0; j<other.size; j++)
	  {
	    long newValue = f.eval (this.values[i], other.values[j]);
	    long newWeight = SMath.multiply (this.weights[i], 
					     other.weights[j]);
	    
	    retval.addWeight (newValue, newWeight);
	  }
      }
    return retval;
  }

  /**
   * Adds 'w' to the relative weight of 'value' in this histogram.
   */
  void addWeight (long value, long w) throws SMathException
  {
    // --- 0 won't change anything anyway.
    if (w == 0) return;

    // --- search for the value.
    int i;
    for (i=0; i<size && values[i]<=value; i++)
      {
	// --- if the value is already there.
	if (values[i] == value) 
	  {
	    weights[i] = SMath.add (weights[i], w);
	    total = SMath.add (total, w);
	    return;
	    // --- the array is sorted.  If we pass it, insert *here*. 
	  } 
      }

    // --- if we hit the end, insert *there*.
    insert (i, value, w);
  }

  /**
   * Insert a new value,weight pair at the given location.
   */
  void insert (int location, long value, long weight) 
  {
    if (values.length <= size)
      grow ();
    
    // --- move the values down by one, if we're not inserting at the end.
    if (location != size)
      {
	System.arraycopy (values, location, 
			  values, location+1, size-location); 
	System.arraycopy (weights, location, 
			  weights, location+1, size-location); 
      }

    // --- insert the value.
    values[location] = value;
    weights[location] = weight;

    total = SMath.add (total, weight);

    // --- remember this new value.
    size++;
  }

  /**
   * Expand internal storage to contain more distributions.
   */
  void grow ()
  {
    long[] newValues = new long[SMath.max (16, values.length * 2)];
    long[] newWeights = new long[SMath.max (16, weights.length * 2)];
    System.arraycopy (values, 0, newValues, 0, values.length);
    System.arraycopy (weights, 0, newWeights, 0, weights.length);
    values = newValues;
    weights = newWeights;
  }

  public void dump (PrintWriter out)
  {
    out.print ("{");
    boolean starting = true;
    for (int i=0; i<size; i++)
      {
	if (weights[i] != 0)
	  {
	    if (!starting) out.print (", "); else starting = false;
	    out.print (values[i]);
	    if (weights[i] != 1)
	      {
		out.print (":");
		out.print (weights[i]);
	      }
	  }
      }
    out.print ("}");
    /*
    out.print ("/");
    out.print (total);
    out.print ("("+size+")");
    */
  }

  public static void main (String[] args)
  {
    Util.out.print ("1d6 == ");
    Distribution d6 = 
      new HistogramIntDistribution (new long[]{1, 2, 3, 4, 5, 6},
                                 new long[]{1, 1, 1, 1, 1, 1});
    d6.dump (Util.out);
    Util.out.println ();
    
    Util.out.print ("2d6 == ");
    Distribution _2d6 = d6.add (d6);
    _2d6.dump (Util.out);
    Util.out.println ();

    Distribution _3d6 = HistogramIntDistribution.dice (3, 6);
    _3d6.dump (Util.out);
    Util.out.println ();

    Distribution _5d6 = _2d6.add (_3d6);
    _5d6.dump (Util.out);
    Util.out.println ();

    _5d6 = HistogramIntDistribution.dice (5, 6);
    _5d6.dump (Util.out);
    Util.out.println ();

    _5d6 = d6.add (d6.add (d6.add (d6.add (d6))));
    _5d6.dump (Util.out);
    Util.out.println ();
  }


}

