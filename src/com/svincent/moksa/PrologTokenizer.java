/*
 * 
 * Copyright (C) 1999  Shawn P. Vincent (svincent@svincent.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * -------------------------------------------------------------------------
 *
 * PrologTokenizer.java
 *
 *
 */
package com.svincent.moksa;

import java.io.*;
import java.util.*;

import com.svincent.moksa.Io.PrologInput;
import com.svincent.util.*;

/*
  TODO:
     - rethink how exception handling should work here... PrologException maybe?
 */

/**
 * <p>This class encapsulates the tokenization of Prolog. </p>
 *
 * <p>A PrologTokenizer has a single method, 'getToken', which returns a
 * PrologTerm.  This term is one of: </p>
 *
 * <ul>
 *    <li><code>atom ('name')</code> - an atom token (i.e. -
 *    <code>foo</code>, <code>a32</code>, <code>'$'</code>).</li>
 *
 *    <li><code>variable ('Name')</code> - a variable token (i.e. -
 *    <code>Foo</code>, <code>_</code>, and <code>A32</code>).</li>
 *
 *    <li><code>integer (value)</li> - an integral value token (i.e. -
 *    <code>42</code>, <code>-3</code>, <code>7</code>).</li>
 *
 *    <li><code>float (value)</li> - a floating-point value token
 *    (i.e. - <code>3.141592</code>, <code>42.0</code>).</li>
 *
 *    <li><code>string (value)</li> - a string value token (i.e. -
 *    <code>"Four score and seven years ago..."</code>,
 *    <code>"hello"</code>).</li>
 *
 *    <li><code>open</li> - an open parenthesis token (i.e. -
 *    <code>(</code>).</li>
 *
 *    <li><code>close</li> - a close parenthesis token (i.e. -
 *    <code>)</code>).</li>
 *
 *    <li><code>open_list</li> - an open square token (i.e. -
 *    <code>[</code>).</li>
 *
 *    <li><code>close_list</li> - a close square token (i.e. -
 *    <code>]</code>).</li>
 *
 *    <li><code>open_curly</li> - an open curly token (i.e. -
 *    <code>{</code>).</li>
 *
 *    <li><code>close_curly</li> - a close curly token (i.e. -
 *    <code>}</code>).</li> 
 *
 *    <li><code>head_tail_separator</li> - a list head/tail seperator
 *    token (i.e. - <code>|</code>).</li>
 *
 *    <li><code>end</li> - an end token (i.e. -
 *    <code>.</code>).</li>
 *
 *    <li><code>end_of_file</li> - an end of file token, returned when
 *    we run out of tokens on the input stream. </li>
 *
 *    <li><code>error(reason)</li> - an error occurred (i.e. -
 *    <code>.</code>).</li>
 *
 * </ul>
 *
 * <p>All strings, etc, have quote marks stripped and escape sequences
 * expanded, so there is no need to do these things later on.</p>
 *
 * <p>I believe that MoksaProlog will ship with two complete parsers:
 * one written in Prolog, which supports the entire Prolog language,
 * and one written completely in Java, which supports a subset. </p>
 *
 * <p>Both of these parsers, I believe, shall be accessable to Prolog
 * users, through various and sundry APIs. </p>
 **/
public class PrologTokenizer extends WamObject {

  PrologFactory factory;
  
  CompoundTerm Open;
  CompoundTerm Close;
  CompoundTerm OpenList;
  CompoundTerm CloseList;
  CompoundTerm OpenCurly;
  CompoundTerm CloseCurly;
  CompoundTerm HtSep;
  //CompoundTerm Comma;
  CompoundTerm End;
  CompoundTerm EndOfFile;

  /** A map of pushback Stack objects keyed by stream. */
  Map<PrologInput, Stack<CompoundTerm>> pushbackStacksByStream = new WeakHashMap<PrologInput, Stack<CompoundTerm>> ();

  // -------------------------------------------------------------------------
  // ---- Constructors: Setup ------------------------------------------------
  // -------------------------------------------------------------------------

