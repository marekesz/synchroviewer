SUBDIRS := src

all: $(SUBDIRS)
$(SUBDIRS):
		$(MAKE) -C $@

.PHONY: all $(SUBDIRS)

clean:
	find ../ -type f -name '*.class' -delete
