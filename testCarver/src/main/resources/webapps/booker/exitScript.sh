#!/bin/bash


pkill -ec java

sleep 10

java -jar /jacoco/jacococli.jar report  /jacoco/cov/auth.exec --classfiles /app/auth/target/classes/ --xml /jacoco/cov/auth.xml
java -jar /jacoco/jacococli.jar report  /jacoco/cov/booking.exec --classfiles /app/booking/target/classes/ --xml /jacoco/cov/booking.xml
java -jar /jacoco/jacococli.jar report  /jacoco/cov/room.exec --classfiles /app/room/target/classes/ --xml /jacoco/cov/room.xml
java -jar /jacoco/jacococli.jar report  /jacoco/cov/report.exec --classfiles /app/report/target/classes/ --xml /jacoco/cov/report.xml
java -jar /jacoco/jacococli.jar report  /jacoco/cov/branding.exec --classfiles /app/branding/target/classes/ --xml /jacoco/cov/branding.xml
java -jar /jacoco/jacococli.jar report  /jacoco/cov/message.exec --classfiles /app/message/target/classes/ --xml /jacoco/cov/message.xml
java -jar /jacoco/jacococli.jar report  /jacoco/cov/assets.exec --classfiles /app/assets/api/target/classes/ --xml /jacoco/cov/assets.xml


sleep 5 



