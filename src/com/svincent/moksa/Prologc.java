/*
 * 
 * Copyright (C) 1999  Shawn P. Vincent (svincent@svincent.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * -------------------------------------------------------------------------
 *
 * Prologc.java
 *
 *
 */
package com.svincent.moksa;

import java.io.*;
import java.util.*;

import com.svincent.util.*;
import com.svincent.smalljava.*;

/**
 * Compiles Prolog source files into Java class files.
 **/
public class Prologc extends WamObject {

  // -------------------------------------------------------------------------
  // ---- Main Execution Chain -----------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * The main entry point.  Parses command line args, does glob expansion,
   * finally calls <code>compile</code>
   **/
  public static void main (String[] args)
  {
    boolean generateClasses = false;
    boolean generateSource = false;
    List<String> sourceFileNames = new ArrayList<String> ();
    boolean miniProlog = false;

    // XXX should maybe be System.err.
    PrintWriter out = Util.out;
    
    for (int i=0; i<args.length; i++)
      {
        if (args[i].equals ("-class")) generateClasses = true;
        else if (args[i].equals ("-java")) generateSource = true;
        else if (args[i].equals ("-miniProlog")) miniProlog = true;
        else if (args[i].startsWith ("-h") ||
                 args[i].equals ("/?") || args[i].equals ("-?"))
          {
            printHelp (out);
            return;
          }
        else if (args[i].startsWith ("-"))
          {
            printError (out, "No such option: "+args[i]);
            return;
          }
        else
          {
            // --- stick this into the list of source fileNames.
            sourceFileNames.add (args[i]);
          }
      }

    // --- default behavior
    if (!generateClasses && !generateSource)
      generateClasses = true;

    try {
      compile (sourceFileNames, generateClasses, generateSource, miniProlog);
    } catch (PrologException ex) {
      printError (out, ex.getMessage ());
      out.println ();
      out.println ("Stack trace follows:");
      ex.printStackTrace (out);
    }
  }

  public static void compile (List<String> sourceFileNames, 
                              boolean generateClasses, boolean generateSource,
                              boolean miniProlog)
    throws PrologException
  {
    // --- make a PrologEngine, get it's compiler.
    PrologEngine engine = new PrologEngine ();

    Util.out.println ("Got source files: "+sourceFileNames);

    // --- for each source file, do a compile on it.
    Iterator<String> i = sourceFileNames.iterator ();
    while (i.hasNext ())
      {
        String sourceFileName = (String)i.next ();
        compilePrologFile (engine, sourceFileName, 
                           generateClasses, generateSource, miniProlog);
      }
  }

