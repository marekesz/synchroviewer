JCLASSDIR = ../bin
JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) -d $(JCLASSDIR)/. $(JFLAGS) $*.java; cp icons ../bin/icons -r

CLASSES = \
	SynchroViewer.java 

default: classes

classes: $(CLASSES:.java=.class)

run:
	cd $(JCLASSDIR); java SynchroViewer

clean:
	rm ../bin -r