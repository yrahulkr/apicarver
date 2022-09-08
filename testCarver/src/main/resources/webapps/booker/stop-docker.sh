#!/bin/bash
cov="${1:-coverage}"
docker exec booker /app/exitScript.sh
sleep 30
docker cp booker:/jacoco/cov  $cov
docker stop booker
docker rm booker
