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
 * Io.java
 *
 * The I/O subsystem and related builtin predicates.
 *
 */
package com.svincent.moksa;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.svincent.util.IoUtil;
import com.svincent.util.Util;

/**
 * Provides the Prolog environment with an ISO I/O subsystem.
 **/
public class Io extends WamObject {

  /** Modes */
  public static final int Read=1, Write=2, Append=3;

  PrologEngine engine;

  /**
   * Special atoms used by the IO subsystem..
   */
  PrologTerm 
    End_of_file,
    TypeFinder,
    EncodingFinder,
    AliasFinder,
    EofActionFinder,
    ForceFinder;

  /**
   * Aliases for streams.
   **/
  Map<String, StreamIdentifier> aliases = new HashMap<String, StreamIdentifier> ();

  /**
   * The current input stream.
   **/
  PrologInput in;

  /**
   * The current output stream.
   **/
  PrologOutput out;

  PrologInput standardInput;
  PrologOutput standardOutput;

  PrologTokenizer tokenizer;
  PrologParser parser;

  public Io (PrologEngine _engine, InputStream _in, OutputStream _out) 
  { 
    // --- store away the engine.
    engine = _engine;

    // --- build a tokenizer.
    tokenizer = new PrologTokenizer (engine.factory);

    // XXX be able to swap this out.
    parser = new IsoPrologParser (engine);

    // --- set up a bunch of PrologTerms that will be used to
    //   - implement various I/O builtins later on.
    PrologFactory factory = engine.factory;
    End_of_file = factory.makeAtom ("end_of_file");
    TypeFinder = 
      factory.makeCompoundTerm ("type", 
                                factory.makeTemporaryVariable ());
    EncodingFinder = 
      factory.makeCompoundTerm ("encoding", 
                                factory.makeTemporaryVariable ());
    AliasFinder = 
      factory.makeCompoundTerm ("alias", 
                                factory.makeTemporaryVariable ());
    EofActionFinder = 
      factory.makeCompoundTerm ("eof_action", 
                                factory.makeTemporaryVariable ());
    ForceFinder = 
      factory.makeCompoundTerm ("force", 
                                factory.makeTemporaryVariable ());

    // --- setup the standard streams.
    standardInput = 
      openInput ("standard_input", _in, engine.factory.makeEmptyList ());
    standardOutput = 
      openOutput ("standard_output", _out, engine.factory.makeEmptyList ());

    // --- the standard streams are also the default ones.
    in = standardInput;
    out = standardOutput;
  }

  /**
   * Open an URI, with a list of options.  If the options list is null
   * or empty, defaults are assumed.
   **/
  public PrologInput openInputUri (String uri) throws IOException
  {
    return openInputUri (uri, engine.factory.makeEmptyList ());
  }

  /**
   * Open an URI, with a list of options.  If the options list is null
   * or empty, defaults are assumed.
   **/
  public PrologInput openInputUri (String uri, CompoundTerm options)
    throws IOException
  { 
    // --- get the options as a list so we can read options out of it.
    CompoundTerm.ListWrapper list = options.asList ();
    if (list == null) 
      throw new IOException ("options ("+options.tag()+") are not a list.");

    // --- open the input stream.
    InputStream inputStream = IoUtil.openInputUri (uri);

    // --- based on the type, create a PrologReader or PrologInputStream
    String type = list.getOption (TypeFinder);

    PrologInput retval;
    if (type == null || type.equals ("text"))
      {
        // --- wrap the InputStream in a Reader.
        String encoding = list.getOption (EncodingFinder);
        Reader reader;
        if (encoding == null)
          reader = new InputStreamReader (inputStream);
        else
          reader = new InputStreamReader (inputStream, encoding);

        // --- make a PrologReader
        retval = openInput (uri, reader, options);
      }
    else if (type.equals ("binary"))
      {
        // --- make a PrologInputStream
        retval = openInput (uri, inputStream, options);
      }
    else 
      throw new IOException ("Unknown file type "+type);

    installAliases (retval, list);

    return retval;
  }