  public static void compilePrologFile (PrologEngine engine,
                                        String fileName,
                                        boolean generateClass,
                                        boolean generateSource,
                                        boolean miniProlog)
    throws PrologException
  {
    try {
      File file = new File (fileName);
      if (!file.exists ()) 
        throw new PrologException ("File not found: "+fileName);

      String packageName = calculatePackageName (fileName);

      // --- define one top-level class, which is the Loader.

      // XXX package name here??
      SmallClass loaderClass = 
        new SmallClass (packageName+".Loader", 
                        "com.svincent.moksa.PrologLoader");

      // --- define the constructor.
  //      SmallConstructor constructor = 
  //        loaderClass.constructor ("()V", new String[0]);


      // --- define the 'load' method.
      SmallMethod loadMethod = 
        loaderClass.method ("load(Lcom.svincent.moksa.PrologEngine;)V", 
                            new String[] {"engine"});
      loadMethod.addThrows ("com.svincent.moksa.PrologException");

      Expr getEngine = new Expr.GetLocal ("engine");

      // --- generate 
      //    PrologFactory factory = engine.factory;
      loadMethod.local ("Lcom.svincent.moksa.PrologFactory;",
                        "factory", 
                        new Expr.Call
                          (getEngine, 
                           "com.svincent.moksa.PrologEngine",
                           "getFactory()Lcom.svincent.moksa.PrologFactory;",
                           new Expr[0]));

      // --- get the rule compiler
      PrologRuleCompiler ruleCompiler = new PrologRuleCompiler (engine);

      // --- make a term compiler for this method.
      PrologTermCompiler termCompiler = new PrologTermCompiler (loadMethod);

      Io io = engine.io;
      PrologParser parser = io.parser;
      if (miniProlog)
        parser = new MiniPrologParser (io);

      // --- parse the Prolog file.
      PrologTerm[] clauses;
      try {
        Io.PrologInput in = io.openInputUri (fileName);
        clauses = parser.parseFile (in);
      } catch (IOException ex) {
        throw new PrologException ("Could not parse file "+fileName, ex);
      } catch (PrologParseException ex) {
        // XXX list, etc?
        throw new PrologException ("Errors while parsing "+fileName, ex);
      }

      // --- compile each of the clauses in the file.
      for (int i=0; i<clauses.length; i++)
        {
          PrologTerm term = clauses[i];
          term = term.uniqueVariables ();
          
          if (term.getName ().equals (":-") &&
              term.getArity () == 1)
            {
              // --- query.
              PrologTerm query = ((CompoundTerm)term).getSubterm (0);
              loadMethod.
                add (new Expr.Call 
                  (new Expr.GetLocal ("engine"), 
                   "com.svincent.moksa.PrologEngine", 
                   "invoke(Lcom.svincent.moksa.PrologTerm;)Z", 
                   new Expr[] {
                     termCompiler.compile (query)
                   }));
            }
          else
            {
              // --- rule.
              SmallClass ruleClass = 
                ruleCompiler.makeRuleClass (packageName, term);
              String className = ruleClass.getName ();
              
              // --- call 
              //   engine.assertz (new RuleClassName ())
              Expr newRule = 
                new Expr.New (className, "()V", new Expr[0]);
              
              loadMethod.
                add (new Expr.Call
                  (new Expr.GetLocal ("engine"),
                   "com.svincent.moksa.PrologEngine",
                   "assertz(Lcom.svincent.moksa.Rule;)V",
                   new Expr[] { newRule }));

              spewClass (packageName, 
                         ruleClass, generateClass, generateSource);
            }
        }

      loadMethod.add (new Expr.Return ());

      loaderClass.finalize ();

      spewClass (packageName, loaderClass, generateClass, generateSource);

    } catch (SmallJavaBuildingException ex) {
      throw new PrologException ("Got building exceptin XXX", ex);
    } catch (SmallJavaValidationException ex) {
      throw new PrologException ("Got Validation exception XXX", ex);
    } catch (IOException ex) {
      throw new PrologException ("Got I/O Exception XXX", ex);
    }
  }

  /**
   * The basic package all compiled Prolog code gets to live within.
   * 
   * XXX rethink this: what about conflicts, etc?
   **/
  static final String CompiledPackagePrefix = "com.svincent.moksa.prolog.";

  public static String calculatePackageName (String prologFileName)
  {
    String packageName = prologFileName.replace ('.', '_'); // not safe?

    if (packageName.equalsIgnoreCase (prologFileName))
      return CompiledPackagePrefix+packageName+"_pkg";
    else
      return CompiledPackagePrefix+packageName;
  }

  public static void spewClass (String packageName, SmallClass c, 
                                boolean generateClass, boolean generateSource)
    throws IOException
  {
    String dirName = packageName.replace ('.', File.separatorChar);

    File file = new File (dirName);
    if (!file.exists ()) file.mkdirs ();

    if (generateClass)
      {
        String fileName = dirName + "/" + c.getRelativeName () + ".class";
        c.writeAsBytecodes (fileName);
      }
    if (generateSource)
      {
        String fileName = dirName + "/" + c.getRelativeName () + ".java";
        c.writeAsJava (fileName);
      }
  }

  // -------------------------------------------------------------------------
  // ---- Usage Printing -----------------------------------------------------
  // -------------------------------------------------------------------------

