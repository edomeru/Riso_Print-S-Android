#!/bin/sh

if [ $# -ne 3 ]
then
	echo " this creates source archive for SDA based on the specified OS folder name"
	echo ""
	echo " usage:"
	echo "   archive.sh <version string> <date time stamp> <OS folder name>"
	echo ""
	echo " sample:"
	echo "   archive.sh 2.0.2.0 20160729 iOS"
	echo ""
	echo "   - creates archive RISO-PRINTS-V2.0.2.0_iOS_20160729.zip. Archive contains the sources in the iOS and CommonLibrary folders"
	exit  
fi

VERSION=$1
DATE=$2
OS=$3

DIRECTORIES="${OS} CommonLibrary"

git archive --worktree-attributes -o "RISO-PRINT-S-V${VERSION}_${OS}_${DATE}.zip" --format=zip HEAD $DIRECTORIES 