# Compiler
JAVAC = javac

# Compiler flags
JFLAGS = -g -d bin -cp $(LIBS)

# Source files directory
SRCDIR = src

# Libraries directory
LIBDIR = lib

# Libraries
LIBS = $(LIBDIR)/*

# Source files
SOURCES := $(wildcard $(SRCDIR)/*.java)

# Compiled files
CLASSES := $(SOURCES:$(SRCDIR)/%.java=bin/%.class)

# Main class
MAIN_CLASS = Main

# Default target
all: $(CLASSES)

# Compile Java source files
bin/%.class: $(SRCDIR)/%.java
	$(JAVAC) $(JFLAGS) $<

# Run the application
run: $(CLASSES)
	java -cp bin:$(LIBS) $(MAIN_CLASS)

# Clean compiled files
clean:
	rm -rf bin/*

.PHONY: all run clean
