package com.svincent.moksa.prolog.parser_prolog;

/*
 * This file was automatically generated by Smalljava.
 * 
 * Smalljava Vnull-null
 * Copyright (C) 1999 Shawn Vincent
 * http://www.svincent.com/moksa/
 * Smalljava is released under the GNU general public license
 * See http://www.gnu.org/copyleft/gpl.html for details.
 * 
 * File generated: Thursday, December 2, 1999 8:07:23 AM EST
 */
public class com.svincent.moksa.prolog.parser_prolog.calculate_prolog_prefix_subpriority_3_alt1
  extends com.svincent.moksa.CompiledRule
{
  public com.svincent.moksa.prolog.parser_prolog.calculate_prolog_prefix_subpriority_3_alt1 ()
  {
  }
  
  public int getArity ()
  {
    return 3;
  }
  
  public com.svincent.moksa.Continuation invokeRule (com.svincent.moksa.Wam wam)
    throws com.svincent.moksa.PrologException
  {
    com.svincent.moksa.Continuation continuation;
    com.svincent.moksa.Variable var_2;
    com.svincent.moksa.Variable var_1;
    com.svincent.moksa.PrologFactory factory;
    com.svincent.moksa.PrologEngine engine;
    com.svincent.moksa.CompoundTerm compound_3;
    com.svincent.moksa.CompoundTerm compound_0;
    engine = wam.getEngine();
    factory = engine.getFactory();
    //  *** test arg fy
    //  --- fy
    compound_0 = factory.makeAtom((java.lang.String)"fy");
    if (wam.badparm((int)0, (com.svincent.moksa.PrologTerm)compound_0))
      return wam.Fail;
    //  *** test arg P
    //  --- P
    var_1 = factory.makeVariable((java.lang.String)"P");
    if (wam.badparm((int)1, (com.svincent.moksa.PrologTerm)var_1))
      return wam.Fail;
    //  *** test arg AP
    //  --- AP
    var_2 = factory.makeVariable((java.lang.String)"AP");
    if (wam.badparm((int)2, (com.svincent.moksa.PrologTerm)var_2))
      return wam.Fail;
    continuation = wam.getContinuation();
    //  --- is(AP, P)
    compound_3 = factory.makeCompoundTerm((java.lang.String)"is", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_2, var_1});
    return com.svincent.moksa.Continuation.make((com.svincent.moksa.PrologEngine)engine, (com.svincent.moksa.PrologTerm)compound_3, (com.svincent.moksa.Continuation)continuation);
  }
  
  public java.lang.String getName ()
  {
    return "calculate_prolog_prefix_subpriority/3";
  }
  
  public com.svincent.moksa.PrologTerm makeTerm (com.svincent.moksa.PrologFactory factory)
  {
    com.svincent.moksa.Variable var_2;
    com.svincent.moksa.Variable var_1;
    com.svincent.moksa.CompoundTerm compound_5;
    com.svincent.moksa.CompoundTerm compound_4;
    com.svincent.moksa.CompoundTerm compound_3;
    com.svincent.moksa.CompoundTerm compound_0;
    //  --- fy
    compound_0 = factory.makeAtom((java.lang.String)"fy");
    //  --- P
    var_1 = factory.makeVariable((java.lang.String)"P");
    //  --- AP
    var_2 = factory.makeVariable((java.lang.String)"AP");
    //  --- calculate_prolog_prefix_subpriority(fy, P, AP)
    compound_3 = factory.makeCompoundTerm((java.lang.String)"calculate_prolog_prefix_subpriority", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_0, var_1, var_2});
    //  --- is(AP, P)
    compound_4 = factory.makeCompoundTerm((java.lang.String)"is", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_2, var_1});
    //  --- ':-'(calculate_prolog_prefix_subpriority(fy, P, AP), is(AP, P))
    compound_5 = factory.makeCompoundTerm((java.lang.String)":-", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_3, compound_4});
    return compound_5;
  }
  
}
