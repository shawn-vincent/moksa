
package com.svincent.util.smath;

import java.math.*;
import java.io.*;

import com.svincent.util.*;

/**
 * This is a class which contains a bunch of static
 * methods to do common mathematical operations.
 * They do the straightforward thing, except that they
 * detect overflow, and throw exceptions when it happens.
 */
public final class SMath {
  private SMath () {} // don't construct instances of SMath.

  public static final long LONG_MIN = 0x8000000000000000L;
  public static final long LONG_MAX = 0x7fffffffffffffffL;

  public static final int INT_MIN = 0x80000000;
  public static final int INT_MAX = 0x7fffffff;

  public static final int SHORT_MIN = 0x8000;
  public static final int SHORT_MAX = 0x7fff;

  public static final BigInteger[] EmptyBigIntegerArray = new BigInteger[0];
  public static final BigInteger BigZero = BigInteger.valueOf (0);
  public static final BigInteger BigOne = BigInteger.valueOf (1);

  public static final long[] EmptyLongArray = new long[0];
  public static final int[] EmptyIntArray = new int[0];

  // -------------------------------------------------------------------------
  // ---- Canonical arithmetic expressions (+-*/) ----------------------------
  // -------------------------------------------------------------------------

  public static final BigInteger add (BigInteger a, BigInteger b) 
    throws SMathException
  {
    return a.add (b);
  }
  
  /** Returns a+b, with overflow checking. */
  public static final long add (long a, long b) throws SMathException
  {
    long retval = a + b;

    // --- check to ensure that the signs of the numbers don't 
    //   - indicate overflow.
    long Overflow = (a & b & ~retval) | (~a & ~b & retval);
    if (Overflow < 0)
      throw new SMathOverflow ("Overflow: "+a+" + "+b+" != 0x"+
			       Long.toHexString (retval));

    // --- that should be all cases in which multiplication can overflow.
    return retval;
  }

  /** Returns a+b, with overflow checking. */
  public static final int add (int a, int b) throws SMathException
  {
    int retval = a + b;

    // --- check to ensure that the signs of the numbers don't 
    //   - indicate overflow.
    int Overflow = (a & b & ~retval) | (~a & ~b & retval);
    if (Overflow < 0)
      throw new SMathOverflow ("Overflow (cheap mech): "+a+" + "+b+" != 0x"+
			       Long.toHexString (retval));

    // --- that should be all cases in which multiplication can overflow.
    return retval;
  }

  public static final BigInteger subtract (BigInteger a, BigInteger b) 
    throws SMathException
  {
    return a.subtract (b);
  }

  /** Returns a-b, with overflow checking. */
  public static final long subtract (long a, long b) throws SMathException
  {
    return add (a, -b);
  }

  /** Returns a-b, with overflow checking. */
  public static final int subtract (int a, int b) throws SMathException
  {
    return add (a, -b);
  }

  public static final BigInteger multiply (BigInteger a, BigInteger b) 
    throws SMathException
  {
    return a.multiply (b);
  }

  static final long LONG_MAX_ROOT = (long)Math.sqrt (LONG_MAX);
  static final int INT_MAX_ROOT = (int)Math.sqrt (INT_MAX);

  /** Returns a*b, with overflow checking. */
  public static final long multiply (long a, long b) throws SMathOverflow
  {
    long absA = a < 0 ? -a : a;
    long absB = b < 0 ? -b : b;

    // these are safe.
    if (absA < LONG_MAX_ROOT && absB < LONG_MAX_ROOT || absA == 0 || absB == 0)
      return a*b;
    
    // test 
    if (absA > LONG_MAX / absB)
      throw new SMathOverflow ("Overflow: "+a+" * "+b+"!");

    return a * b;
  }

  /** Returns a*b, with overflow checking. */
  public static final int multiply (int a, int b) throws SMathOverflow
  {
    int absA = a < 0 ? -a : a;
    int absB = b < 0 ? -b : b;
    
    // these are safe.
    if (absA < INT_MAX_ROOT && absB < INT_MAX_ROOT || absA == 0 || absB == 0)
      return a*b;
    
    // test 
    if (absA > INT_MAX / absB)
      throw new SMathOverflow ("Overflow: "+a+" * "+b+"!");

    return a * b;
  }

