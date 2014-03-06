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
public class WordNetUtil {
  public static final File WnSearchFile = new File ("d:/apps/WordNet16/dict");

  /**
   * XXX works only on NT.
   */
  public static File getDataFile (String fileNameRoot)
  { return new File (WnSearchFile, fileNameRoot+".dat"); }

  /**
   * XXX works only on NT.
   */
  public static File getIndexFile (String fileNameRoot)
  { return new File (WnSearchFile, fileNameRoot+".idx"); }

  /**
   * Tokenize the given dictionary database line.
   */
  public static String[] tokenize (String line)
  {
    StringTokenizer tokenizer = new StringTokenizer (line, " ");
    int tokenCount = tokenizer.countTokens ();
    String[] retval = new String[tokenCount];

    for (int i=0; i<tokenCount; i++) retval[i] = tokenizer.nextToken ();

    return retval;
  }

  /**
   * Returns the line at the given offset.
   */
  public static String lineAt (RandomAccessFile file, long offset)
    throws IOException
  {
    file.seek (offset);
    return readLine (file);
  }

  /**
   * Read a line of text, up to and including a newline character or EOF
   * XXX gross: doesn't read Unicode files, etc.
   */
  public static String readLine (RandomAccessFile file) throws IOException
  {
    StringBuffer out = new StringBuffer ();
    try {
      char c = (char)file.readByte ();
      out.append (c);
      // XXX gross: deal with various newline conventions here.
      while (c != '\n')
        {
          c = (char)file.readByte ();
          out.append (c);
        }
    } catch (EOFException ex) {
      // ignore, and return what we got before EOF.
    }
    return out.toString ();
  }

  public static int calcNumLines (RandomAccessFile file) throws IOException
  {
    BufferedReader r = new BufferedReader (new FileReader (file.getFD ()));
    int lines = 0;
    String line = r.readLine ();
    while (line != null)
      {
        lines++;
        line = r.readLine ();
      }
    return lines;
  }

  /**
   *
   */
  public static String binarySearchFile (RandomAccessFile file, String key)
    throws IOException
  { return binarySearchFile (file, key, new HashMap (), -1); }

  /**
   *
   */
  public static String binarySearchFile (RandomAccessFile file, String key, 
                                         Map cache, int cacheDepth)
    throws IOException
  {
    key = key + ' ';
    int keyLen = key.length ();
    long start = 0;
    long end = file.length ();

    OffsetLine offsetLine;
    
    int currentDepth = 0;
    while (start < end)
      {
        long middle = (start + end) / 2;
        // --- try to get the line from the cache.
        offsetLine = (OffsetLine)cache.get (new Long (middle));
        if (offsetLine == null)
          {
            // --- if it's not there, cache it.
            file.seek (Math.max (0, middle-1));
            readLine (file);
            offsetLine = new OffsetLine (file.getFilePointer (),
                                         readLine (file));
            if (currentDepth < cacheDepth)
              cache.put (new Long (middle), offsetLine);
          }

        long offset = offsetLine.offset;
        String line = offsetLine.line;

        // --- do the binary search stuff.
        if (offset > end)
          {
            if (end == middle - 1)
              throw new IOException ("Infinite loop");
            end = middle - 1;
          }
        else if (line.startsWith (key) && line.charAt (keyLen + 1) == ' ')
          return line;
        else 
          {
            int compare = line.compareTo (key);
            if (compare > 0) // line > key
              {
                if (end == middle - 1)
                  throw new IOException ("Infinite loop");
                end = middle - 1;
              }
            else if (compare < 0) // line < key
              {
                start = offset + line.length () - 1;
              }
          }
        currentDepth ++;
      }

    // --- did not find it.
    return null;
  }

  public static class OffsetLine {
    long offset;
    String line;
    
    public OffsetLine (long _offset, String _line) 
    { offset = _offset; line = _line; }
  }

  public static void main (String[] args)
    throws Exception
  {
    RandomAccessFile f = new RandomAccessFile (args[0], "r");
    while (true)
      {
        String l = readLine (f);
        if (l.length () == 0) break;
        Util.out.println (l);
      }
  }
}
