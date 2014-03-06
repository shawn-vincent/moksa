/*
 * Based on the WordNet interface by Oliver Steele (steele@cs.brandeis.edu)
 * Source: http://www.cs.brandeis.edu/~steele/sources/wordnet.py
 *  Home Page: http://www.cs.brandeis.edu/~steele/sources/wordnet-python.html
 *
 * Copyright (c) 1998-1999 by Oliver Steele.  Use this however you like,
 * so long as you preserve the attribution and label your changes.
 *
 * This Java version is
 *   Copyright (c) 1999 by Shawn Vincent.
 *
 */
package com.svincent.wordnet;

import java.io.*;
import java.util.*;

import com.svincent.util.*;


/**
 *
 */
public class WordNet implements WordNetConstants {

  Map posNormalizationTable;

  WordDictionary nouns;
  WordDictionary verbs;
  WordDictionary adjectives;
  WordDictionary adverbs;
  Map wordDictionaries;

  Map pointerTypeTable;

  public WordNet () throws WordNetException
  {
    initializePOSTables ();
  }

  public String normalizePOS (String pos) throws WordNetException
  {
    String normalized = (String)posNormalizationTable.get (pos);
    if (normalized == null)
      throw new WordNetException ("'"+pos+"' is not a part of speech type.");
    return normalized;
  }

  public String getPointerType (String pointerCode) throws WordNetException
  {
    String pointerType = (String)pointerTypeTable.get (pointerCode);
    if (pointerType == null)
      throw new WordNetException ("'"+pointerCode+
                                  "' is not a valid pointer type code.");
    return pointerType;
  }

  // -------------------------------------------------------------------------
  // ---- Loading things from the database -----------------------------------
  // -------------------------------------------------------------------------

  public Synset getSynset (String pos, long offset) throws WordNetException
  { return getWordDictionary (pos).getSynset (offset); }

  public Word getWord (String pos, String form) throws WordNetException
  { return getWordDictionary (pos).getWord (form); }

  public Word getNoun (String form) throws WordNetException
  { return nouns.getWord (form); }

  public Word getVerb (String form) throws WordNetException
  { return verbs.getWord (form); }

  public Word getAdjective (String form) throws WordNetException
  { return adjectives.getWord (form); }

  public Word getAdverb (String form) throws WordNetException
  { return adverbs.getWord (form); }

  public Word getNounOrNull (String form) throws WordNetException
  { 
    try { return nouns.getWord (form); } 
    catch (WordNetException ex) { return null; }
  }

  public Word getVerbOrNull (String form) throws WordNetException
  { 
    try { return verbs.getWord (form); } 
    catch (WordNetException ex) { return null; }
  }

  public Word getAdjectiveOrNull (String form) throws WordNetException
  { 
    try { return adjectives.getWord (form); } 
    catch (WordNetException ex) { return null; }
  }

  public Word getAdverbOrNull (String form) throws WordNetException
  { 
    try { return adverbs.getWord (form); } 
    catch (WordNetException ex) { return null; }
  }

  protected WordDictionary getWordDictionary (String pos)
  { return (WordDictionary)wordDictionaries.get (pos); }

  // -------------------------------------------------------------------------
  // ---- Test Code ----------------------------------------------------------
  // -------------------------------------------------------------------------

  public static void main (String[] args) throws WordNetException
  {
    WordNet wordNet = new WordNet ();

    Word word;

    word = wordNet.getNounOrNull (args[0]);
    if (word != null) { Util.out.println ("Noun"); renderSenses (word); }

    word = wordNet.getVerbOrNull (args[0]);
    if (word != null) { Util.out.println ("Verb"); renderSenses (word); }

    word = wordNet.getAdjectiveOrNull (args[0]);
    if (word != null) { Util.out.println ("Adjective"); renderSenses (word); }

    word = wordNet.getAdverbOrNull (args[0]);
    if (word != null) { Util.out.println ("Adverb"); renderSenses (word); }
  }

