#!/bin/sh

docker run -td -p 8080:8080 -p 3000:3000 --entrypoint /app/entrypoint.sh --name medical webappdockers/medical:latest /bin/bash
