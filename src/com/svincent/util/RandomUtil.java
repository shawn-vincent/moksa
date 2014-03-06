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
 * RandomUtil.java 
 * 
 */


package com.svincent.util;

import java.io.PrintWriter;
import java.util.Random;

/**
 *
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class RandomUtil {
  private RandomUtil () {}

  private static Random random;

  private static Random getRandom () 
  { if (random == null) random = new Random (); return random; }

  public static String randomFixedLengthString (int len, 
                                                char loChar, char hiChar)
  {
    char[] chars = new char[len];
    fillRandomly (chars, loChar, hiChar);
    return new String (chars);
  }

  public static void fillRandomly (char[] chars, char loChar, char hiChar)
  {
    for (int i=0; i<chars.length; i++)
      chars[i] = randomChar (loChar, hiChar);
  }

  public static char randomChar (char loChar, char hiChar)
  { return (char)randomInt ((int)loChar, (int)hiChar); }

  /**
   * Generates a random integer between 'lo' and 'hi', inclusively.
   */
  public synchronized static int randomInt (int lo, int hi)
  {
    int r = Math.abs (getRandom ().nextInt ());
    // --- We want lo..hi, inclusively.
    //   - So, we want (lo-lo..hi-lo), inclusively
    //   - Mod excludes the last in the range, so we must add one.
    return (r%(hi-lo+1))+lo;
  }

  public static class Histogram extends BaseObject {
    public static final boolean debug = false;

    private int[] values;
    private int[] weights;
    private int size;

    public Histogram ()
    {
      values = new int[8];
      weights = new int[8];
      size = 0;
    }

    public void addDataPoint (int value) { addDataPoint (value, 1); }
    public void addDataPoint (int value, int weight) 
    {
      if (debug)
        Util.out.println ("++++ addDataPoint ("+value+", "+weight+")");
      int index = getIndex (value);
      if (index == -1)
        insertDataPoint (value, weight);
      else
        weights[index]+=weight;
      if (debug)
        {
          Util.out.println ("++++ Done addDataPoint ("+value+", "+weight+")");
          Util.dump (Util.out, values);
          Util.dump (Util.out, weights);
        }
    }

    public int getWeight (int value)
    {
      int index = getIndex (value);
      if (index == -1)
        return 0;
      else
        return weights[index];
    }

    /** XXX binary search!? */
    private int getIndex (int value)
    {
      for (int i=0; i<size; i++)
        if (values[i] == value) return i;
      return -1;
    }

    private void insertDataPoint (int value, int weight)
    {
      // --- never worry about size.
      if (size >= values.length) grow ();

      if (debug)
        {
          Util.out.println ("Before:");
          Util.dump (Util.out, values);
        }

      // --- get a pointer to the position we'd like to be in.
      int pos;
      for (pos=0; pos<size; pos++)
        if (values[pos] > value) break;

      // --- make a hole.
      for (int i=size; i>=pos+1; i--)
        {
          values[i] = values[i-1];
          weights[i] = weights[i-1];
        }
      values[pos] = 0;
      weights[pos] = 0;

      if (debug)
        {
          Util.out.println ("Shifted:");
          Util.dump (Util.out, values);
        }
      
      // --- insert the new node.
      values[pos] = value;
      weights[pos] = weight;
      size++;

      if (debug)
        {
          Util.out.println ("After:");
          Util.dump (Util.out, values);
        }
    }

    private void grow ()
    {
      int newSize = size * 2;
      int[] newValues = new int[newSize];
      System.arraycopy (values, 0, newValues, 0, values.length);
      values = newValues;

      int[] newWeights = new int[newSize];
      System.arraycopy (weights, 0, newWeights, 0, weights.length);
      weights = newWeights;
    }

    public void dump (PrintWriter out)
    {
      for (int i=0; i<size; i++)
        {
          out.print (values[i]);
          out.print (": ");
          out.print (weights[i]);
          out.println ();
        }
    }

    public static void main (String[] args)
    {
      Histogram h = new Histogram ();
      h.addDataPoint (1);
      h.addDataPoint (3);
      h.addDataPoint (4);
      h.addDataPoint (5);
      h.addDataPoint (6);
      h.addDataPoint (7);
      h.addDataPoint (8);
      h.addDataPoint (9);

      h.addDataPoint (1);
      h.addDataPoint (2);

    }
  }

  // -------------------------------------------------------------------------
  // ---- Test Code ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   *
   */
  public static void main (String[] args)
  {
    Histogram h = new Histogram ();
    for (int i=0; i<50000; i++)
      h.addDataPoint (randomInt (1, 10));

    h.dump (Util.out);

    for (int i=0; i<10; i++)
      Util.out.println (RandomUtil.randomFixedLengthString (20, ' ', '~'));

  }
}