  public PrologInput openString (String s) throws IOException
  {
    return openString (s, engine.factory.makeEmptyList ());
  }

  /**
   * Open a String as an input source, with a list of options.  If the
   * options list is null or empty, defaults are assumed.
   *
   * This method allows parsing from a string, etc.
   **/
  public PrologInput openString (String s, CompoundTerm options)
    throws IOException
  { 
    // --- get the options as a list so we can read options out of it.
    CompoundTerm.ListWrapper list = options.asList ();
    if (list == null) 
      throw new IOException ("options ("+options.tag()+") are not a list.");

    // --- based on the type, create a PrologReader or PrologInputStream
    String type = list.getOption (TypeFinder);

    PrologInput retval;
    if (type == null || type.equals ("text"))
      {
        // --- It's a Reader.  Yay -- straightforward.
        Reader reader = new StringReader (s);

        // --- make a PrologReader
        // XXX maybe quote 's' here??
        retval = openInput ("string:"+s, reader, options);
      }
    else if (type.equals ("binary"))
      {
        // --- it's a binary stream.

        // --- get the bytes using some encoding.
        byte[] bytes;
        String encoding = list.getOption (EncodingFinder);
        if (encoding == null)
          bytes = s.getBytes ();
        else
          bytes = s.getBytes (encoding);

        // --- open a stream on it.
        InputStream inputStream = new ByteArrayInputStream (bytes);
          
        // --- make a PrologInputStream
        retval = openInput ("string:"+s, inputStream, options);
      }
    else 
      throw new IOException ("Unknown file type "+type);

    installAliases (retval, list);

    return retval;
  }

  public PrologReader openInput (String uri, Reader in, CompoundTerm options)
  { return new PrologReader (this, uri, options, in); }

  public PrologInputStream openInput (String uri, InputStream in, 
                                      CompoundTerm options)
  { return new PrologInputStream (this, uri, options, in); }


  public PrologOutput openOutputUri (String uri, boolean append)
    throws IOException
  {
    return openOutputUri (uri, engine.factory.makeEmptyList (), append);
  }

  public PrologOutput openOutputUri (String uri, CompoundTerm options, 
                                     boolean append)
    throws IOException
  {
    // --- get the options as a list so we can read options out of it.
    CompoundTerm.ListWrapper list = options.asList ();
    if (list == null) 
      throw new IOException ("options ("+options.tag()+") are not a list.");

    // --- open the output stream.
    OutputStream outputStream = IoUtil.openOutputUri (uri, append);

    // --- based on the type, create a PrologWriter or PrologOutputStreamStream
    PrologOutput retval;
    String type = list.getOption (TypeFinder);
    if (type == null || type.equals ("text"))
      {
        // --- wrap the InputStream in a Reader.
        String encoding = list.getOption (EncodingFinder);
        Writer writer;
        if (encoding == null)
          writer = new OutputStreamWriter (outputStream);
        else
          writer = new OutputStreamWriter (outputStream, encoding);

        // --- make a PrologReader
        retval = openOutput (uri, writer, options);
      }
    else if (type.equals ("binary"))
      {
        // --- make a PrologInputStream
        retval = openOutput (uri, outputStream, options);
      }
    else 
      throw new IOException ("Unknown file type "+type);

    installAliases (retval, list);

    return retval;
  }

  private void installAliases (StreamIdentifier stream, 
                               CompoundTerm.ListWrapper options)
  {
    Iterator<PrologTerm> i = options.iterator ();
    while (i.hasNext ())
      {
        PrologTerm option = (PrologTerm)i.next ();
        if (option.unifyWithoutBindings (AliasFinder))
          {
            String aliasName = 
              (String)((CompoundTerm)option).getSubterm (0).getName ();
            aliases.put (aliasName, stream);
          }
      }
  }

