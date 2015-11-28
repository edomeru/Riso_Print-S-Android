if [ ! -d _coverage ]
then
    mkdir _coverage
fi
     
lcov --gcov-tool gcov --directory . --zerocounters

make check

lcov --gcov-tool gcov --directory . --capture --rc lcov_branch_coverage=1 --output-file _coverage/raw.info
lcov --gcov-tool gcov --rc lcov_branch_coverage=1 --e _coverage/raw.info "*/src/*"  -o _coverage/src.info
lcov --gcov-tool gcov --rc lcov_branch_coverage=1 --e _coverage/raw.info "*/tests/*"  -o _coverage/tests.info

genhtml --rc lcov_branch_coverage=1 -o _coverage/src _coverage/src.info
genhtml --rc lcov_branch_coverage=1 -o _coverage/tests _coverage/tests.info
