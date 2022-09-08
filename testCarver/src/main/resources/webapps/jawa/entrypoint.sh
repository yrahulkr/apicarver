#!/bin/bash
cd /app
java -javaagent:/jacoco/jacocoagent.jar=destfile=/jacoco/cov/app.exec,output=file,append=false -jar   target/app.jar

while true; do 
    sleep 10
done
