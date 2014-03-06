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
public class Sense extends SynsetOrSense implements WordNetConstants {

  Synset synset;
  String pos;
  int senseIndex;
  String[] senseTuple;
  int[] verbFrames;

  // --- derived from senseTuple.
  String position;
  String form;

  public Sense (Synset _synset, int _senseIndex, String[] _senseTuple) 
    throws WordNetException
  {
    this (_synset, _senseIndex, _senseTuple, null);
  }

  public Sense (Synset _synset, int _senseIndex, 
                String[] _senseTuple, int[] _verbFrames)
    throws WordNetException
  {
    synset = _synset;
    pos = synset.pos;
    senseIndex = _senseIndex;
    senseTuple = _senseTuple;
    verbFrames = _verbFrames;

    String _form = senseTuple[0];
    String idString = senseTuple[1];

    int openParenIndex = _form.indexOf ('(');
    if (openParenIndex != -1)
      {
        String key = _form.substring (openParenIndex+1, _form.length ()-1);
        if (key.equals ("a")) position = ATTRIBUTIVE;
        else if (key.equals ("p")) position = PREDICATIVE;
        else if (key.equals ("ip")) position = IMMEDIATE_POSTNOMINAL;
        else
          throw new WordNetException ("Unknown attribute "+key);
      }
    form = _form.replace ('_', ' ');
  }

  public WordNet getWordNet () { return synset.getWordNet (); }

  public String getForm () { return form; }
  public int getSenseIndex () { return senseIndex; }

  public String getGloss () { return synset.getGloss (); }

  public Word getWord () throws WordNetException
  { return getWordNet ().getWord (form, pos); }

  public List getPointers (String pointerType) throws WordNetException
  {
    List retval = new LinkedList ();

    List pointers = synset.getPointers (pointerType);
    Iterator i = pointers.iterator ();
    while (i.hasNext ())
      {
        Pointer p = (Pointer)i.next ();
        if (p.sourceIndex == 0 || p.sourceIndex-1 ==senseIndex)
          retval.add (p);
      }

    return retval;
  }

  public List getPointerTargets (String pointerType) throws WordNetException
  {
    List retval = new LinkedList ();

    List pointers = getPointers (pointerType);
    Iterator i = pointers.iterator ();
    while (i.hasNext ())
      {
        Pointer p = (Pointer)i.next ();
        retval.add (p.getTarget ());
      }

    return retval;
  }

  public String tag ()
  {
    return form + " -- (" + getGloss () + ")";
  }
}
