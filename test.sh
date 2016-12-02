#!/bin/sh
WORKDIR=`dirname $0`
MAVEN_OPTS="-Dstory.name=tt -Dstory.home=/media/zhh/FileStorage/IdeaProjects/JavaBDD/src/main/java/com/autotest/bdd/story/ -Dwebdriver.chrome.driver=/home/zhh/Downloads/chromedriver/2.24/chromedriver -Ddependency.locations.enabled=false"

cd $WORKDIR
mvn clean integration-test -e $MAVEN_OPTS
