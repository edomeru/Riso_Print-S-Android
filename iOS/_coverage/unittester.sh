# Load configuration
source config.sh

EX_PATH=$(pwd)

# Setup paths
RUN_DATA_PATH=$EX_PATH/$DATA_PATH/$UT_PATH
DERIVED_PATH=$EX_PATH/$DATA_PATH/$XCODE_DERIVED_DIR

if [ ! -d "$RUN_DATA_PATH" ]
then
    mkdir -p $RUN_DATA_PATH
fi


# Prepare build
cd "$PROJ_PATH"

PREFIX=$(pwd)

# Get object directory
OBJ_PATH=$(xcodebuild -project "$XCODE_PROJ.xcodeproj" -configuration $XCODE_CONFIG -scheme $XCODE_SCHEME -sdk $XCODE_SDK -derivedDataPath $DERIVED_PATH -showBuildSettings | grep OBJECT_FILE_DIR_normal | grep -o "/.*")/i386

# Clean and build
xcodebuild -project "$XCODE_PROJ.xcodeproj" -configuration $XCODE_CONFIG -scheme $XCODE_SCHEME -sdk $XCODE_SDK -derivedDataPath $DERIVED_PATH clean
WRITE_JUNIT_XML=1 JUNIT_XML_DIR="$EX_PATH/$DATA_PATH/result" xcodebuild -project "$XCODE_PROJ.xcodeproj" -configuration $XCODE_CONFIG -scheme $XCODE_SCHEME -sdk $XCODE_SDK -derivedDataPath $DERIVED_PATH build

# Reset counters
lcov --gcov-tool gcov-4.2 --directory "$OBJ_PATH" --zerocounters

GHUNIT_CLI=1 GHUNIT_AUTOSTART=1 GHUNIT_AUTOEXIT=1 WRITE_JUNIT_XML=1 JUNIT_XML_DIR="$EX_PATH/$DATA_PATH/result" xcodebuild -project "$XCODE_PROJ.xcodeproj" -configuration $XCODE_CONFIG -scheme $XCODE_SCHEME -sdk $XCODE_SDK -derivedDataPath $DERIVED_PATH

lcov --gcov-tool gcov-4.2 --directory "$OBJ_PATH" --capture --rc lcov_branch_coverage=1 --output-file "$RUN_DATA_PATH/raw.info"

# Extract app and tests
lcov --rc lcov_branch_coverage=1 --e "$RUN_DATA_PATH/raw.info" "$PATTERN_APP" -o "$RUN_DATA_PATH/$TITLE_APP.info"
lcov --rc lcov_branch_coverage=1 --e "$RUN_DATA_PATH/raw.info" "$PATTERN_TEST" -o "$RUN_DATA_PATH/$TITLE_TEST.info"

genhtml -p $PREFIX --rc lcov_branch_coverage=1 -o "$RUN_DATA_PATH/raw" "$RUN_DATA_PATH/raw.info"

# Extract app and tests
genhtml -o --rc lcov_branch_coverage=1 "$RUN_DATA_PATH/$TITLE_APP" "$RUN_DATA_PATH/$TITLE_APP.info"
genhtml -o --rc lcov_branch_coverage=1 "$RUN_DATA_PATH/$TITLE_TEST" "$RUN_DATA_PATH/$TITLE_TEST.info"

