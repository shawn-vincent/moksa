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
 * A Dictionary contains all the Words in a given part of speech.
 */
public class WordDictionary {

  WordNet wordNet;

  String pos;
  _IndexFile indexFile;
  
  String dataFileName;
  RandomAccessFile dataFile;

  public WordDictionary (WordNet _wordNet, String _pos, String fileNameRoot) 
    throws WordNetException
  {
    wordNet = _wordNet;
    pos = _pos;
    File f = WordNetUtil.getDataFile (fileNameRoot);
    dataFileName = f.getAbsolutePath ();

    try {
      indexFile = new _IndexFile (pos, fileNameRoot);
    } catch (IOException ex) {
      throw new WordNetException ("Could not open index file for "+tag (), ex);
    }

    try {
      dataFile = new RandomAccessFile (f, "r");
    } catch (IOException ex) {
      throw new WordNetException ("Could not open data file for "+tag (), ex);
    }
  }

  // -------------------------------------------------------------------------
  // ---- Accessors ----------------------------------------------------------
  // -------------------------------------------------------------------------

  public WordNet getWordNet () { return wordNet; }

  // -------------------------------------------------------------------------
  // ---- Core stuff ---------------------------------------------------------
  // -------------------------------------------------------------------------
  
  public Word getWord (String form) throws WordNetException
  { return getWord (form, null); }

  /**
   *
   */
  public Word getWord (String form, String line) throws WordNetException
  {
    if (line == null)
      {
        String key = form.replace (' ', '_').toLowerCase ();
        try {
          line = indexFile.loadLine (key);
        } catch (IOException ex) {
          throw new WordNetException ("Could not load index line for word '"+
                                      form+"'", ex);
        }
      }

    if (line == null)
      throw new WordNetException ("Word '"+form+"' is not in the '"+
                                  pos+"' database");

    // XXX caching???
    Word retval = new Word (this, line);
    return retval;
  }

  public Synset getSynset (long offset) throws WordNetException
  { 
    // XXX caching???
    String line;
    try {
      line = WordNetUtil.lineAt (dataFile, offset);
    } catch (IOException ex) {
      throw new WordNetException ("Could not load synset line at offset '"+
                                  offset+"'", ex);
    }
    return new Synset (this, pos, offset, line);
  }

//    public static void main (String[] args) throws Exception
//    {
//      WordDictionary dict = new WordDictionary ("noun", "noun");
//      Util.out.println ("Opened dictionary "+dict.tag ());
//      Word word = dict.getWord (args[0]);
//      Util.out.println ("Loaded word "+word.tag ());
//    }

  public String tag () { return "WordDictionary("+dataFileName+")"; }
}
