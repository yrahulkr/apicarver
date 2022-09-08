#!/bin/bash
/tomcat/bin/catalina.sh start


deregister_runner() {
    echo "stopping tomcat"
    /tomcat/bin/catalina.sh stop
    sleep 5
    echo "getting coverage"
    /tomcat/jacoco/getCov.sh
    exit
}
trap deregister_runner SIGTERM

while true; do
    sleep 10
done
