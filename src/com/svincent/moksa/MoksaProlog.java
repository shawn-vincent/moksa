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
 * MoksaProlog.java
 *
 * The main entry point.
 *
 */
package com.svincent.moksa;

import java.io.IOException;
import java.io.PrintWriter;

import com.svincent.util.Util;

/**
 * <p>The main entry point for the current version of MoksaProlog.
 * Eventually, a new entry point will be added, called 'Moksa' that
 * will do fancier things more in line with the mandate of Moksa.  For
 * now, this will have to do.</p>
 *
 * <p>Mostly does fancy command line processing, user interaction, and
 * launching the PrologEngine.</p>
 **/
public class MoksaProlog extends WamObject {

  public static void main (String[] args)
  {
    try {
      go (args, Util.out, Util.err);
    } catch (PrologException ex) {
      ex.printStackTrace (Util.out);
    } catch (Throwable ex) {
      ex.printStackTrace (Util.out);
    }
  }

  public static void go (String[] args, PrintWriter out, PrintWriter err)
    throws PrologException
  {
    // --- print a banner if '-banner' is specified.
    boolean banner = false;
    for (int i=0; i<args.length; i++)
      if (args[i].equals ("-banner"))
        { banner = true; break; }
    if (banner) MoksaUtil.printBanner (out);

    PrologEngine engine = new PrologEngine ();

    // XXX need some way of finding this stuff, including searching classpath.

    // load the parser.
    engine.packageManager.loadModule (engine, 
                                      new MiniPrologParser (engine.io), 
                                      "parser.prolog");

    // load the system predicates.
    engine.loadModule ("system.prolog");

    // XXX probably set up default input/output streams based on our parms.

    PrologParser parser = engine.io.parser;

    // --- no args: print help.
    if (args.length == 0)
      {
        printHelp (out);
        return;
      }

    // --- read args, executing as we go.
    // XXX this was written before Cloptus, so go figure.
    for (int i=0; i<args.length; i++)
      {
        String arg = args[i];

        if (arg.equals ("-rule"))
          {
            if (i+1 >= args.length)
              { usageError (err, "Expected rule after '-rule'"); return; }

            String rule = args[++i];

            // --- parse rule
            CompoundTerm r;
            try {
              r = (CompoundTerm)parser.parseClause (engine.io, rule);
            } catch (IOException ex) {
              throw new PrologException ("Could not parse term '"+rule+"'",ex);
            }

            // --- install rule.
            engine.asserta (engine.compileRule (r));
          }
        else if (arg.equals ("-goal"))
          {
            if (i+1 >= args.length)
              { usageError (err, "Expected goal after '-goal'"); return; }

            String goal = args[++i];

            // --- parse goal
            CompoundTerm p;
            try {
              p = (CompoundTerm)parser.parseTerm (engine.io, goal);
            } catch (IOException ex) {
              throw new PrologException ("Could not parse term '"+goal+"'",ex);
            }

            // --- invoke goal
            Variable[] solution = engine.solve (p);
            if (solution != null)
              {
                out.println ("'"+goal+"' succeeded with bindings:");
                for (int varNum=0; varNum<solution.length; varNum++)
                  out.println ("     "+
                               solution[varNum].getName ()+" = "+
                               solution[varNum].deref ().tag ());
              }
            else
              {
                out.println ("'"+p.tag ()+"' failed.");
              }
          }
        else if (arg.equals ("-banner"))
          {
            // ignore.
          }
        else if (arg.endsWith ("-help") || arg.equals ("-h") ||
                 arg.endsWith ("?"))
          {
            printHelp (out);
            return;
          }
        else if (arg.equals ("-src"))
          {
            if (i+1 >= args.length)
              { usageError (err, "Expected filename after '-src'"); return; }

            String fileName = args[++i];
            // --- read prolog file
            engine.loadModule (fileName);
          }
        else if (arg.startsWith ("-"))
          {
            usageError (err, "Unrecognized option "+arg);
            return;
          }
        else
          {
            String fileName = arg;
            // --- read prolog file
            engine.loadModule (fileName);
          }
      }
  }

  public static void usageError (PrintWriter out, String msg)
  {
    out.println ("Usage error: "+msg);
    out.println ();
    printUsage (out);
    // exit?
  }

  public static void printHelp (PrintWriter out)
  {
    MoksaUtil.printBanner (out);
    out.println ();
    printUsage (out);
    // exit?
  }

  public static void printUsage (PrintWriter out)
  {
    out.println ("Usage:");
    out.println ("  java "+MoksaProlog.class.getName ()+" [options]");
    out.println (" Prolog Options (executed in order of appearance):");
    out.println ("  [-src] <fileName>       Read and exec Prolog source from <fileName>");
    out.println ("  -rule <rule>          Define rule <rule>");
    out.println ("  -goal <goal>          Execute goal <goal> on startup");
    //out.println ("  -flag <atom> <term>   Set Prolog flag <atom> to <term>");

    out.println (" Misc Options:");
    out.println ("  -banner               Print a copyright banner");
    out.println ("  -help                 Print this help message");
    out.println ();
  }

}