  public PrologTokenizer (PrologFactory _factory)
  { 
    factory = _factory; 
    Open = factory.makeAtom ("open");
    Close = factory.makeAtom ("close");
    OpenList = factory.makeAtom ("open_list");
    CloseList = factory.makeAtom ("close_list");
    OpenCurly = factory.makeAtom ("open_curly");
    CloseCurly = factory.makeAtom ("close_curly");
    HtSep = factory.makeAtom ("head_tail_separator");
    //Comma = factory.makeAtom ("comma");
    End = factory.makeAtom ("end");
    EndOfFile = factory.makeAtom ("end_of_file");
  }

  // -------------------------------------------------------------------------
  // ---- Read Tokens --------------------------------------------------------
  // -------------------------------------------------------------------------

  // XXX rework to actually push tokens back onto the stream, perhaps???

  public void pushback (Io.PrologInput in, CompoundTerm token)
  { 
    if (!token.getName ().equals ("end_of_file"))
      pushbackPush (in, token); 
  }

  public CompoundTerm peek (Io.PrologInput in) throws IOException
  {
    CompoundTerm tok = readToken (in);
    pushback (in, tok);
    return tok;
  }

  public void consume (Io.PrologInput in, CompoundTerm expected)
    throws IOException, PrologParseException
  { 
    CompoundTerm token = readToken (in);
    try {
      if (!token.unify (expected, false))
        throw new PrologParseException ("Expected token "+expected.tag ()+
                                        ", got token "+token.tag ());
    } catch (PrologParseException ex) {
      throw ex;
    }
  }

  /**
   * Read a single token from 'in'.  This is the only public API on
   * PrologTokenizer: all the rest of its methods exist solely to
   * service it. <p>
   **/
  public CompoundTerm readToken (Io.PrologInput in)
    throws IOException
  {
    if (!pushbackIsEmpty (in)) return pushbackPop (in);

    // --- strip preceeding whitespace.
    consumeWhitespace (in);

    // --- do different sorts of things based on single-char lookahead.
    int c = in.peekChar ();
    switch (c)
      {
        // EOF
      case -1: return EndOfFile;

        // "string"
      case '"': 
        return factory.makeCompoundTerm 
          ("string", 
           parseCompoundTerm (readDoubleQuotedString (in)));

        // `string`
      case '`': 
        return factory.makeCompoundTerm 
          ("string", 
           parseCompoundTerm (readBackquotedString (in)));

        // various single-char tokens.
      case '(': in.consume ('('); return Open;
      case ')': in.consume (')'); return Close;

      case ']': in.consume (']'); return CloseList;

      case '}': in.consume ('}'); return CloseCurly;
        //case ',': in.consume (','); return Comma;
      case '|': in.consume ('|'); return HtSep;

        // number
      case '0': case '1': case '2': case '3': case '4': 
      case '5': case '6': case '7': case '8': case '9':
        return readNumber (in);

        // --- these special 2-char names can fall through to 
        //   - They are also magic one-char names.
      case '[': 
        if (in.peekChar (2) == ']') 
          { 
            in.consume ('['); in.consume (']'); 
            return factory.makeCompoundTerm ("name", factory.makeEmptyList()); 
          }
        else
          { in.consume ('['); return OpenList; }
      case '{': 
        if (in.peekChar (2) == '}') 
          { 
            in.consume ('{'); in.consume ('}'); 
            return 
              factory.makeCompoundTerm ("name", factory.makeEmptyCurlies ());
          }
        else
          { in.consume ('{'); return OpenCurly; }

      default:
        if (isVariableStart ((char)c))
          // variable
          return readVariable (in);
        else
          // name - everything else.
          return readPossibleName (in);
      }
  }

  // -------------------------------------------------------------------------
  // ---- Read Strings -------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Consumes a string token surrounded by double-quotes.
   * Automatically expands all escape sequences, etc, in the process
   * of doing so.
   **/
  String readDoubleQuotedString (Io.PrologInput in)
    throws IOException
  {
    StringBuffer out = new StringBuffer ();

    in.consume ('"');

    // --- read some number of double-quoted items
    int c = readDoubleQuotedChar (in);
    while (c != -2)
      {
        out.append ((char)c);
        c = readDoubleQuotedChar (in);
      }

    in.consume ('"');

    return out.toString ();
  }

