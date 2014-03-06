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
public class Pointer {
  WordNet wordNet;

  long sourceOffset;
  String[] pointerTuple;

  // parsed from pointer tuple.
  String type;
  long targetOffset;
  String pos;
  int sourceIndex;
  int targetIndex;

  public Pointer (WordNet _wordNet, long _sourceOffset, String[] _pointerTuple)
    throws WordNetException
  {
    wordNet = _wordNet;
    sourceOffset = _sourceOffset;
    pointerTuple = _pointerTuple;

    // (type, offset, pos, indices) = pointerTuple
    type = getWordNet ().getPointerType (pointerTuple[0]);
    targetOffset = QuoteUtil.parseLong (pointerTuple[1]);
    pos = getWordNet ().normalizePOS (pointerTuple[2]);
    int indices = QuoteUtil.parseInt (pointerTuple[3], 16, 0);
    sourceIndex = indices >> 8;
    targetIndex = indices & 255;
  }

  public WordNet getWordNet () { return wordNet; }

  public String getType () { return type; }

  public SynsetOrSense getSource () throws WordNetException
  {
    Synset synset= getWordNet ().getSynset (pos, sourceOffset);
    if (sourceIndex != 0)
      return synset.getSense (sourceIndex - 1);
    else
      return synset;
  }

  public SynsetOrSense getTarget () throws WordNetException
  {
    Synset synset= getWordNet ().getSynset (pos, targetOffset);
    if (targetIndex != 0)
      return synset.getSense (targetIndex - 1);
    else
      return synset;
  }

  public String tag () 
  { 
    try {
      return getType () + "->" + getTarget ().tag (); 
    } catch (WordNetException ex) {
      throw new WordNetRuntimeException ("Exception during printing.", ex);
    }
  }
}
