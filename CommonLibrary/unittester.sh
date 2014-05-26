if [ ! -d _coverage ]
then
    mkdir _coverage
fi
     
lcov --gcov-tool gcov-4.2 --directory . --zerocounters

#make clean
#make check
./tests_app

lcov --gcov-tool gcov-4.2 --directory . --capture --rc lcov_branch_coverage=1 --output-file _coverage/raw.info
lcov --gcov-tool gcov-4.2 --rc lcov_branch_coverage=1 --e _coverage/raw.info "*/src/*"  -o _coverage/src.info
lcov --gcov-tool gcov-4.2 --rc lcov_branch_coverage=1 --e _coverage/raw.info "*/tests/*"  -o _coverage/tests.info

genhtml --rc lcov_branch_coverage=1 -o _coverage/src _coverage/src.info
genhtml --rc lcov_branch_coverage=1 -o _coverage/tests _coverage/tests.info
