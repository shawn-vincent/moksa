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
 * File generated: Thursday, December 2, 1999 8:07:18 AM EST
 */
public class com.svincent.moksa.prolog.parser_prolog.parse_prolog_term_2_alt1
  extends com.svincent.moksa.CompiledRule
{
  public com.svincent.moksa.prolog.parser_prolog.parse_prolog_term_2_alt1 ()
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
    com.svincent.moksa.WamInteger int_2;
    com.svincent.moksa.CompoundTerm compound_6;
    com.svincent.moksa.CompoundTerm compound_5;
    com.svincent.moksa.Continuation continuation;
    com.svincent.moksa.CompoundTerm compound_4;
    com.svincent.moksa.PrologFactory factory;
    com.svincent.moksa.CompoundTerm compound_3;
    com.svincent.moksa.PrologEngine engine;
    engine = wam.getEngine();
    factory = engine.getFactory();
    //  *** test arg Tokens
    //  --- Tokens
    var_0 = factory.makeVariable((java.lang.String)"Tokens");
    if (wam.badparm((int)0, (com.svincent.moksa.PrologTerm)var_0))
      return wam.Fail;
    //  *** test arg Term
    //  --- Term
    var_1 = factory.makeVariable((java.lang.String)"Term");
    if (wam.badparm((int)1, (com.svincent.moksa.PrologTerm)var_1))
      return wam.Fail;
    continuation = wam.getContinuation();
    //  --- 1201
    int_2 = factory.makeInteger((int)1201);
    //  --- end
    compound_3 = factory.makeAtom((java.lang.String)"end");
    //  --- []
    compound_4 = factory.makeAtom((java.lang.String)"[]");
    //  --- [end]
    compound_5 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_3, compound_4});
    //  --- parse_prolog_term(Tokens, 1201, Term, [end])
    compound_6 = factory.makeCompoundTerm((java.lang.String)"parse_prolog_term", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, int_2, var_1, compound_5});
    return com.svincent.moksa.Continuation.make((com.svincent.moksa.PrologEngine)engine, (com.svincent.moksa.PrologTerm)compound_6, (com.svincent.moksa.Continuation)continuation);
  }
  
  public java.lang.String getName ()
  {
    return "parse_prolog_term/2";
  }
  
  public com.svincent.moksa.PrologTerm makeTerm (com.svincent.moksa.PrologFactory factory)
  {
    com.svincent.moksa.Variable var_1;
    com.svincent.moksa.Variable var_0;
    com.svincent.moksa.WamInteger int_3;
    com.svincent.moksa.CompoundTerm compound_8;
    com.svincent.moksa.CompoundTerm compound_7;
    com.svincent.moksa.CompoundTerm compound_6;
    com.svincent.moksa.CompoundTerm compound_5;
    com.svincent.moksa.CompoundTerm compound_4;
    com.svincent.moksa.CompoundTerm compound_2;
    //  --- Tokens
    var_0 = factory.makeVariable((java.lang.String)"Tokens");
    //  --- Term
    var_1 = factory.makeVariable((java.lang.String)"Term");
    //  --- parse_prolog_term(Tokens, Term)
    compound_2 = factory.makeCompoundTerm((java.lang.String)"parse_prolog_term", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_1});
    //  --- 1201
    int_3 = factory.makeInteger((int)1201);
    //  --- end
    compound_4 = factory.makeAtom((java.lang.String)"end");
    //  --- []
    compound_5 = factory.makeAtom((java.lang.String)"[]");
    //  --- [end]
    compound_6 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_4, compound_5});
    //  --- parse_prolog_term(Tokens, 1201, Term, [end])
    compound_7 = factory.makeCompoundTerm((java.lang.String)"parse_prolog_term", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, int_3, var_1, compound_6});
    //  --- ':-'(parse_prolog_term(Tokens, Term), parse_prolog_term(Tokens, 1201, Term, [end]))
    compound_8 = factory.makeCompoundTerm((java.lang.String)":-", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_2, compound_7});
    return compound_8;
  }
  
}