  /** Returns a*b, with overflow checking. */
  public static final long multiplybad (long a, long b) throws SMathException
  {
    long retval = a*b;
    // --- multiplication overflow doesn't care about the signs of the
    //   - multiplicands.  What does matter is magnitude.

    // --- Overflow test 1:
    //   - Signs must agree (code snippet stolen from 
    //   - Paul G. Cumming di316@freenet.carleton.ca, found on the 'net at
    //   - http://www.algonquinc.on.ca/~alleni/c_programming/
    //   -                                   catchingIntegerOverflow.html)
    boolean Overflow = (((a ^ b) & LONG_MIN) != (retval & LONG_MIN));
    if (Overflow)
      throw new SMathOverflow ("Overflow (cheap): "+a+" * "+b+" != "+retval);

    // --- Overflow test 2:
    //   - the magnitude of the result must be larger than either of the
    //   - multiplicand's magnitudes.
    long absRetval = abs (retval);
    if (absRetval < abs (a) || absRetval < abs (b))
      throw new SMathOverflow ("Overflow: "+a+" * "+b+" != "+retval);

    // --- 
    return retval;
  }

  /** Returns a*b, with overflow checking. */
  public static final int multiplybad (int a, int b) throws SMathException
  {
    if (a==0 || b==0) return 0;

    int retval = a*b;
    // --- multiplication overflow doesn't care about the signs of the
    //   - multiplicands.  What does matter is magnitude.

    // --- See the long version for comments.
    boolean Overflow = (((a ^ b) & INT_MIN) != (retval & INT_MIN));
    if (Overflow)
      throw new SMathOverflow ("Overflow (cheap): "+a+" * "+b+" != "+retval);
    int absRetval = abs (retval);
    if (absRetval < abs (a) || absRetval < abs (b))
      throw new SMathOverflow ("Overflow: "+a+" * "+b+" != "+retval);

    return retval;
  }

  public static final BigInteger divide (BigInteger a, BigInteger b) 
    throws SMathException
  {
    return a.divide (b);
  }

  /**
   * Division cannot overflow.  It can underflow, but that's 
   * the danger of using ints to represent ratios.  Too bad for you, then.
   */
  public static final long divide (long a, long b) throws SMathException
  {
    long retval = a/b;
    return retval;
  }

  /**
   * Division cannot overflow.  It can underflow, but that's 
   * the danger of using ints to represent ratios.  Too bad for you, then.
   */
  public static final int divide (int a, int b) throws SMathException
  {
    int retval = a/b;
    return retval;
  }

  public static final long sign (long x) throws SMathException
  {
    return x==0?0:(x<0?-1:1);
  }

  public static final int sign (int x) throws SMathException
  {
    return x==0?0:(x<0?-1:1);
  }

  // -------------------------------------------------------------------------
  // ---- Array-based +-*/ ops -----------------------------------------------
  // -------------------------------------------------------------------------

  public static final long sum (long[] x) throws SMathException
  { return sum (x, x.length); }

  public static final long sum (long[] x, int size) throws SMathException
  {
    if (size < x.length)
      throw new IllegalArgumentException ("Passed size: too big.");
    long s = 0;
    for (int i=0; i<size; i++) s = add (s, x[i]);
    return s;
  }

  public static final BigInteger sum (BigInteger[] x) throws SMathException
  { return sum (x, x.length); }

  public static final BigInteger sum (BigInteger[] x, int size) 
    throws SMathException
  {
    if (size < x.length)
      throw new IllegalArgumentException ("Passed size: too big.");
    BigInteger s = BigZero;
    for (int i=0; i<size; i++) s = add (s, x[i]);
    return s;
  }

  public static final long product (long[] x) throws SMathException
  { return sum (x, x.length); }

