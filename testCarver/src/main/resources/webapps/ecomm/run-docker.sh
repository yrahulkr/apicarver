#!/bin/sh

docker run -td -p 8081:8081 --name ecomm-ui --entrypoint ./entrypoint.sh webappdockers/ecomm-ui:latest

#docker run -td -p 8080:8080 -v mysql-data:/var/lib/mysql --name ecomm webappdockers/ecomm:latest
docker run -td -p 8080:8080 --name ecomm webappdockers/ecomm:latest
sleep 30
nohup docker exec ecomm /app/entrypoint.sh &
#docker exec ecomm /app-frontend/frontend.sh
#sleep 10
echo 'done'
