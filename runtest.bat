@echo off
setlocal


set MAVEN_OPTS="-Dstory.name=tt -Dstory.home=/media/zhh/FileStorage/IdeaProjects/JavaBDD/src/main/java/com/autotest/bdd/story/ -Dwebdriver.chrome.driver=/home/zhh/Downloads/chromedriver/2.24/chromedriver -Ddependency.locations.enabled=false"

call mvn clean integration-test -e -q %MAVEN_OPTS%