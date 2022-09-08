#!/bin/bash
java -jar /tomcat/jacoco/jacococli.jar report /tomcat/jacoco/cov/jacoco.exec --classfiles /tomcat/webapps/parabank-3.0.0-SNAPSHOT/WEB-INF/classes/ --xml /tomcat/jacoco/cov/cov.xml
