#!/bin/bash

pkill -ec java

sleep 10

java -jar  /jacoco/jacococli.jar report /jacoco/cov/app.exec --classfiles /app/target/classes/ --xml /jacoco/cov/app.xml

sleep 5