  static void printHelp (PrintWriter out) { printUsage (out); }

  static void printError (PrintWriter out, String msg)
  {
    out.println ("Usage error: "+msg);
    out.println ();
    printUsage (out);
  }

  static void printUsage (PrintWriter out)
  {
    out.println ("Usage:");
    out.println ("    java "+Prologc.class.getName ()+" [options] sourcefile");
    out.println ();
    out.println ("    -class      Generate .class files");
    out.println ("    -java       Generate .java source");
    out.println ();
    out.println ("Note that multiple things can be generated:  ");
    out.println (" the compiler can generate Java source AND .class ");
    out.println (" files in one execution.  If no options are specified,");
    out.println (" only .class files are generated.");
  }

  // -------------------------------------------------------------------------
  // ---- Prolog Rule Compiler -----------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * A compiler for Prolog rules.
   **/
  public static class PrologRuleCompiler extends WamObject {

    Set<String> uniqueNames = new HashSet<String> ();
    SmallClassLoader classLoader = new SmallClassLoader ();

    public PrologRuleCompiler (PrologEngine _engine)
    {
    }

    public String uniquify (String name)
    {
      String newName = name;
      int i = 1;
      while (uniqueNames.contains (newName)) newName = name + "_alt" +(i++);
      uniqueNames.add (newName);
      return newName;
    }

    /**
     * Compile a term into a Rule.
     **/
    public CompiledRule compileRule (PrologTerm term) throws PrologException
    {
      SmallClass ruleClass = makeRuleClass (term);

      return loadClass (ruleClass);
    }

    /**
     * Load a CompiledRule given a SmallClass instance.
     **/
    public CompiledRule loadClass (SmallClass ruleClass) throws PrologException
    {
      // --- load the class, using a new ClassLoader.
      Class<?> loadedClass = classLoader.loadClass (ruleClass);

      // --- make a new instance.
      CompiledRule rule;
      try {
        rule = (CompiledRule)loadedClass.newInstance ();
      } catch (ClassCastException ex) {
        throw new PrologException ("Generated rule class wrong type "+
                                loadedClass.getName (), ex);
      } catch (Exception ex) {
        throw new PrologException ("XXX Probably reflection error instantiating "+
                                loadedClass.getName (), ex);
      }

      return rule;
    }

    /**
     * Make a new SmallClass instance from a Rule.
     **/
    public SmallClass makeRuleClass (PrologTerm term) throws PrologException
    { return makeRuleClass (null, term); }

    public SmallClass makeRuleClass (String packageName, PrologTerm term) 
      throws PrologException
    {
      PrologTerm head;
      if (term.isCompoundTerm () && term.getName ().equals (":-"))
        {
          head = ((CompoundTerm)term).getSubterm (0);
        }
      else
        {
          head = term;
        }

      String name = head.getName () + "/" + head.getArity ();
      int arity = head.getArity ();

      // XXX do fancier quoting here for name for wierd names!
      String className = name.replace ('/', '_');

      if (packageName != null) className = packageName + '.' + className;

      className = uniquify (className);

      // --- make the class.
      try {
        SmallClass ruleClass = 
          new SmallClass (className, "com.svincent.moksa.CompiledRule");

        
        // --- generate 'String getName () {...}'
        SmallMethod getNameMethod = 
          ruleClass.method ("getName()Ljava.lang.String;", new String[0]);
        getNameMethod.add (new Expr.Return (new Expr.StringConst (name)));

        // --- generate 'int getArity () {...}'
        SmallMethod getArityMethod = 
          ruleClass.method ("getArity()I", new String[0]);
        getArityMethod.add (new Expr.Return (new Expr.IntConst (arity)));

        // --- generate 'PrologTerm makeTerm (PrologFactory factory) {...}'
        SmallMethod makeTermMethod = 
          ruleClass.method 
          ("makeTerm(Lcom.svincent.moksa.PrologFactory;)"+
           "Lcom.svincent.moksa.PrologTerm;",
           new String[] { "factory" });
        PrologTermCompiler termCompiler = 
          new PrologTermCompiler (makeTermMethod);
        makeTermMethod.add (new Expr.Return (termCompiler.compile (term)));
        
        // --- generate 'Continuation invokeRule (PrologEngine engine) {...}'
        generateInvokeRule (ruleClass, term);
        
        // --- finalize the class.
        ruleClass.finalize ();

        // --- all done.
        return ruleClass;

      } catch (SmallJavaBuildingException ex) {
        throw new PrologException 
          ("Could not build generated class for term "+term.tag (), ex);
      } catch (SmallJavaValidationException ex) {
        throw new PrologException 
          ("Could not validate generated class for term "+term.tag (), ex);
      }
    }

