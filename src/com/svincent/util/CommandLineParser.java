package com.svincent.util;

import java.io.*;
import java.util.*;

/**
 * Does a search for each parameter.
 * Too expensive, I hear you say!?  Fools!
 * Command line processing takes almost no time anyway.
 * Get your head out of super-optimized C-land.  This is 
 * the 90's!  We have over 100MHz machines these days, with
 * memory in the dozens of megabytes!
 * @author <a href="http://www.svincent.com/shawn/">Shawn Vincent</a>
 */
public class CommandLineParser extends BaseObject {
  String[] args;

  Set usedParms = new HashSet ();

  public CommandLineParser (String[] _args)
  {
    args = _args;
  }

  /**
   * If there exists an argument like
   *     -name arg
   * returns 'arg'.
   * Else, throws NoSuchElementException.
   */
  private String _getStringArg (String name) throws NoSuchElementException
  {
    String searchFor = '-' + name;

    for (int i=0; i<args.length-1; i++)
      if (args[i].equals (searchFor))
	{
	  usedParms.add (args[i]);
	  return args[i+1];
	}
    throw new NoSuchElementException (searchFor);
  }

  /**
   * If there exists an argument like
   *     -name arg
   * returns 'arg'.
   * Else, returns null.
   */
  public String getStringArg (String name)
  {
    try {
      return _getStringArg (name);
    } catch (NoSuchElementException ex) {
      return null;
    }
  }

  /**
   * If there exists an argument like
   *     -name
   * returns 'true'.
   * Else, returns false.
   */
  public boolean getBooleanArg (String name)
  {
    String searchFor = '-' + name;

    for (int i=0; i<args.length; i++)
      if (args[i].equals (searchFor))
	{
	  usedParms.add (args[i]);
	  return true;
	}
    return false;
  }

  /**
   * If there exists an argument like
   *     -name arg
   * returns 'arg'.
   * Else, throws NoSuchElementException.
   */
  public int getIntArg (String name) throws NoSuchElementException
  {
    return QuoteUtil.parseInt (_getStringArg (name));
  }

  /**
   * Returns all args which are not associated with
   * a named parameter.
   */
  public String[] getUnboundArgs ()
  {
    String[] unbound = new String[args.length];
    int unboundCnt = 0;

    boolean justGotParm = false;
    for (int i=0; i<args.length; i++)
      {
	String arg = args[i];
	// if we got a parameter
	if (arg.startsWith ("-"))
	  justGotParm = true;
	// if we didn't get a parameter, and last loop we didn't,
	else if (!justGotParm)
	  unbound[unboundCnt++] = arg;
	// if we didn't get a parameter, and last loop we *did*.
	else
	  justGotParm = false;
      }

    String[] retval = new String[unboundCnt];
    System.arraycopy (unbound, 0, retval, 0, unboundCnt);

    return retval;
  }

  /**
   * Returns an array containing all of the unused args.
   */
  public String[] getUnusedArgs ()
  {
    String[] unused = new String[args.length];
    int unusedCnt = 0;
    
    for (int i=0; i<args.length; i++)
      if (args[i].startsWith ("-"))
	if (!usedParms.contains (args[i]))
	  unused[unusedCnt++] = args[i];

    String[] retval = new String[unusedCnt];
    System.arraycopy (unused, 0, retval, 0, unusedCnt);

    return retval;
  }
}
