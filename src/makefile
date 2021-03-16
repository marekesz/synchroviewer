JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	SynchroViewer.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	find . -type f -name '*.class' -delete
