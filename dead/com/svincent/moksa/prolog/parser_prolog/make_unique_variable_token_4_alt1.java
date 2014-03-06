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
 * File generated: Thursday, December 2, 1999 8:07:17 AM EST
 */
public class com.svincent.moksa.prolog.parser_prolog.make_unique_variable_token_4_alt1
  extends com.svincent.moksa.CompiledRule
{
  public com.svincent.moksa.prolog.parser_prolog.make_unique_variable_token_4_alt1 ()
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
    com.svincent.moksa.CompoundTerm compound_8;
    com.svincent.moksa.CompoundTerm compound_7;
    com.svincent.moksa.Continuation continuation;
    com.svincent.moksa.CompoundTerm compound_5;
    com.svincent.moksa.PrologFactory factory;
    com.svincent.moksa.CompoundTerm compound_3;
    com.svincent.moksa.CompoundTerm compound_11;
    com.svincent.moksa.CompoundTerm compound_10;
    com.svincent.moksa.PrologEngine engine;
    com.svincent.moksa.Variable var_6;
    com.svincent.moksa.Variable var_4;
    com.svincent.moksa.Variable var_2;
    engine = wam.getEngine();
    factory = engine.getFactory();
    //  *** test arg Name
    //  --- Name
    var_0 = factory.makeVariable((java.lang.String)"Name");
    if (wam.badparm((int)0, (com.svincent.moksa.PrologTerm)var_0))
      return wam.Fail;
    //  *** test arg [VarDef | MoreVars]
    //  --- VarDef
    var_1 = factory.makeVariable((java.lang.String)"VarDef");
    //  --- MoreVars
    var_2 = factory.makeVariable((java.lang.String)"MoreVars");
    //  --- [VarDef | MoreVars]
    compound_3 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, var_2});
    if (wam.badparm((int)1, (com.svincent.moksa.PrologTerm)compound_3))
      return wam.Fail;
    //  *** test arg VarToken
    //  --- VarToken
    var_4 = factory.makeVariable((java.lang.String)"VarToken");
    if (wam.badparm((int)2, (com.svincent.moksa.PrologTerm)var_4))
      return wam.Fail;
    //  *** test arg [VarDef | MoreVars]
    //  --- [VarDef | MoreVars]
    compound_5 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, var_2});
    if (wam.badparm((int)3, (com.svincent.moksa.PrologTerm)compound_5))
      return wam.Fail;
    continuation = wam.getContinuation();
    //  --- Var
    var_6 = factory.makeVariable((java.lang.String)"Var");
    //  --- varDef(Name, Var)
    compound_7 = factory.makeCompoundTerm((java.lang.String)"varDef", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_6});
    //  --- '='(VarDef, varDef(Name, Var))
    compound_8 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, compound_7});
    //  --- variable(Name, Var)
    compound_9 = factory.makeCompoundTerm((java.lang.String)"variable", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_6});
    //  --- '='(VarToken, variable(Name, Var))
    compound_10 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_4, compound_9});
    //  --- ','('='(VarDef, varDef(Name, Var)), '='(VarToken, variable(Name, Var)))
    compound_11 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_8, compound_10});
    return com.svincent.moksa.Continuation.make((com.svincent.moksa.PrologEngine)engine, (com.svincent.moksa.PrologTerm)compound_11, (com.svincent.moksa.Continuation)continuation);
  }
  
  public java.lang.String getName ()
  {
    return "make_unique_variable_token/4";
  }
  
  public com.svincent.moksa.PrologTerm makeTerm (com.svincent.moksa.PrologFactory factory)
  {
    com.svincent.moksa.Variable var_1;
    com.svincent.moksa.Variable var_0;
    com.svincent.moksa.CompoundTerm compound_9;
    com.svincent.moksa.CompoundTerm compound_8;
    com.svincent.moksa.CompoundTerm compound_6;
    com.svincent.moksa.CompoundTerm compound_5;
    com.svincent.moksa.CompoundTerm compound_3;
    com.svincent.moksa.CompoundTerm compound_13;
    com.svincent.moksa.CompoundTerm compound_12;
    com.svincent.moksa.CompoundTerm compound_11;
    com.svincent.moksa.CompoundTerm compound_10;
    com.svincent.moksa.Variable var_7;
    com.svincent.moksa.Variable var_4;
    com.svincent.moksa.Variable var_2;
    //  --- Name
    var_0 = factory.makeVariable((java.lang.String)"Name");
    //  --- VarDef
    var_1 = factory.makeVariable((java.lang.String)"VarDef");
    //  --- MoreVars
    var_2 = factory.makeVariable((java.lang.String)"MoreVars");
    //  --- [VarDef | MoreVars]
    compound_3 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, var_2});
    //  --- VarToken
    var_4 = factory.makeVariable((java.lang.String)"VarToken");
    //  --- [VarDef | MoreVars]
    compound_5 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, var_2});
    //  --- make_unique_variable_token(Name, [VarDef | MoreVars], VarToken, [VarDef | MoreVars])
    compound_6 = factory.makeCompoundTerm((java.lang.String)"make_unique_variable_token", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, compound_3, var_4, compound_5});
    //  --- Var
    var_7 = factory.makeVariable((java.lang.String)"Var");
    //  --- varDef(Name, Var)
    compound_8 = factory.makeCompoundTerm((java.lang.String)"varDef", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_7});
    //  --- '='(VarDef, varDef(Name, Var))
    compound_9 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_1, compound_8});
    //  --- variable(Name, Var)
    compound_10 = factory.makeCompoundTerm((java.lang.String)"variable", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_7});
    //  --- '='(VarToken, variable(Name, Var))
    compound_11 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_4, compound_10});
    //  --- ','('='(VarDef, varDef(Name, Var)), '='(VarToken, variable(Name, Var)))
    compound_12 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_9, compound_11});
    //  --- ':-'(make_unique_variable_token(Name, [VarDef | MoreVars], VarToken, [VarDef | MoreVars]), ','('='(VarDef, varDef(Name, Var)), '='(VarToken, variable(Name, Var))))
    compound_13 = factory.makeCompoundTerm((java.lang.String)":-", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_6, compound_12});
    return compound_13;
  }
  
}