  public static final long product (long[] x, int size) throws SMathException
  {
    if (size < x.length)
      throw new IllegalArgumentException ("Passed size: too big.");
    long p = 1;
    for (int i=0; i<size; i++) p = multiply (p, x[i]);
    return p;
  }

  // -------------------------------------------------------------------------
  // ---- Other common arithmetic expressions --------------------------------
  // -------------------------------------------------------------------------

  /** Returns the absolute value of a*/
  public static final long abs (long a) { return (a<0)?(-a):(a); }

  /** Returns the absolute value of a*/
  public static final int abs (int a) { return (a<0)?(-a):(a); }

  /** Returns the smaller of a and b */
  public static final long min (long a, long b) { return a<b?a:b; }

  /** Returns the smaller of a and b */
  public static final int min (int a, int b) { return a<b?a:b; }

  /** Returns the larger of a and b */
  public static final long max (long a, long b) { return a<b?b:a; }

  /** Returns the larger of a and b */
  public static final int max (int a, int b) { return a<b?b:a; }

  /** Returns the smallest value in x[0]..x[x.length] */
  public static final long min (long[] x) { return min (x, x.length); }

  /** Returns the smallest value in x[0]..x[size] */
  public static final long min (long[] x, int size)
  {
    if (size < x.length)
      throw new IllegalArgumentException ("Passed size: too big.");
    long m = 0x7fffffffffffffffL;
    for (int i=0; i<size; i++) if (x[i]<m) m=x[i];
    return m;
  }

  /** Returns the largest value in x[0]..x[x.length] */
  public static final long max (long[] x) { return max (x, x.length); }

  /** Returns the largest value in x[0]..x[size] */
  public static final long max (long[] x, int size)
  {
    if (size < x.length)
      throw new IllegalArgumentException ("Passed size: too big.");
    long m = 0x8000000000000000L;
    for (int i=0; i<size; i++) if (x[i]>m) m=x[i];
    return m;
  }

  /**
   * Calculates x^y, with overflow checking.
   */
  public static final long pow (long x, long y) throws SMathException
  {
    if (y==0) return 1;
    if (y<0) throw new SMathException ("Can't calculate negative powers!");
    long retval = 1;
    // --- the multiply here checks for overflow.
    for (long i=0; i<y; i++) retval = multiply (retval, x);
    return retval;
  }

  // -------------------------------------------------------------------------
  // ---- GCDs and factoring -------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Returns the greatest common divisor of x and y.
   * Algorithm borrowed from 
   * Applied Cryptography Second Edition, by Bruce Schneier.
   * His algorithm was copied from Knuth's Art of Computer Programming.
   * I heartily recommend both books.
   */
  public static final long gcd (long x, long y) throws SMathException
  {
    if (x<0) x = -x;
    if (y<0) y = -y;
    if (x+y == 0) throw new SMathException ("Undefined GCD (0,0).");
    long g = y;
    while (x>0)
      {
	g = x;
	x = y % x;
	y = g;
      }
    return g;
  }

  /**
   * Returns the greatest common divisor of x[0], x[1], ..., x[x.length].
   * Algorithm borrowed from 
   * Applied Cryptography Second Edition, by Bruce Schneier.
   * His algorithm was copied from Knuth's Art of Computer Programming.
   * I heartily recommend both books.
   */
  public static final long gcd (long[] x) throws SMathException
  {
    return gcd (x, x.length);
  }

  /**
   * Returns the greatest common divisor of x[0], x[1], ..., x[m].
   *
   * @see #gcd(long[])
   */
  public static final long gcd (long[] x, int size) throws SMathException
  {
    if (size < x.length)
      throw new IllegalArgumentException ("Passed size: too big.");
    // --- gcd ({}) == 0
    if (size<1) return 0;
    long g = x[0];
    for (int i=1; i<size; i++)
      {
	g = gcd (g, x[i]);
	// --- optimization, since for random x[], g == 1 60% of the time,
	//   -   by Knuth.
	if (g==1) return 1;
      }
    return g;
  }

