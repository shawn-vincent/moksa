/*
 * 
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
 * PrologUtil.java
 *
 */
package com.svincent.moksa;

import com.svincent.util.*;

/**
 * Contains a bunch of static methods for doing useful tasks for compiling
 * Prolog.  In particular, contains routines to convert strings to and from
 * quoted form.
 */
public class PrologUtil extends BaseObject {

  private PrologUtil () {}

  // -------------------------------------------------------------------------
  // ---- Prolog String Quoting Routines -------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Returns 'true' if the string 's' has enough wierd characters in
   * it that it should be quoted for user consumption.
   */
  public static boolean shouldQuote (String s)
  {
    for (int i=0; i<s.length (); i++)
      {
        char c = s.charAt (i);
        switch (c)
          {
            // --- any comma character
          case ',': return true;

            // --- any graphics character
          case '$': return true; case '&': return true; case '*': return true;
          case '+': return true; case '-': return true; case '.': return true;
          case '/': return true; case ':': return true; case '<': return true;
          case '=': return true; case '>': return true; case '?': return true;
          case '@': return true; case '^': return true; case '~': return true;
          case '#': return true;

            // --- any quoting character
          case '"': return true; case '\'': return true; case '`': return true;

            // --- any whitespace character
          case ' ': return true; case '\t': return true;
          case '\r': return true; case '\n': return true;

            // --- any control character
          case '\u0007': return true; // bell/alert
          case '\b': return true;
          case '\f': return true;
          case '\u000b': return true; // vertical tab
          case '\\': return true;
          }

        if (c < 0x0020 || c > 0x007e) // not printable
          return true;
      }
    return false;
  }

  public static String doubleQuotify (String s) { return quotify (s, '"'); }
  public static String unDoubleQuotify (String s)
    throws PrologStringFormatException
  { return unquotify (s, '"'); }

  public static String singleQuotify (String s) { return quotify (s, '\''); }
  public static String unSingleQuotify (String s)
    throws PrologStringFormatException
  { return unquotify (s, '\''); }

  public static String backQuotify (String s) { return quotify (s, '`'); }
  public static String unBackQuotify (String s)
    throws PrologStringFormatException
  { return unquotify (s, '`'); }

  private static String quotify (String s, char quoteChar)
  {
    StringBuffer out = new StringBuffer ();
    char[] chars = s.toCharArray ();

    out.append (quoteChar);
    for (int i=0; i<chars.length; i++)
      {
        char c = chars[i];
        switch (c)
          {
          case '"':
            if (quoteChar == '"')
              {out.append ('"'); out.append ('"');}
            else
              out.append ('"'); 
            break;

          case '\'':
            if (quoteChar == '\'')
              {out.append ('\''); out.append ('\'');}
            else
              out.append ('\''); 
            break;

          case '`':
            if (quoteChar == '`')
              {out.append ('`'); out.append ('`');}
            else
              out.append ('`'); 
            break;

          case '\u0007': // bell character/alert
            out.append ('\\'); out.append ('a'); break;
            
          case '\b':
            out.append ('\\'); out.append ('b'); break;

          case '\f':
            out.append ('\\'); out.append ('f'); break;

          case '\n':
            out.append ('\\'); out.append ('n'); break;

          case '\r':
            out.append ('\\'); out.append ('r'); break;

          case '\t':
            out.append ('\\'); out.append ('t'); break;

          case '\u000b': // vertical tab
            out.append ('\\'); out.append ('v'); break;

          case '\\':
            out.append ('\\'); out.append ('\\'); break;

          default:
            // normal character.
            // if the character is printable normal ASCII, print it.
            if (c >= ' ' && c <= '~')
              out.append (c);
            // otherwise, print a hex constant instead.
            else
              {
                out.append ('\\');
                out.append ('x');
                out.append (Integer.toString ((int)c, 16));
                out.append ('\\');
              }
            break;
          }
      }
    out.append (quoteChar);
    return out.toString ();
  }