  public PrologWriter openOutput (String uri, Writer out, CompoundTerm options)
  { return new PrologWriter (this, uri, options, out); }

  public PrologOutputStream openOutput (String uri, OutputStream out, 
                                       CompoundTerm options)
  { return new PrologOutputStream (this, uri, options, out); }

  /**
   * <p>Retrieves a StreamIdentifier, given a PrologTerm. </p>
   *
   * <p>If the given term is itself a StreamIdentifier, just return it.
   * If it is an atom, and the given atom has been used as a stream
   * alias for a currently open stream, return that stream.
   * Otherwise, return null.</p>
   **/
  public StreamIdentifier getStream (PrologTerm term)
  {
    if (term.isStreamId ()) return (StreamIdentifier)term;

    if (term.isAtom ()) return (StreamIdentifier)aliases.get (term.getName ());

    return null;
  }
  

  /**
   * Parses the mode name, returns -1 on error.
   **/
  public static int parseModeName (String modeName)
  {
    if (modeName.equals ("read"))
      return Read;
    else if (modeName.equals ("write"))
      return Write;
    else if (modeName.equals ("append"))
      return Append;
    else
      return -1;
  }

  // -------------------------------------------------------------------------
  // ---- Predicates ---------------------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Opens the specified file.
   *
   * Note: supports the additional option <code>encoding(ENC)</code>,
   * where ENC must be a valid character encoding scheme identifier,
   * as defined by Java.
   **/
  public static class Open_4 extends Builtin.BuiltinRule {
    public String getName () { return "open/4"; }
    public int getArity () { return 4; }

    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameters.
      PrologTerm source_sink = wam.getRegister (0).deref ();
      PrologTerm modeAtom = wam.getRegister (1).deref ();
      PrologTerm stream = wam.getRegister (2).deref ();
      PrologTerm _options = wam.getRegister (3).deref ();

      // --- 
      if (source_sink.isVariable ())
        return wam.getFactory ().callThrowInstantiationError ();

      if (!modeAtom.isAtom ())
        return wam.getFactory ().callThrowTypeError ("atom", modeAtom);

      if (!_options.isList ())
        return wam.getFactory ().callThrowTypeError ("list", _options);

      CompoundTerm options = (CompoundTerm)_options;

      if (!stream.isVariable ())
        return wam.getFactory ().callThrowTypeError ("variable", modeAtom);

      // XXX more errors here.

      // --- retrieve the file uri.
      String uri = source_sink.getName ();

      // --- determine the open mode.
      String modeName = modeAtom.getName ();
      int mode = parseModeName (modeName);

      if (mode == -1)
        return wam.getFactory ().callThrowDomainError ("io_mode", modeAtom);
      
      // --- open the file.
      Io io = wam.getIo ();
      StreamIdentifier si;
      try {
        switch (mode)
          {
          case Read: 
            si = io.openInputUri (uri, options);
            break;
          case Write: 
            si = io.openOutputUri (uri, options, false);
            break;
          case Append: 
            si = io.openOutputUri (uri, options, true);
            break;
          default: Util.assertTrue (false, "Unexpected mode "+mode);
            return null;
          }
      } catch (IOException ex) {
        // XXX what about the message?  ISO doesn't let us send it!
        ex.printStackTrace ();
        return wam.getFactory ().
          callThrowPermissionError ("open", source_sink);
      }
  
      // --- examine options, deal with them appropriately.

      // --- XXX ?? we know that 'stream' is a variable.  Just bind.
      ((Variable)stream).bind (si);

