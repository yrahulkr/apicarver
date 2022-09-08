#!/bin/bash

pkill -ec java

sleep 10

java -jar  /jacoco/jacococli.jar report /jacoco/cov/cov.exec --classfiles /app/target/classes/ --xml /jacoco/cov/cov.xml

sleep 5
