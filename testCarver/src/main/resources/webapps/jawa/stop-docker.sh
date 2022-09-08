#!/bin/bash
cov="${1:-coverage}"
docker exec jawa /app/exitScript.sh
sleep 30
docker cp jawa:/jacoco/cov  $cov
docker stop jawa
docker rm jawa
