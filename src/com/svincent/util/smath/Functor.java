
package com.svincent.util.smath;

import java.math.*;

public abstract class Functor {

  public abstract long eval (long a, long b) throws SMathException;
  public abstract BigInteger eval (BigInteger a, BigInteger b) 
    throws SMathException;

  /** */
  public static class AddFunctor_ extends Functor {
    public long eval (long a, long b) throws SMathException 
    { return SMath.add (a, b); }

    public BigInteger eval (BigInteger a, BigInteger b) throws SMathException 
    { return SMath.add (a, b); }
  }
  public static AddFunctor_ AddFunctor = new AddFunctor_();

  /** */
  public static class SubtractFunctor_ extends Functor {
    public long eval (long a, long b) throws SMathException 
    { return SMath.subtract (a, b); }

    public BigInteger eval (BigInteger a, BigInteger b) throws SMathException 
    { return SMath.subtract (a, b); }
  }
  public static SubtractFunctor_ SubtractFunctor = new SubtractFunctor_();

  /** */
  public static class MultiplyFunctor_ extends Functor {
    public long eval (long a, long b) throws SMathException 
    { return SMath.multiply (a, b); }

    public BigInteger eval (BigInteger a, BigInteger b) throws SMathException 
    { return SMath.multiply (a, b); }
  }
  public static MultiplyFunctor_ MultiplyFunctor = new MultiplyFunctor_();

  /** */
  public static class DivideFunctor_ extends Functor {
    public long eval (long a, long b) throws SMathException 
    { return SMath.divide (a, b); }

    public BigInteger eval (BigInteger a, BigInteger b) throws SMathException 
    { return SMath.divide (a, b); }
  }
  public static DivideFunctor_ DivideFunctor = new DivideFunctor_();
 
}
