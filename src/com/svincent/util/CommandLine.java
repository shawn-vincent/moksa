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
 * CommandLine.java 
 *
 */
package com.svincent.util;

import java.io.*;
import java.util.*;

/**
 * <p>A class which provides facilities for processing command line
 * arguments. </p>
 *
 * <p>To do:</p>
 * <ul>
 *
 *   <li>separate the result processing from the input processing:
 *   this would allow multiple instances of the same arg, more
 *   thread-safety and multi-user stuff for the class, and other
 *   benefits.</li>
 *
 *   <li>recognize required arguments</li>
 *
 *   <li>implement non-strict mode</li>
 *
 *   <li>option groups.</li>
 *
 * </ul>
 **/
public class CommandLine {

  public static final boolean debug = true;

  /**
   * The user of this CommandLine.  Used for printing usage
   * messages.
   **/
  protected Class user;

  /**
   * Where to print error messages.
   **/
  protected PrintWriter err;

  /**
   * True iff unknown arguments represent an error.
   **/
  protected boolean strict;

  protected Map longArgs = new HashMap ();
  protected Map shortArgs = new HashMap ();

  /**
   * How many leftovers are allowed (-1 means any number).
   **/
  protected int allowedLeftovers = -1;


  protected List leftovers = new ArrayList ();

  /**
   * A list of Strings and Args, representing the parsed args, in
   * order.
   **/
  protected List resultArgs = new ArrayList ();


  public CommandLine (Class _user)
  { this (_user, new PrintWriter (System.err, true), true); }

  public CommandLine (Class _user, PrintWriter _err, boolean _strict)
  { user = _user; err = _err; strict = _strict; }

  /**
   * Returns a sorted list of all the Arg objects.
   **/
  public List getAllArgs () 
  { 
    List sortedArgs = new ArrayList ();
    sortedArgs.addAll (longArgs.values ());
    Collections.sort (sortedArgs);
    return sortedArgs; 
  }

  public List getResultArgs () { return resultArgs; }

  public void addArg (Arg arg)
  { 
    longArgs.put (arg.longName, arg); 
    if (arg.shortName != null)
      shortArgs.put (arg.shortName, arg); 
  }

  /**
   * Return true iff there were no errors.
   **/
  public boolean process (String[] args)
  {
    for (int i=0; i<args.length; i++)
      {
        String argStr = args[i];

        // --- long arg
        if (argStr.startsWith ("--"))
          {
            String name = argStr.substring (2);
            Arg arg = (Arg)longArgs.get (name);
            if (arg == null)
              {
                if (strict)
                  {
                    printError ("Unknown long option '"+argStr+"'");
                    return false;
                  }
              }
            else
              {
                resultArgs.add (arg);
                try {
                  arg.parse (args, i+1);
                  i += arg.getArgCount ();
                } catch (ParseException ex) {
                  printError ("Error parsing long option '"+argStr+"': "+
                              ex.getMessage ());
                  return false;
                }
              }
          }
        // --- short arg group
        else if (argStr.startsWith ("-"))
          {
            String argList = argStr.substring (1);
            char[] chars = argList.toCharArray ();
            int consumed = 1;
            for (int j=0; j<chars.length; j++)
              {
                Arg arg = (Arg)shortArgs.get (String.valueOf (chars[j]));
                if (arg == null)
                  {
                    if (strict)
                      {
                        printError ("Unknown short option '-"+chars[j]+"'");
                        return false;
                      }
                  }
                else
                  {
                    try {
                      arg.parse (args, i+consumed);
                      consumed += arg.getArgCount ();
                    } catch (ParseException ex) {
                      printError ("Error parsing short option '-"+
                                  chars[j]+"': "+ex.getMessage ());
                      return false;
                    }
                    resultArgs.add (arg);
                  }
              }
            i = i+consumed;
          }
        // --- not an arg: a leftover.
        else
          {
            leftovers.add (argStr);
            resultArgs.add (argStr);
          }
      }

    // --- all done. Now make sure all required arguments were specified.
    Iterator i = longArgs.values ().iterator ();
    List notFound = new ArrayList ();
    while (i.hasNext ())
      {
        Arg arg = (Arg)i.next ();
        if (arg.isRequired () && !resultArgs.contains (arg))
          {
            notFound.add ("--"+arg.getLongName ());
          }
      }

    if (!notFound.isEmpty ())
      {
        printError 
          ("The following required arguments were not specified: "+notFound);
        return false;
      }

    return true;
  }

