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
public class com.svincent.moksa.prolog.parser_prolog.parse_prolog_term_4
  extends com.svincent.moksa.CompiledRule
{
  public com.svincent.moksa.prolog.parser_prolog.parse_prolog_term_4 ()
  {
  }
  
  public int getArity ()
  {
    return 4;
  }
  
  public com.svincent.moksa.Continuation invokeRule (com.svincent.moksa.Wam wam)
    throws com.svincent.moksa.PrologException
  {
    com.svincent.moksa.Variable var_1;
    com.svincent.moksa.Variable var_0;
    com.svincent.moksa.CompoundTerm compound_9;
    com.svincent.moksa.CompoundTerm compound_7;
    com.svincent.moksa.CompoundTerm compound_6;
    com.svincent.moksa.CompoundTerm compound_5;
    com.svincent.moksa.Continuation continuation;
    com.svincent.moksa.PrologFactory factory;
    com.svincent.moksa.CompoundTerm compound_12;
    com.svincent.moksa.CompoundTerm compound_11;
    com.svincent.moksa.CompoundTerm compound_10;
    com.svincent.moksa.PrologEngine engine;
    com.svincent.moksa.WamInteger int_8;
    com.svincent.moksa.Variable var_4;
    com.svincent.moksa.Variable var_3;
    com.svincent.moksa.Variable var_2;
    engine = wam.getEngine();
    factory = engine.getFactory();
    //  *** test arg Tokens
    //  --- Tokens
    var_0 = factory.makeVariable((java.lang.String)"Tokens");
    if (wam.badparm((int)0, (com.svincent.moksa.PrologTerm)var_0))
      return wam.Fail;
    //  *** test arg Priority
    //  --- Priority
    var_1 = factory.makeVariable((java.lang.String)"Priority");
    if (wam.badparm((int)1, (com.svincent.moksa.PrologTerm)var_1))
      return wam.Fail;
    //  *** test arg Term
    //  --- Term
    var_2 = factory.makeVariable((java.lang.String)"Term");
    if (wam.badparm((int)2, (com.svincent.moksa.PrologTerm)var_2))
      return wam.Fail;
    //  *** test arg Rest
    //  --- Rest
    var_3 = factory.makeVariable((java.lang.String)"Rest");
    if (wam.badparm((int)3, (com.svincent.moksa.PrologTerm)var_3))
      return wam.Fail;
    continuation = wam.getContinuation();
    //  --- Number
    var_4 = factory.makeVariable((java.lang.String)"Number");
    //  --- integer(Number)
    compound_5 = factory.makeCompoundTerm((java.lang.String)"integer", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_4});
    //  --- [integer(Number) | Rest]
    compound_6 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_5, var_3});
    //  --- '='(Tokens, [integer(Number) | Rest])
    compound_7 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, compound_6});
    //  --- 0
    int_8 = factory.makeInteger((int)0);
    //  --- '>='(Priority, 0)
    compound_9 = factory.makeCompoundTerm((java.lang.String)">=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, int_8});
    //  --- is(Term, Number)
    compound_10 = factory.makeCompoundTerm((java.lang.String)"is", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_2, var_4});
    //  --- ','('>='(Priority, 0), is(Term, Number))
    compound_11 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_9, compound_10});
    //  --- ','('='(Tokens, [integer(Number) | Rest]), ','('>='(Priority, 0), is(Term, Number)))
    compound_12 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_7, compound_11});
    return com.svincent.moksa.Continuation.make((com.svincent.moksa.PrologEngine)engine, (com.svincent.moksa.PrologTerm)compound_12, (com.svincent.moksa.Continuation)continuation);
  }
  
  public java.lang.String getName ()
  {
    return "parse_prolog_term/4";
  }
  
  public com.svincent.moksa.PrologTerm makeTerm (com.svincent.moksa.PrologFactory factory)
  {
    com.svincent.moksa.Variable var_1;
    com.svincent.moksa.Variable var_0;
    com.svincent.moksa.CompoundTerm compound_8;
    com.svincent.moksa.CompoundTerm compound_7;
    com.svincent.moksa.CompoundTerm compound_6;
    com.svincent.moksa.CompoundTerm compound_4;
    com.svincent.moksa.CompoundTerm compound_14;
    com.svincent.moksa.CompoundTerm compound_13;
    com.svincent.moksa.CompoundTerm compound_12;
    com.svincent.moksa.CompoundTerm compound_11;
    com.svincent.moksa.CompoundTerm compound_10;
    com.svincent.moksa.WamInteger int_9;
    com.svincent.moksa.Variable var_5;
    com.svincent.moksa.Variable var_3;
    com.svincent.moksa.Variable var_2;
    //  --- Tokens
    var_0 = factory.makeVariable((java.lang.String)"Tokens");
    //  --- Priority
    var_1 = factory.makeVariable((java.lang.String)"Priority");
    //  --- Term
    var_2 = factory.makeVariable((java.lang.String)"Term");
    //  --- Rest
    var_3 = factory.makeVariable((java.lang.String)"Rest");
    //  --- parse_prolog_term(Tokens, Priority, Term, Rest)
    compound_4 = factory.makeCompoundTerm((java.lang.String)"parse_prolog_term", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_1, var_2, var_3});
    //  --- Number
    var_5 = factory.makeVariable((java.lang.String)"Number");
    //  --- integer(Number)
    compound_6 = factory.makeCompoundTerm((java.lang.String)"integer", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_5});
    //  --- [integer(Number) | Rest]
    compound_7 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_6, var_3});
    //  --- '='(Tokens, [integer(Number) | Rest])
    compound_8 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, compound_7});
    //  --- 0
    int_9 = factory.makeInteger((int)0);
    //  --- '>='(Priority, 0)
    compound_10 = factory.makeCompoundTerm((java.lang.String)">=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, int_9});
    //  --- is(Term, Number)
    compound_11 = factory.makeCompoundTerm((java.lang.String)"is", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_2, var_5});
    //  --- ','('>='(Priority, 0), is(Term, Number))
    compound_12 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_10, compound_11});
    //  --- ','('='(Tokens, [integer(Number) | Rest]), ','('>='(Priority, 0), is(Term, Number)))
    compound_13 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_8, compound_12});
    //  --- ':-'(parse_prolog_term(Tokens, Priority, Term, Rest), ','('='(Tokens, [integer(Number) | Rest]), ','('>='(Priority, 0), is(Term, Number))))
    compound_14 = factory.makeCompoundTerm((java.lang.String)":-", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_4, compound_13});
    return compound_14;
  }
  
}