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
 * PrologPackageManager.java
 *
 */
package com.svincent.moksa;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import com.svincent.util.*;

/**
 * <p>Manages Prolog source. </p>
 *
 * <p>Currently operates on a &quot;class path&quot; concept, with no
 * versioning, and thus, all the same issues as the normal Java
 * classloader has. </p>
 *
 * <p>By default, the prolog path includes the Java CLASSPATH, as well
 * as searching in the 'library' subdirectory of the installation
 * directory. </p>
 **/
public class PrologPackageManager extends WamObject {

  PrologEngine engine;

  /**
   * <p>A List of File objects of places to look for Prolog source files
   * and compiled Prolog. </p>
   **/
  List<File> packageDirList = new ArrayList<File> ();

  /**
   * <p>Contains the installation directory: defined as the directory
   * in which the class <code>com.svincent.moksa.PrologEngine</code>
   * is found.  This directory (or Jar file) has a subdirectory called
   * 'library' which is always in the Prolog search path: this is
   * where all the standard library stuff is. </p>
   *
   * <p>Note that the 'library' directory is always at the end of the
   * classpath.  This may have to change.</p>
   **/
  File installDir;

  /**
   * The class loader to use when loading things out of the Prolog path.
   **/
  PrologClassLoader classLoader = new PrologClassLoader (this);

  /**
   *
   **/
  public PrologPackageManager (PrologEngine _engine)
  {
    engine = _engine;

    // --- all classes in this path (which can include jar files) are
    //   - searched.
    String prologPath = System.getProperty ("com.svincent.moksa.path");
    if (prologPath != null)
      addDirs (prologPath);
    String classPath = System.getProperty ("java.class.path");
    addDirs (classPath);

    // --- all classes and jars in this path are searched.
    String prologExtPath = System.getProperty ("com.svincent.moksa.ext.path");
    if (prologExtPath != null) addJarDirs (prologExtPath);
    String extDirs = System.getProperty ("java.ext.dirs");
    addJarDirs (extDirs);

    // --- find the installation directory.
    installDir = findInstallationDirectory ();

    // XXX hackfest.  This would be WAY better if we were able to look inside Jarfiles 
    // (the library code is now packed in the jar, but we can't see it there.  <sigh>... more work.
    File libDir = new File (installDir, "src/library");
    if (!libDir.exists())
    	libDir = new File (installDir, "../src/library");
    if (!libDir.exists())
    	libDir = new File (installDir, "library");
    if (!libDir.exists())
    	Util.assertTrue(false, "Could not find prolog library directory: "+libDir);
	addDir (libDir);

    // XXX finish.
  }

  File findInstallationDirectory ()
  {
    File whereIsMainClass = findClass (PrologEngine.class.getName ());
    if (whereIsMainClass.isFile ())
      return whereIsMainClass.getParentFile ();
    return whereIsMainClass;
  }

  // -------------------------------------------------------------------------
  // ---- Package list manipulation ------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Finds a Prolog module.  It can either return a class file or a
   * Prolog source file, depending on the ages of the relative
   * files. </p>
   **/
  public File findModule (String moduleName)
  {
    // --- first, try to find a prolog source file.
    File sourceFile = new File (findFile (moduleName), moduleName);
    boolean sourceExists = sourceFile.exists ();

    // --- next, try to find a compiled version.
    String compiledName = 
      Prologc.calculatePackageName (moduleName).replace ('.', 
                                                         File.separatorChar)+
      File.separatorChar+
      "Loader.class";
    File compiledFile = new File (findFile (compiledName), compiledName);
    boolean compiledExists = compiledFile.exists ();

    // --- if neither exists, return null;
    if (!sourceExists && !compiledExists) return null;

    // --- if the compiled file doesn't exist, return the source file.
    if (!compiledExists) return sourceFile;

    // --- if the source file doesn't exist, return the compiled file.
    if (!sourceExists) return compiledFile;

    // --- otherwise, both exist.  Compare their dates & return the newest one.
    long sourceTime = sourceFile.lastModified ();
    long compiledTime = compiledFile.lastModified ();
    if (sourceTime > compiledTime) return sourceFile;
    return compiledFile;
  }

  /**
   * Find the location of the file corresponding to the given class in
   * the current prolog search path.
   *
   * Return null if nothing is found.
   **/
  public File findClass (String className)
  {
    String fileName = className.replace ('.', File.separatorChar)+".class";
    return findFile (fileName);
  }

