SUBDIRS := src

all: $(SUBDIRS)
$(SUBDIRS):
		$(MAKE) -C $@

.PHONY: all $(SUBDIRS)

clean:
	rm bin -r

run:
	cd bin; java SynchroViewer