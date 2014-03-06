
package com.svincent.util;

import java.io.*;

/**
 * A StringPrintWriter writes its output to a string.
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class StringPrintWriter extends PrintWriter
{
  protected StringBuffer sb;

  public StringPrintWriter () { this (new StringBuffer ()); }
  protected StringPrintWriter (StringBuffer _sb) 
  { super (new StringWriter ()/*unused*/); sb = _sb; }

  public void flush () {}

  public void print (boolean b) { sb.append (b); }
  public void print (char c) { sb.append (c); }
  public void print (int i) { sb.append (i); }
  public void print (long l) { sb.append (l); }
  public void print (float f) { sb.append (f); }
  public void print (double d) { sb.append (d); }
  public void print (char s[]) { sb.append (s); }
  public void print (String s) { sb.append (s); }
  public void print (Object obj) { sb.append (obj); }

  public void newLine () { sb.append (Util.NL); flush (); }
  public void println () { newLine (); }
  public void println (boolean x) { print (x); newLine (); }
  public void println (char x) { print (x); newLine (); }
  public void println (int x) { print (x); newLine (); }
  public void println (long x) { print (x); newLine (); }
  public void println (float x) { print (x); newLine (); }
  public void println (double x) { print (x); newLine (); }
  public void println (char x[]) { print (x); newLine (); }
  public void println (String x) { print (x); newLine (); }
  public void println (Object x) { print (x); newLine (); }

  public void write(int c) { sb.append ((char)c); }
  public void write(char[] buf) { sb.append (buf); }
  public void write(String s) { sb.append (s); }

  public void write(char[] buf, int off, int len) 
  { sb.append (buf, off, len); }
  public void write(String s, int off, int len) 
  { sb.append (s.toCharArray (), off, len); }

  public String tag () { return sb.toString (); }
  public String toString () { return sb.toString (); }
}
