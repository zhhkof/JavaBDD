#!/bin/sh
WORKDIR=`dirname $0`
MAVEN_OPTS="-Dstory.name=tt -Dstory.home=/media/zhh/FileStorage/IdeaProjects/JavaBDD/src/main/java/com/autotest/bdd/story/ -Dwebdriver.chrome.driver=/home/zhh/Downloads/chromedriver/2.24/chromedriver -Ddependency.locations.enabled=false"

#MAVEN_OPTS="-Dstory.name=tt -Dstory.home=/Users/ZHH/IdeaProjects/JavaBDD/src/main/java/com/autotest/bdd/story/ -Dwebdriver.chrome.driver=/Users/ZHH/IdeaProjects/JavaBDD/driver/chromedriver"

cd $WORKDIR
mvn clean integration-test -e $MAVEN_OPTS
