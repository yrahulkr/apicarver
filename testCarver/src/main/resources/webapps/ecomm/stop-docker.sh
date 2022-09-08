#!/bin/bash
cov="${1:-coverage}"
docker exec ecomm /app/exitScript.sh
sleep 30
docker cp ecomm:/jacoco/cov  $cov
docker rm -f ecomm
docker rm -f ecomm-ui
