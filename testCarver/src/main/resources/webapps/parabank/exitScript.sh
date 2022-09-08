#!/bin/bash

/tomcat/bin/catalina.sh stop

sleep 10

/tomcat/jacoco/getCov.sh

