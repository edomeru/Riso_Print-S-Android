# c compiler
CC = cc

OUTPUT_OPTION=-MMD -MP -o $@

# net-snmp
NETSNMP = 3rdParty/net-snmp
ifeq ($(OS), Windows_NT)
NETSNMPLIB = $(NETSNMP)/lib/winnt
WININET = standalone/wininet_ntoa.c
else
NETSNMPLIB = $(NETSNMP)/lib/darwin
endif

# compiler flags
CFLAGS = -Wall -Iinclude -I$(NETSNMP)/include -std=c99

CMNLIBSRC = src/directprint.c src/snmp.c src/printsettings.c
CMNLIBOBJ = $(CMNLIBSRC:.c=.o)
CMNLIBOUT = lib/libcommon.a
CMNLIBDEP = $(CMNLIBOBJ:.o=.d)

#PRINTSRC  = standalone/printsample.cpp standalone/printcallback.cpp
#PRINTOBJ  = $(PRINTSRC:.cpp=.o)
#PRINTOUT  = printsample
#PRINTDEP  = $(PRINTOBJ:.o=.d)

SNMPSRC   = standalone/snmpsample.c $(WININET)
SNMPOBJ   = $(SNMPSRC:.c=.o)
SNMPOUT   = snmpsample
SNMPDEP   = $(SNMPOBJ:.o=.d)

SRC = $(CMNLIBSRC) $(SNMPSRC)# $(PRINTSRC) $(SNMPSRC)
OBJ = $(CMNLIBOBJ) $(SNMPOBJ) # $(PRINTOBJ) $(SNMPOBJ)
DEP = $(CMNLIBDEP) $(SNMPDEP)# $(PRINTDEP) $(SNMPDEP)

.PHONY : clean

all: lib snmpsample

#printstandalone: $(PRINTOBJ) lib
#	$(CC) -o $(PRINTOUT) $(PRINTOBJ) $(CMNLIBOUT)

snmpsample: $(SNMPOBJ) lib
	$(CC) -o $(SNMPOUT) $(SNMPOBJ) $(CMNLIBOUT) $(NETSNMPLIB)/libnetsnmp.a

lib: $(CMNLIBOBJ)
	ar rcs $(CMNLIBOUT) $(CMNLIBOBJ)

-include $(DEP)

clean:
	rm -f $(OBJ) $(DEP) $(CMNLIBOUT) $(SNMPOUT)