  /**
   * Find the first file with the given name in the path, return it.
   *
   * Search the path for this file, then return the File corresponding
   * to the archive or directory in which it can be found.
   *
   * Return null if nothing is found.
   **/
  public File findFile (String fileName)
  {
    String fileNameFS = fileName.replace ('\\', '/');
    String fileNameBS = fileName.replace ('/', '\\');
    Iterator<File> i = packageDirList.iterator ();
    while (i.hasNext ())
      {
	File classPathElement = (File)i.next ();

	// --- directory.
	if (classPathElement.isDirectory ())
	  {
	    File potentialClass;

	    potentialClass = new File (classPathElement, fileNameFS);
	    if (potentialClass.exists ()) return classPathElement;

	    potentialClass = new File (classPathElement, fileNameBS);
	    if (potentialClass.exists ()) return classPathElement;
	  }
	// --- zip or jar file.  Treat as ZIP.
	else
	  {
		ZipFile zipFile = null;
	    try {
	      zipFile = new ZipFile (classPathElement);
	      if (zipFile.getEntry (fileNameFS) != null)
		    return classPathElement;
	      else if (zipFile.getEntry (fileNameBS) != null)
		    return classPathElement;
	    } catch (IOException ex) {
	    	throw new RuntimeException(ex);
	    } finally {
	      try {
			zipFile.close();
		  } catch (IOException e) {
			// XXX should not happen: don't overwrite thrown exception above.
			e.printStackTrace();
		  }
	    }
	  }
      }

    return null;
  }

  /**
   * Add all the directories (and archive files) in the given path to
   * the Prolog search path.
   **/
  public void addDirs (String path)
  {
    char pathSeperator = File.pathSeparatorChar;
    StringTokenizer tok = new StringTokenizer (path, ","+pathSeperator);
    while  (tok.hasMoreTokens ())  
      {
        String entry = tok.nextToken ().trim ();
        addDir (new File (entry));
      }
  }

  /**
   * Add all the archive files in the directories in the given path to
   * the Prolog search path.
   **/
  public void addJarDirs (String path)
  {
    char pathSeperator = File.pathSeparatorChar;
    StringTokenizer tok = new StringTokenizer (path, ","+pathSeperator);
    while  (tok.hasMoreTokens ())  
      {
        String entry = tok.nextToken ().trim ();
        addJarDir (new File (entry));
      }
  }

  void addJarDir (File dir)
  {
    if (!dir.exists ()) 
      {
        // --- XXX log somewhere proper?
        //Util.err.println 
        //  ("Warning: ignoring absent prolog jar dir entry '"+
        //   dir.getAbsolutePath ()+"'");
        return;
      }

    File[] files = dir.listFiles (new FileFilter ()
      {
        public boolean accept (File f)
        {
          if (f == null) return false;
          if (f.isDirectory ()) return false;
          String name = f.getName ();
          if (name.endsWith (".jar") || name.endsWith (".zip")) return true;
          return false;
        }
      });

    for (int i=0; i<files.length; i++) addDir (files[i]);
  }

  void addDir (File dir)
  {
    if (!dir.exists ()) 
      {
        // --- XXX log somewhere proper?
        //Util.err.println 
        //  ("Warning: ignoring absent prolog path entry '"+
        //   dir.getAbsolutePath ()+
        //   "'.  Check your CLASSPATH for bad directories?");
        return;
      }

    // --- either a directory or a Jar/Zip file, at this point...
    packageDirList.add (new File (dir.getAbsolutePath ()));
  }

  public static void main (String[] args)
  {
    PrologPackageManager pkgManager = 
      new PrologPackageManager (new PrologEngine ());

    File f = pkgManager.findModule (args[0]);
    Util.out.println (f);
  }

  // -------------------------------------------------------------------------
  // ---- Interface to outside world. ----------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Loads the given filename into the given PrologEngine.
   *
   * Searches for an appropriate Loader instance first.
   **/
  public void loadModule (PrologEngine engine, String moduleName)
    throws PrologException
  {
    loadModule (engine, engine.io.parser, moduleName);
  }

  public void loadModule (PrologEngine engine, PrologParser parser,
                          String moduleName)
    throws PrologException
  {
    File foundModule = findModule (moduleName);
    if (foundModule == null)
      throw new PrologException 
        ("Could not find "+moduleName+" OR compiled version!");

    if (foundModule.getName ().endsWith (".class"))
      {
        Util.out.println ("Loading precompiled package "+moduleName);
        String className = calculateClassName (moduleName);

        Class<?> loaderClass;
        try {
          loaderClass = classLoader.loadClass (foundModule, className);
        } catch (IOException ex) {
          throw new PrologException ("Could not load class "+className, ex);
        } catch (ClassNotFoundException ex) {
          throw new PrologException ("Could not find class "+className, ex);
        }

        PrologLoader loader;
        try {
          loader = (PrologLoader)ReflectUtil.construct (loaderClass, 
                                                        Util.EmptyObjectArray);
        } catch (ReflectException ex) {
          throw new PrologException ("Could not construct "+loaderClass, ex);
        }
        
        loader.load (engine);
      }
    else
      {
        //Util.out.println ("Dynamically loading package "+moduleName);
        readProlog (engine, parser, foundModule.getAbsolutePath ());
      }
  }