  private static String unquotify (String s, char quoteChar)
    throws PrologStringFormatException
  {
    StringBuffer out = new StringBuffer ();

    if (s.charAt (0) != quoteChar)
      throw new PrologStringFormatException 
        ("Quoted string must begin with '"+quoteChar+"'.");

    if (s.charAt (s.length ()-1) != quoteChar)
      throw new PrologStringFormatException 
        ("Quoted string must end with '"+quoteChar+"'.");

    char[] chars = new char[s.length () - 2];

    s.getChars (1, s.length ()-1, chars, 0);

    // --- beware: i changes within the loop.
    for (int i=0; i<chars.length; i++)
      {
        char c = chars[i];
        /*
          self ISSUE: I don't much like the three arms for ', ", ` all expanded
                      like this.  :/
         */
        switch (c)
          {
            // --- double quoted string ----
          case '"':
            if (quoteChar == '"')
              {
                if (i+1<chars.length)
                  {
                    if (chars[i+1] == quoteChar) 
                      {
                        // --- got two quotes: convert to one double quote.
                        i++;
                        out.append (quoteChar);
                      }
                    else
                      throw new PrologStringFormatException 
                        ("Got double quote followed by '"+chars[i+1]+
                         "', expected double quote.");
                  }
                else
                  throw new PrologStringFormatException 
                    ("Got end of string after double quote, "+
                     "expected double quote.");
              }
            else
              out.append ('"');
            break;

            // --- single quoted string ----
          case '\'':
            if (quoteChar == '\'')
              {
                if (i+1<chars.length)
                  {
                    if (chars[i+1] == quoteChar) 
                      {
                        // --- got two quotes: convert to one double quote.
                        i++;
                        out.append (quoteChar);
                      }
                    else
                      throw new PrologStringFormatException 
                        ("Got single quote followed by '"+chars[i+1]+
                         "', expected single quote.");
                  }
                else
                  throw new PrologStringFormatException 
                    ("Got end of string after single quote, "+
                     "expected single quote.");
              }
            else
              out.append ('\'');
            break;

            // --- back quoted string ----
          case '`':
            if (quoteChar == '`')
              {
                if (i+1<chars.length)
                  {
                    if (chars[i+1] == quoteChar) 
                      {
                        // --- got two quotes: convert to one double quote.
                        i++;
                        out.append (quoteChar);
                      }
                    else
                      throw new PrologStringFormatException 
                        ("Got back quote followed by '"+chars[i+1]+
                         "', expected back quote.");
                  }
                else
                  throw new PrologStringFormatException 
                    ("Got end of string after back quote, "+
                     "expected back quote.");
              }
            else
              out.append ('`');
            break;

            // --- escape sequence ----
          case '\\':
            if (i+1<chars.length)
              {
                i++;
                switch (chars[i])
                  {
                    // --- control escape sequence
                  case 'a': out.append ('\u0007'); break;
                  case 'b': out.append ('\b'); break;
                  case 'f': out.append ('\f'); break;
                  case 'n': out.append ('\n'); break;
                  case 'r': out.append ('\r'); break;
                  case 't': out.append ('\t'); break;
                  case 'v': out.append ('\u000b'); break;

                    // --- meta escape sequence
                  case '\\': out.append ('\\'); break;
                  case '\'': out.append ('\''); break;
                  case '"': out.append ('"'); break;
                  case '`': out.append ('`'); break;

                    // --- hexadecimal escape sequence
                  case 'x': 
                    // --- here we got a hex character of the form:
                    //   - \xHEX\
                    {
                      int end = findEndOfCharConstant (chars, i);
                      try {
                        char hexValue = parseCharConstant(chars, i+1, end, 16);
                        out.append (hexValue);
                      } catch (NumberFormatException ex) {
                        throw new PrologStringFormatException 
                          ("Bad hex char constant '"+
                           new String (chars, i+1, end-i+1)+"'", ex);
                      }
                      i = end;
                    }
                    break;

                    // --- octal escape sequence
                  case 'o': 
                    // --- here we got a hex character of the form:
                    //   - \oOCTAL\
                    {
                      int end = findEndOfCharConstant (chars, i);
                      try {
                        char octalValue = parseCharConstant(chars,i+1, end, 8);
                        out.append (octalValue);
                      } catch (NumberFormatException ex) {
                        throw new PrologStringFormatException 
                          ("Bad octal char constant '"+
                           new String (chars, i+1, end-i+1)+"'", ex);
                      }
                      i = end;
                    }
                    break;

                  default:
                    throw new PrologStringFormatException 
                      ("Found '"+chars[i]+"' after '\\'.  '"+
                       chars[i]+"' is an unknown escape character, "+
                       "and is therefore illegal there.");
                  }
              }
            else
              throw new PrologStringFormatException 
                ("Got end of string after '\\', expected escape sequence.");
            break;

          default:
            out.append (c);
            break;

          }
      }
    
    return out.toString ();
  }

