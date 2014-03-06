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
public class com.svincent.moksa.prolog.parser_prolog.make_unique_variable_token_4
  extends com.svincent.moksa.CompiledRule
{
  public com.svincent.moksa.prolog.parser_prolog.make_unique_variable_token_4 ()
  {
  }
  
  public int getArity ()
  {
    return 4;
  }
  
  public com.svincent.moksa.Continuation invokeRule (com.svincent.moksa.Wam wam)
    throws com.svincent.moksa.PrologException
  {
    com.svincent.moksa.CompoundTerm compound_12;
    com.svincent.moksa.CompoundTerm compound_11;
    com.svincent.moksa.CompoundTerm compound_10;
    com.svincent.moksa.PrologFactory factory;
    com.svincent.moksa.Variable var_6;
    com.svincent.moksa.Variable var_4;
    com.svincent.moksa.Variable var_3;
    com.svincent.moksa.Variable var_2;
    com.svincent.moksa.Variable var_0;
    com.svincent.moksa.CompoundTerm compound_9;
    com.svincent.moksa.CompoundTerm compound_8;
    com.svincent.moksa.CompoundTerm compound_7;
    com.svincent.moksa.CompoundTerm compound_5;
    com.svincent.moksa.CompoundTerm compound_1;
    com.svincent.moksa.CompoundTerm compound_16;
    com.svincent.moksa.PrologEngine engine;
    com.svincent.moksa.CompoundTerm compound_15;
    com.svincent.moksa.CompoundTerm compound_14;
    com.svincent.moksa.CompoundTerm compound_13;
    com.svincent.moksa.Continuation continuation;
    engine = wam.getEngine();
    factory = engine.getFactory();
    //  *** test arg Name
    //  --- Name
    var_0 = factory.makeVariable((java.lang.String)"Name");
    if (wam.badparm((int)0, (com.svincent.moksa.PrologTerm)var_0))
      return wam.Fail;
    //  *** test arg []
    //  --- []
    compound_1 = factory.makeAtom((java.lang.String)"[]");
    if (wam.badparm((int)1, (com.svincent.moksa.PrologTerm)compound_1))
      return wam.Fail;
    //  *** test arg VarToken
    //  --- VarToken
    var_2 = factory.makeVariable((java.lang.String)"VarToken");
    if (wam.badparm((int)2, (com.svincent.moksa.PrologTerm)var_2))
      return wam.Fail;
    //  *** test arg NewVars
    //  --- NewVars
    var_3 = factory.makeVariable((java.lang.String)"NewVars");
    if (wam.badparm((int)3, (com.svincent.moksa.PrologTerm)var_3))
      return wam.Fail;
    continuation = wam.getContinuation();
    //  --- Var
    var_4 = factory.makeVariable((java.lang.String)"Var");
    //  --- set_var_nameXXX(Name, Var)
    compound_5 = factory.makeCompoundTerm((java.lang.String)"set_var_nameXXX", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_4});
    //  --- VarDef
    var_6 = factory.makeVariable((java.lang.String)"VarDef");
    //  --- varDef(Name, Var)
    compound_7 = factory.makeCompoundTerm((java.lang.String)"varDef", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_4});
    //  --- '='(VarDef, varDef(Name, Var))
    compound_8 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_6, compound_7});
    //  --- variable(Name, Var)
    compound_9 = factory.makeCompoundTerm((java.lang.String)"variable", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_4});
    //  --- '='(VarToken, variable(Name, Var))
    compound_10 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_2, compound_9});
    //  --- []
    compound_11 = factory.makeAtom((java.lang.String)"[]");
    //  --- [VarDef]
    compound_12 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_6, compound_11});
    //  --- '='(NewVars, [VarDef])
    compound_13 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_3, compound_12});
    //  --- ','('='(VarToken, variable(Name, Var)), '='(NewVars, [VarDef]))
    compound_14 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_10, compound_13});
    //  --- ','('='(VarDef, varDef(Name, Var)), ','('='(VarToken, variable(Name, Var)), '='(NewVars, [VarDef])))
    compound_15 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_8, compound_14});
    //  --- ','(set_var_nameXXX(Name, Var), ','('='(VarDef, varDef(Name, Var)), ','('='(VarToken, variable(Name, Var)), '='(NewVars, [VarDef]))))
    compound_16 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_5, compound_15});
    return com.svincent.moksa.Continuation.make((com.svincent.moksa.PrologEngine)engine, (com.svincent.moksa.PrologTerm)compound_16, (com.svincent.moksa.Continuation)continuation);
  }
  
  public java.lang.String getName ()
  {
    return "make_unique_variable_token/4";
  }
  
  public com.svincent.moksa.PrologTerm makeTerm (com.svincent.moksa.PrologFactory factory)
  {
    com.svincent.moksa.CompoundTerm compound_12;
    com.svincent.moksa.CompoundTerm compound_11;
    com.svincent.moksa.CompoundTerm compound_10;
    com.svincent.moksa.Variable var_7;
    com.svincent.moksa.Variable var_5;
    com.svincent.moksa.Variable var_3;
    com.svincent.moksa.Variable var_2;
    com.svincent.moksa.Variable var_0;
    com.svincent.moksa.CompoundTerm compound_9;
    com.svincent.moksa.CompoundTerm compound_8;
    com.svincent.moksa.CompoundTerm compound_6;
    com.svincent.moksa.CompoundTerm compound_4;
    com.svincent.moksa.CompoundTerm compound_18;
    com.svincent.moksa.CompoundTerm compound_17;
    com.svincent.moksa.CompoundTerm compound_1;
    com.svincent.moksa.CompoundTerm compound_16;
    com.svincent.moksa.CompoundTerm compound_15;
    com.svincent.moksa.CompoundTerm compound_14;
    com.svincent.moksa.CompoundTerm compound_13;
    //  --- Name
    var_0 = factory.makeVariable((java.lang.String)"Name");
    //  --- []
    compound_1 = factory.makeAtom((java.lang.String)"[]");
    //  --- VarToken
    var_2 = factory.makeVariable((java.lang.String)"VarToken");
    //  --- NewVars
    var_3 = factory.makeVariable((java.lang.String)"NewVars");
    //  --- make_unique_variable_token(Name, [], VarToken, NewVars)
    compound_4 = factory.makeCompoundTerm((java.lang.String)"make_unique_variable_token", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, compound_1, var_2, var_3});
    //  --- Var
    var_5 = factory.makeVariable((java.lang.String)"Var");
    //  --- set_var_nameXXX(Name, Var)
    compound_6 = factory.makeCompoundTerm((java.lang.String)"set_var_nameXXX", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_5});
    //  --- VarDef
    var_7 = factory.makeVariable((java.lang.String)"VarDef");
    //  --- varDef(Name, Var)
    compound_8 = factory.makeCompoundTerm((java.lang.String)"varDef", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_5});
    //  --- '='(VarDef, varDef(Name, Var))
    compound_9 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_7, compound_8});
    //  --- variable(Name, Var)
    compound_10 = factory.makeCompoundTerm((java.lang.String)"variable", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_0, var_5});
    //  --- '='(VarToken, variable(Name, Var))
    compound_11 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_2, compound_10});
    //  --- []
    compound_12 = factory.makeAtom((java.lang.String)"[]");
    //  --- [VarDef]
    compound_13 = factory.makeCompoundTerm((java.lang.String)".", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_7, compound_12});
    //  --- '='(NewVars, [VarDef])
    compound_14 = factory.makeCompoundTerm((java.lang.String)"=", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {var_3, compound_13});
    //  --- ','('='(VarToken, variable(Name, Var)), '='(NewVars, [VarDef]))
    compound_15 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_11, compound_14});
    //  --- ','('='(VarDef, varDef(Name, Var)), ','('='(VarToken, variable(Name, Var)), '='(NewVars, [VarDef])))
    compound_16 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_9, compound_15});
    //  --- ','(set_var_nameXXX(Name, Var), ','('='(VarDef, varDef(Name, Var)), ','('='(VarToken, variable(Name, Var)), '='(NewVars, [VarDef]))))
    compound_17 = factory.makeCompoundTerm((java.lang.String)",", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_6, compound_16});
    //  --- ':-'(make_unique_variable_token(Name, [], VarToken, NewVars), ','(set_var_nameXXX(Name, Var), ','('='(VarDef, varDef(Name, Var)), ','('='(VarToken, variable(Name, Var)), '='(NewVars, [VarDef])))))
    compound_18 = factory.makeCompoundTerm((java.lang.String)":-", (com.svincent.moksa.PrologTerm[])new com.svincent.moksa.PrologTerm[] {compound_4, compound_17});
    return compound_18;
  }
  
}
