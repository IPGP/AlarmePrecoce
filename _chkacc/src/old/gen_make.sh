#!/bin/bash
# Copyright (c) 2009, Josef Nygrin, http://justcheckingonall.wordpress.com/
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY JOSEF NYGRIN ''AS IS'' AND ANY
# EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL JOSEF NYGRIN BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# This is a simple script to generate Makefile
# based on source files (.c, .cpp, .c++ and .cxx) present
# in current directory, using 'gcc -MM'.
# Requirements - bash, ls, gcc.
# Future extensions:
# - add support for subdirectories
# - more checks done (valid compiler, executable name, ...)
# - support for other -MM compatible compilers
# - support for other command line syntaxes
# - allow different compilers (and settings) for .c, .cpp and .cxx
# - place binaries in build dirs and add make target 'run'?
# - allow filtering of source files used for generation of Makefile

###############################################################################
# if no parameter passed, print out usage and quit
if [ $# -le '0' ]
then
printf "Simple Makefile generator, gen_make.sh, version 1.5.\n";
printf "Copyright (c) 2009, Josef Nygrin, http://justcheckingonall.wordpress.com/\n";
printf "\n";
printf "Usage:\n";
printf "    gen_make.sh exec [d|debug|r|release|dp|rp [add_flags [link_libs [compiler]]]]\n";
printf "\n";
printf "    exec ... name of executable file to build\n";
printf "    d|debug|r|release|dp|rp ... select build target\n"
printf "        'd' or 'debug' ... CFLAGS set to \"-g -Wall -DDEBUG\" (default)\n";
printf "        'r' or 'release' ... CFLAGS set to \"-O3\"\n";
printf "        'dp' or 'rp' ... with profiling, adds \"-p\" to CFLAGS\n";
printf "    add_flags ... additional flags to pass to compiler\n";
printf "    link_libs ... link libraries, passed in -lxxx format\n";
printf "    compiler ... compiler to use, default is g++\n";
printf "\n";
printf "Parameters have to appear in presented order.\n";
printf "Script also creates the output directory for binary files.\n";
printf "\n";
printf "Examples:\n";
printf "\n";
printf "gen_make.sh myexec > Makefile\n";
printf "    Creates Makefile for debug build of 'myexec' using source files\n";
printf "    in current directory and using g++ for compilation.\n";
printf "\n";
printf "gen_make.sh myexec r '-D_UNICODE -DUNICODE' > Makefile\n";
printf "    Creates Makefile for release build of 'myexec' using source files\n";
printf "    in current directory with UNICODE support enabled.\n";
printf "\n";
printf "gen_make.sh myexec '' '' '-lgd' cc > Makefile\n";
printf "    Creates Makefile for debug build of 'myexec' using source files\n";
printf "    in current directory, linking with libgd.a, and using cc.\n";
printf "\n";
printf "gen_make.sh myexec rp > Makefile\n";
printf "    Creates Makefile for release build of 'myexec' using source files\n";
printf "    in current directory, with profiling info included.\n";
printf "    To create the profile, run the built executable as usual - this will\n";
printf "    produce 'gmon.out' file, and then run 'gprof myexec > profile.txt',\n";
printf "    which will produce the text file 'profile.txt' with the profile data.\n";
printf "    See man pages for gprof(1) for more information.\n";
exit;
fi

###############################################################################
# set executable name from 1. parameter
exec="$1";

# determine executable's type
exec_type="exe";
if [[ "$exec" == lib*.a || "$exec" == *.lib ]]
then exec_type="lib";
elif [[ "$exec" == lib*.so || "$exec" == *.dll ]]
then exec_type="dll";
fi

###############################################################################
# check for build target (if set)
cflags="-g -Wall -DDEBUG";
out_dir="debug_build";
if [ $# -gt '1' ]
then
case "$2" in
"dp")
	cflags="-p -g -Wall -DDEBUG";
	out_dir="debug_profile";
	;;
"r")
	cflags="-O3";
	out_dir="release_build";
	;;
