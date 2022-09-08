#!/bin/bash
cov="${1:-coverage}"
docker exec realworld pkill -ec node
sleep 30
docker cp realworld:/app/nyc_output  $cov
docker cp realworld:/app/.nyc_output $cov/raw
docker stop realworld
docker rm realworld
