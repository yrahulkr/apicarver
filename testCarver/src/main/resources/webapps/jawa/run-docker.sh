#!/bin/bash
docker run -td -p 8080:8081 --entrypoint "/app/entrypoint.sh" --name jawa webappdockers/java-angular-web-app:latest