  public Arg getArg (String longArg)
  {
    Arg arg = (Arg)longArgs.get (longArg);
    if (arg == null) 
      throw new CommandLineException ("Cannot find arg "+longArg);
    return arg;
  }

  public boolean getBooleanArg (String longArg)
  {
    try {
      return ((BooleanArg)getArg (longArg)).getValue ();
    } catch (ClassCastException ex) {
      throw new TypeException ("Arg "+longArg+" is not a boolean arg!", ex);
    }
  }

  public String getStringArg (String longArg)
  {
    try {
      return ((StringArg)getArg (longArg)).getValue ();
    } catch (ClassCastException ex) {
      throw new TypeException ("Arg "+longArg+" is not a String arg!", ex);
    }
  }

  public int getIntArg (String longArg)
  {
    try {
      return ((IntArg)getArg (longArg)).getValue ();
    } catch (ClassCastException ex) {
      throw new TypeException ("Arg "+longArg+" is not an int arg!", ex);
    }
  }

  public String[] getLeftovers ()
  { return (String[])leftovers.toArray (new String[leftovers.size ()]); }

  protected void printError (String msg)
  {
    err.println (msg);
    err.println ();
    printUsage (err);
  }

  public void printUsage (PrintWriter out)
  {
    out.println ("Usage:");
    out.print ("  java "+user.getName ());

    // --- Get a sorted list of the options.
    List sortedArgs = getAllArgs ();

    // --- print the required arguments.
    Iterator it = sortedArgs.iterator ();
    while (it.hasNext ())
      {
        Arg arg = (Arg)it.next ();
        if (arg.isRequired ())
          {
            out.print (' ');
            arg.printTypicalUsage (out);
          }
      }

    out.println ();

    // --- now we have a sorted list of args.
    //   - format it up real pretty-like.

    // --- find the longest short and long args, to help formatting.
    int maxArgUsageLength = 0; 
    for (int i=0; i<sortedArgs.size (); i++)
      {
        Arg arg = (Arg)sortedArgs.get (i);
        int argUsageLength = arg.getUsageLength ();
        if (argUsageLength > maxArgUsageLength)
          maxArgUsageLength = argUsageLength;
      }
    
    // --- figure out the offset of the help text for any option.
    int helpTextOffset = 2 + maxArgUsageLength + 2;

    // --- start printing.
    for (int i=0; i<sortedArgs.size (); i++)
      {
        Arg arg = (Arg)sortedArgs.get (i);
        out.print ("  ");
        arg.printUsage (out);
        int argUsageLength = arg.getUsageLength ();
        int numSpaces = maxArgUsageLength - argUsageLength;
        for (int j=0; j<numSpaces; j++) out.print (' ');
        out.print ("   ");
        printFormattedHelp (out, helpTextOffset, 75, arg.getDescription ());
      }
  }

  static void printFormattedHelp (PrintWriter out, int startColumn, 
                                  int endColumn, String s)
  {
    int width = endColumn - startColumn;
    if (width <= 0) throw new IllegalArgumentException ();

    int pos = startColumn;
    StringTokenizer tokenizer = new StringTokenizer (s);
    while (tokenizer.hasMoreTokens ())
      {
        String tok = tokenizer.nextToken ();
        int tokLen = tok.length ();
        if (tokLen > width)
          {
            while (tokLen > width)
              {
                String start = tok.substring (0, endColumn-pos);
                tok = tok.substring (endColumn-pos);
                tokLen = tok.length ();

                out.println (start);
                for (int i=0; i<=startColumn; i++) out.print (' ');
                pos = startColumn;
              }
          }
        if (pos+tokLen > endColumn)
          {
            // wrap word.
            out.println ();
            for (int i=0; i<=startColumn; i++) out.print (' ');
            pos = startColumn;
          }
        out.print (tok);
        pos += tokLen;

        out.print (' ');
        pos += 1;
      }
    out.println ();
  }

