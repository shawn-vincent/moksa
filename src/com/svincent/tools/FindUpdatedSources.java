package com.svincent.tools;


import java.util.Hashtable;
import java.util.Vector;
import java.io.File;
import java.util.Enumeration;
import java.io.PrintStream;

public class FindUpdatedSources {

  public static void main (String[] args)
  {
    String dir = ".";
    String srcFileExt = ".java";
    String targetFileExt = ".class";
    Vector rootDirs = new Vector ();

    for (int i=0; i<args.length; i++)
      {
	String arg = args[i];
	if (arg.startsWith ("-"))
	  {
	    if (arg.startsWith ("-s"))
	      srcFileExt = arg.substring (2);
	    else if (arg.startsWith ("-t"))
	      targetFileExt = arg.substring (2);
	    else
	      {
		System.out.println ("Unknown argument "+arg);
		System.out.println ("usage: FindUpdatedSources "+
				    " [-sSRCFILEEXT] [-tTARGETFILEEXT] dirs");
		return;
	      }
	  }
	else
          rootDirs.addElement (arg);
      }

    // --- Make handler that filters files
    FindUpdatedSourcesFileHandler fh = new FindUpdatedSourcesFileHandler ();
    fh.setSrcFileExt (srcFileExt);
    fh.setTargetFileExt (targetFileExt);

    // --- Walk all specified source dirs, firing all files against handler
    for (int i=0; i<rootDirs.size (); i++)
      {
        FileTreeWalker walker = 
          new FileTreeWalker ((String)rootDirs.elementAt (i), fh);
        walker.walk ();
      }

    // --- Dump list of files handler accepted
    fh.dump (System.out);
  }

  static class FindUpdatedSourcesFileHandler implements FileHandler {

    Hashtable files;
    String srcFileExt;
    String targetFileExt;
    int srcFileExtLen;
    int targetFileExtLen;

    public FindUpdatedSourcesFileHandler ()
    {
      files = new Hashtable ();
      setSrcFileExt (".java");
      setTargetFileExt (".class");
    }


    public void setSrcFileExt (String _srcFileExt)
    {
      srcFileExt = _srcFileExt;
      srcFileExtLen = srcFileExt.length ();
    }

    public void setTargetFileExt (String _targetFileExt)
    {
      targetFileExt = _targetFileExt;
      targetFileExtLen = targetFileExt.length ();
    }


    public void acceptFile (File file)
    {
      File javaFile = null;
      File classFile = null;
      String path = file.getPath ();

      if (path.endsWith (srcFileExt))
	{
	  javaFile = file;
	  classFile = (File)files.get (path.substring (0, path.length () 
						       - srcFileExtLen));
	}
      else if (path.endsWith (targetFileExt))
	{
	  classFile = file;
	  javaFile = (File)files.get (path.substring (0, path.length () 
						      - targetFileExtLen) +
				      srcFileExt);
	}

      if (javaFile != null && classFile != null)
	{
	  if (javaFile.lastModified () < classFile.lastModified ())
	    {
	      files.remove (javaFile.getPath ());
	      files.remove (classFile.getPath ());
	    }
	  else
	    files.put (javaFile.getPath (), javaFile);

	}
      else if (javaFile != null)
	files.put (path, javaFile);
      else if (classFile != null)
	files.put (path.substring (0, path.length () - targetFileExtLen), 
		   classFile);


    }

    void dump (PrintStream out)
    {
      // -- spit the contents of the hashtable
      for (Enumeration e = files.keys (); e.hasMoreElements ();)
	{
	  String s = e.nextElement ().toString ();
	  if (s.endsWith (srcFileExt))
	      out.println (s);
	}
    }


  }
}

