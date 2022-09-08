#!/bin/bash
cov="${1:-coverage}"
docker exec medical /app/exitScript.sh
sleep 30
docker cp medical:/jacoco/cov  $cov
docker stop medical
docker rm medical
