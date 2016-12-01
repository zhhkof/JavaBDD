#!/bin/bash

RUNDIR=`pwd`
WORKDIR=`dirname $0`

_usage () {
	echo "$0 [ -h STORY_HOME ] [ -c ConfigFile ] <STORY_NAME>"
}

echo "get arguments..."

while getopts :c:h: OPTION
do
	case $OPTION in
	c)
		CONFTAG=$OPTARG
		MAVEN_OPTS="-Dconfig.name=$OPTARG" $MAVEN_OPTS
		;;
	h)
		STORY_HOME=$OPTARG
		;;
	\?) # usage statment
		_usage
		exit 1
		;;
		esac
done

shift `expr $OPTIND - 1`

STORY_NAME=$1

if [ x$STORY_HOME == x ]
then
	echo "Error: the enviroment parameter STORY_HOME is not exists!"
	exit 1
fi

if [ ! -d $STORY_HOME ]
then
	echo "Error: the directory $STORY_HOME is not exists!"
	exit 1
fi

PATH=$PATH:$WORKDIR/lnxdrv
MAVEN_OPTS="-Dstory.home=$STORY_HOME -Ddependency.locations.enabled=false $MAVEN_OPTS"
if [ x$STORY_NAME != x ]
then
	MAVEN_OPTS="-Dstory.name=$STORY_NAME $MAVEN_OPTS"
else
	_usage
	exit 1
fi
if [ x$ResultKey != x ]
then
	MAVEN_OPTS="-Dresult.key=$ResultKey $MAVEN_OPTS"
fi
if [ x$ResultsUrl != x ]
then
	MAVEN_OPTS="-Dresult.url=$ResultsUrl $MAVEN_OPTS"
fi
if [ -f $WORKDIR/fail.txt ]
then
	echo "Error: testCtl run failed"
	exit 1
fi

cd $WORKDIR
mvn clean integration-test -e -q $MAVEN_OPTS
if [ $? -ne 0 ]
then
	exit $?
fi

if [ -d $WORKDIR/target/jbehave/view ]
then
	mkdir -p $RUNDIR/report
	cp -rf $WORKDIR/target/jbehave/view $RUNDIR/report/$STORY_NAME
else
	echo "Oops, the directory $WORKDIR/target/jbehave/view is not exists!"
fi
