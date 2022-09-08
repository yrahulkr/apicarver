#!/bin/bash
cov="${1:-coverage}"
docker exec parabank /home/exitScript.sh
docker cp parabank:/tomcat/jacoco/cov $cov
docker stop parabank
docker rm parabank