  public static void renderSenses (Word word) throws WordNetException
  {
    List synsets = word.getSynsets ();

    Iterator i = synsets.iterator ();
    int idx = 0;
    while (i.hasNext ())
      {
        Synset synset = (Synset)i.next ();
        Util.out.println ("  "+(idx++)+". "+synset.tag ()+" -- ("+
                          synset.getGloss ()+")");
      }
    Util.out.println ();
  }

  // -------------------------------------------------------------------------
  // ---- Initialize internal tables -----------------------------------------
  // -------------------------------------------------------------------------

  protected void initializePOSTables () throws WordNetException
  {
    initializePOSNormalizationTable ();
    initializeWordDictionaries ();
    initializePointerTypeTable ();
  }
  
  protected void initializePointerTypeTable ()
  {
    pointerTypeTable = new HashMap ();
    pointerTypeTable.put ("!", ANTONYM); 
    pointerTypeTable.put ("@", HYPERNYM); 
    pointerTypeTable.put ("~", HYPONYM);
    pointerTypeTable.put ("=", ATTRIBUTE); 
    pointerTypeTable.put ("^", ALSO_SEE); 
    pointerTypeTable.put ("*", ENTAILMENT); 
    pointerTypeTable.put (">", CAUSE);
    pointerTypeTable.put ("$", VERB_GROUP);
    pointerTypeTable.put ("#m", MEMBER_MERONYM); 
    pointerTypeTable.put ("#s", SUBSTANCE_MERONYM); 
    pointerTypeTable.put ("#p", PART_MERONYM);
    pointerTypeTable.put ("%m", MEMBER_HOLONYM); 
    pointerTypeTable.put ("%s", SUBSTANCE_HOLONYM); 
    pointerTypeTable.put ("%p", PART_HOLONYM);
    pointerTypeTable.put ("&", SIMILAR); 
    pointerTypeTable.put ("<", PARTICIPLE_OF); 
    pointerTypeTable.put ("\\", PERTAINYM); 
  }
    
  protected void initializePOSNormalizationTable ()
  {
    Map lowerMap = new HashMap ();
    lowerMap.put ("noun", "noun");
    lowerMap.put ("n", "noun");
    lowerMap.put ("n.", "noun");

    lowerMap.put ("verb", "verb");
    lowerMap.put ("v", "verb");
    lowerMap.put ("v.", "verb");

    lowerMap.put ("adjective", "adjective");
    lowerMap.put ("adj", "adjective");
    lowerMap.put ("adj.", "adjective");
    lowerMap.put ("a", "adjective");
    lowerMap.put ("s", "adjective");

    lowerMap.put ("adverb", "adverb");
    lowerMap.put ("adv", "adverb");
    lowerMap.put ("adv.", "adverb");
    lowerMap.put ("r", "adverb");

    posNormalizationTable = new HashMap ();
    Iterator i = lowerMap.keySet ().iterator ();
    while (i.hasNext ())
      {
        String key = (String)i.next ();
        String value = (String)lowerMap.get (key);
        posNormalizationTable.put (key, value);
        posNormalizationTable.put (key.toUpperCase (), value);
      }
  }

  protected void initializeWordDictionaries () throws WordNetException
  {
    // --- make all the primary word dictionaries.
    nouns = new WordDictionary (this, NOUN, "noun");
    verbs = new WordDictionary (this, VERB, "verb");
    adjectives = new WordDictionary (this, ADJECTIVE, "adj");
    adverbs = new WordDictionary (this, ADVERB, "adv");

    // --- make the dictionaries map.
    wordDictionaries = new HashMap ();
    wordDictionaries.put (NOUN, nouns);
    wordDictionaries.put (VERB, verbs);
    wordDictionaries.put (ADJECTIVE, adjectives);
    wordDictionaries.put (ADVERB, adverbs);
  }
}