  // -------------------------------------------------------------------------
  // ---- Different types of args --------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * An Arg represents the definition of a potential argument on the
   * command line.
   **/
  public abstract static class Arg implements Comparable
  {
    boolean required = false;
    String shortName = null;
    String longName = null;
    String description = null;

    public Arg (String _longName) { longName = _longName; }

    // -----------------------------------------------------------------------
    // ---- Accessors --------------------------------------------------------
    // -----------------------------------------------------------------------

    public Arg required () { required = true; return this; }
    public Arg shortName (char c) { shortName=String.valueOf(c); return this; }
    public Arg description (String s) { description = s; return this; }

    public boolean isRequired () { return required; }
    public String getShortName () { return shortName; }
    public String getLongName () { return longName; }
    public String getDescription () { return description; }

    // -----------------------------------------------------------------------
    // ---- java.lang.Comparable implementation ------------------------------
    // -----------------------------------------------------------------------

    /**
     *
     **/
    public int compareTo (Object _that)
    {
      if (_that == null) return 1;
      if (!(_that instanceof Arg))
        {
          String thisClassName = this.getClass ().getName ();
          String thatClassName = _that.getClass ().getName ();
          // JDK 1.2-specific method here...
          return thisClassName.compareTo (thatClassName);
        }

      Arg that = (Arg)_that;

      // --- required/not required is the most important bit of state.
      if (this.isRequired () && !that.isRequired ()) return -1;
      if (!this.isRequired () && that.isRequired ()) return 1;

      // --- then sort by long name.
      return this.getLongName().compareTo (that.getLongName());
    }

    // -----------------------------------------------------------------------
    // ---- Parsing ----------------------------------------------------------
    // -----------------------------------------------------------------------

    public abstract int getArgCount ();
    public abstract void parse (String[] args, int index);

    // -----------------------------------------------------------------------
    // ---- Viewing this Arg in various ways. --------------------------------
    // -----------------------------------------------------------------------

    public String toString () { return tag (); }

    public String tag ()
    {
      StringBuffer out = new StringBuffer ();
      appendType (out);
      out.append ("(");
      out.append ("--"+longName);
      out.append (", ");
      appendValue (out);
      out.append (")");

      return out.toString ();
    }

    String getExampleValue () { return null; }

    abstract void appendType (StringBuffer out);
    abstract void appendValue (StringBuffer out);

    int getUsageLength () 
    { 
      // slow, but we're PRINTING USAGE MESSAGES.  Who cares!?
      StringPrintWriter out = new StringPrintWriter ();
      printUsage (out);
      out.flush ();
      return out.toString ().length ();
    }

    void printUsage (PrintWriter out)
    {
      if (shortName != null)
        {
          out.print ("-"); out.print (shortName); out.print (", ");
        }
      else
        {
          out.print ("    ");
        }

      out.print ("--"); out.print (getLongName ());

      String exampleValue = getExampleValue ();
      if (exampleValue != null)
        {
          out.print (' '); out.print (exampleValue);
        }
    }

    public void printTypicalUsage (PrintWriter out) 
    {
      out.print ("--"); out.print (getLongName ()); 
      String exampleValue = getExampleValue ();
      if (exampleValue != null)
        {
          out.print (' '); out.print (exampleValue);
        }
    }
  }

  public static class BooleanArg extends Arg
  {
    boolean value;
    public BooleanArg (String _longName) { super (_longName); }

    public void parse (String[] args, int argIndex)
    { value = true; }

    public int getArgCount () { return 0; }

    public boolean getValue () { return value; }