    private void generateInvokeRule (SmallClass ruleClass, PrologTerm _term) 
      throws PrologException, SmallJavaBuildingException, 
             SmallJavaValidationException
    {
      // XXX what about rule like:
      //       animalPoodle.
      // this is valid!!  XXX FIXME
      if (!_term.isCompoundTerm ())
        throw new PrologException ("Can only compile compound terms...");

      CompoundTerm term = (CompoundTerm)_term;

      // --- generate 'Continuation invokeRule (Wam wam) {...}'
      SmallMethod invokeRuleMethod = 
        ruleClass.method 
        ("invokeRule(Lcom.svincent.moksa.Wam;)"+
         "Lcom.svincent.moksa.Continuation;", 
         new String[] { "wam" });
      invokeRuleMethod.addThrows ("com.svincent.moksa.PrologException");

      Expr getWam = new Expr.GetLocal ("wam");
      Expr getFail = new Expr.GetField (getWam, 
                                        "com.svincent.moksa.Wam",
                                        "Lcom.svincent.moksa.Continuation;",
                                        "Fail");

      // --- generate 
      //    PrologEngine engine = wam.getEngine ();
      invokeRuleMethod.local 
        ("Lcom.svincent.moksa.PrologEngine;",
         "engine", 
         new Expr.Call 
           (getWam, 
            "com.svincent.moksa.Wam",
            "getEngine()Lcom.svincent.moksa.PrologEngine;",
            new Expr[0]));

      Expr getEngine = new Expr.GetLocal ("engine");

      // --- generate 
      //    PrologFactory factory = engine.factory;
      invokeRuleMethod.local 
        ("Lcom.svincent.moksa.PrologFactory;",
         "factory", 
         new Expr.Call 
           (getEngine, 
            "com.svincent.moksa.PrologEngine",
            "getFactory()Lcom.svincent.moksa.PrologFactory;",
            new Expr[0]));


      // --- make the term compiler for this method.
      PrologTermCompiler termCompiler = 
        new PrologTermCompiler(invokeRuleMethod);

      // --- figure out the head, and thus the parameter list.
      CompoundTerm head = getHead (term);

      // --- for each parameter, generate:
      //  
      //    if (wam.badParm (i, compile (term (i)))) return wam.Fail;
      //
      for (int i=0; i<head.getArity (); i++)
        {
          PrologTerm arg = head.getSubterm (i);
          invokeRuleMethod.add 
            (new Expr.Comment (" *** test arg "+arg.tag ()));
          Expr cond = 
            new Expr.Call (getWam, 
                           "com.svincent.moksa.Wam", 
                           "badparm(ILcom.svincent.moksa.PrologTerm;)Z", 
                           new Expr[] {
                             new Expr.IntConst (i),
                             termCompiler.compile (arg)
                           });

          Expr then = new Expr.Return (getFail);

          invokeRuleMethod.add (new Expr.If (cond, then));
        }

      // --- generate 
      //   -    Continuation continuation = wam.getContinuation ();
      invokeRuleMethod.local 
        ("Lcom.svincent.moksa.Continuation;", "continuation", 
         new Expr.Call 
           (getWam, "com.svincent.moksa.Wam", 
            "getContinuation()Lcom.svincent.moksa.Continuation;", 
            new Expr[0]));
      Expr getContinuation = 
        new Expr.GetLocal ("continuation");

      // --- figure out the body.
      CompoundTerm body = getBody (term);


      // --- if body is null
      if (body == null)
        {
          // --- generate 
          //   -    return continuation;
          invokeRuleMethod.add 
            (new Expr.Return (getContinuation));
        }
      else
        {
          // --- generate 
          //   -    return Continuation.make (engine, compile (body), 
          //   -                              continuation)
          invokeRuleMethod.add 
            (new Expr.Return 
              (new Expr.Call ("com.svincent.moksa.Continuation", 
                              "make(Lcom.svincent.moksa.PrologEngine;"+
                              "Lcom.svincent.moksa.PrologTerm;"+
                              "Lcom.svincent.moksa.Continuation;"+
                              ")Lcom.svincent.moksa.Continuation;",
                              new Expr[] {
                                getEngine,
                                termCompiler.compile (body), 
                                getContinuation
                              })));
        }
      // invokeRuleMethod.add (new Expr.Return (new Expr.NullConst ()));
    }

