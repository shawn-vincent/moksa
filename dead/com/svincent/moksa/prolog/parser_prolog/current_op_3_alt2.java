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
 * File generated: Thursday, December 2, 1999 8:07:26 AM EST
 */
public class com.svincent.moksa.prolog.parser_prolog.current_op_3_alt2
  extends com.svincent.moksa.CompiledRule
{
  public com.svincent.moksa.prolog.parser_prolog.current_op_3_alt2 ()
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
    com.svincent.moksa.PrologFactory factory;
    com.svincent.moksa.PrologEngine engine;
    com.svincent.moksa.CompoundTerm compound_2;
    com.svincent.moksa.CompoundTerm compound_1;
    com.svincent.moksa.WamInteger int_0;
    engine = wam.getEngine();
    factory = engine.getFactory();
    //  *** test arg 1200
    //  --- 1200
    int_0 = factory.makeInteger((int)1200);
    if (wam.badparm((int)0, (com.svincent.moksa.PrologTerm)int_0))
      return wam.Fail;
    //  *** test arg fx
    //  --- fx
    compound_1 = factory.makeAtom((java.lang.String)"fx");
    if (wam.badparm((int)1, (com.svincent.moksa.PrologTerm)compound_1))
      return wam.Fail;
    //  *** test arg :-
    //  --- :-
    compound_2 = factory.makeAtom((java.lang.String)":-");
    if (wam.badparm((int)2, (com.svincent.moksa.PrologTerm)compound_2))
      return wam.Fail;
    continuation = wam.getContinuation();
    return continuation;
  }
  
  public java.lang.String getName ()
  {
    return "current_op/3";
  }
  
  public com.svincent.moksa.PrologTerm makeTerm (com.svincent.moksa.PrologFactory factory)
  {
    com.svincent.moksa.CompoundTerm compound_3;
    com.svincent.moksa.CompoundTerm compound_2;
    com.svincent.moksa.CompoundTerm compound_1;
    com.svincent.moksa.WamInteger int_0;
    //  --- 1200
    int_0 = factory.makeInteger((int)1200);
    //  --- fx
    compound_1 = factory.makeAtom((java.lang.String)"fx");
    //  --- :-
    compound_2 = factory.makeAtom((java.lang.String)":-");
    //  --- current_op(1200, fx, :-)
    compound_3 = factory.makeCompoundTerm((java.lang.String)"current_op", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {int_0, compound_1, compound_2});
    return compound_3;
  }
  
}
