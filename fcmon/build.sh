#!/bin/sh

rootcint -f guifc_Dict.cxx -c -p guifc.h guifc_LinkDef.h
g++ -W -Wall -Wshadow -Wstrict-aliasing -pthread -m64 -I/home/hpsrun/apps/root_v5.34.21.Linux-slc6-amd64-gcc4.4/include -I/home/hpsrun/.root -c fcmon.cxx
g++ -W -Wall -Wshadow -Wstrict-aliasing -pthread -m64 -I/home/hpsrun/apps/root_v5.34.21.Linux-slc6-amd64-gcc4.4/include -I/home/hpsrun/.root -c guifc_Dict.cxx
g++ -W -Wall -Wshadow -Wstrict-aliasing -pthread -m64 -I/home/hpsrun/apps/root_v5.34.21.Linux-slc6-amd64-gcc4.4/include -L/home/hpsrun/apps/root_v5.34.21.Linux-slc6-amd64-gcc4.4/lib -lCore -lCint -lRIO -lNet -lHist -lGraf -lGraf3d -lGpad -lTree -lRint -lPostscript -lMatrix -lPhysics -lMathCore -lThread -pthread -lm -ldl -rdynamic -L/home/hpsrun/apps/root_v5.34.21.Linux-slc6-amd64-gcc4.4/lib -lGui -I/home/hpsrun/.root -o fcmon fcmon.o  guifc_Dict.o

