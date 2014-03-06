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
public class Word {

  WordDictionary wordDictionary;
  String line;

  String form;
  String pos;
  int taggedSenseCount;
  long[] synsetOffsets;

  // --- cached.
  List senses = null;
  List synsets = null;

  public Word (WordDictionary _dict, String _line) throws WordNetException
  { 
    wordDictionary = _dict; 
    line = _line; 

    String[] tokens = WordNetUtil.tokenize (line);

    int idx = 0;

    // token 0 is the string.
    form = tokens[idx++].replace ('_', ' ');

    // token 1 is the part of speech.
    pos = getWordNet ().normalizePOS (tokens[idx++]);

    // token 2 is an unknown beast....?
    idx++;

    // token 3 is the number of wierd tokens we have.
    int wierdTokenCount = QuoteUtil.parseInt (tokens[idx++]);
    
    // now, we want to skip these tokens.
    for (int i=0; i<wierdTokenCount; i++)
      idx++;

    // token i is the number of synset offsets we have.
    int synsetOffsetCount = QuoteUtil.parseInt (tokens[idx++]);

    // token i+1 is the number of tagged senses we have.
    taggedSenseCount = QuoteUtil.parseInt (tokens[idx++]);

    // and the rest of the tokens are synset offsets.
    synsetOffsets = new long[synsetOffsetCount];
    for (int i=0; i<synsetOffsetCount; i++)
      synsetOffsets[i] = QuoteUtil.parseLong (tokens[idx++]);
  }

  public WordNet getWordNet ()
  { return wordDictionary.getWordNet (); }

  public List getSynsets () throws WordNetException
  {
    if (synsets == null)
      {
        synsets = new LinkedList ();
        for (int i=0; i<synsetOffsets.length; i++)
          {
            long offset = synsetOffsets[i];
            synsets.add (wordDictionary.getSynset (offset));
          }
      }
    return synsets;
  }

  public List getSenses () throws WordNetException
  {
    if (senses == null)
      {
        senses = new LinkedList ();
        for (int i=0; i<synsetOffsets.length; i++)
          {
            long offset = synsetOffsets[i];
            Synset synset = wordDictionary.getSynset (offset);
            senses.add (synset.getSense (form));
          }
      }
    return senses;
  }

  public boolean isTagged () { return taggedSenseCount > 0; }

  public String tag ()
  { return form+"("+pos+")"; }
}