    private CompoundTerm getHead (CompoundTerm rule)
    {
      if (rule.getName ().equals (":-")) 
        return (CompoundTerm)rule.getSubterm (0);
      else return rule;
    }

    private CompoundTerm getBody (CompoundTerm rule)
    {
      if (rule.getName ().equals (":-")) 
        return (CompoundTerm)rule.getSubterm (1);
      else return null;
    }
  }

  // -------------------------------------------------------------------------
  // ---- Prolog Term Compiler -----------------------------------------------
  // -------------------------------------------------------------------------

  /**
   * Make one of these for the entire method construction process: it
   * keeps state about what it's created so far.
   **/
  public static class PrologTermCompiler extends PrologTermVisitor {

    SmallMethod method;
    Expr factoryVar;
    Map<Object, Expr> foundTerms = new HashMap<Object, Expr> ();
    int unique = 0;

    public PrologTermCompiler (SmallMethod _method) 
    { 
      method = _method; 
      factoryVar = new Expr.GetLocal ("factory");
    }

    /**
     * Note that this adds statements to the current method to define
     * and instantiate various variables.
     **/
    Expr compile (PrologTerm t) throws PrologException
    { 
      try {
        return (Expr)t.accept (this, null); 
      } catch (WamVisitorException ex) {
        throw (PrologException)ex.getNestedException ();
      }
    }

    public Object visitPrologTerm (PrologTerm v, Object parm)
    {
      throw new WamVisitorException 
        ("", new PrologException ("Cannot generate code to build PrologTerm "+
                               "of type "+v.getClass ().getName ()));
    }

    public Object visitVariable (Variable v, Object parm)
    { 
      try {
        Expr retval = (Expr)foundTerms.get (v);
        if (retval == null)
          {
            Expr makeVar = new Expr.Call 
              (factoryVar,
               "com.svincent.moksa.PrologFactory", 
               "makeVariable(Ljava.lang.String;)"+
               "Lcom.svincent.moksa.Variable;", 
               new Expr[] { new Expr.StringConst (v.getName ()) });

            String varName = "var_"+(unique++);

            method.add (new Expr.Comment (" --- "+v.tag ()));

            // XXX make more readable var names
            method.local ("Lcom.svincent.moksa.Variable;", varName, makeVar);

            retval = new Expr.GetLocal (varName);

            foundTerms.put (v, retval);
          }
        return retval;
      } catch (SmallJavaBuildingException ex) {
        throw new WamVisitorException
          ("", new PrologException ("Error building variable builder", ex));
      }
    }

