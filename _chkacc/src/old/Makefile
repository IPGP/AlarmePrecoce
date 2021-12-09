########################################################################
#                                                                      #
# Copyright (C) 2014 OVSM/IPGP                                         #
#                                                                      #
# This program is free software: you can redistribute it and/or modify #
# it under the terms of the GNU General Public License as published by #
# the Free Software Foundation, either version 3 of the License, or    #
# (at your option) any later version.                                  #
#                                                                      #
# This program is distributed in the hope that it will be useful,      #
# but WITHOUT ANY WARRANTY; without even the implied warranty of       #
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        #
# GNU General Public License for more details.                         #
#                                                                      #
# This program is part of 'Projet TSUAREG - INTERREG IV Caraïbes'.     #
# It has been co-financed by the European Union and le Minitère de     #
# l'Ecologie, du Développement Durable, des Transports et du Logement. #
#                                                                      #
########################################################################

#
# Makefile for 'chkacc'.
#
# Type 'make' or 'make chkacc' to create the executable.
# Type 'make clean' to delete all temporaries.
#

CC = g++

# build target specs
CFLAGS = -O3 `xml2-config --cflags --libs`
OUT_DIR = obj
LIBS = `xml2-config --libs`

# first target entry is the target invoked when typing 'make'
default: chkacc

chkacc: $(OUT_DIR)/configfile.cpp.o $(OUT_DIR)/main.cpp.o $(OUT_DIR)/xmlshakemapparser.cpp.o
	$(CC) $(CFLAGS) -o chkacc $(OUT_DIR)/configfile.cpp.o $(OUT_DIR)/main.cpp.o $(OUT_DIR)/xmlshakemapparser.cpp.o $(LIBS)

#$(OUT_DIR)/xmlshakemapparser.cpp.o: lib/xmlshakemapparser.h
#	$(CC) $(CFLAGS) -o $(OUT_DIR)/xmlshakemapparser.cpp.o -c lib/xmlshakemapparser.cpp
$(OUT_DIR)/xmlshakemapparser.cpp.o: lib/xmlshakemapparser.cpp lib/xmlshakemapparser.h
	$(CC) $(CFLAGS) -o $(OUT_DIR)/xmlshakemapparser.cpp.o -c lib/xmlshakemapparser.cpp

$(OUT_DIR)/configfile.cpp.o: lib/configfile/configfile.cpp lib/configfile/configfile.h
	$(CC) $(CFLAGS) -o $(OUT_DIR)/configfile.cpp.o -c lib/configfile/configfile.cpp

$(OUT_DIR)/main.cpp.o: main.cpp lib/xmlshakemapparser.h \
 lib/configfile/configfile.h lib/tclap/CmdLine.h lib/tclap/SwitchArg.h lib/tclap/Arg.h \
 lib/tclap/ArgException.h lib/tclap/Visitor.h lib/tclap/CmdLineInterface.h \
 lib/tclap/ArgTraits.h lib/tclap/StandardTraits.h lib/tclap/MultiSwitchArg.h \
 lib/tclap/UnlabeledValueArg.h lib/tclap/ValueArg.h lib/tclap/Constraint.h \
 lib/tclap/OptionalUnlabeledTracker.h lib/tclap/UnlabeledMultiArg.h \
 lib/tclap/MultiArg.h lib/tclap/XorHandler.h lib/tclap/HelpVisitor.h \
 lib/tclap/CmdLineOutput.h lib/tclap/VersionVisitor.h lib/tclap/IgnoreRestVisitor.h \
 lib/tclap/StdOutput.h lib/tclap/ValuesConstraint.h
	$(CC) $(CFLAGS) -o $(OUT_DIR)/main.cpp.o -c main.cpp

#$(OUT_DIR)/xmlshakemapparser.cpp.o: lib/xmlshakemapparser.cpp lib/xmlshakemapparser.h
#	$(CC) $(CFLAGS) -o $(OUT_DIR)/xmlshakemapparser.cpp.o -c lib/xmlshakemapparser.cpp

clean:
	rm -f chkacc $(OUT_DIR)/*.o
