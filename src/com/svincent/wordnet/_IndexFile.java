/*
 * Based on the WordNet interface by Oliver Steele (steele@cs.brandeis.edu)
 * Source: http://www.cs.brandeis.edu/~steele/sources/wordnet.py
 *  Home Page: http://www.cs.brandeis.edu/~steele/sources/wordnet-python.html
 *
 * Copyright (c) 1998-1999 by Oliver Steele.  Use this however you like,
 * so long as you preserve the attribution and label your changes.
 *
 * This Java version is
 * Copyright (c) 1999 by Shawn Vincent.
 *
 */
package com.svincent.wordnet;

import java.io.*;
import java.util.*;

import com.svincent.util.*;

/**
 *
 */
public class _IndexFile /*implements List, Map*/ {
  String pos;
  RandomAccessFile file;
  String fileName;

  /**
   * Table of (pathname, offset) -> (line, nextOffset) XXX
   */
  Map offsetLineCache;

  RandomAccessFile indexCache;
  String shelfName;

  long nextIndex;
  long nextOffset;
  
  Throwable lastError = null;

  public _IndexFile (String _pos, String _filenameRoot)
    throws IOException
  {
    pos = _pos;

    File f = WordNetUtil.getIndexFile (_filenameRoot);
    file = new RandomAccessFile (f, "r");
    fileName = f.getAbsolutePath ();

    offsetLineCache = new HashMap ();
    rewind ();

    // XXX use the python idx files here?  Probably.  :)
    f = new File (WordNetUtil.WnSearchFile, pos+".pyidx");
    shelfName = f.getAbsolutePath ();
    try {
      indexCache = new RandomAccessFile (f, "r");
    } catch (FileNotFoundException ex) {
      indexCache = null;
    }
  }

  public boolean hasError () { return lastError != null; }
  public Throwable getLastError () { return lastError; }

  void rewind () throws IOException
  {
    //Util.out.println ("Rewinding index file "+tag ());

    long offset; 
    String line;

    // --- go to beginning.
    file.seek (0);
    while (true)
      {
        offset = file.getFilePointer ();
        line = WordNetUtil.readLine (file);
        //Util.out.println ("    Read line '"+line+"'");
        if (line.charAt (0) != ' ') break;
      }
    nextIndex = 0;
    nextOffset = offset;
  }

  public int size ()
  {
    //Util.out.println ("Calculating size of index file "+tag ());
    try {
      return WordNetUtil.calcNumLines (file);
    } catch (IOException ex) {
      lastError = ex;
      ex.printStackTrace ();
      return -1;
    }
  }

  /**
   * Returns the line from the index for the given key.
   */
  String loadLine (String key) throws IOException
  { return WordNetUtil.binarySearchFile (file, key); }

  public static void main (String[] args) throws Exception
  {
    _IndexFile file = new _IndexFile ("noun", "noun");
    Util.out.println ("Loaded index file "+file.tag ());
    //Util.out.println ("Got indexfile size == "+file.size ());
    Util.out.println ("entry == "+file.loadLine (args[0]));
  }

  public String tag ()
  {
    return "_IndexFile("+fileName+")";
  }
}


