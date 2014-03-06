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
 * File generated: Thursday, December 2, 1999 8:07:16 AM EST
 */
public class com.svincent.moksa.prolog.parser_prolog.tokenize_prolog_term_3
  extends com.svincent.moksa.CompiledRule
{
  public com.svincent.moksa.prolog.parser_prolog.tokenize_prolog_term_3 ()
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
    com.svincent.moksa.CompoundTerm compound_7;
    com.svincent.moksa.CompoundTerm compound_6;
    com.svincent.moksa.CompoundTerm compound_5;
    com.svincent.moksa.Continuation continuation;
    com.svincent.moksa.CompoundTerm compound_4;
    com.svincent.moksa.PrologFactory factory;
    com.svincent.moksa.PrologEngine engine;
    com.svincent.moksa.Variable var_3;
    com.svincent.moksa.Variable var_2;
    engine = wam.getEngine();
    factory = engine.getFactory();
    //  *** test arg S
    //  --- S
    var_0 = factory.makeVariable((java.lang.String)"S");
    if (wam.badparm((int)0, (com.svincent.moksa.PrologTerm)var_0))
      return wam.Fail;
    //  *** test arg Tokens
    //  --- Tokens
    var_1 = factory.makeVariable((java.lang.String)"Tokens");
    if (wam.badparm((int)1, (com.svincent.moksa.PrologTerm)var_1))
      return wam.Fail;
    //  *** test arg Vars
    //  --- Vars
    var_2 = factory.makeVariable((java.lang.String)"Vars");
    if (wam.badparm((int)2, (com.svincent.moksa.PrologTerm)var_2))
      return wam.Fail;
    continuation = wam.getContinuation();
    //  --- Next
    var_3 = factory.makeVariable((java.lang.String)"Next");
    //  --- get_prolog_token(S, Next)
    compound_4 = factory.makeCompoundTerm((java.lang.String)"get_prolog_token", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_3});
    //  --- []
    compound_5 = factory.makeAtom((java.lang.String)"[]");
    //  --- prolog_term_to_tokens(Next, S, Tokens, [])
    compound_6 = factory.makeCompoundTerm((java.lang.String)"prolog_term_to_tokens", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_3, var_0, var_1, compound_5});
    //  --- ','(get_prolog_token(S, Next), prolog_term_to_tokens(Next, S, Tokens, []))
    compound_7 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_4, compound_6});
    return com.svincent.moksa.Continuation.make((com.svincent.moksa.PrologEngine)engine, (com.svincent.moksa.PrologTerm)compound_7, (com.svincent.moksa.Continuation)continuation);
  }
  
  public java.lang.String getName ()
  {
    return "tokenize_prolog_term/3";
  }
  
  public com.svincent.moksa.PrologTerm makeTerm (com.svincent.moksa.PrologFactory factory)
  {
    com.svincent.moksa.Variable var_1;
    com.svincent.moksa.Variable var_0;
    com.svincent.moksa.CompoundTerm compound_9;
    com.svincent.moksa.CompoundTerm compound_8;
    com.svincent.moksa.CompoundTerm compound_7;
    com.svincent.moksa.CompoundTerm compound_6;
    com.svincent.moksa.CompoundTerm compound_5;
    com.svincent.moksa.CompoundTerm compound_3;
    com.svincent.moksa.Variable var_4;
    com.svincent.moksa.Variable var_2;
    //  --- S
    var_0 = factory.makeVariable((java.lang.String)"S");
    //  --- Tokens
    var_1 = factory.makeVariable((java.lang.String)"Tokens");
    //  --- Vars
    var_2 = factory.makeVariable((java.lang.String)"Vars");
    //  --- tokenize_prolog_term(S, Tokens, Vars)
    compound_3 = factory.makeCompoundTerm((java.lang.String)"tokenize_prolog_term", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_1, var_2});
    //  --- Next
    var_4 = factory.makeVariable((java.lang.String)"Next");
    //  --- get_prolog_token(S, Next)
    compound_5 = factory.makeCompoundTerm((java.lang.String)"get_prolog_token", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_4});
    //  --- []
    compound_6 = factory.makeAtom((java.lang.String)"[]");
    //  --- prolog_term_to_tokens(Next, S, Tokens, [])
    compound_7 = factory.makeCompoundTerm((java.lang.String)"prolog_term_to_tokens", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_4, var_0, var_1, compound_6});
    //  --- ','(get_prolog_token(S, Next), prolog_term_to_tokens(Next, S, Tokens, []))
    compound_8 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_5, compound_7});
    //  --- ':-'(tokenize_prolog_term(S, Tokens, Vars), ','(get_prolog_token(S, Next), prolog_term_to_tokens(Next, S, Tokens, [])))
    compound_9 = factory.makeCompoundTerm((java.lang.String)":-", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_3, compound_8});
    return compound_9;
  }
  
}