  public static String calculateClassName (String prologSourceName)
  {
    return Prologc.calculatePackageName (prologSourceName)+".Loader";
  }


  public static class PrologClassLoader extends ClassLoader {
    PrologPackageManager packageManager;

    public PrologClassLoader (PrologPackageManager _packageManager)
    { packageManager = _packageManager; }

    Map<String, Class<?>> definedClasses = new HashMap<String, Class<?>> ();

    public Class<?> loadClass (File file, String className) 
      throws IOException, ClassNotFoundException
    {
      Class<?> c = (Class<?>)definedClasses.get (className);
      if (c == null)
        {
          installClass (file, className);
          c = loadClass (className);
        }
      return c;
    }

    public Class<?> installClass (File file, String className) 
      throws IOException
    {
      byte[] bytes = loadBytes (file);
      Class<?> c = defineClass (className, bytes, 0, bytes.length);
      definedClasses.put (className, c);

      return c;
    }

    public Class<?> findClass (String name) throws ClassNotFoundException
    {
      Class<?> retval = (Class<?>)definedClasses.get (name);
      if (retval == null) 
        {
          if (name.startsWith (Prologc.CompiledPackagePrefix))
            {
              File f = packageManager.findClass (name);
              File classFile = 
                new File (f, name.replace ('.', File.separatorChar)+".class");

              try {
                retval = installClass (classFile, name);
              } catch (IOException ex) {
                throw new ClassNotFoundException 
                  ("Could not load class from "+classFile+": "+ex.toString ());
              }
            }
          else
            {
              retval = findSystemClass (name);
            }

          definedClasses.put (name, retval);
        }
      return retval;
    }

    private byte[] loadBytes (File file) throws IOException
    {
      byte[] retval = new byte[4096];
      int pos = 0;

      FileInputStream in = new FileInputStream (file);
      try {
    	  int available = in.available ();
    	  while (available != 0)
    	  {
    		  if (pos+available >= retval.length)
    			  retval = grow (retval, pos+available);

    		  in.read (retval, pos, available);
    		  pos += available;

    		  available = in.available ();
    	  }
      } finally {
    	  in.close();
      }

      // --- rightsize array.
      byte[] rightsized = new byte[pos];
      System.arraycopy (retval, 0, rightsized, 0, rightsized.length);

      return rightsized;
    }

    private byte[] grow (byte[] bytes, int required)
    {
      byte[] newBytes = new byte[Math.max (bytes.length*2, required)];
      System.arraycopy (bytes, 0, newBytes, 0, bytes.length);
      return newBytes;
    }
  }

  // -------------------------------------------------------------------------
  // ---- Dynamic Prolog loading ---------------------------------------------
  // -------------------------------------------------------------------------

  public static void readProlog (PrologEngine engine, 
                                 PrologParser parser,
                                 String fileName) 
    throws PrologException
  {
    PrologTerm[] clauses;

    try {
      Io io = engine.io;
      Io.PrologInput in = io.openInputUri (fileName);
      clauses = parser.parseFile (in);
    } catch (IOException ex) {
      throw new PrologException ("Could not parse file "+fileName, ex);
    } catch (PrologParseException ex) {
      // XXX list, etc?
      throw new PrologException ("Errors while parsing "+fileName, ex);
    }

    // Util.out.println ("Parsed Prolog file "+fileName);

    Prologc.PrologRuleCompiler ruleCompiler = 
      new Prologc.PrologRuleCompiler (engine);

    for (int i=0; i<clauses.length; i++)
      {
        PrologTerm term = clauses[i];
            
        // XXX allow for queries here, too.
        if (term.getName ().equals (":-") &&
            term.getArity () == 1)
          {
            // --- query.
            PrologTerm query = ((CompoundTerm)term).getSubterm (0);
            //Util.out.println ("Got query "+query.tag ());
            if (!engine.invoke ((CompoundTerm)query))
              throw new PrologException ("Query failed: "+query.tag ());
          }
        else
          {
            // --- rule.
            Rule rule = ruleCompiler.compileRule (term);
            //Util.out.println ("Loaded rule "+rule.getName ()+" ("+
            //                  rule.getClass ().getName ()+")");
            engine.assertz (rule);
          }
      }
  }


}
