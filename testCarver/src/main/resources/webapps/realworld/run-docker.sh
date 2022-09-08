#!/bin/bash
docker run -td -p 3000:3000 --entrypoint "/app/entrypoint.sh" --name realworld webappdockers/realworld:latest
