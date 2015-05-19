#!/bin/sh
# For now this build shell script will only build on the clonpcxx where xx=11-19 (64-bit)
rootcint -f guifc_Dict.cxx -c -p guifc.h guifc_LinkDef.h
g++ -W -Wall -Wshadow -Wstrict-aliasing -pthread -m32 -I${ROOTSYS}/include  -c fcmon.cxx
g++ -W -Wall -Wshadow -Wstrict-aliasing -pthread -m32 -I${ROOTSYS}/include  -c guifc_Dict.cxx
g++ -W -Wall -Wshadow -Wstrict-aliasing -pthread -m32 -I${ROOTSYS}/include -L${ROOTSYS}/lib -lCore -lCint -lRIO -lNet -lHist -lGraf -lGraf3d -lGpad -lTree -lRint -lPostscript -lMatrix -lPhysics -lMathCore -lThread -pthread -lm -ldl -rdynamic -L${ROOTSYS}/lib -lGui -o fcmon fcmon.o  guifc_Dict.o