  /**
   * Similar to readDoubleQuotedString, but reads a string with
   * back-quotes (`) surrounding it.
   **/
  String readBackquotedString (Io.PrologInput in)
    throws IOException
  {
    StringBuffer out = new StringBuffer ();

    in.consume ('`');

    // --- read some number of double-quoted items
    int c = readBackQuotedChar (in);
    while (c != -2)
      {
        out.append ((char)c);
        c = readBackQuotedChar (in);
      }

    in.consume ('`');

    return out.toString ();
  }

  /**
   * From something of the form
   *   "Shawn"
   * Make something of the form
   *   [83,104,97,119,110]
   */
  public CompoundTerm parseCompoundTerm (String value)
  {
    char[] chars = value.toCharArray ();
    CompoundTerm retval = factory.makeEmptyList ();
    for (int i=chars.length-1; i>=0; i--)
      retval = 
        factory.makeCompoundTerm (".", factory.makeInteger ((int)chars[i]), retval);
    return retval;
  }

  // -------------------------------------------------------------------------
  // ---- Read Numbers -------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Can read an INT, or FLOAT token. </p>
   *
   * <p>Note that negative constants are expressions in Prolog. </p>
   **/
  CompoundTerm readNumber (Io.PrologInput in)
    throws IOException
  {
    int c = in.peekChar ();

    // --- special case handling for '0': might be different sort of
    //   - constant, or might be '0' (the start of a decimal constant)
    if (c == '0')
      {
        // --- get the next char.
        c = in.peekChar (2);

        switch (c)
          {
          case '\'':
            in.consume ('0');
            in.consume ('\'');
            // --- character constant.
            return factory.makeCompoundTerm 
              ("integer", factory.makeInteger (readSingleQuotedChar (in)));
          case 'b':
            in.consume ('0');
            in.consume ('b');
            return factory.makeCompoundTerm ("integer", 
                                         factory.makeInteger (readInt (in, 2)));
          case 'o':
            in.consume ('0');
            in.consume ('o');
            return factory.makeCompoundTerm ("integer", 
                                         factory.makeInteger (readInt (in, 8)));
          case 'x':
            in.consume ('0');
            in.consume ('x');
            return factory.makeCompoundTerm ("integer", 
                                         factory.makeInteger (readInt (in, 16)));

          case '0': case '1': case '2': case '3': case '4': 
          case '5': case '6': case '7': case '8': case '9':
          }
      }

    // --- now we have to eat
    //   -     dec-token (.dec-token (E[+-]dec-token)?)?

    // --- read the decimal constant.
    StringBuffer out = new StringBuffer ();
    out.append (readIntToken (in, 10));
    
    // --- if it's a float, do float stuff.
    c = in.peekChar ();
    if (c == '.')
      {
        c = in.peekChar (2);
        if (is0to9 ((char)c))
          {
            // --- do float stuff.
            in.consume ('.');
            out.append ('.');

            out.append (readIntToken (in, 10));

            // --- now we might have the whole 'E' thing.
            c = in.peekChar ();
            if (c == 'e' || c == 'E')
              {
                in.consume ((char)c);
                out.append ('E');
                c = in.readChar ();
                if (c != '-' && c != '+')
                  throw new IOException ("Parsing float "+out.toString ()+
                                         ", expected [-+], got "+(char)c);
                out.append ((char)c);

                out.append (readIntToken (in, 10));
              }
            // --- return a new float.

            double dvalue;
            try {
              dvalue = Double.parseDouble (out.toString ());
            } catch (NumberFormatException ex) {
              Util.assertTrue 
                (false, "Unable to parse "+out.toString ()+" as double");
              return null;
            }

            return factory.makeCompoundTerm ("float", factory.makeFloat (dvalue));
          }
      }

    // --- not a float: just an integer.
    int value;
    try {
      value = Integer.parseInt (out.toString ());
    } catch (NumberFormatException ex) {
      Util.assertTrue (false, "Unable to parse "+out.toString()+" as base-10 int");
      return null;
    }

    return factory.makeCompoundTerm ("integer", factory.makeInteger (value));
  }

