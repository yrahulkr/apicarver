#!/bin/bash
docker run -td -p 8080:8080 --entrypoint "/home/entrypoint.sh" --name "parabank" webappdockers/parabank:latest
