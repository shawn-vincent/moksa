/**
 * ClassWhich.java
 */

package com.svincent.tools;


import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * WHICH for Java classes.  Command line interface available.
 **/
public class ClassWhich {

  static final boolean verbose = true;
  static final boolean debug = true;

  /**
   * Returns a pathname to where in the classpath the given class is. <p>
   *
   * Returns null if it can't find anything. <p>
   */
  public static String findClass (String className)
  {
    String classNameAsPath1 = className.replace ('.', '/')+".class";
    String classNameAsPath2 = className.replace ('.', '\\')+".class";
    File[] classpath = getClasspath ();
    for (int i=0; i<classpath.length; i++)
      {
	File classPathElement = classpath[i];

	if (verbose)
	  System.out.println (" Checking "+classPathElement.getAbsolutePath());

	// --- directory.
	if (classPathElement.isDirectory ())
	  {
	    File potentialClass;

	    potentialClass = new File (classPathElement, classNameAsPath1);
	    if (potentialClass.exists ())
	      return classPathElement.getAbsolutePath ();

	    potentialClass = new File (classPathElement, classNameAsPath2);
	    if (potentialClass.exists ())
	      return classPathElement.getAbsolutePath ();
	  }
	// --- zip or jar file.  Treat as ZIP.
	else
	  {
	    try {
	      ZipFile zipFile = new ZipFile (classPathElement);
	      if (zipFile.getEntry (classNameAsPath1) != null)
		return classPathElement.getAbsolutePath ();
	      else if (zipFile.getEntry (classNameAsPath2) != null)
		return classPathElement.getAbsolutePath ();
	    } catch (IOException ex) {
	    }
	  }
      }
    return null;
  }

  public static void main (String[] args)
  {
    if (args.length < 1)
      System.out.println ("Usage: java "+
			  ClassWhich.class.getName ()+" [className]");
    else
      {
	String location = findClass (args[0]);
	if (location == null)
	  System.out.println ("Can't find "+args[0]);
	else
	  System.out.println (args[0]+" is at "+location);
      }
  }

  /**
   * Gets a list of File objs corresponding to the classpath on this machine.
   */
  public static File[] getClasspath () throws SecurityException
  {
    String classpath = System.getProperty ("java.class.path");

    Vector directories = new Vector ();
    char pathSeperator = File.pathSeparatorChar;
    int lastSepIndex = -1;
    int sepIndex = -1;
    while ((sepIndex = classpath.indexOf (pathSeperator, sepIndex+1)) != -1)
      {
	// -- now lastSepIndex..sepIndex is the string we want.
	String pathName = classpath.substring (lastSepIndex + 1, sepIndex);
	
	// -- add this pathname to the vector.
	directories.addElement (pathName);

	lastSepIndex = sepIndex;
      }
    // --- do the last one.
    if (lastSepIndex != -1 && lastSepIndex+1 < classpath.length ())
      {
	String pathName = classpath.substring (lastSepIndex+1);
	directories.addElement (pathName);
      }

    File[] retval = new File[directories.size ()];
    for (int i=0; i<retval.length; i++)
      retval[i] = new File ((String)directories.elementAt (i));

    return retval;
  }

}
