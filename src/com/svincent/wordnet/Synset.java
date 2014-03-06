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
 * A set of synonyms that share a common meaning.
 */
public class Synset extends SynsetOrSense implements WordNetConstants {

  WordDictionary wordDictionary;
  String pos;
  long offset;
  String line;
  
  // --- derived from 'line'
  String gloss;
  String ssType;
  String[][] senseTuples;
  String[][] pointerTuples;
  int[][] senseVerbFrames;

  // --- lazy.
  List senses;
  List pointers;

  public Synset (WordDictionary _dict, String _pos, long _offset, String _line)
    throws WordNetException
  {
    wordDictionary = _dict;
    pos = _pos;
    offset = _offset;
    line = _line;

    // --- line of the form TOKENS | gloss entry
    //   - Split it.
    int barIndex = line.indexOf ('|');
    if (barIndex == -1)
      throw new WordNetException 
        ("Corruption: could not find '|' char in data file line: '"+
         QuoteUtil.debugQuote (line)+"'");

    // --- get the gloss entry.
    gloss = line.substring (barIndex+1).trim ();

    // --- read the tokens.
    String tokenStr = line.substring (0, barIndex);
    String[] tokens = WordNetUtil.tokenize (tokenStr);
    int idx = 0;

    ssType = tokens[2];

    TokenGrouper grouper = new TokenGrouper (tokens, 4, tokens.length - 4);
    int numSenseTuples = QuoteUtil.parseInt (tokens[3], 16, 0);
    senseTuples = grouper.getGroupSequence (2, numSenseTuples);
    int numPointerTuples = QuoteUtil.parseInt (grouper.nextToken ());
    pointerTuples = grouper.getGroupSequence (4, numPointerTuples);

    if (pos.equals (VERB))
      {
        int numVfTuples = QuoteUtil.parseInt (grouper.nextToken ());
        String[][] vfTuples = grouper.getGroupSequence (3, numVfTuples);
        senseVerbFrames = new int[senseTuples.length][];
        for (int i=1; i<senseTuples.length+1; i++)
          senseVerbFrames[i-1] = extractVerbFrames (i, vfTuples);

        // --- XXX also 'verbFrames', somehow?  
      }
  }

  public WordDictionary getWordDictionary ()
  { return wordDictionary; }

  public WordNet getWordNet ()
  { return getWordDictionary ().getWordNet (); }

  public String getGloss () { return gloss; }

  public List getSenses () throws WordNetException
  {
    if (senses == null)
      {
        senses = new ArrayList ();
        for (int i=0; i<senseTuples.length; i++)
          {
            Sense newSense;
            if (senseVerbFrames != null)
              newSense = 
                new Sense (this, i, senseTuples[i], senseVerbFrames[i]);
            else
              newSense = new Sense (this, i, senseTuples[i]);
            
            senses.add (newSense);
          }
      }
    return senses;
  }

  public Sense getSense (String form) throws WordNetException
  {
    int index = lookupSenseIndex (form);
    return getSense (index);
  }

  public int lookupSenseIndex (String form) throws WordNetException
  {
    List senses = getSenses ();
    for (int i=0; i<senses.size (); i++)
      {
        Sense s = (Sense)senses.get (i);
        if (s.getForm ().toLowerCase ().startsWith (form.toLowerCase ())) 
          return i;
      }
    throw new WordNetException ("Sense with name '"+form+
                                "' not available on synset "+tag ());
  }

  public Sense getSense (int idx) throws WordNetException
  {
    return (Sense)getSenses ().get (idx);
  }

  public List getPointers () throws WordNetException
  {
    if (pointers == null)
      {
        pointers = new LinkedList ();
        for (int i=0; i<pointerTuples.length; i++)
          {
            String[] pointerTuple = pointerTuples[i];
            pointers.add (new Pointer (getWordNet (), offset, pointerTuple));
          }
      }
    return pointers;
  }

  public List getPointers (String pointerType) throws WordNetException
  {
    if (pointerType == null) return getPointers ();

    List retval = new LinkedList ();

    List pointers = getPointers ();
    Iterator i = pointers.iterator ();
    while (i.hasNext ())
      {
        Pointer p = (Pointer)i.next ();
        if (p.getType ().equals (pointerType))
          retval.add (p);
      }

    return retval;
  }

  private int[] extractVerbFrames (int index, String[][] vfTuples)
  {
    // first, filter such that
    //    { x in vfTuples | x[2] in [0..index] }

    int[] r = new int[vfTuples.length];
    int size = 0;

    // --- filter on tuple[2], return list of tuple[1].
    for (int i=0; i<vfTuples.length; i++)
      {
        String[] tuple = vfTuples[i];
        int tuple2 = QuoteUtil.parseInt (tuple[2]);
        if (tuple2 >= 0 && tuple2 <= index)
          r[size++] = QuoteUtil.parseInt (tuple[1]);
      }

    // --- rightsize the array.
    if (size < r.length)
      {
        int[] retval = new int[size];
        System.arraycopy (r, 0, retval, 0, size);
        r = retval;
      }

    return r;
  }

  public String tag ()
  { 
    StringBuffer sb = new StringBuffer ();
    sb.append ("{");
    sb.append (pos);
    sb.append (": ");
    try {
      List senses = getSenses ();
      Iterator i=senses.iterator ();
      while (i.hasNext ())
        {
          Sense sense = (Sense)i.next ();
          sb.append (sense.getForm ());
          if (i.hasNext ())
            sb.append (", ");
        }
    } catch (WordNetException ex) {
      throw new WordNetRuntimeException ("Exception while getting tag", ex);
    }
    sb.append ("}");
           
    return sb.toString ();
  }

  public void dump (PrintWriter out)
  { 
    out.println (tag ());
    out.println ("gloss == "+gloss);

    try {
      List senses = getSenses ();
      Iterator i = senses.iterator ();
      while (i.hasNext ())
        {
          Sense sense = (Sense)i.next ();
          out.println (sense.tag ());
        }

      List pointers = getPointers ();
      i = pointers.iterator ();
      while (i.hasNext ())
        {
          Pointer pointer = (Pointer)i.next ();
          out.println (pointer.tag ());
        }
    } catch (WordNetException ex) {
      throw new WordNetRuntimeException ("Exception during printing.", ex);
    }
  }

}


class TokenGrouper {
  String[] tokens;
  int idx;
  public TokenGrouper (String[] _tokens, int start, int size)
  {
    tokens = new String[size];
    System.arraycopy (_tokens, start, tokens, 0, size);
    idx = 0;
  }

  public String nextToken ()
  {
    return tokens[idx++];
  }

  public String[][] getGroupSequence (int groupSize, int howManyGroups)
  {
    String[][] retval = new String[howManyGroups][];
    for (int i=0; i<howManyGroups; i++) retval[i] = getGroup (groupSize);
    return retval;
  }

  public String[] getGroup (int groupSize)
  {
    String[] retval = new String[groupSize];
    for (int j=0; j<groupSize; j++) retval[j] = nextToken ();
    return retval;
  }
}