  /**
   * <p>Parses an integral value from the input stream in the given
   * base, and even parses it into a number for the lucky caller. </p>
   **/
  int readInt (Io.PrologInput in, int base) throws IOException
  {
    String tok = readIntToken (in, base);
    try {
      return Integer.parseInt (tok, base);
    } catch (NumberFormatException ex) {
      Util.assertTrue 
        (false, "Unable to parse "+tok+" as a base-"+base+" integer");
      return 0;
    }
  }
  
  /**
   * <p>Parses an integral value in the given base from the input
   * stream and returns it as a string.</p>
   *
   * @see #readInt
   **/
  String readIntToken (Io.PrologInput in, int base) 
    throws IOException
  {
    StringBuffer out = new StringBuffer ();
    int c = in.peekChar ();

    // --- read the first character.
    if (!isBaseNDigit ((char)c, base)) 
      throw new IOException ("Expected base-"+base+" digit, got "+(char)c);
    in.consume ((char)c);
    out.append ((char)c);
    c = in.peekChar ();

    // --- read the rest of the token
    while (isBaseNDigit ((char)c, base))
      {
        in.consume ((char)c);
        out.append ((char)c);
        c = in.peekChar ();
      }

    return out.toString ();
  }

  // -------------------------------------------------------------------------
  // ---- Read Variables -----------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Reads a VAR token.  Expects an alphanumeric sequence starting
   * with '_' or a capital letter.
   **/
  CompoundTerm readVariable (Io.PrologInput in)
    throws IOException
  {
    StringBuffer out = new StringBuffer ();
    int c = in.peekChar ();

    // --- read the first character.
    if (!isVariableStart ((char)c)) throw new IOException ();
    in.consume ((char)c);
    out.append ((char)c);
    c = in.peekChar ();

    // --- read the rest of the token
    while (isAlphanumeric ((char)c) || c == '_')
      {
        in.consume ((char)c);
        out.append ((char)c);
        c = in.peekChar ();
      }
    return factory.makeCompoundTerm ("variable", factory.makeAtom (out.toString ()));
  }

  // -------------------------------------------------------------------------
  // ---- Read Names ---------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * <p>Can read an END, HT_SEP, or NAME token. </p>
   *
   * <p>The problem with Prolog is that anything that isn't something
   * else is automagically a name (more or less).  Thus, this creepy
   * method. </p>
   **/
  CompoundTerm readPossibleName (Io.PrologInput in)
    throws IOException
  {
    int c = in.peekChar ();

    // --- certain tokens have special meaning sometimes.
    switch (c)
      {
        // the wierd single-char (solo) names.
      case ',':
        in.consume (',');
        return factory.makeCompoundTerm ("name", factory.makeAtom (","));
      case ';':
        in.consume (';');
        return factory.makeCompoundTerm ("name", factory.makeAtom (";"));
      case '!':
        in.consume ('!');
        return factory.makeCompoundTerm ("name", factory.makeAtom ("!"));
      case '.':
        {
          // either '.FOO' graphic token, or '.   \n' end token.
          String graphicToken = parseGraphicToken (in);

          if (graphicToken.equals ("."))
            {
              // --- wierdo special END token handling.
              //   - 
              consumeNonNLWhitespace (in);
              c = in.peekChar ();
              if (c == -1 || c == '\n')
                {
                  return End;
                }
            }

          return factory.makeCompoundTerm ("name", factory.makeAtom (graphicToken));
        }
      case '\'':
        // quoted token.
        return factory.makeCompoundTerm 
          ("name", factory.makeAtom (parseQuotedToken (in)));
      default:
        // either an alphanumeric name or graphic name
        if (isSmallLetter ((char)c))
          return factory.makeCompoundTerm 
            ("name", factory.makeAtom (parseIdentifierToken (in)));
        else if (isGraphicTokenChar ((char)c))
          return factory.makeCompoundTerm 
            ("name", factory.makeAtom (parseGraphicToken (in)));
        
      }
    throw new IOException ("Unexpected character in name token "+(char)c);
  }