      return wam.getContinuation ();
    }
  }

  /**
   * Closes the specified file.
   **/
  public static class Close_2 extends Builtin.BuiltinRule {
    public String getName () { return "close/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameters.
      PrologTerm source_sink = wam.getRegister (0).deref ();
      
      // XXX implement use of options.
      // PrologTerm options = wam.getRegister (1).deref ();

      // --- you can close anything you like that isn't open.
      if (source_sink.isAtom () && 
          !wam.getIo ().aliases.containsKey (source_sink.getName ()))
        return wam.getContinuation ();

      // --- you can also close closed streams.
      if (source_sink.isStreamId () && 
          !((StreamIdentifier)source_sink).isOpen ())
        return wam.getContinuation ();

      Io io = wam.getIo ();

      // --- if the stream is the standard input or output stream, it
      //   - succeeds (you can't really close them)
      if (source_sink == io.standardInput || 
          source_sink == io.standardOutput)
        return wam.getContinuation ();

      // --- reset the current input, output streams, if we're closing them.
      if (source_sink == io.in) io.in = io.standardInput;
      if (source_sink == io.out) io.out = io.standardOutput;

      // --- close the stream.
      try {
        ((StreamIdentifier)source_sink).close ();
      } catch (IOException ex) {
        // XXX huh?
        return wam.getFactory ().
          callThrowPermissionError ("close", source_sink);
      }

      // --- all done.
      return wam.getContinuation ();
    }
  }

  /**
   * get_stream_properties (S, Props)
   **/
  public static class Get_stream_properties_2 extends Builtin.BuiltinRule {
    public String getName () { return "get_stream_properties/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameters.
      PrologTerm _stream = wam.getRegister (0).deref ();
      PrologTerm _propsVar = wam.getRegister (1).deref ();

      StreamIdentifier stream = wam.getIo ().getStream (_stream);
      if (stream == null)
        return wam.getFactory ().
          callThrowDomainError ("stream_or_alias", _stream);

      if (!_propsVar.unify (stream.getOptions ())) return wam.Fail;

      // --- all done.
      return wam.getContinuation ();
    }
  }

  /**
   * Flushes the specified stream.
   **/
  public static class Flush_output_1 extends Builtin.BuiltinRule {
    public String getName () { return "flush_output/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameters.
      PrologTerm stream = wam.getRegister (0).deref ();

      // --- flush the stream.
      try {
        ((StreamIdentifier)stream).flush ();
      } catch (IOException ex) {
        // XXX huh?
        throw new PrologException ("XXX Help", ex);
      }

      // --- all done.
      return wam.getContinuation ();
    }
  }

  /**
   * Unifies the given variable with the current input
   **/
  public static class Current_input_1 extends Builtin.BuiltinRule {
    public String getName () { return "current_input/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameters.
      PrologTerm stream = wam.getRegister (0).deref ();

      if (!stream.unify (wam.getIo ().in)) return wam.Fail;

      return wam.getContinuation ();
    }
  }

  /**
   * Unifies the given variable with the current output
   **/
  public static class Current_output_1 extends Builtin.BuiltinRule {
    public String getName () { return "current_output/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameters.
      PrologTerm stream = wam.getRegister (0).deref ();

      if (!stream.unify (wam.getIo ().out)) return wam.Fail;

      return wam.getContinuation ();
    }
  }


  /**
   * Puts the given character to the given output stream.
   * 
   * If the stream is binary, write it as a byte.  If it's a text
   * stream, write it as a character.
   **/
  public static class Put_char_2 extends Builtin.BuiltinRule {
    public String getName () { return "put_char/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameters.
      PrologTerm _stream = wam.getRegister (0).deref ();
      PrologTerm character = wam.getRegister (1).deref ();

      if (!_stream.isStreamId ()) 
        return wam.getFactory ().
          callThrowDomainError ("stream_or_alias", _stream);

      StreamIdentifier stream = (StreamIdentifier)_stream;
      if (stream.isInput ())
        return wam.getFactory ().
          callThrowPermissionError ("output", "stream", _stream);

      PrologOutput outStream = (PrologOutput)stream;

      try {
        outStream.putChar (character.intValue ());
      } catch (IOException ex) {
        // help! XXX
        throw new PrologException ("XXX wrong thing to do.", ex);
      }

      return wam.getContinuation ();
    }
  }

  /**
   * Prints a newline to the given output stream.
   **/
  public static class Nl_1 extends Builtin.BuiltinRule {
    public String getName () { return "nl/1"; }
    public int getArity () { return 1; }

    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameters.
      PrologTerm _stream = wam.getRegister (0).deref ();

      if (_stream.isVariable ()) 
        return wam.getFactory ().callThrowInstantiationError ();

      StreamIdentifier stream = wam.getIo ().getStream (_stream);

      if (stream == null) 
        return wam.getFactory ().
          callThrowDomainError ("stream_or_alias", _stream);

      if (stream.isInput ())
        return wam.getFactory ().
          callThrowPermissionError ("output", "stream", _stream);

      PrologOutput outStream = (PrologOutput)stream;

      try {
        outStream.nl ();
      } catch (IOException ex) {
        // help! XXX
        throw new PrologException ("XXX wrong thing to do.", ex);
      }

      return wam.getContinuation ();
    }
  }


  /**
   * Gets a character from the given output stream.
   * 
   * If the stream is binary, reads a byte.  If it's a text stream,
   * reads a character.
   **/
  public static class Get_char_2 extends Builtin.BuiltinRule {
    public String getName () { return "get_char/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameters.
      PrologTerm _stream = wam.getRegister (0).deref ();
      PrologTerm character = wam.getRegister (1).deref ();

      if (_stream.isVariable ()) 
        return wam.getFactory ().callThrowInstantiationError ();

      StreamIdentifier stream = wam.getIo ().getStream (_stream);

      if (stream == null) 
        return wam.getFactory ().
          callThrowDomainError ("stream_or_alias", _stream);

      if (stream.isOutput ())
        return wam.getFactory ().
          callThrowPermissionError ("input", "stream", _stream);

      if (!stream.isOpen ())
        return wam.getFactory ().callThrowExistenceError ("stream", _stream);

      PrologInput inStream = (PrologInput)stream;

      PrologTerm c;
      try {
        c = inStream.readCharTerm ();
      } catch (IOException ex) {
        // help! XXX
        throw new PrologException ("XXX wrong thing to do.", ex);
      }

      if (!character.unify (c))
        return wam.Fail;

      return wam.getContinuation ();
    }
  }


  /**
   * Gets a Prolog token from the given output stream.
   **/
  public static class Get_prolog_token_2 extends Builtin.BuiltinRule {
    public String getName () { return "get_prolog_token/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException 
    {
      // --- get the parameters.
      PrologTerm _stream = wam.getRegister (0).deref ();
      PrologTerm token = wam.getRegister (1).deref ();

      if (_stream.isVariable ()) 
        return wam.getFactory ().callThrowInstantiationError ();

      StreamIdentifier stream = wam.getIo ().getStream (_stream);

      if (stream == null) 
        return wam.getFactory ().
          callThrowDomainError ("stream_or_alias", _stream);

      if (stream.isOutput ())
        return wam.getFactory ().
          callThrowPermissionError ("input", "stream", _stream);

      if (!stream.isOpen ())
        return wam.getFactory ().
          callThrowExistenceError ("stream", _stream);

      PrologInput inStream = (PrologInput)stream;

      PrologTerm term;
      try {
        term = wam.getIo ().tokenizer.readToken (inStream);
      } catch (IOException ex) {
        // help! XXX
        throw new PrologException ("XXX wrong thing to do.", ex);
      }

      if (!token.unify (term))
        return wam.Fail;

      return wam.getContinuation ();
    }
  }

  /**
   * Writes its argument to the given output stream.
   **/
  public static class Write_2 extends Builtin.BuiltinRule {
    public String getName () { return "write/2"; }
    public int getArity () { return 2; }

    public Continuation invokeRule (Wam wam) throws PrologException
    {
      // --- get the parameter.
      PrologTerm stream = wam.getRegister (0).deref ();
      PrologTerm x = wam.getRegister (1).deref ();

      String tag = x.tag ();

      try {
        ((PrologOutput)stream).write (tag);
      } catch (IOException ex) {
        throw new PrologException ("XXX", ex);
      }

      return wam.getContinuation ();
    }
  }


  // -------------------------------------------------------------------------
  // ---- Various sorts of I/O Identifiers -----------------------------------
  // -------------------------------------------------------------------------


  /**
   * An identifier for a currently open input or output stream.
   **/
  public abstract static class StreamIdentifier extends PrologTerm {
    Io io;
    String uri;
    CompoundTerm options;
    boolean open;

    public StreamIdentifier (Io _io, String _uri, CompoundTerm _options) 
    { 
      super (_io.engine); 
      io = _io; uri = _uri; options = _options; open = true; 
    }

    public String getUri () { return uri; }
    public CompoundTerm getOptions () { return options; }

    public boolean isStreamId () { return true; }

    public abstract int getMode ();

    public boolean isOpen () { return open; }

    public boolean isInput () { return false; }
    public boolean isOutput () { return false; }

    public void close () throws IOException { open = false; }
    public abstract void flush () throws IOException;

    // XXX
    public String getName () { return uri; }

    public boolean unify (PrologTerm _that, boolean bindVars) 
    {
      if (_that.isVariable ()) return _that.unify (this, bindVars);
      if (!_that.isStreamId ()) return false;

      StreamIdentifier that = (StreamIdentifier)_that;
      if (!(this.getMode () == that.getMode ())) return false;
      if (!this.getUri ().equals (that.getUri ())) return false;
      if (!this.getOptions ().unify (that.getOptions ())) return false;

      return true;
    }

    public PrologTerm clonePrologTerm (Map<PrologTerm,PrologTerm> objs) { return this; }
  }

  /**
   * An identifier for an output stream.
   **/
  public abstract static class PrologOutput extends StreamIdentifier {
    int mode;

    /**
     * 
     **/
    PrologOutput (Io _io, String _fileName, CompoundTerm _options) 
    {
      super (_io, _fileName, _options);
      mode = Write; // XXX what about Append? 
    }

    public int getMode () { return mode; }
    public boolean isOutput () { return true; }

    public abstract void close () throws IOException;
    public abstract void flush () throws IOException;
    public abstract void putChar (int c) throws IOException;
    public abstract void write (String s) throws IOException;
    public abstract void nl () throws IOException;
  }

  public static class PrologWriter extends PrologOutput {
    Writer out;

    PrologWriter (Io _io, String _fileName, CompoundTerm _options, Writer _out)
    { super (_io, _fileName, _options); out = _out; }

    public void close () throws IOException { out.close (); out = null; }
    public void flush () throws IOException { out.flush (); }
    public void putChar (int c) throws IOException { out.write ((char)c); }
    public void write (String s) throws IOException { out.write (s); }
    public void nl () throws IOException
    {
      // XXX should be system-specific newline.
      out.write ('\n');
    }
  }

  public static class PrologOutputStream extends PrologOutput {
    OutputStream out;

    PrologOutputStream (Io _io, String _fileName, CompoundTerm _options, 
                        OutputStream _out)
    { super (_io, _fileName, _options); out = _out; }

    public void close () throws IOException { out.close (); out = null; }
    public void flush () throws IOException { out.flush (); }
    public void putChar (int c) throws IOException { out.write ((byte)c); }
    public void write (String s) throws IOException 
    { 
      // XXX specify encoding here somehow???
      byte[] bytes = s.getBytes ();
      out.write (bytes); 
    }
    public void nl () throws IOException
    {
      // XXX should be system-specific newline.
      out.write ('\n');
    }
  }

  /**
   * An identifier for an input stream.
   **/
  public abstract static class PrologInput extends StreamIdentifier {
    static final int InitialPushbackBufferSize = 4;

    // --- pushback buffer, initially empty.
    int[] buffer = new int[InitialPushbackBufferSize];
    int bufferPos = 0;

    /**
     * Opens the file.
     **/
    public PrologInput (Io _io, String _fileName, CompoundTerm _options) 
    { super (_io, _fileName, _options); }

    public int getMode () { return Read; }
    public boolean isInput () { return true; }

    public abstract int readCharImpl () throws IOException;
    public abstract void close () throws IOException; 

    /**
     * Returns the next character in the stream as a PrologTerm
     * (either an Integer or an Atom: 'end_of_file').
     **/
    public PrologTerm readCharTerm () throws IOException
    {
      int c = readChar ();
      if (c == -1) return io.End_of_file;
      else return io.engine.factory.makeInteger (c);
    }

    /**
     * Returns the character which will be returned from the next call
     * to 'readChar'. <p>
     **/
    public int peekChar () throws IOException
    {
      int peek = readChar ();
      pushbackChar (peek);
      return peek;
    }

    /**
     * Peeks <code>n</code> chars ahead. <p>
     *
     * peekChar (1) == peekChar () <p>
     **/
    public int peekChar (int n) throws IOException
    {
      if (n == 1) return peekChar ();

      // --- XXX this could be optimized for 2, 3, 4, 5 to not create obj.
      int[] peeks = new int[n];

      // --- read to desired character
      for (int i=0; i<n; i++)
        peeks[i] = readChar ();

      // --- store away the desired character.
      int retval = peeks[n-1];

      // --- unread everything.
      for (int i=n-1; i>=0; i--)
        pushbackChar (peeks[i]);

      return retval;
    }

    /**
     * If 'c' is the next character in the stream, read and discard
     * it.  If it is not, do not read it, and throw an exception.
     **/
    public void consume (int c) throws IOException
    {
      if (peekChar () != c) 
        throw new IOException ("Expected '"+(char)c+
                               "', but got "+(char)peekChar ());

      readChar ();
    }

    /**
     * Returns 'true' iff the character 'c' is 'n' characters ahead in
     * this stream.
     **/
    public boolean lookahead (int n, int c) throws IOException
    { return peekChar (n) == c; }

    /**
     * Pushes a character back into the stream.  This character will
     * be read again later. <p>
     *
     * XXX export this functionality to Prolog. <p>
     **/
    public void pushbackChar (int c) throws IOException
    {
      bufferPush (c);
    }

    /**
     * Reads a character from this stream.
     **/
    public int readChar () throws IOException
    {
      if (!bufferEmpty ())
        {
          int c = bufferPop ();
          return c;
        }

      return readCharImpl ();
    }

    public void flush () throws IOException { /*NOP*/ }

    void bufferPush (int c)
    {
      if (bufferPos >= buffer.length)
        {
          int[] newBuffer = new int[buffer.length*2];
          System.arraycopy (buffer, 0, newBuffer, 0, buffer.length);
          buffer = newBuffer;
        }
      buffer[bufferPos++] = c;
    }

    boolean bufferEmpty () { return bufferPos == 0; }

    int bufferPop ()
    {
      if (bufferPos == 0) throw new EmptyStackException ();
      return buffer[--bufferPos];
    }
  }

  /**
   * A wrapper for a Java Reader.
   **/
  public class PrologReader extends PrologInput {
    Reader in;

    public PrologReader (Io _io, String _fileName, CompoundTerm _options, 
                         Reader _in)
    { super (_io, _fileName, _options); in = _in; }

    public int readCharImpl () throws IOException
    { return in.read (); }

    public void close () throws IOException
    {
      in.close ();
      in = null;
    }
  }

  /**
   * A wrapper for a Java InputStream.
   **/
  public class PrologInputStream extends PrologInput {
    InputStream in;

    public PrologInputStream (Io _io, String _fileName, CompoundTerm _options, 
                              InputStream _in)
    { super (_io, _fileName, _options); in = _in; }

    public int readCharImpl () throws IOException
    { return in.read (); }

    public void close () throws IOException
    {
      in.close ();
      in = null;
    }
  }

}
