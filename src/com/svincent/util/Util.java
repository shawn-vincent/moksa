/*
 * Util: a utility library for Java programs.
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
 * Util.java 
 * 
 */

package com.svincent.util;

import java.io.*;
import java.util.*;
import java.text.*;

/**
 * The Util class is a useful little thingie which 
 * is an equivalent of the java.lang.System class.  It contains
 * miscellaneous utility methods which are useful in a wide variety
 * of contexts.
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class Util {

  /**
   * A protected no-arg constructor which just prevents people from
   * creating instances of this class.
   */
  protected Util ()
  {
    // You can't create instances of the Util class.
    // Only call static methods.
  }

  /**
   * self ISSUE: Java does not allow us to make the *contents* of this
   * list final through typing.  Make a custom subclass of list that is 
   * immutable for here.
   */
  public final static List EmptyList = new ArrayList ();

  public final static Object[] EmptyObjectArray = new Object[0];
  public final static Class[] EmptyClassArray = new Class[0];
  public final static String[] EmptyStringArray = new String[0];

  public static final long MinByte = 0x80;
  public static final long MaxByte = 0x7f;
  
  public static final int MinShort = 0x8000;
  public static final int MaxShort = 0x7fff;

  public static final int MinInt = 0x80000000;
  public static final int MaxInt = 0x7fffffff;

  public static final long MinLong = 0x8000000000000000L;
  public static final long MaxLong = 0x7fffffffffffffffL;

  // ----------------------------------------------------------------------
  // -------------------------------------------- I/O Standard streams ----
  // ----------------------------------------------------------------------

  /**
   * My version of java.lang.System.out.  Made a PrintWriter so
   * that JDK1.1 doesn't hate me with warnings.
   */
  public static PrintWriter out /*=null*/;
  /**
   * My version of java.lang.System.err.  Made a PrintWriter so
   * that JDK1.1 doesn't hate me with warnings.
   */
  public static PrintWriter err /*=null*/;
  /**
   * My version of java.lang.System.in.
   */
  public static Reader in /*=null*/;

  static {
    // default the streams to the standard system ones.
    setOut (System.out);
    setErr (System.err);
    setIn (System.in);
  }

  /* to set all the standard streams. */
  public static void setOut (PrintWriter _out) { out = _out; }
  //{ out = new IndentPrintWriter (_out); }
  public static void setOut (PrintStream ps) 
  { setOut (new PrintWriter (ps, true)); }
  public static void setErr (PrintWriter _err) { err = _err; }
  //{ err = new IndentPrintWriter (_err); }
  public static void setErr (PrintStream ps) 
  { setErr (new PrintWriter (ps, true)); }
  public static void setIn (Reader _in) { in = _in; }
  public static void setIn (InputStream _in) 
  { setIn (new InputStreamReader (_in)); }

  public static String defaulttag (Object o)
  {
    if (o == null) return "null";
    
    return 
      o.getClass ().getName () + '@' +
      System.identityHashCode (o);
  }

  public static void indent (PrintWriter out) { indent (out, "  "); }
  
  public static void indent (PrintWriter out, String indentString)
  {
    if (out instanceof IndentPrintWriter)
      ((IndentPrintWriter)out).indent (indentString);
    else
      out.println (">>");
  }

  public static void outdent (PrintWriter out)
  {
    if (out instanceof IndentPrintWriter)
      ((IndentPrintWriter)out).outdent ();
    else
      out.println ("<<");
  }

  /** a newline for the current system. */
  public static final String NL = System.getProperty ("line.separator");

  /** 
   * self ISSUE: crappy.  Remove this, and all references to this, 
   * in favor of Util.NL.  Much nicer. 
   */
  protected static final char cNL = '\n';

  /**
   * Reads a string from the specified reader, stopping at
   * newline (specified by Util.cNL).  Supports Unicode, by virtue
   * of using a Reader.
   */
  public static String readLine (Reader in) throws IOException
  {
    boolean done = false;
    StringBuffer sb = new StringBuffer ();
    while (!done)
      {
	int ic = in.read ();
	char c = (char)ic;
	if (c == cNL) done = true;
	sb.append (c);
      }
    return sb.toString ();
  }

  /**
   * Same as Util.readLine (Util.in).
   */
  public static String readLine () 
       throws IOException
  { return readLine (Util.in); } 

  // ----------------------------------------------------------------------
  // ---- Time methods ----------------------------------------------------
  // ----------------------------------------------------------------------

  /**
   * Returns the current date and time in a nice String format.
   */
  public static String dateAndTimeToString ()
  { 
    return 
      DateFormat.getDateTimeInstance (DateFormat.FULL, DateFormat.FULL).
      format (new Date ()); 
  }

  public static String timetag (double seconds)
  {
    long milliseconds = (long)(seconds * 1000);
    return timetag (milliseconds);
  }

  public static String timetag (long milliseconds)
  {
    long totalmillis = milliseconds;
    long millis = totalmillis % 1000;
    totalmillis /= 1000;
    long seconds = totalmillis % 60;
    totalmillis /= 60;
    long minutes = totalmillis % 60;
    totalmillis /= 60;
    long hours = totalmillis;

    // -- build up the output string, then return it.
    StringBuffer sb = new StringBuffer ();
    if (hours < 10) sb.append ('0');
    sb.append (hours);
    sb.append (':');
    if (minutes < 10) sb.append ('0');
    sb.append (minutes);
    sb.append (':');
    if (seconds < 10) sb.append ('0');
    sb.append (seconds);
    sb.append ('.');
    if (millis < 100) sb.append ('0');
    if (millis < 10) sb.append ('0');
    sb.append (millis);

    return sb.toString ();
  }


  // ----------------------------------------------------------------------
  // -------------------------------------------------- Hashcode stuff ----
  // ----------------------------------------------------------------------

  public static int catHash (int hc, int v)
  { return hc << 3 + v; }

  public static int catHash (int hc, Object o)
  { return catHash (hc, o.hashCode ()); }

  

  // ----------------------------------------------------------------------
  // --------------------------------------------- String Manipulation ----
  // ----------------------------------------------------------------------

  public static String stripQuotes (String s)
  {
    if (s.charAt (0) == '"' &&
        s.charAt (s.length () - 1) == '"')
      return s.substring (1, s.length () - 1);
    return s;
  }



  // ----------------------------------------------------------------------
  // ---------------------------------------- Random Number Generation ----
  // ----------------------------------------------------------------------

  /**
   * The current system random number generation.  Simplifies the world,
   * since we can just say Util.random.nextInt () instead of maintaining
   * a random number generator ourselves.
   */
  public static Random random = new Random ();

  /**
   * setRandom lets us set a new random number generator.
   * The com.randomX package is filled with them!
   */
  public static void setRandom (Random _random) { random = _random; }

  // ----------------------------------------------------------------------
  // ------------------------------------------------------ Assertions ----
  // ----------------------------------------------------------------------

  public static void assertTrue (boolean condition)
  { assertTrue (condition, "Assertion Failed"); }

  public static void assertTrue (boolean condition, String msg)
  { if (!condition) throw new AssertionException (msg); }

  public static void assertTrue (boolean condition, String msg, Throwable ex)
  { if (!condition) throw new AssertionException (msg, ex); }

  public static void printStackTrace ()
  { new Throwable ().printStackTrace (Util.out); }

  // ----------------------------------------------------------------------
  // -------------------------------------------------- Dumping things ----
  // ----------------------------------------------------------------------

  /**
   * Dumps the given integer array.
   * XXX do stuff like 5 per line, etc.
   */
  public static void dump (PrintWriter out, int[] values)
  {
    for (int i=0; i<values.length; i++)
      out.print (values[i]+" ");
    out.println ();
  }

  // ----------------------------------------------------------------------
  // ------------------------------ Sleeps that don't throw exceptions ----
  // ----------------------------------------------------------------------

  public static void sleepSeconds (int seconds)
  {
    sleep (seconds * 1000);
  }
  
  public static void sleep (long milliseconds)
  {
    try {
      Thread.sleep (milliseconds);
    } catch (InterruptedException e) {}
  }

  public static void sleep (long milliseconds, int nanoseconds)
  {
    try {
      Thread.sleep (milliseconds, nanoseconds);
    } catch (InterruptedException e) {}
  }

  public static int compare (Object a, Object b)
  { 
    if (a == b)
      return 0;

    if (a == null) return -1;
    if (b == null) return 1;

    return System.identityHashCode (a) - System.identityHashCode (b);
  }

  public static boolean equals (Object a, Object b) { return a == b; }

  public static int compare (Collection a, Collection b)
  { return compare (a, b, DefaultComparator); }

  public static int compare (Collection a, Collection b, Comparator c)
  {
    Iterator ia = a.iterator ();
    Iterator ib = b.iterator ();

    while (ia.hasNext () && ib.hasNext ())
      {
	Object ao = ia.next ();
	Object bo = ib.next ();
	int compare = c.compare (ao, bo);
	if (compare != 0)
	  return compare;
      }

    if (ia.hasNext ())
      return 1;
    if (ib.hasNext ())
      return -1;

    return 0;
  }

  public static Comparator DefaultComparator = new Comparator () {
    public int compare (Object a, Object b)
    { return Util.compare (a, b); }
    public boolean equals (Object o) 
    { return Util.equals (this, o); }
  };

  public static void main (String [] argv)
  {
    Util.out.println ("Hello, world - this should be seen first");
    System.out.println ("Hello, world - this should be seen second");
  }

}