  /**
   * Takes the n-ary ratio (of the form x[0]:x[1]:x[2], etc), and reduces
   * it to its smallest representable form.
   * Note: modifies 'x' in place!
   * Returns gcd(x), for those who are interested.
   */
  public static final long reduceRatio (long[] x) throws SMathException
  {
    return reduceRatio (x, x.length);
  }

  /**
   * @see #reduceRatio(long[])
   */
  public static final long reduceRatio (long[] x, int size) 
    throws SMathException
  {
    if (size < x.length)
      throw new IllegalArgumentException ("Passed size: too big.");
    // --- get the greatest common divisor.
    long divisor = gcd (x, size);
    // --- if that divisor is 1, give up.
    //   - Unfortunately, for random x[], gcd (x) == 1 60% of the time!  :(
    if (divisor != 1) 
      // --- reduce x.
      for (int i=0; i<size; i++) 
	x[i] /= divisor;
    // --- return the divisor, for curiosity's sake.
    return divisor;
  }

  // -------------------------------------------------------------------------
  // ---- Combinatorics ------------------------------------------------------
  // -------------------------------------------------------------------------

  /** 
   * factorials are poky to evaluate, so cache them 
   * Note that we can only calculate up to 20! anyway before we overflow
   * a long, so we only need 20 slots here.
   */
  static long[] cachedFactorial = new long[21];
  static { for (int i=0; i<=20; i++) cachedFactorial[i] = (long)-1; }

  /**
   * Calculates (n)!
   * I'd like to cache the results here....speed things
   * up considerably.
   */
  public static final long factorial (long n) throws SMathException
  {
    // --- optimize.
    if (n <= 0) return 1;

    // --- if possible, return a cached factorial.
    if (n <= 20 && cachedFactorial[(int)n] != -1)
      return cachedFactorial[(int)n];

    // --- check for overflow.
    if (n > 20)
      throw new SMathOverflow
  	("Numbers larger than 20! are not representable by a long");

    // --- this is now guaranteed to succeed, because of the check above.
    //   - Thus, use regular multiplication.
    long retval = n;
    for (long i = (n-1); i>1; i--) retval = retval * i;

    // --- cache the factorial, for next time.
    if (n <= 20)
      cachedFactorial[(int)n] = retval;

    return retval;
  }

  /** 
   * factorials are poky to evaluate, so cache them 
   * Cache the first 100.  Larger than that, and we're going banannas!
   */
  static BigInteger[] cachedBigFactorial = new BigInteger[100];

  /**
   * Calculates (n)!
   * I'd like to cache the results here....speed things
   * up considerably.
   */
  public static final BigInteger factorialBig (long n) throws SMathException
  {
    // --- optimize.
    if (n <= 0) return BigOne;

    // --- if possible, return a cached factorial.
    if (n <= 100 && cachedBigFactorial[(int)n] != null)
      return cachedBigFactorial[(int)n];

    BigInteger retval = BigInteger.valueOf (n);
    for (long i = (n-1); i>1; i--) 
      retval = retval.multiply (BigInteger.valueOf (i));

    // --- cache the factorial, for next time.
    if (n <= 100)
      cachedBigFactorial[(int)n] = retval;

    return retval;
  }