  private static int findEndOfCharConstant (char[] in, int startIndex)
  {
    // --- find the ordinal of the last slash.
    int lastSlash = startIndex;
    while (lastSlash < in.length && in[lastSlash] != '\\')
      {
//          Util.out.println ("Got in["+lastSlash+"] == '"+in[lastSlash]+"'");
        lastSlash++;
      }
    if (lastSlash >= in.length)
      lastSlash = -1;

    return lastSlash;

    // now, we have
    //     " ... \xHEXHEX\ ... "
    //            I      L
    // (where L == lastSlash)
    
    // get the chars between I and L into a string,
    // parse as a hex constant, throw an exception
    // if the parsing fails, and finally, make sure
    // the hex constant is in 0x0000 ... 0xFFFF.
    // Finally, print THAT character to the output stream.
    
    // ACK!  What about octal constants!?!?!?
    // Factor into a submethod.
                      
    //"BLARGLE.  FINISH ME SOON.";
  }                    

  private static char parseCharConstant (char[] in, 
                                         int start, int end, 
                                         int radix)
    throws NumberFormatException
  {
//      Util.out.println ("Got start == "+start+", end == "+end+
//                        ", radix == "+radix);
    String value = new String (in, start, end-start);
    int val = Integer.parseInt (value, radix);
    if (val < 0x0000 || val > 0xffff)
      throw new NumberFormatException ("Got char constant too big: "+
                                       Integer.toString (val, 16));
    return (char)val;
  }

  public static void main (String[] args)
    throws PrologStringFormatException
  {
//      testSimpleCases ();

    //int reps = 10000000; // succeeded on May 30/99
    int reps = 100000;

    Util.out.println ("Test double-quoting ASCII strings");
    testDoubleBeatAndBeat (reps, '\u0000', '\u007f');

    Util.out.println ("Test double-quoting ASCII+Latin-1 strings");
    testDoubleBeatAndBeat (reps, '\u0000', '\u00ff');

    Util.out.println ("Test single-quoting ASCII strings");
    testSingleBeatAndBeat (reps, '\u0000', '\u007f');

    Util.out.println ("Test single-quoting ASCII+Latin-1 strings");
    testSingleBeatAndBeat (reps, '\u0000', '\u00ff');

    Util.out.println ("Test back-quoting ASCII strings");
    testBackBeatAndBeat (reps, '\u0000', '\u007f');

    Util.out.println ("Test back-quoting ASCII+Latin-1 strings");
    testBackBeatAndBeat (reps, '\u0000', '\u00ff');

    testErrorCases ();
  }

  public static void testDoubleBeatAndBeat (int reps, char lo, char hi)
  {
    for (int i=0; i<reps; i++)
      {
        String s = RandomUtil.randomFixedLengthString (20, lo, hi);
        String quoted = doubleQuotify (s);
        //Util.out.println ("Got quoted == "+quoted+"");
        try {
          String unquoted = unDoubleQuotify (quoted);
          if (!s.equals (unquoted))
            {
              Util.assertTrue (false, "Bad quotification of '"+s+"'.quoted == '"+
                           quoted+"': got '"+unquoted+"' when parsing!");
            }
          else
            {
              if (i%1000==0)
                {
                  Util.out.print ("."); 
                  Util.out.flush ();
                }
            }
        } catch (PrologStringFormatException ex) {
          Util.assertTrue (false, "Bad quotification of '"+s+"'.quoted == '"+
                       quoted+"'");
        }
      }
    Util.out.println ();
  }

