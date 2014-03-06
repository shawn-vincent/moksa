
#
# Makefile for the Moksa project
#
# Adapted from a Meteko Makefile by David Ziegler.
# Adapted by Shawn Vincent.
#
# Note this is kindof wierd.  These days I'd probably just use an Ant
# file and be done with it.  That said, there's something kindof
# nostalgic about make.
#

#
# Prerequisite environment variable:
#
#   MOKSA must be set to the root of the Moksa source tree.  For
#   example, on my machine, it is set to /users/svincent/moksa
#
# Do not put a drive letter into this path.  It confuses some versions
# of the Make utility.  Put it in your Cygnus root drive.  I did
# mention that you need Cygnus tools to compile this on a Win32
# platform, no?
#

MOKSA=~/Downloads/moksa
TEMP=/tmp


# Turn off built-in rules to speed things up a bit and reduce confusion.
MAKEFLAGS := $(MAKEFLAGS) -r

# Setup some 
ROOT=$(subst \,/,$(MOKSA))
TMP=$(subst \,/,$(TEMP))
JARSCRATCH=$(TMP)/moksaJarScratch
COMPILELIST=$(TMP)/moksaCompile.lst
JAVAC = javac
JAVACOPT = -g
JAVA = java
JDKROOT=c:/spe/jdk1.3

.PHONY: all compile clean javadoc tags buildUtils always backup prologLibs jar

all: compile prologLibs

debug:
	echo root==$(ROOT)
	echo $(ROOT)/com/svincent/tools/FindUpdatedSources.class

update: 
	cd $(ROOT); cvs update -d -P

commit: 
	cd $(ROOT); cvs commit

ezcommit: 
	cd $(ROOT); cvs -ecat commit

compile: buildUtils
	java com.svincent.tools.FindUpdatedSources $(ROOT)/com > $(COMPILELIST)
	if [ -s $(COMPILELIST) ]; then $(JAVAC) $(JAVACOPT) @$(COMPILELIST); fi

# Note: The way we are running javac below (using xargs) can result in the
#       whole system getting compiled several times.  So this method should
#       not be used for normal builds of the system (only when one wants a
#       second opinion on a vexing error message from jikes).
compileUsingJavac: buildUtils
	java com.svincent.tools.FindUpdatedSources $(ROOT)/com > $(COMPILELIST)
	if [ -s $(COMPILELIST) ]; then cat $(COMPILELIST) | tr \\\\ / | xargs --max-args=500 -P 1 javac -g; fi

clean:
	rm -f `find $(ROOT)/com -name '*.class'`

tags:
	find $(ROOT)/com -name '*.java' | etags --member -l java -o $(ROOT)/TAGS -

jdktags:
	find $(JDKROOT)/src -name '*.java' | etags --member -l java -o $(ROOT)/JDKTAGS -

# add in --members once we get new etags
count:
	find $(ROOT)/com  -name '*.java' | xargs --max-args=100 -P 1 cat | wc -l

# makes javadoc docs
javadoc:
	javadoc \
	    -splitIndex -version -author -use \
	    -sourcepath $(ROOT) \
	    -d $(ROOT)/doc/api \
	    -windowtitle "Moksa Prolog - Java API" \
	    -doctitle "Moksa Prolog - Java API" \
	    -bottom "<hr><p><em>This is documentation for Moksa Prolog, which can be found at <a href='http://www.svincent.com/moksa/'>http://www.svincent.com/moksa/</a></em></p>  <p><em>Copyright &copy; 1999 <a href='http://www.svincent.com/shawn/'>Shawn P. Vincent</a>.</em></p> <p><em>This program is free software; you can redistribute it and/or modify it under the terms of the <a href='http://www.gnu.org/copyleft/gpl.html'>GNU General Public License</a> as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.</em></p>" \
	    @$(ROOT)/package.list

jar:
	cd $(ROOT);jar cfm moksa.jar com/svincent/moksa/moksa.manifest \
	    com/svincent/moksa/moksa.manifest `find com -name '*.class'`

# Package up Moksa for distribution.
# XXX This is a little hacky, because I don't know how to strip
#     prefixes off of stuff in ZIP.
package: all javadoc jar
	rm -f $(ROOT)/../moksa.zip
	cd $(ROOT)/..;zip -r moksa.zip moksa

# build the build utilities.
buildUtils: $(ROOT)/com/svincent/tools/FindUpdatedSources.class

$(ROOT)/com/svincent/tools/FindUpdatedSources.class: $(ROOT)/com/svincent/tools/FindUpdatedSources.java
	$(JAVAC) $(JAVAOPTS) $< 

backup:
	rm -f $(ROOT)/`date +src-%b-%d-%Y.zip`
	find $(ROOT)/com -not -name "*.class" -not -name "*~" | zip -@ $(ROOT)/`date +src-%b-%d-%Y.zip`




