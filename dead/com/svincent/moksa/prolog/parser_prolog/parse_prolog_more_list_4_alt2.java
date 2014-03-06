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
 * File generated: Thursday, December 2, 1999 8:07:25 AM EST
 */
public class com.svincent.moksa.prolog.parser_prolog.parse_prolog_more_list_4_alt2
  extends com.svincent.moksa.CompiledRule
{
  public com.svincent.moksa.prolog.parser_prolog.parse_prolog_more_list_4_alt2 ()
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
    com.svincent.moksa.CompoundTerm compound_9;
    com.svincent.moksa.CompoundTerm compound_8;
    com.svincent.moksa.CompoundTerm compound_7;
    com.svincent.moksa.Continuation continuation;
    com.svincent.moksa.PrologFactory factory;
    com.svincent.moksa.CompoundTerm compound_2;
    com.svincent.moksa.CompoundTerm compound_0;
    com.svincent.moksa.CompoundTerm compound_11;
    com.svincent.moksa.CompoundTerm compound_10;
    com.svincent.moksa.PrologEngine engine;
    com.svincent.moksa.Variable var_6;
    com.svincent.moksa.Variable var_5;
    com.svincent.moksa.Variable var_4;
    com.svincent.moksa.Variable var_3;
    engine = wam.getEngine();
    factory = engine.getFactory();
    //  *** test arg [head_tail_separator | Rest]
    //  --- head_tail_separator
    compound_0 = factory.makeAtom((java.lang.String)"head_tail_separator");
    //  --- Rest
    var_1 = factory.makeVariable((java.lang.String)"Rest");
    //  --- [head_tail_separator | Rest]
    compound_2 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_0, var_1});
    if (wam.badparm((int)0, (com.svincent.moksa.PrologTerm)compound_2))
      return wam.Fail;
    //  *** test arg Priority
    //  --- Priority
    var_3 = factory.makeVariable((java.lang.String)"Priority");
    if (wam.badparm((int)1, (com.svincent.moksa.PrologTerm)var_3))
      return wam.Fail;
    //  *** test arg List
    //  --- List
    var_4 = factory.makeVariable((java.lang.String)"List");
    if (wam.badparm((int)2, (com.svincent.moksa.PrologTerm)var_4))
      return wam.Fail;
    //  *** test arg Tail
    //  --- Tail
    var_5 = factory.makeVariable((java.lang.String)"Tail");
    if (wam.badparm((int)3, (com.svincent.moksa.PrologTerm)var_5))
      return wam.Fail;
    continuation = wam.getContinuation();
    //  --- Exp
    var_6 = factory.makeVariable((java.lang.String)"Exp");
    //  --- close_list
    compound_7 = factory.makeAtom((java.lang.String)"close_list");
    //  --- [close_list | Tail]
    compound_8 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_7, var_5});
    //  --- parse_prolog_exp(Rest, Priority, Exp, [close_list | Tail])
    compound_9 = factory.makeCompoundTerm((java.lang.String)"parse_prolog_exp", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, var_3, var_6, compound_8});
    //  --- '='(List, Exp)
    compound_10 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_4, var_6});
    //  --- ','(parse_prolog_exp(Rest, Priority, Exp, [close_list | Tail]), '='(List, Exp))
    compound_11 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_9, compound_10});
    return com.svincent.moksa.Continuation.make((com.svincent.moksa.PrologEngine)engine, (com.svincent.moksa.PrologTerm)compound_11, (com.svincent.moksa.Continuation)continuation);
  }
  
  public java.lang.String getName ()
  {
    return "parse_prolog_more_list/4";
  }
  
  public com.svincent.moksa.PrologTerm makeTerm (com.svincent.moksa.PrologFactory factory)
  {
    com.svincent.moksa.Variable var_1;
    com.svincent.moksa.CompoundTerm compound_9;
    com.svincent.moksa.CompoundTerm compound_8;
    com.svincent.moksa.CompoundTerm compound_6;
    com.svincent.moksa.CompoundTerm compound_2;
    com.svincent.moksa.CompoundTerm compound_13;
    com.svincent.moksa.CompoundTerm compound_12;
    com.svincent.moksa.CompoundTerm compound_0;
    com.svincent.moksa.CompoundTerm compound_11;
    com.svincent.moksa.CompoundTerm compound_10;
    com.svincent.moksa.Variable var_7;
    com.svincent.moksa.Variable var_5;
    com.svincent.moksa.Variable var_4;
    com.svincent.moksa.Variable var_3;
    //  --- head_tail_separator
    compound_0 = factory.makeAtom((java.lang.String)"head_tail_separator");
    //  --- Rest
    var_1 = factory.makeVariable((java.lang.String)"Rest");
    //  --- [head_tail_separator | Rest]
    compound_2 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_0, var_1});
    //  --- Priority
    var_3 = factory.makeVariable((java.lang.String)"Priority");
    //  --- List
    var_4 = factory.makeVariable((java.lang.String)"List");
    //  --- Tail
    var_5 = factory.makeVariable((java.lang.String)"Tail");
    //  --- parse_prolog_more_list([head_tail_separator | Rest], Priority, List, Tail)
    compound_6 = factory.makeCompoundTerm((java.lang.String)"parse_prolog_more_list", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_2, var_3, var_4, var_5});
    //  --- Exp
    var_7 = factory.makeVariable((java.lang.String)"Exp");
    //  --- close_list
    compound_8 = factory.makeAtom((java.lang.String)"close_list");
    //  --- [close_list | Tail]
    compound_9 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_8, var_5});
    //  --- parse_prolog_exp(Rest, Priority, Exp, [close_list | Tail])
    compound_10 = factory.makeCompoundTerm((java.lang.String)"parse_prolog_exp", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, var_3, var_7, compound_9});
    //  --- '='(List, Exp)
    compound_11 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_4, var_7});
    //  --- ','(parse_prolog_exp(Rest, Priority, Exp, [close_list | Tail]), '='(List, Exp))
    compound_12 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_10, compound_11});
    //  --- ':-'(parse_prolog_more_list([head_tail_separator | Rest], Priority, List, Tail), ','(parse_prolog_exp(Rest, Priority, Exp, [close_list | Tail]), '='(List, Exp)))
    compound_13 = factory.makeCompoundTerm((java.lang.String)":-", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_6, compound_12});
    return compound_13;
  }
  
}
