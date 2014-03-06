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
 * File generated: Thursday, December 2, 1999 8:07:00 AM EST
 */
public class com.svincent.moksa.prolog.system_prolog.memberOf_2_alt1
  extends com.svincent.moksa.CompiledRule
{
  public com.svincent.moksa.prolog.system_prolog.memberOf_2_alt1 ()
  {
  }
  
  public int getArity ()
  {
    return 2;
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
    //  *** test arg X
    //  --- X
    var_0 = factory.makeVariable((java.lang.String)"X");
    if (wam.badparm((int)0, (com.svincent.moksa.PrologTerm)var_0))
      return wam.Fail;
    //  *** test arg [_ | Y]
    //  --- _
    var_1 = factory.makeVariable((java.lang.String)"_");
    //  --- Y
    var_2 = factory.makeVariable((java.lang.String)"Y");
    //  --- [_ | Y]
    compound_3 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, var_2});
    if (wam.badparm((int)1, (com.svincent.moksa.PrologTerm)compound_3))
      return wam.Fail;
    continuation = wam.getContinuation();
    //  --- memberOf(X, Y)
    compound_4 = factory.makeCompoundTerm((java.lang.String)"memberOf", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_2});
    return com.svincent.moksa.Continuation.make((com.svincent.moksa.PrologEngine)engine, (com.svincent.moksa.PrologTerm)compound_4, (com.svincent.moksa.Continuation)continuation);
  }
  
  public java.lang.String getName ()
  {
    return "memberOf/2";
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
    //  --- X
    var_0 = factory.makeVariable((java.lang.String)"X");
    //  --- _
    var_1 = factory.makeVariable((java.lang.String)"_");
    //  --- Y
    var_2 = factory.makeVariable((java.lang.String)"Y");
    //  --- [_ | Y]
    compound_3 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, var_2});
    //  --- memberOf(X, [_ | Y])
    compound_4 = factory.makeCompoundTerm((java.lang.String)"memberOf", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, compound_3});
    //  --- memberOf(X, Y)
    compound_5 = factory.makeCompoundTerm((java.lang.String)"memberOf", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_2});
    //  --- ':-'(memberOf(X, [_ | Y]), memberOf(X, Y))
    compound_6 = factory.makeCompoundTerm((java.lang.String)":-", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_4, compound_5});
    return compound_6;
  }
  
}
