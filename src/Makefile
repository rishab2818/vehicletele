# Compiler
JAVAC = javac

# Compiler flags
ifeq ($(OS),Windows_NT)
    # Windows
    JFLAGS = -cp ".;$(LIBDIR)/*" -d bin
    RM = del /Q /S
    SEP = ;
else
    # Linux
    JFLAGS = -cp ".:$(LIBDIR)/*" -d bin
    RM = rm -rf
    SEP = :
endif

# Libraries directory
LIBDIR = lib

# Source file
SOURCE = Main.java

# Compiled file
CLASS = bin/Main.class

# Main class
MAIN_CLASS = Main

# Default target
all: $(CLASS)

# Compile Java source file
$(CLASS): $(SOURCE)
	$(JAVAC) $(JFLAGS) $(SOURCE)

# Run the application
run:
	java -cp "bin$(SEP)$(LIBDIR)/*" $(MAIN_CLASS)

# Clean compiled files
clean:
	$(RM) bin

.PHONY: all run clean
