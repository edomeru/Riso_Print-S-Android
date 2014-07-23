AC_INIT([common], [1.0])
AC_CONFIG_AUX_DIR([build-aux])
AC_CONFIG_MACRO_DIR([m4])
AC_CANONICAL_SYSTEM
AM_INIT_AUTOMAKE([foreign -Wall subdir-objects])
AC_PROG_CC
AC_PROG_CXX
AM_PROG_AR
LT_INIT
AC_CONFIG_HEADERS([config.h])
AC_CONFIG_FILES([Makefile])

# Check if DARWIN
darwin_host=.*darwin.*
echo $host | grep -xq $darwin_host
AS_IF([test x$? = x0 ],
      [AC_SUBST([SUPPORTED_HOST],["darwin"])])

AC_OUTPUT
