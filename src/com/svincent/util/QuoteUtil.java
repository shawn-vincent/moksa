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
 * QuoteUtil.java 
 * 
 */

package com.svincent.util;

import java.io.*;
import java.util.*;
import java.text.*;


/**
 * Provides facilities for printing and parsing of strings.
 *
 * QuoteUtil contains a bunch of utility methods used for
 * quoting strings in useful ways.  In particular, this beastie
 * has methods for quoting strings in a Java-style way, suitable
 * for generating Java code, and a debug quoter, used for quoting
 * things sent out to the user.
 *
 *
 *
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class QuoteUtil {
  private QuoteUtil () {}

  // -------------------------------------------------------------------------
  // ---- Formatting Utilities -----------------------------------------------
  // -------------------------------------------------------------------------

  /**
   *
   */
  public abstract static class DigitMaker extends BaseObject {
    public abstract char makeDigit (int digit, int radix);
    //  XXX put this in sometime.
//      public int parseDigit (char digit, int radix) throws ParseException;
  }

  static final char[] radixChars = new char[] {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
  };
  public static final DigitMaker StandardDigitMaker = new DigitMaker () {

    public char makeDigit (int digit, int radix)
    { 
      if (radix > 36) 
        throw new IllegalArgumentException 
         ("The StandardDigitMaker only supports digits for radixes up to 36.");
      if (digit < 0 || digit > radix-1) 
        throw new IllegalArgumentException 
         ("Bad digit '"+digit+"', for radix '"+radix+"'");
      return radixChars[digit % radix]; 
    }

    //  XXX put this in sometime.
//      public int parseDigit (char digit, int radix) throws ParseException
//      { 
//        Util.assert (radix < 36);
//        int retval;
//        if (digit >= '0' && digit <= '9') retval = (int)(digit - '0');
//        else if (digit >= 'a' && digit <= 'z') retval = 9+(int)(digit - 'a');
//        else throw new ParseException ("Unknown digit '"+digit+"'");
//        return retval;
//      }
  };

  /**
   *
   */
  public static String numberToString (int number, int radix)
  { return numberToString (number, radix, -1); }

  /**
   * @param size specifies how big to make the number, including padding.
   */
  public static String numberToString (int number, int radix, int size)
  { return numberToString (number, radix, size, StandardDigitMaker); }
  

  /**
   * @param size specifies how big to make the number, including padding.
   */
  public static String numberToString (int _number, int radix, int size,
                                       DigitMaker digitMaker)
  {
    if (radix < 2) 
      throw new IllegalArgumentException ("Radixes below 2 make no sense.");

    // --- make sure we're dealing with an unsigned quantity.
    long number = ((long)_number) & 0xffffffffL;

    StringBuffer out = new StringBuffer ();
    int s = 0;

    // --- special case: if the number is 0, the representation is always 0.
    if (number == 0) { out.append ("0"); s++; }
    // --- otherwise, do lots of divs/mods.
    else while (number > 0)
      {
        int digit = (int)(number % radix);
        out.append (digitMaker.makeDigit (digit, radix));
        number = number / radix;
        s++;
      }

    // --- prepend padding characters.
    while (s < size)
      {
        out.append ('0');
        s++;
      }

    // --- we built the string in reverse.
    out.reverse ();

    // --- return the nicely formatted value.
    return out.toString ();
  }

  public static String toHexString (int number)
  {
    return numberToString (number, 16);
  }

  public static String toHexString (byte[] bytes)
  {
    StringBuffer out = new StringBuffer ();
    for (int i=0; i<bytes.length; i++)
      {
        String s = numberToString (bytes[i], 16, 2);
        out.append (s);
        if (i+1<bytes.length)
          out.append (' ');
      }
    return out.toString ();
  }

  public static int parseInt (String numberString, int radix, int def)
  {
    try {
      return Integer.parseInt (numberString, radix);
    } catch (NumberFormatException ex) {
      return def;
    }
  }

  public static int parseInt (String numberString)
  { return parseInt (numberString, 10, 0); }

  public static long parseLong (String numberString, int radix, long def)
  {
    try {
      return Long.parseLong (numberString, radix);
    } catch (NumberFormatException ex) {
      return def;
    }
  }

  public static long parseLong (String numberString)
  { return parseLong (numberString, 10, 0); }


  public static boolean isPrintableAscii (char c)
  {
    return (c >= ' ' && c <= '~');
  }

  public static String javaQuote (String _in)
  {
    StringBuffer out = new StringBuffer ();
    char[] in = _in.toCharArray ();
    for (int i=0; i<in.length; i++)
      {
        char c = in[i];
        switch (c)
          {
          case '\\':
          case '"':
          case '\'':  out.append ('\\'); out.append (c); break;
          case '\b':  out.append ("\\b"); break;
          case '\t':  out.append ("\\t"); break;
          case '\n':  out.append ("\\n"); break;
          case '\f':  out.append ("\\f"); break;
          case '\r':  out.append ("\\r"); break;
          default:
            if (isPrintableAscii (c))
              out.append (c);
            else
              {
                out.append ("\\u");
                out.append (numberToString ((int)c, 16, 4));
              }
            break;
          }
      }
    return out.toString ();
  }

  public static String javaUnquote (String src)
  {
    // XXX do this sometime.
    return src;
  }

  public static String debugQuote (String src)
  { return javaQuote (src); }

  public static void main (String[] args)
  {
    for (int i=2; i<37; i++)
      {
        Util.out.println ("555 ="+i+"= "+numberToString (555, i));
      }
    Util.out.println (javaQuote ("\n\rHello\u5733"));
  }

}
