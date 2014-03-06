package com.svincent.moksa.prolog.system_prolog;

/*
 * This file was automatically generated by Smalljava.
 * 
 * Smalljava Vnull-null
 * Copyright (C) 1999 Shawn Vincent
 * http://www.svincent.com/moksa/
 * Smalljava is released under the GNU general public license
 * See http://www.gnu.org/copyleft/gpl.html for details.
 * 
 * File generated: Thursday, December 2, 1999 8:06:59 AM EST
 */
public class com.svincent.moksa.prolog.system_prolog.open_3
  extends com.svincent.moksa.CompiledRule
{
  public com.svincent.moksa.prolog.system_prolog.open_3 ()
  {
  }
  
  public int getArity ()
  {
    return 3;
  }
  
  public com.svincent.moksa.Continuation invokeRule (com.svincent.moksa.Wam wam)
    throws com.svincent.moksa.PrologException
  {
    com.svincent.moksa.Variable var_1;
    com.svincent.moksa.Variable var_0;
    com.svincent.moksa.Continuation continuation;
    com.svincent.moksa.CompoundTerm compound_4;
    com.svincent.moksa.PrologFactory factory;
    com.svincent.moksa.CompoundTerm compound_3;
    com.svincent.moksa.PrologEngine engine;
    com.svincent.moksa.Variable var_2;
    engine = wam.getEngine();
    factory = engine.getFactory();
    //  *** test arg Source_sink
    //  --- Source_sink
    var_0 = factory.makeVariable((java.lang.String)"Source_sink");
    if (wam.badparm((int)0, (com.svincent.moksa.PrologTerm)var_0))
      return wam.Fail;
    //  *** test arg Mode
    //  --- Mode
    var_1 = factory.makeVariable((java.lang.String)"Mode");
    if (wam.badparm((int)1, (com.svincent.moksa.PrologTerm)var_1))
      return wam.Fail;
    //  *** test arg Stream
    //  --- Stream
    var_2 = factory.makeVariable((java.lang.String)"Stream");
    if (wam.badparm((int)2, (com.svincent.moksa.PrologTerm)var_2))
      return wam.Fail;
    continuation = wam.getContinuation();
    //  --- []
    compound_3 = factory.makeAtom((java.lang.String)"[]");
    //  --- open(Source_sink, Mode, Stream, [])
    compound_4 = factory.makeCompoundTerm((java.lang.String)"open", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_1, var_2, compound_3});
    return com.svincent.moksa.Continuation.make((com.svincent.moksa.PrologEngine)engine, (com.svincent.moksa.PrologTerm)compound_4, (com.svincent.moksa.Continuation)continuation);
  }
  
  public java.lang.String getName ()
  {
    return "open/3";
  }
  
  public com.svincent.moksa.PrologTerm makeTerm (com.svincent.moksa.PrologFactory factory)
  {
    com.svincent.moksa.Variable var_2;
    com.svincent.moksa.Variable var_1;
    com.svincent.moksa.CompoundTerm compound_6;
    com.svincent.moksa.Variable var_0;
    com.svincent.moksa.CompoundTerm compound_5;
    com.svincent.moksa.CompoundTerm compound_4;
    com.svincent.moksa.CompoundTerm compound_3;
    //  --- Source_sink
    var_0 = factory.makeVariable((java.lang.String)"Source_sink");
    //  --- Mode
    var_1 = factory.makeVariable((java.lang.String)"Mode");
    //  --- Stream
    var_2 = factory.makeVariable((java.lang.String)"Stream");
    //  --- open(Source_sink, Mode, Stream)
    compound_3 = factory.makeCompoundTerm((java.lang.String)"open", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_1, var_2});
    //  --- []
    compound_4 = factory.makeAtom((java.lang.String)"[]");
    //  --- open(Source_sink, Mode, Stream, [])
    compound_5 = factory.makeCompoundTerm((java.lang.String)"open", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_1, var_2, compound_4});
    //  --- ':-'(open(Source_sink, Mode, Stream), open(Source_sink, Mode, Stream, []))
    compound_6 = factory.makeCompoundTerm((java.lang.String)":-", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_3, compound_5});
    return compound_6;
  }
  
}