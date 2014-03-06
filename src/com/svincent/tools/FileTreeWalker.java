package com.svincent.tools;


import java.util.Hashtable;
import java.io.File;
import java.util.Enumeration;
import java.io.PrintStream;

public class FileTreeWalker
{
  File root;
  FileHandler fh;
  
  public FileTreeWalker (String rootDirectory, FileHandler fh)
  {
    setRoot (new File (rootDirectory));
    setFileHandler (fh);
  }
  
  void setFileHandler (FileHandler _fh)
  {
    fh = _fh;
  }
  
  void setRoot (File _root)
  {
    if (!_root.isDirectory ()) 
      throw new RuntimeException (_root.getPath () + " is not a directory");
    root = _root;
    // == check that root is a directory
  }
  
  void walk (File root)
  {
    //System.err.println ("Entering " + root.getPath ());
    File[] list = root.listFiles ();
    for (int i = 0; i < list.length; i++)
      if (list[i].isDirectory ())
	walk (list [i]);
      else
	fh.acceptFile (list [i]);
  }

  public void walk ()
  {
    walk (root);
  }
}

