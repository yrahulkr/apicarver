#!/bin/bash
export NVM_DIR="$([ -z "${XDG_CONFIG_HOME-}" ] && printf %s "${HOME}/.nvm" || printf %s "${XDG_CONFIG_HOME}/nvm")"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" # This loads nvm
cd Backend
#mvn spring-boot:run &
java -javaagent:/jacoco/jacocoagent.jar=destfile=/jacoco/cov/app.exec,output=file,append=false -jar target/project2-0.0.1-SNAPSHOT.jar &
cd ../frontend
/root/.nvm/versions/node/v14.19.1/bin/npm run start