  /**
   * This is the sort of name we all know and love.  Starts with a
   * lower-case letter, continues with alphanumerics or '_'.
   **/
  String parseIdentifierToken (Io.PrologInput in)
    throws IOException
  {
    StringBuffer out = new StringBuffer ();
    int c = in.peekChar ();

    // --- read the first character.
    if (!isSmallLetter ((char)c)) 
      throw new IOException ("Expected lower-case letter, got "+(char)c);
    in.consume ((char)c);
    out.append ((char)c);
    c = in.peekChar ();

    // --- read the rest of the token
    while (isAlphanumeric ((char)c) || c == '_')
      {
        in.consume ((char)c);
        out.append ((char)c);
        c = in.peekChar ();
      }
    return out.toString ();
  }

  /**
   * <p>This is the wierdo name token in Prolog.  Things like '+' and
   * '->' are graphic tokens. </p>
   *
   * <p>Graphic and name tokens cannot mix in Prolog.  Thus, foo+foo
   * is 3 tokens. </p>
   **/
  String parseGraphicToken (Io.PrologInput in) 
    throws IOException
  {
    // --- XXX can return empty tokens.  fix.
    StringBuffer out = new StringBuffer ();
    int c = in.peekChar ();
    while (isGraphicTokenChar ((char)c))
      {
        in.consume ((char)c);
        out.append ((char)c);
        c = in.peekChar ();
      }
    return out.toString ();
  }

  /**
   * <p>A quoted token for those abusive folks who want more power in
   * their names than is allowed by identifiers+quoted names. </p>
   **/
  String parseQuotedToken (Io.PrologInput in) throws IOException
  {
    StringBuffer out = new StringBuffer ();
    int c = in.peekChar ();
    if (c != '\'') 
      throw new IOException ("Expected single quote, got "+(char)c);
    in.consume ('\'');
    
    // --- read some number of single-quoted items
    c = readSingleQuotedChar (in);
    while (c != -2)
      {
        out.append ((char)c);
        c = readSingleQuotedChar (in);
      }

    // --- read final single-quote.
    c = in.peekChar ();
    if (c != '\'') 
      throw new IOException ("Expected single quote, got "+(char)c);
    in.consume ('\'');

    return out.toString ();
  }

  // -------------------------------------------------------------------------
  // ---- Escape Sequences ---------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Read a char inside a single-quoted string.
   **/
  int readSingleQuotedChar (Io.PrologInput in) throws IOException
  { return readQuotedChar (in, '\''); }

  /**
   * Read a char inside a double-quoted string.
   **/
  int readDoubleQuotedChar (Io.PrologInput in) throws IOException
  { return readQuotedChar (in, '"'); }

  /**
   * Read a char inside a back-quoted string.
   **/
  int readBackQuotedChar (Io.PrologInput in) throws IOException
  { return readQuotedChar (in, '`'); }

  /**
   * <p>Reads a quoted character (from within a quoted token).  Pass
   * in the character that's quoting the string, and it'll return the
   * char it sees. </p>
   *
   * <p>Returns -2 on EOS (plus, doesn't consume final quote char)</p>
   **/
  int readQuotedChar (Io.PrologInput in, char quoteType)
    throws IOException
  {
    int c = in.peekChar ();
    switch (c)
      {
      case '`': case '"': case '\'':
        if (c == quoteType)
          {
            c = in.peekChar (2);
            if (c == quoteType)
              {
                in.consume ((char)c);
                in.consume ((char)c);
                return (char)c;
              }
            else
              {
                return -2;
              }
          }
        else
          {
            in.consume ((char)c);
            return (char)c;
          }

      case '\\':
        return readEscapeSequence (in);

      default:
        if (isGraphic ((char)c) || isAlphanumeric ((char)c) ||
            isSolo ((char)c) || c == ' ')
          {
            in.consume ((char)c);
            return (char)c;
          }
        else
          return -2;
      }
  }