    public Object visitCompoundTerm (CompoundTerm v, Object parm)
    {
      try {
        Expr retval = (Expr)foundTerms.get (v);
        if (retval == null)
          {
            int arity = v.getArity ();

            Expr makeTerm;
            if (arity == 0)
              {
                // --- atoms are simplest.

                makeTerm = new Expr.Call 
                  (factoryVar,
                   "com.svincent.moksa.PrologFactory",
                   "makeAtom(Ljava.lang.String;)"+
                   "Lcom.svincent.moksa.CompoundTerm;",
                   new Expr[] {new Expr.StringConst (v.getName ())});
              }
            else
              {
                // --- more complex compound terms are more complex.

                // --- make array intializer.
                Expr[] arrayInit = new Expr[arity];
                for (int i=0; i<arity; i++)
                  arrayInit[i] = compile (v.getSubterm (i));

                SmallType termType = 
                  new SmallType.ObjectType ("com.svincent.moksa.PrologTerm");

                // --- make an array.
                Expr newArray = new Expr.NewArray (termType, arrayInit);

                // --- make name
                Expr nameExp = new Expr.StringConst (v.getName ());

                // --- make a new term.
                makeTerm = new Expr.Call 
                  (factoryVar,
                   "com.svincent.moksa.PrologFactory", 
                   "makeCompoundTerm(Ljava.lang.String;"+
                   "[Lcom.svincent.moksa.PrologTerm;)"+
                   "Lcom.svincent.moksa.CompoundTerm;", 
                   new Expr[] { nameExp, newArray });
              }

            // XXX make more readable var names
            String varName = "compound_"+(unique++);

            method.add (new Expr.Comment (" --- "+v.tag ()));

            method.local ("Lcom.svincent.moksa.CompoundTerm;",
                          // XXX is this name safe?  No.
                          varName,
                          makeTerm);

            retval = new Expr.GetLocal (varName);
          }
        return retval;

      } catch (SmallJavaBuildingException ex) {
        throw new WamVisitorException
          ("", new PrologException ("Error building Compound Term builder", ex));
      } catch (PrologException ex) {
        throw new WamVisitorException ("", ex);
      }
    }

    public Object visitWamInteger (WamInteger v, Object parm)
    { 
      try {
        Integer key = new Integer (v.intValue ());
        Expr retval = (Expr)foundTerms.get (key);
        if (retval == null)
          {
            Expr makeInt = new Expr.Call 
              (factoryVar,
               "com.svincent.moksa.PrologFactory", 
               "makeInteger(I)Lcom.svincent.moksa.WamInteger;", 
               new Expr[] { new Expr.IntConst (v.intValue ()) });

            String varName = "int_"+(unique++);

            method.add (new Expr.Comment (" --- "+v.tag ()));

            // XXX make more readable var names
            method.local ("Lcom.svincent.moksa.WamInteger;", varName, makeInt);

            retval = new Expr.GetLocal (varName);

            foundTerms.put (key, retval);
          }
        return retval;
      } catch (SmallJavaBuildingException ex) {
        throw new WamVisitorException
          ("", new PrologException ("Error building variable builder", ex));
      }
    }

    public Object visitWamFloat (WamFloat v, Object parm)
    { 
      try {
        Double key = new Double (v.floatValue ());
        Expr retval = (Expr)foundTerms.get (key);
        if (retval == null)
          {
            Expr makeFloat = new Expr.Call 
              (factoryVar,
               "com.svincent.moksa.PrologFactory", 
               "makeFloat(I)Lcom.svincent.moksa.WamFloat;", 
               new Expr[] { new Expr.DoubleConst (v.floatValue ()) });

            String varName = "float_"+(unique++);

            method.add (new Expr.Comment (" --- "+v.tag ()));

            // XXX make more readable var names
            method.local ("Lcom.svincent.moksa.WamFloat;", varName, makeFloat);

            retval = new Expr.GetLocal (varName);

            foundTerms.put (key, retval);
          }
        return retval;
      } catch (SmallJavaBuildingException ex) {
        throw new WamVisitorException
          ("", new PrologException ("Error building variable builder", ex));
      }
    }

    public Object visitJavaTerm (JavaTerm v, Object parm)
    { return visitPrologTerm (v, parm); }


  }


}