  /**
   * Calculates C(n, r).
   */
  public static final long choose (long n, long r) throws SMathException
  {
    // figure out the max, min of the two lower terms.
    long min = min (r, (n-r));
    long max = max (r, (n-r));

    // self ISSUE: can we use GCD here somewhere???

    //Util.out.println("max == "+max+", min == "+min);

    // how many factors of various primes can we remove from the top, bottom?
    // ('min' is the only term on the bottom.)
    // (If you need more precision, just add more primes in here!)
    long factorsOf2 = min / 2;
    long factorsOf3 = min / 3;
    long factorsOf5 = min / 5;
    long factorsOf7 = min / 7;
    long factorsOf11 = min / 11;
    long factorsOf13 = min / 13;
    long factorsOf17 = min / 17;
    long factorsOf19 = min / 19;

    // we're going to *drop* the 'max', and 
    // factor it into the top, removing prime factors when we can!
    long factorsOf2Left = factorsOf2;
    long factorsOf3Left = factorsOf3;
    long factorsOf5Left = factorsOf5;
    long factorsOf7Left = factorsOf7;
    long factorsOf11Left = factorsOf11;
    long factorsOf13Left = factorsOf13;
    long factorsOf17Left = factorsOf17;
    long factorsOf19Left = factorsOf19;
    long top = 1;
    for (long i=n; i>max; i--) 
      {
	top = SMath.multiply (top, i);
	// primes cannot interfere with one another.
	if (i%19==0 && factorsOf19Left > 0) {top /= 19; factorsOf19Left --;}
	if (i%17==0 && factorsOf17Left > 0) {top /= 17; factorsOf17Left --;}
	if (i%13==0 && factorsOf13Left > 0) {top /= 13; factorsOf13Left --;}
	if (i%11==0 && factorsOf11Left > 0) {top /= 11; factorsOf11Left --;}
	if (i%7==0 && factorsOf7Left > 0) {top /= 7; factorsOf7Left --;}
	if (i%5==0 && factorsOf5Left > 0) {top /= 5; factorsOf5Left --;}
	if (i%3==0 && factorsOf3Left > 0) {top /= 3; factorsOf3Left --;}
	if (i%2==0 && factorsOf2Left > 0) {top /= 2; factorsOf2Left --;}
      }

    // --- division is safe.  Don't have to check it.
    long bottom = factorial(min);
    bottom /= SMath.pow(3, factorsOf3);
    bottom /= SMath.pow(5, factorsOf5);
    bottom /= SMath.pow(7, factorsOf7);
    bottom /= SMath.pow(11, factorsOf11);
    bottom /= SMath.pow(13, factorsOf13);
    bottom /= SMath.pow(17, factorsOf17);
    bottom /= SMath.pow(19, factorsOf19);

    bottom /= SMath.pow(2, factorsOf2);


    long retval = top/bottom;

    return retval;
  }

  /**
   * Calculates C(n, r).
   */
  public static final BigInteger chooseBig (long n, long r) 
    throws SMathException
  {
    // figure out the max, min of the two lower terms.
    long min = min (r, (n-r));
    long max = max (r, (n-r));

    BigInteger top = BigOne;
    for (long i=n; i>max; i--) 
      top = SMath.multiply (top, BigInteger.valueOf (i));

    BigInteger bottom = factorialBig (min);
    BigInteger retval = top.divide (bottom);

    return retval;
  }

  // -------------------------------------------------------------------------
  // ---- Rolls sets of dice (using longs) -----------------------------------
  // -------------------------------------------------------------------------

  /** The highest number of rolls of a dice to cache. */
  static final int MaxRs = 64;
  /** The highest number of sides of a dice to bother caching. */
  static final int MaxSs = 64;

  /** Cached dice rolls. */
  static long[][][] possibleRs = new long[MaxRs][][];

  /**
   * Returns an integer array containing the values for
   *  rolling r dice with s sides each, and summing them.
   * <p>
   * Actually, this function also expands the polynomial
   * (1 + x + x^2 + x^3 + ... + x^(s-1))^r
   *
   * Caches rolls up to (MaxRs)d(MaxSs) for posterity.
   */
  public static long[] dice (int r, int s) throws SMathException
  {
    if (r < MaxRs && s < MaxSs)
      {
	long[][] possibleSs = possibleRs[r];
	if (possibleSs == null)
	  {
	    possibleSs = new long[MaxSs][];
	    possibleRs[r] = possibleSs;
	  }
	if (possibleSs[s] == null)
	  {
	    //Util.out.println ("Calculating "+r+"d"+s+"");
	    possibleSs[s] = _calculateDice (r, s);
	  }
	else
	  {
	    //Util.out.println ("Returning cached "+r+"d"+s+"");
	  }
	return possibleSs[s];
      }
    else
      return _calculateDice (r, s);
  }