    void appendType (StringBuffer out) { out.append ("BooleanArg"); }
    void appendValue (StringBuffer out) { out.append (value); };
  }

  public static class IntArg extends Arg
  {
    int value;
    public IntArg (String _longName) { super (_longName); }

    public void parse (String[] args, int argIndex)
    {
      if (argIndex >= args.length)
        throw new ParseException ("Expected integer argument: got nothing.");
      String valueStr = args[argIndex];
      try {
        value = Integer.parseInt (valueStr);
      } catch (NumberFormatException ex) {
        throw new ParseException ("Expected integer argument: got '"+
                                  valueStr+"'");
      }
    }

    public int getArgCount () { return 1; }

    public int getValue () { return value; }

    void appendType (StringBuffer out) { out.append ("IntArg"); }
    void appendValue (StringBuffer out) { out.append (value); };

    String getExampleValue () { return "INT"; }
  }

  public static class StringArg extends Arg
  {
    String value;
    public StringArg (String _longName) { super (_longName); }

    public void parse (String[] args, int argIndex)
    {
      if (argIndex >= args.length)
        throw new ParseException ("Expected String argument: got nothing.");
      value = args[argIndex];
    }

    public int getArgCount () { return 1; }

    public String getValue () { return value; }

    void appendType (StringBuffer out) { out.append ("StringArg"); }
    void appendValue (StringBuffer out) { out.append (value); };

    String getExampleValue () { return "STRING"; }
  }


  // -------------------------------------------------------------------------
  // ---- Exception Handling -------------------------------------------------
  // -------------------------------------------------------------------------

  public static class CommandLineException extends NestableRuntimeException {
    public CommandLineException () { super (); }
    public CommandLineException (String msg) { super (msg); }
    public CommandLineException (String msg, Throwable ex) { super (msg, ex); }
  }

  public static class ArgRedefinedException extends CommandLineException {
    public ArgRedefinedException () { super (); }
    public ArgRedefinedException (String msg) { super (msg); }
    public ArgRedefinedException (String msg, Throwable ex) { super (msg,ex); }
  }

  public static class TypeException extends CommandLineException {
    public TypeException () { super (); }
    public TypeException (String msg) { super (msg); }
    public TypeException (String msg, Throwable ex) { super (msg,ex); }
  }

  public static class ParseException extends CommandLineException {
    public ParseException () { super (); }
    public ParseException (String msg) { super (msg); }
    public ParseException (String msg, Throwable ex) { super (msg,ex); }
  }

  // -------------------------------------------------------------------------
  // ---- Test Code ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Print out the args, using this guy's fancy arg-scheme.
   **/
  public static void main (String[] args)
  {
    CommandLine commandLine =
      new CommandLine (CommandLine.class);

    commandLine.addArg (new BooleanArg ("likesPoodles").
      shortName ('p').description ("You really like poodles"));
    commandLine.addArg (new BooleanArg ("likesDogs").
      shortName ('d').description ("You like dogs, but not poodles."));

    commandLine.addArg (new StringArg ("favorite").
      shortName ('f').description ("Your favorite string.").required ());
    commandLine.addArg (new StringArg ("worst").
      shortName ('w').description ("Your least favorite string.").required ());

    commandLine.addArg (new IntArg ("age").
      shortName ('a').description ("the best int: your age."));
    commandLine.addArg (new IntArg ("height").
      shortName ('h').description 
      ("The worst int: your height.  "+
       "This is really bad, particularly if you're as tall as me!!"+
       "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+
       "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"+
       "ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc").
      required ());

    commandLine.addArg (new BooleanArg ("long-only").
      description ("This guy is long-only."));

    commandLine.process (args);

    List resultArgs = commandLine.getResultArgs ();
    Iterator i = resultArgs.iterator ();
    while (i.hasNext ())
      {
        Object next = i.next ();
        if (next instanceof String)
          System.out.println ("Leftover: "+next);
        else
          System.out.println ("Arg: "+next);
      }
  }
}
