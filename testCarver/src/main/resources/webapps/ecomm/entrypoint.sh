#!/bin/bash
#export NVM_DIR="$([ -z "${XDG_CONFIG_HOME-}" ] && printf %s "${HOME}/.nvm" || printf %s "${XDG_CONFIG_HOME}/nvm")"
#[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" # This loads nvm
#cd Backend
#mvn spring-boot:run &
mysql -u root -proot -e "create database ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci"
mysql -u root -proot ecommerce < /app/database-dump.sql
#cd /app-frontend/
#/root/.nvm/versions/node/v14.19.1/bin/npm run start
#nohup /root/.nvm/versions/node/v14.20.0/bin/npm run serve -- --port 8081 &
nohup java -javaagent:/jacoco/jacocoagent.jar=destfile=/jacoco/cov/cov.exec,output=file,append=false -jar /app/target/ecommerce-backend-0.0.1-SNAPSHOT.jar &

