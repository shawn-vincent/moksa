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
public class com.svincent.moksa.prolog.system_prolog.set_input_1_alt1
  extends com.svincent.moksa.CompiledRule
{
  public com.svincent.moksa.prolog.system_prolog.set_input_1_alt1 ()
  {
  }
  
  public int getArity ()
  {
    return 1;
  }
  
  public com.svincent.moksa.Continuation invokeRule (com.svincent.moksa.Wam wam)
    throws com.svincent.moksa.PrologException
  {
    com.svincent.moksa.Continuation continuation;
    com.svincent.moksa.PrologFactory factory;
    com.svincent.moksa.Variable var_0;
    com.svincent.moksa.PrologEngine engine;
    com.svincent.moksa.CompoundTerm compound_1;
    engine = wam.getEngine();
    factory = engine.getFactory();
    //  *** test arg S_or_a
    //  --- S_or_a
    var_0 = factory.makeVariable((java.lang.String)"S_or_a");
    if (wam.badparm((int)0, (com.svincent.moksa.PrologTerm)var_0))
      return wam.Fail;
    continuation = wam.getContinuation();
    //  --- current_input(S_or_a)
    compound_1 = factory.makeCompoundTerm((java.lang.String)"current_input", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0});
    return com.svincent.moksa.Continuation.make((com.svincent.moksa.PrologEngine)engine, (com.svincent.moksa.PrologTerm)compound_1, (com.svincent.moksa.Continuation)continuation);
  }
  
  public java.lang.String getName ()
  {
    return "set_input/1";
  }
  
  public com.svincent.moksa.PrologTerm makeTerm (com.svincent.moksa.PrologFactory factory)
  {
    com.svincent.moksa.Variable var_0;
    com.svincent.moksa.CompoundTerm compound_3;
    com.svincent.moksa.CompoundTerm compound_2;
    com.svincent.moksa.CompoundTerm compound_1;
    //  --- S_or_a
    var_0 = factory.makeVariable((java.lang.String)"S_or_a");
    //  --- set_input(S_or_a)
    compound_1 = factory.makeCompoundTerm((java.lang.String)"set_input", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0});
    //  --- current_input(S_or_a)
    compound_2 = factory.makeCompoundTerm((java.lang.String)"current_input", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0});
    //  --- ':-'(set_input(S_or_a), current_input(S_or_a))
    compound_3 = factory.makeCompoundTerm((java.lang.String)":-", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_1, compound_2});
    return compound_3;
  }
  
}