  public static void testSingleBeatAndBeat (int reps, char lo, char hi)
  {
    for (int i=0; i<reps; i++)
      {
        String s = RandomUtil.randomFixedLengthString (20, lo, hi);
        String quoted = singleQuotify (s);
        //Util.out.println ("Got quoted == "+quoted+"");
        try {
          String unquoted = unSingleQuotify (quoted);
          if (!s.equals (unquoted))
            {
              Util.assertTrue (false, "Bad quotification of '"+s+"'.quoted == '"+
                           quoted+"': got '"+unquoted+"' when parsing!");
            }
          else
            {
              if (i%1000==0)
                {
                  Util.out.print ("."); 
                  Util.out.flush ();
                }
            }
        } catch (PrologStringFormatException ex) {
          Util.assertTrue (false, "Bad quotification of '"+s+"'.quoted == '"+
                       quoted+"'");
        }
      }
    Util.out.println ();
  }

  public static void testBackBeatAndBeat (int reps, char lo, char hi)
  {
    for (int i=0; i<reps; i++)
      {
        String s = RandomUtil.randomFixedLengthString (20, lo, hi);
        String quoted = backQuotify (s);
        //Util.out.println ("Got quoted == "+quoted+"");
        try {
          String unquoted = unBackQuotify (quoted);
          if (!s.equals (unquoted))
            {
              Util.assertTrue (false, "Bad quotification of '"+s+"'.quoted == '"+
                           quoted+"': got '"+unquoted+"' when parsing!");
            }
          else
            {
              if (i%1000==0)
                {
                  Util.out.print ("."); 
                  Util.out.flush ();
                }
            }
        } catch (PrologStringFormatException ex) {
          Util.assertTrue (false, "Bad quotification of '"+s+"'.quoted == '"+
                       quoted+"'");
        }
      }
    Util.out.println ();
  }

  public static void testSimpleCases ()
    throws PrologStringFormatException
  {
    Util.out.println (unDoubleQuotify ("\"hi hi \"\" there!\""));
    Util.out.println (unDoubleQuotify ("\"hi hi \\x004d\\ there!\""));
    Util.out.println (unDoubleQuotify ("\"hi hi \\x00D5\\ there!\""));

    Util.out.println ("------------------------------------------");

    Util.out.println (doubleQuotify ("hello\n there-"));
    Util.out.println (doubleQuotify ("fjsldsjfl\"jfsld\t<-tab??"));
    Util.out.println (doubleQuotify ("\u4388 whatsit?? \u001b"));
  }

  public static void testErrorCases ()
  {

    try {
      Util.out.println (unDoubleQuotify ("\"hi hi \" there!\""));
      Util.assertTrue (false);
    } catch (PrologStringFormatException ex) {
      Util.out.println ("Failed: good.  ("+ex.getMessage ()+")");
    }

    try {
      Util.out.println (unDoubleQuotify ("\"hi hi there!\"\""));
      Util.assertTrue (false);
    } catch (PrologStringFormatException ex) {
      Util.out.println ("Failed: good.  ("+ex.getMessage ()+")");
    }

    try {
      Util.out.println (unDoubleQuotify ("\"hi hi \\w there!\""));
      Util.assertTrue (false);
    } catch (PrologStringFormatException ex) {
      Util.out.println ("Failed: good.  ("+ex.getMessage ()+")");
    }

    try {
      Util.out.println (unDoubleQuotify ("\"hi hi there!\\\""));
      Util.assertTrue (false);
    } catch (PrologStringFormatException ex) {
      Util.out.println ("Failed: good.  ("+ex.getMessage ()+")");
    }

    try {
      Util.out.println (unDoubleQuotify ("\"hi hi \\xfoodle\\ there!\""));
      Util.assertTrue (false);
    } catch (PrologStringFormatException ex) {
      Util.out.println ("Failed: good.  ("+ex.getMessage ()+")");
    }

    try {
      Util.out.println (unDoubleQuotify ("\"hi hi \\xfffff\\ there!\""));
      Util.assertTrue (false);
    } catch (PrologStringFormatException ex) {
      Util.out.println ("Failed: good.  ("+ex.getMessage ()+")");
    }
  }
}