  /**
   * <p>Read a Prolog string escape sequence.  Some of these are
   * similar to Java-style sequences, others aren't. </p>
   **/
  char readEscapeSequence (Io.PrologInput in) throws IOException
  {
    in.consume ('\\');

    int c = in.peekChar ();

    switch (c)
      {
      case 'a': in.consume ('a'); return '\u0007'; // bell/alert
      case 'b': in.consume ('b'); return '\b'; // backspace
      case 'f': in.consume ('f'); return '\b'; // form feed
      case 'n': in.consume ('n'); return '\n'; // linefeed
      case 'r': in.consume ('r'); return '\r'; // carriage return
      case 't': in.consume ('t'); return '\t'; // horizontal tab
      case 'v': in.consume ('v'); return '\u000b'; // vertical tab
      case '\'': in.consume ('\''); return '\'';
      case '\"': in.consume ('\"'); return '\"';
      case '0': case '1': case '2': case '3': case '4': 
      case '5': case '6': case '7': 
        {
          // --- octal escape sequence.
          char retval = (char)readInt (in, 8);
          in.consume ('\\');
          return retval;
        }
      case 'x': 
        {
          // --- hex escape sequence
          in.consume ('x');
          char retval = (char)readInt (in, 16);
          in.consume ('\\');
          return retval;
        }
      default:
        throw new IOException 
          ("Char '"+(char)c+
           "' is not allowed in a Prolog string escape sequence.");
      }
  }


  // -------------------------------------------------------------------------
  // ---- Whitespace ---------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Consume all pending whitespace tokens in the stream.
   **/
  void consumeWhitespace (Io.PrologInput in) throws IOException
  {
    int c = in.peekChar ();
    boolean stillWhitespace = true;
    while (stillWhitespace)
      {
        if (c == -1) stillWhitespace = false;
        else if (Character.isWhitespace ((char)c)) in.consume ((char)c);
        else if (c == '%') consumeSLComment (in);
        else if (c == '/' && in.lookahead (2, '*')) consumeMLComment (in);
        else stillWhitespace = false;
        c = in.peekChar ();
      }
  }

  /**
   * Consume all pending whitespace tokens in the stream, except for
   * newline.  This is a creepy method needed to distinguish END
   * tokens from '.' NAME tokens.
   **/
  void consumeNonNLWhitespace (Io.PrologInput in) throws IOException
  {
    int c = in.peekChar ();
    boolean stillWhitespace = true;
    while (stillWhitespace)
      {
        if (c == -1) stillWhitespace = false;
        else if (Character.isWhitespace ((char)c) && c != '\n') 
          in.consume ((char)c);
        else if (c == '%') consumeSLComment (in);
        else if (c == '/' && in.lookahead (2, '*')) consumeMLComment (in);
        else stillWhitespace = false;
        c = in.peekChar ();
      }
  }

  /**
   * Consume a single-line comment. ('%' to end-of-line)
   **/
  void consumeSLComment (Io.PrologInput in) throws IOException
  {
    in.consume ('%');
    int c = in.peekChar ();
    while (c != -1 && c != '\n')
      {
        in.consume ((char)c);
        c = in.peekChar ();
      }
    if (c == '\n') in.consume ('\n');
  }

  /** 
   * Consume a multi-line comment. ('/*' to '* /', just like in C).
   * (sorry about the spaces between '*' and '/'.  The text you are
   * currently reading is written IN a comment, and so to type the
   * end-of-comment token would mean ending the comment.  Rare problem
   * to have....  :)
   **/
  void consumeMLComment (Io.PrologInput in) 
    throws IOException
  {
    in.consume ('/');
    in.consume ('*');
    while (true)
      {
        int c = in.peekChar ();
        switch (c)
          {
          case -1: 
            return;
          case '*':
            if (in.lookahead (2, '/')) 
              {
                in.consume ('*');
                in.consume ('/');
                return;
              }
            else
              in.consume ('*');
            break;
          default:
            in.consume (c);
            break;
          }
      }
  }