  /**
   * Does the actual scary combinatorial work for dice ().
   */
  static long[] _calculateDice (int r, int s) throws SMathException
  {
    s--;

    int rTimesS = multiply (r, s);
    int sPlus1 = add (s, 1);
    int rTimesSPlus1 = multiply (r, sPlus1);

    long[] result = new long[rTimesS+1];

    // loop over the coefficients we want to find:
    for (int exp=0; exp<=rTimesS; exp++) 
      {
	//Util.out.println("Calculating coeff of exp == "+exp);
	
	// loop over the exponents of 'A', find ones which
	// are smaller.
	for (int expA=0,               i=0; 
	     (expA <= rTimesSPlus1) && (expA <= exp); 
	     expA +=        sPlus1,     i++) 
	  {
	    //Util.out.println("  expA == "+expA);
	    //Util.out.print("    Interested in expA == "+expA);
	    //Util.out.println("  (exp # "+i+")");
	    
	    // figure out the coefficient of x^expA in A.
	    long coeffA = choose(r, i);
	    coeffA *= (i%2 == 0)?1:-1;
	    //Util.out.println("    coeffA == "+coeffA);
	    
	    // figure out the exponent of B we're interested in.
	    // self ISSUE: can this overflow??
	    int expB = exp - expA;
	    //Util.out.println("    expB == "+expB);
	    
	    // figure out the coefficient of B.
	    long coeffB = choose (expB+r-1, expB);
	    //Util.out.println("    coeffB == "+coeffB);
	    
	    // figure out the coefficient factor from A*B.
	    long factor = multiply (coeffA, coeffB);
	    //Util.out.println("    factor == "+factor);
	    
	    // factor in this factor.
	    result[exp] = add (result[exp], factor);
	  }
	if (result[exp] < 0)
	  Util.out.println("Overflow error! result["+exp+"] == "+result[exp]);
    }

    return result;
  }


  // -------------------------------------------------------------------------
  // ---- Rolls sets of dice (using BigInteger's) -----------------------------
  // -------------------------------------------------------------------------

  /** The highest number of rolls of a dice to cache. */
  static final int MaxBigRs = 64;
  /** The highest number of sides of a dice to bother caching. */
  static final int MaxBigSs = 64;

  /** Cached dice rolls. */
  static BigInteger[][][] possibleBigRs = new BigInteger[MaxBigRs][][];

  /**
   * Returns an integer array containing the values for
   *  rolling r dice with s sides each, and summing them.
   * <p>
   * Actually, this function also expands the polynomial
   * (1 + x + x^2 + x^3 + ... + x^(s-1))^r
   *
   * Caches rolls up to (MaxBigRs)d(MaxBigSs) for posterity.
   */
  public static BigInteger[] bigDice (int r, int s) throws SMathException
  {
    if (r < MaxBigRs && s < MaxBigSs)
      {
	BigInteger[][] possibleBigSs = possibleBigRs[r];
	if (possibleBigSs == null)
	  {
	    possibleBigSs = new BigInteger[MaxBigSs][];
	    possibleBigRs[r] = possibleBigSs;
	  }
	if (possibleBigSs[s] == null)
	  {
	    //Util.out.println ("Calculating "+r+"d"+s+"");
	    possibleBigSs[s] = _calculateBigDice (r, s);
	  }
	else
	  {
	    //Util.out.println ("Returning cached "+r+"d"+s+"");
	  }
	return possibleBigSs[s];
      }
    else
      return _calculateBigDice (r, s);
  }

