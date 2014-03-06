
package com.svincent.util.smath;

import java.io.*;
import java.math.*;

import com.svincent.util.*;

/**
 * A class which can perform arithmetic on distributions.
 */
public class HistogramBignumDistribution extends Distribution {

  static final BigInteger[] SingletonWeights = new BigInteger[] {SMath.BigOne};

  BigInteger[] values = SMath.EmptyBigIntegerArray;
  BigInteger[] weights = SMath.EmptyBigIntegerArray;
  int size = 0;
  BigInteger total = SMath.BigZero;

  public HistogramBignumDistribution () {}

  public HistogramBignumDistribution (BigInteger[] _values, 
				      BigInteger[] _weights)
    throws SMathException
  {
    init (_values, _weights);
  }

  public static HistogramBignumDistribution singleton (BigInteger value)
    throws SMathException
  {
    return new HistogramBignumDistribution (new BigInteger[] {value}, 
                                            SingletonWeights);
  }

  public void init (BigInteger[] _values, BigInteger[] _weights) 
    throws SMathException
  {
    int newSize = SMath.min (_values.length, _weights.length);

    values = new BigInteger[newSize];
    System.arraycopy (_values, 0, values, 0, newSize);

    weights = new BigInteger[newSize];
    System.arraycopy (_weights, 0, weights, 0, newSize);

    size = newSize;
    updateTotal ();
  }

  /**
   * Converts this histogram into one containing longs.
   */
  public HistogramIntDistribution grease () throws SMathException
  {
    long[] fastValues = SMath.toLongArray (values);
    long[] fastWeights = SMath.toLongArray (weights);

    return new HistogramIntDistribution (fastValues, fastWeights);
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
      HistogramBignumDistribution other = (HistogramBignumDistribution)_other;
      return composeBig (other, f);
    } catch (ClassCastException ex) {
      throw new SMathException 
	("Type Error: cannot compose HistogramBignumDistribution and "+
	 _other.getClass ().getName ());
    }
  }

  public HistogramBignumDistribution composeBig 
    (HistogramBignumDistribution other, Functor f)
    throws SMathException
  {
    HistogramBignumDistribution retval = new HistogramBignumDistribution ();
    // --- now we must do a pairwise composition of each element in the
    //   - distribution.
    for (int i=0; i<size; i++)
      {
	for (int j=0; j<other.size; j++)
	  {
	    BigInteger newValue = 
	      f.eval (this.values[i], other.values[j]);
	    // self ISSUE: is this right? or is * better?
	    //long newWeight = SMath.add (this.weights[i], other.weights[j]);
	    BigInteger newWeight = 
	      SMath.multiply (this.weights[i], other.weights[j]);
	    
	    retval.addWeight (newValue, newWeight);
	  }
      }
    return retval;
  }

  /**
   * Adds 'w' to the relative weight of 'value' in this histogram.
   */
  void addWeight (BigInteger value, BigInteger w) throws SMathException
  {
    // --- 0 won't change anything anyway.
    if (w.equals (SMath.BigZero)) return;

    // --- search for the value.
    int i;
    for (i=0; i<size && values[i].compareTo (value)<=0; i++)
      {
	// --- if the value is already there.
	if (values[i].equals (value)) 
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
  void insert (int location, BigInteger value, BigInteger weight) 
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
    BigInteger[] newValues = new BigInteger[SMath.max (16, values.length * 2)];
    BigInteger[] newWeights = new BigInteger[SMath.max (16, weights.length * 2)];
    System.arraycopy (values, 0, newValues, 0, values.length);
    System.arraycopy (weights, 0, newWeights, 0, weights.length);
    values = newValues;
    weights = newWeights;
  }

  public void dump (PrintWriter out)
  {
    out.print ("{");
    for (int i=0; i<size; i++)
      {
	if (i>0) out.print (", ");
	out.print (values[i]);
	out.print (":");
	out.print (weights[i]);
      }
    out.print ("}");
    /*
    out.print ("/");
    out.print (total);
    out.print ("("+size+")");
    */
  }

  public static HistogramBignumDistribution dice (int rolls, int sides)
    throws SMathException
  {
    BigInteger[] weights = SMath.bigDice (rolls, sides);
    BigInteger[] values = new BigInteger[rolls*sides+1];
    for (int i=rolls; i<rolls*sides+1; i++)
      values[i-rolls] = BigInteger.valueOf (i);
    return new HistogramBignumDistribution (values, weights);
  }

  public static HistogramBignumDistribution flat (int sides)
    throws SMathException
  {
    BigInteger[] values = new BigInteger[sides];
    BigInteger[] weights = new BigInteger[sides];
    for (int i=0; i<sides; i++)
      values[i] = BigInteger.valueOf (i);
    for (int i=0; i<sides; i++)
      weights[i] = SMath.BigOne; 
    return new HistogramBignumDistribution (values, weights);
  }

  public static void main (String[] args)
  {
    Util.out.print ("1d6 == ");
    Distribution d6 = 
      new HistogramBignumDistribution (new BigInteger[] 
				       {SMath.BigOne, 
					BigInteger.valueOf (2), 
					BigInteger.valueOf (3), 
					BigInteger.valueOf (4), 
					BigInteger.valueOf (5), 
					BigInteger.valueOf (6)},
                                       new BigInteger[]
				       {SMath.BigOne, 
					SMath.BigOne, 
					SMath.BigOne, 
					SMath.BigOne, 
					SMath.BigOne, 
					SMath.BigOne});
    d6.dump (Util.out);
    Util.out.println ();
    
    Util.out.print ("2d6 == ");
    Distribution _2d6 = d6.add (d6);
    _2d6.dump (Util.out);
    Util.out.println ();

    Distribution _3d6 = HistogramBignumDistribution.dice (3, 6);
    _3d6.dump (Util.out);
    Util.out.println ();

    Distribution _5d6 = _2d6.add (_3d6);
    _5d6.dump (Util.out);
    Util.out.println ();

    _5d6 = HistogramBignumDistribution.dice (5, 6);
    _5d6.dump (Util.out);
    Util.out.println ();

    _5d6 = d6.add (d6.add (d6.add (d6.add (d6))));
    _5d6.dump (Util.out);
    Util.out.println ();

    Distribution _20d20 = HistogramBignumDistribution.dice (20, 20);
    _20d20.dump (Util.out);
    Util.out.println ();
  }


}

