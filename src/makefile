JCLASSDIR = ../bin
JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) -d $(JCLASSDIR)/. $(JFLAGS) $*.java

CLASSES = \
	SynchroViewer.java 

default: classes

classes: $(CLASSES:.java=.class)

run:
	cp icons ../bin/ -r; cd $(JCLASSDIR); java SynchroViewer

clean:
	find ../ -type f -name '*.class' -delete