  /**
   * Does the actual scary combinatorial work for dice ().
   */
  static BigInteger[] _calculateBigDice (int r, int s) throws SMathException
  {
    s--;

    int rTimesS = multiply (r, s);
    int sPlus1 = add (s, 1);
    int rTimesSPlus1 = multiply (r, sPlus1);

    BigInteger[] result = new BigInteger[rTimesS+1];
    for (int i=0; i<result.length; i++)
      result[i] = BigZero;

    // loop over the coefficients we want to find:
    for (int exp=0; exp<=rTimesS; exp++) 
      {
	//Util.out.println("Calculating coeff of exp == "+exp);
	
	// loop over the exponents of 'A', find ones which
	// are smaller.
	for (int expA=0,               i=0; 
	     (expA <= rTimesSPlus1) && (expA <= exp); 
	     expA +=        sPlus1,     i++) 
	  {
	    //Util.out.println("  expA == "+expA);
	    //Util.out.print("    Interested in expA == "+expA);
	    //Util.out.println("  (exp # "+i+")");
	    
	    // figure out the coefficient of x^expA in A.
	    BigInteger coeffA = chooseBig (r, i);
	    coeffA = coeffA.multiply (BigInteger.valueOf ((i%2 == 0)?1:-1));
	    //Util.out.println("    coeffA == "+coeffA);
	    
	    // figure out the exponent of B we're interested in.
	    // self ISSUE: can this overflow??
	    int expB = exp - expA;
	    //Util.out.println("    expB == "+expB);
	    
	    // figure out the coefficient of B.
	    BigInteger coeffB = chooseBig (expB+r-1, expB);
	    //Util.out.println("    coeffB == "+coeffB);
	    
	    // figure out the coefficient factor from A*B.
	    BigInteger factor = multiply (coeffA, coeffB);
	    //Util.out.println("    factor == "+factor);
	    
	    // factor in this factor.
	    result[exp] = add (result[exp], factor);
	  }
    }

    return result;
  }


  // -------------------------------------------------------------------------
  // ---- Conversion Routines ------------------------------------------------
  // -------------------------------------------------------------------------

  public static BigInteger toBigInteger (long l) throws SMathException
  {
    return BigInteger.valueOf (l);
  }

  public static long toLong (BigInteger bi) throws SMathException
  {
    if (bi.bitLength ()+1 > 64)
      throw new SMathOverflow ("BigInteger "+bi+
			       " too long to store into 'long' value");
    return bi.longValue ();
  }

  public static BigInteger[] toBigIntegerArray (long[] l) throws SMathException
  {
    BigInteger[] retval = new BigInteger[l.length];
    for (int i=0; i<retval.length; i++) retval[i] = toBigInteger (l[i]);
    return retval;
  }

  public static long[] toLongArray (BigInteger[] bi) throws SMathException
  {
    long[] retval = new long[bi.length];
    for (int i=0; i<retval.length; i++) retval[i] = toLong (bi[i]);
    return retval;
  }

  // -------------------------------------------------------------------------
  // ---- Test Routines ------------------------------------------------------
  // -------------------------------------------------------------------------

  static void printfancy (int num)
  {
    if (num > Math.sqrt (INT_MAX))
      Util.out.print ('(');
    Util.out.print (Integer.toHexString (num));
    if (num > Math.sqrt (INT_MAX))
      Util.out.print (')');
  }

  public static void main (String[] args)
  {
    int top = INT_MAX;
    long steps = 10;
    int step = (int)(top/steps);
    Util.out.println ("top == "+top);
    Util.out.println ("step == "+step);
    Util.out.print ("\t\t");
    for (int j=0+step; j<top && j>=0; j+=step)
      {
	printfancy (j);
	Util.out.print ('\t');
      }
    Util.out.println ();
    for (int i=0+step; i<top && i>=0; i+=step)
      {
	printfancy (i);
	Util.out.print (":\t");
	for (int j=0+step; j<top && j>=0; j+=step)
	  {
	    int p = i*j;
	    if (i==j)
	      Util.out.print ('[');
	    Util.out.print (p>0?"1":(p<0?"-1":"0"));
	    if (i==j)
	      Util.out.print (']');
	    Util.out.print ("\t\t");
	  }
	Util.out.println ();
      }
  }


