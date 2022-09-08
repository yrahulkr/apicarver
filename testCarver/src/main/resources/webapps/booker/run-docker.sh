#!/bin/bash
docker run -td -p 8080:8080 -p 3000-3006:3000-3006 --entrypoint "/app/entrypoint.sh" --name booker webappdockers/restful-booker:latest
sleep 30
