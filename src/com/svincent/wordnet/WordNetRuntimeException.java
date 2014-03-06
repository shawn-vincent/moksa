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

public class WordNetRuntimeException extends NestableRuntimeException {
  public WordNetRuntimeException () { super (); }
  public WordNetRuntimeException (String msg) { super (msg); }
  public WordNetRuntimeException (String msg, Throwable ex) { super (msg,ex); }
}
