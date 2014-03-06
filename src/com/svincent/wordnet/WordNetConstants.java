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

public interface WordNetConstants {

  public static final String NOUN = "noun";
  public static final String VERB = "verb";
  public static final String ADJECTIVE = "adjective";
  public static final String ADVERB = "adverb";
  public static final String[] PartsOfSpeech = {NOUN, VERB, ADJECTIVE, ADVERB};

  public static final String ANTONYM = "antonym";
  public static final String HYPERNYM = "hypernym";
  public static final String HYPONYM = "hyponym";
  public static final String ATTRIBUTE = "attribute";
  public static final String ALSO_SEE = "also see";
  public static final String ENTAILMENT = "entailment";
  public static final String CAUSE = "cause";
  public static final String VERB_GROUP = "verb group";
  public static final String MEMBER_MERONYM = "member meronym";
  public static final String SUBSTANCE_MERONYM = "substance meronym";
  public static final String PART_MERONYM = "part meronym";
  public static final String MEMBER_HOLONYM = "member holonym";
  public static final String SUBSTANCE_HOLONYM = "substance holonym";
  public static final String PART_HOLONYM = "part holonym";
  public static final String SIMILAR = "similar";
  public static final String PARTICIPLE_OF = "participle of";
  public static final String PERTAINYM = "pertainym";

  public static final String[] POINTER_TYPES = 
    {ANTONYM, HYPERNYM, HYPONYM, ATTRIBUTE, ALSO_SEE,
     ENTAILMENT, CAUSE, VERB_GROUP,
     MEMBER_MERONYM, SUBSTANCE_MERONYM, PART_MERONYM,
     MEMBER_HOLONYM, SUBSTANCE_HOLONYM, PART_HOLONYM, SIMILAR,
     PARTICIPLE_OF, PERTAINYM};

  public static final String ATTRIBUTIVE = "attributive";
  public static final String PREDICATIVE = "predicative";
  public static final String IMMEDIATE_POSTNOMINAL = "immediate postnominal";
  public static final String[] ADJECTIVE_POSITIONS = 
    {ATTRIBUTIVE, PREDICATIVE, IMMEDIATE_POSTNOMINAL, null};

  public static final String[] VERB_FRAME_STRINGS = {
    null,
    "Something %s",
    "Somebody %s",
    "It is %sing",
    "Something is %sing PP",
    "Something %s something Adjective/Noun",
    "Something %s Adjective/Noun",
    "Somebody %s Adjective",
    "Somebody %s something",
    "Somebody %s somebody",
    "Something %s somebody",
    "Something %s something",
    "Something %s to somebody",
    "Somebody %s on something",
    "Somebody %s somebody something",
    "Somebody %s something to somebody",
    "Somebody %s something from somebody",
    "Somebody %s somebody with something",
    "Somebody %s somebody of something",
    "Somebody %s something on somebody",
    "Somebody %s somebody PP",
    "Somebody %s something PP",
    "Somebody %s PP",
    "Somebody's (body part) %s",
    "Somebody %s somebody to INFINITIVE",
    "Somebody %s somebody INFINITIVE",
    "Somebody %s that CLAUSE",
    "Somebody %s to somebody",
    "Somebody %s to INFINITIVE",
    "Somebody %s whether INFINITIVE",
    "Somebody %s somebody into V-ing something",
    "Somebody %s something with something",
    "Somebody %s INFINITIVE",
    "Somebody %s VERB-ing",
    "It %s that CLAUSE",
    "Something %s INFINITIVE"};
}
