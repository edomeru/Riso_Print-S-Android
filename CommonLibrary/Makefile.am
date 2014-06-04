lib_LTLIBRARIES = libcommon.la
libcommon_la_SOURCES = src/printsettings.h src/printsettings.c src/directprint.c src/snmp.c
libcommon_la_CFLAGS = -Iinclude -I3rdParty/net-snmp/include/@SUPPORTED_HOST@
include_HEADERS = include/common.h

# Unit Test

TESTS = tests_app

check_LTLIBRARIES = libgtest.la
libgtest_la_SOURCES = gtest/gtest-all.cc gtest/gtest.h

check_PROGRAMS = tests_app
tests_app_SOURCES = tests/main.cpp tests/printsettings_test.cpp tests/directprint_test.cpp tests/snmp_test.cpp tests/util_test.cpp
tests_app_SOURCES += src/printsettings.c src/directprint.c src/snmp.c src/util.c
tests_app_CFLAGS = -I3rdParty/net-snmp/include/@SUPPORTED_HOST@ -Iinclude -Isrc -fprofile-arcs -ftest-coverage -DIS_IN_UT
tests_app_CXXFLAGS = -I3rdParty/net-snmp/include/@SUPPORTED_HOST@ -I. -Iinclude -Isrc -fprofile-arcs -ftest-coverage -DIS_IN_UT
tests_app_LDFLAGS = -frprofile-arcs -fno-inline -lpthread
tests_app_LDADD = libgtest.la 3rdParty/net-snmp/lib/@SUPPORTED_HOST@/libnetsnmp.a

CLEANFILES = *.gcda *.gcno src/*.gcda src/*.gcno tests/*.gcda tests/*.gcno