  /**
   * Test bed.
   */
  public static void main2 (String[] args)
  {
    Util.out.println (" --- Testing addition");
    Util.out.println (SMath.add (2, 4));

    try {
      Util.out.println (SMath.add (0x7fffffffffffffffL, 1));
    } catch (SMathException ex) {ex.printStackTrace (Util.out);}
    try {
      Util.out.println (SMath.add (0x7fffffffffffffffL, 0x7fffffffffffffffL));
    } catch (SMathException ex) {ex.printStackTrace (Util.out);}
    try {
      Util.out.println(SMath.add(-9000000000000000000L,-9000000000000000000L));
    } catch (SMathException ex) {ex.printStackTrace (Util.out);}

    Util.out.println (" --- Testing multiplication");
    try {
      Util.out.println (SMath.multiply (0x7fffffffffffffffL, 
					0x7fffffffffffffffL));
    } catch (SMathException ex) {ex.printStackTrace (Util.out);}
    try {
      Util.out.println (SMath.multiply (-9000000000000000000L,
					-9000000000000000000L));
    } catch (SMathException ex) {ex.printStackTrace (Util.out);}

    try {
      Util.out.println (SMath.multiply (0x7fffffffffffffffL, 
					-9000000000000000000L));
    } catch (SMathException ex) {ex.printStackTrace (Util.out);}

    Util.out.println ("gcd == "+ SMath.gcd (new long[] {11, 122}));
    Util.out.println ("gcd == "+ SMath.gcd (new long[] {11, 121}));
    Util.out.println ("gcd == "+ SMath.gcd (new long[] {4, 8, 16, 32}));

    long[] _3d6 = dice (3, 6);
    Util.out.print ("3d6 == ");
    printArray (Util.out, _3d6);
    Util.out.println ();

    long[] _10d8 = dice (10, 8);
    Util.out.print ("10d8 == ");
    printArray (Util.out, _10d8);
    Util.out.println ();

    long[] _10d10 = dice (10, 10); Util.out.print ("10d10 == ");
    printArray (Util.out, _10d10); Util.out.println ();

    try {
      long[] _11d10 = dice (11, 10); Util.out.print ("11d10 == ");
      printArray (Util.out, _11d10); Util.out.println ();

      long[] _12d10 = dice (12, 10); Util.out.print ("12d10 == ");
      printArray (Util.out, _12d10); Util.out.println ();

      long[] _13d10 = dice (13, 10); Util.out.print ("13d10 == ");
      printArray (Util.out, _13d10); Util.out.println ();

      long[] _14d10 = dice (14, 10); Util.out.print ("14d10 == ");
      printArray (Util.out, _14d10); Util.out.println ();

      long[] _15d10 = dice (15, 10); Util.out.print ("15d10 == ");
      printArray (Util.out, _15d10); Util.out.println ();

      long[] _18d10 = dice (18, 10); Util.out.print ("1810 == ");
      printArray (Util.out, _18d10); Util.out.println ();

      long[] _20d10 = dice (20, 10); Util.out.print ("20d10 == ");
      printArray (Util.out, _20d10); Util.out.println ();

      long[] _30d10 = dice (30, 10); Util.out.print ("30d10 == ");
      printArray (Util.out, _30d10); Util.out.println ();

      long[] _50d10 = dice (50, 10); Util.out.print ("50d10 == ");
      printArray (Util.out, _50d10); Util.out.println ();
    } catch (SMathException ex) {ex.printStackTrace (Util.out);}

    try {
      Util.out.println ("10! == "+factorial (10));
      Util.out.println ("20! == "+factorial (20));
      Util.out.println ("21! == "+factorial (21));
      Util.out.println ("25! == "+factorial (25));
      Util.out.println ("30! == "+factorial (30));
      Util.out.println ("40! == "+factorial (40));
      Util.out.println ("80! == "+factorial (80));
      Util.out.println ("120! == "+factorial (120));
      Util.out.println ("500! == "+factorial (500));
    } catch (SMathException ex) {ex.printStackTrace (Util.out);}
    
  }

  // -------------------------------------------------------------------------
  // ---- Output Routines ----------------------------------------------------
  // -------------------------------------------------------------------------

  static final void printArray (PrintWriter out, long[] x)
  {
    printArray (out, x, x.length);
  }

  static final void printArray (PrintWriter out, long[] x, int size)
  {
    if (size < x.length)
      throw new IllegalArgumentException ("Passed size: too big.");
    for (int i=0; i<size; i++)
      {
	if (i>0) out.print (", ");
	out.print (x[i]);
      }
  }


}
