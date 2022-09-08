#!/bin/bash
docker run -d -it -p 9966:9966 --name=petclinic --entrypoint=/app/entrypoint.sh webappdockers/petclinic-rest:latest
docker run -d -p 8080:8080 --name=petclinic_ui webappdockers/petclinic-angular:latest
