# MoksaProlog

MoksaProlog is a toy Prolog interpreter written in Java.

MoksaProlog is an offshoot of constraint programming research to do constraint-based RPG character generation.  I no longer have time to do this, so I've released MoksaProlog, in case anybody else would like to play with it.

MoksaProlog compiles Prolog source into Java byte codes that run on a simple Warren's Abstract Machine (WAM).


## Update: March 4, 2014: Updated to Java 1.7

So I brought this thing back to life, mostly out of nostalgia. It's pretty cool: a complete Prolog environment written in Java.  It even has a parser written in Prolog that it bootstraps itself onto.

Worth playing around with if you've ever wondered how a Prolog compiler or runtime works.  It's a pretty clean implementation.

There are also some cool libraries it's built on top of, including some neato Java bytecode generation utilities (Smalljava); some mathematics libraries (smath) that do things like calculate dice distributions and throw errors on overflow of primitive expressions; and other goodies.

## Usage

Compile as normal.  

```
ant
```

Current limitation: Ensure that the resulting jar file is in the same directory as the 'src' directory -- this is required in order to find the Prolog libraries.  The Prolog libraries are actually linked into the Jar, but the code's not smart enough right now to read things out of the Jar.

To test whether Moksa is installed properly on your system, you can use this:

```
java -jar moksa.jar test/testInstall.prolog
```

This should print out a friendly message.  If you get a horrible error, something's gone wrong.  You can email me (svincent@svincent.com) if you want help setting this up.

Note that in order for this message to print, MoksaProlog is parsing and compiling the Prolog-written grammar, loading the generated bytecodes into the JVM, using the parser to parse the specified file (and system libraries), loading those generated bytecodes, and then running them.

You can also get help using:

```
java -jar moksa.jar -help
```

## Using Prologc: the Moksa Prolog compiler

There is a rudimentary compiler shipped with Moksa which compiles Prolog source files into Java classes.  It can generate Java source, for your own edification and amusement, or generate bytecodes directly.

To use, go to the directory where the source file is (currently
necessary, unfortunately), and type something like:

```
java com.svincent.moksa.Prologc myPrologSource.prolog
```

To learn more about Prologc, you can type:

```
java com.svincent.moksa.Prologc -help
```


## License

MoksaProlog is released under the terms of the GNU General Public License.  Yes, that's viral.  If you want to use it in something that the GPL causes you trouble for, contact me at svincent@svincent.com, and we can work something out.
