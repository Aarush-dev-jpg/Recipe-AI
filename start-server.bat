@echo off
echo Starting Recipe Recommender Server...
cd /d %~dp0
java -Dorg.eclipse.jetty.LEVEL=DEBUG -jar build/libs/recipe-recommender.jar
pause