  // -------------------------------------------------------------------------
  // ---- Utility Methods ----------------------------------------------------
  // -------------------------------------------------------------------------

  boolean isGraphicTokenChar (char c)
  { return c == '\\' || isGraphic (c); }

  /**
   * Return true iff 'c' is a normal graphic character.
   **/
  boolean isGraphic (char c)
  { 
    switch (c)
      {
      case '#': case '$': case '&': case '*': case '+': case '-': case '.':
      case '/': case ':': case '<': case '=': case '>': case '?': case '@':
      case '^': case '~':
        return true;
      default:
        return false;
      }
  }

  /**
   * Return true iff 'c' is a solo character: normally appears by
   * itself as a single-char token, but represents itself in a quoted
   * name.
   **/
  boolean isSolo (char c)
  { 
    switch (c)
      {
      case '!': case '(': case ')': case ',': case ';': case '[': case ']':
      case '{': case '}': case '|': case '%':
        return true;
      default:
        return false;
      }
  }

  /** Return true iff 'c' is allowed to start a variable */
  boolean isVariableStart (char c)
  {return c == '_' || (Character.isLetter (c) && Character.isUpperCase (c));}

  /** Return true iff 'c' is a letter and is lower-case */
  boolean isSmallLetter (char c)
  { return Character.isLetter (c) && Character.isLowerCase (c); }

  /** Return true iff 'c' is an alphanumeric character */
  boolean isAlphanumeric (char c) 
  { return Character.isLetterOrDigit (c); }

  /** Return true iff 'c' is in the range '0' to '9' */
  boolean is0to9 (char c) 
  { return c >= '0' && c <= '9'; }

  /** supports up to base-36 */
  static final int[] radix = new int[256];
  static {
    for (int i=0; i<256; i++)
      {
        if (i>='0' && i<='9') radix[i] = i-'0';
        else if (i>='a' && i<='z') radix[i] = i-'a'+10;
        else if (i>='A' && i<='Z') radix[i] = i-'A'+10;
        else radix[i] = -1;
      }
  }

  /**
   * Returns true iff 'c' is a base-'base' digit.
   **/
  boolean isBaseNDigit (char c, int base) 
  {
    int maxRadix = radix[c]; 
    return maxRadix != -1 && maxRadix < base; 
  }

  // -------------------------------------------------------------------------
  // ---- Pushback Stack Operations ------------------------------------------
  // -------------------------------------------------------------------------

  private Stack<CompoundTerm> getPushbackStack (Io.PrologInput in)
  {
    Stack<CompoundTerm> retval = (Stack<CompoundTerm>)pushbackStacksByStream.get (in);
    if (retval == null)
      { retval = new Stack<CompoundTerm> (); pushbackStacksByStream.put (in, retval); }
    return retval;
  }

  private boolean pushbackIsEmpty (Io.PrologInput in)
  { return getPushbackStack (in).isEmpty (); }

  private void pushbackPush (Io.PrologInput in, CompoundTerm tok)
  { getPushbackStack (in).push (tok); }

  private CompoundTerm pushbackPop (Io.PrologInput in)
  { return (CompoundTerm)getPushbackStack (in).pop (); }

  // -------------------------------------------------------------------------
  // ---- Test Code ----------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Test code.
   **/
  public static void main (String[] args) throws Exception
  {
    PrologEngine engine = new PrologEngine ();
    Io.PrologInput in = engine.io.standardInput;

    PrologTokenizer tokenizer = new PrologTokenizer (engine.factory);

    CompoundTerm tok = tokenizer.readToken (in);
    while (tok != tokenizer.EndOfFile)
      {
        Util.out.println ("Got token "+tok.tag ());
        tok = tokenizer.readToken (in);
      }
  }
}
