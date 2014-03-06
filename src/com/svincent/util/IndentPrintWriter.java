
package com.svincent.util;

import java.io.*;
import java.util.Stack;


/**
 * self ISSUE: assumes that all newlines are printed with println(),
 *             for efficiency.
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class IndentPrintWriter extends PrintWriter {

  Stack indents = new Stack ();
  // TRUE iff last character printed was a newline.
  boolean lastWasNewline = false;

  int defaultIndent;
  String cachedDefaultIndentStr = null;

  public IndentPrintWriter (Writer w) { this (w, 2); }
  public IndentPrintWriter (Writer w, int _defaultIndent) 
  { super (w, true); defaultIndent = _defaultIndent; }

  private String getDefaultIndentStr ()
  {
    if (cachedDefaultIndentStr == null)
      {
        StringBuffer sb = new StringBuffer ();
        for (int i=0; i<defaultIndent; i++) sb.append (' ');
        cachedDefaultIndentStr = sb.toString ();
      }
    return cachedDefaultIndentStr;
  }

  public String currentIndent ()
  {
    if (indents.empty ())
      return "";
    return (String)indents.peek ();
  }

  public void indent () { indent ("  "); }

  public void indent (String newIndent)
  { indents.push (newIndent + currentIndent ()); }

  public void outdent () { indents.pop (); }

  void possiblyPrintIndent ()
  {
    // NOTE: this code is copied into print (char c), for efficiency.
    if (lastWasNewline) 
      {
	super.print (currentIndent ());
	lastWasNewline = false;
      }
  }

  public void print (boolean b) { possiblyPrintIndent (); super.print (b); }
  public void print (int i) { possiblyPrintIndent (); super.print (i); }
  public void print (long l) { possiblyPrintIndent (); super.print (l); }
  public void print (float f) { possiblyPrintIndent (); super.print (f); }
  public void print (double d) { possiblyPrintIndent (); super.print (d); }

  // ISSUE: these can print unchecked newlines.  Possibly check.
  public void print (char c) 
  { 
    if (lastWasNewline) 
      {
	super.print (currentIndent ());
	lastWasNewline = false;
      }
    super.print (c); 
    if (c == '\n') // ISSUE: Somehow parse Util.NL????
      lastWasNewline = true;
  }

  // ISSUE: these can print unchecked newlines.  Possibly check.
  public void print (char s[]) 
  { 
    if (s.length != 0)
      {
	possiblyPrintIndent ();
	for (int i=0; i<s.length; i++)
	  print (s[i]); 
      }
  }

  static final char[] NullCharArray = new char[] {'n', 'u', 'l', 'l'};

  // ISSUE: these can print unchecked newlines.  Possibly check.
  public void print (String s) 
  {
    if (s == null)
      {
	possiblyPrintIndent ();
	print (NullCharArray);
	return;
      }
    if (!"".equals (s))
      {
	possiblyPrintIndent (); 
	print (s.toCharArray ()); 
      }
  }

  // ISSUE: these can print unchecked newlines.  Possibly check.
  public void print (Object obj) 
  {
    String stringRep = String.valueOf (obj);
    if (!"".equals (stringRep))
      {
	possiblyPrintIndent (); 
	print (stringRep); 
      }
  }

  public void newLine () 
  { 
    super.println (); 
    lastWasNewline = true;
  }
  public void println () { possiblyPrintIndent (); newLine (); }
  public void println (boolean x) { print (x); newLine (); }
  public void println (char x) { print (x); newLine (); }
  public void println (int x) { print (x); newLine (); }
  public void println (long x) { print (x); newLine (); }
  public void println (float x) { print (x); newLine (); }
  public void println (double x) { print (x); newLine (); }
  public void println (char x[]) { print (x); newLine (); }
  public void println (String x) { print (x); newLine (); }
  public void println (Object x) { print (x); newLine (); }

  public static void main (String[] argv)
  {
    PrintWriter out = Util.out;
    out.print ("Hello");
    Util.indent (out);
      out.print ("Hello");
      out.println ("Hello");
      out.println ("3434");
      Util.indent (out);
        out.println ("this is indented, too");
      Util.outdent (out);
    Util.outdent (out);
    out.println ("This shouldn't be indented");
  }





}