"release")
	cflags="-O3";
	out_dir="release_build";
	;;
"rp")
	cflags="-p -O3";
	out_dir="release_profile";
	;;
esac
fi

# dynamic link libs should be compiled with '-fPIC'
if [ "$exec_type" == "dll" ]
then cflags=$cflags" -fPIC"
fi

# create the binary output directory, if it doesn't exist yet
mkdir $out_dir &> /dev/null;

###############################################################################
# check for additional flags (if any)
addflags="";
if [ $# -gt '2' ]
then addflags="$3";
fi

###############################################################################
# check for link libraries (if any)
link_libs="";
if [ $# -gt '3' ]
then link_libs="$4";
fi

###############################################################################
# check for selected compiler (if any)
compiler="g++";
if [ $# -gt '4' ]
then compiler="$5";
fi

###############################################################################
# print out the common header
printf "#\n";
printf "# Makefile for '$exec'.\n";
printf "#\n";
printf "# Type 'make' or 'make $exec' to create the executable.\n";
printf "# Type 'make clean' to delete all temporaries.\n";
printf "#\n";
printf "\n";
printf "CC = $compiler\n";
printf "\n";
printf "# build target specs\n";
printf "CFLAGS = $cflags $addflags\n";
printf "OUT_DIR = $out_dir\n";
printf "LIBS = $link_libs\n";
printf "\n";
printf "# first target entry is the target invoked when typing 'make'\n";
printf "default: $exec\n";
printf "\n";

###############################################################################
# print for each file with provided extension ($1 - first
# parameter) string " $(OUT_DIR)/filename.o"
function bin_deps_for_ext {
	ls *.$1 &> /dev/null;
	# did the 'ls' exit with 0? (all ok)
	if [ $? -eq '0' ]
	then
		for i in `ls *.$1`
		do printf " \$(OUT_DIR)/$i.o";
		done
	fi
}

# print the binary's deps (depends on all object files,
# one for each source .c, .cpp and .cxx file)
# if the exec's name ends with '.a' or '.lib', treat it as static library
printf "$exec:";

# output binary depends on all created object files
bin_deps_for_ext c;
bin_deps_for_ext cpp;
bin_deps_for_ext c++;
bin_deps_for_ext cxx;
printf "\n";

# print command used to put the output binary together, depending on type
if [ "$exec_type" == "lib" ]
then printf "\tar rsc $exec";
elif [ "$exec_type" == "dll" ]
then printf "\t\$(CC) -shared -Wl,-soname,$exec -o $exec";
else
printf "\t\$(CC) \$(CFLAGS) -o $exec";
fi

# and print its input - all created object files
bin_deps_for_ext c;
bin_deps_for_ext cpp;
bin_deps_for_ext c++;
bin_deps_for_ext cxx;
printf " \$(LIBS)\n\n";

###############################################################################
# print out dependencies and build rules for all files
# with extension passed as parameter ($1)
function print_deps_for_ext {
	ls *.$1 &> /dev/null;
	# did the 'ls' exit with 0? (all ok)
	if [ $? -eq '0' ]
	then
		for i in `ls *.$1`
		do gcc -MM -MG -MT \$\(OUT_DIR\)/$i.o $i;
		printf "\t\$(CC) \$(CFLAGS) -o \$(OUT_DIR)/$i.o -c $i\n\n";
		done
	fi
}

# print out individual deps for object files
print_deps_for_ext c;
print_deps_for_ext cpp;
print_deps_for_ext c++;
print_deps_for_ext cxx;

###############################################################################
# add the 'clean up' target
printf "clean:\n"
printf "\trm -f $exec \$(OUT_DIR)/*.o\n\n";

###############################################################################
# add the 'create profile' target
#
# this target generates profile.txt using gprof(1),
# and profile.png using gprof2dot.py script and dot tool
printf "profile:\n"
printf "\tgprof $exec > profile.txt; ./gprof2dot.py profile.txt | dot -Tpng -o profile.png\n";
