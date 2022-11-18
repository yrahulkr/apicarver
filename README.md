# Requirements
- JDK-8 & Maven, Python-3, Google Chrome 

# Installation
- install crawljax 
```
cd crawljax-private 
mvn clean install -DskipTests
```
- package testCarver project
```
 cd testCarver
 mvn clean package -DskipTests
```

# Usage
- Running Test Carver project
> With Python
```
cd testCarver/pythonCode
python runCarver.py
```
> or Java
```
cd testCarver
java -cp ./target/testCarver-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.apicarv.testCarver.Main <subject> <crawl-time>
```

# Experimental data
- Available at https://zenodo.org/record/7058905 <br>
 - Place the folders <code>out</code> and <code>crawlOut</code> under <code>testCarver</code> folder if you want to use experimental data with testCarver project

# API specification inconsistencies.
We contacted the developers to find out if the inconsistencies we found were useful. Here are the links to the GitHub issues

- https://github.com/mwinteringham/restful-booker-platform/issues/223
- https://github.com/cirosantilli/node-express-sequelize-nextjs-realworld-example-app/issues/43
- https://github.com/amanganiello90/java-angular-web-app/issues/32
- https://github.com/Antardeep/Medical-Clinic-Web-App/issues/1
- https://github.com/parasoft/parabank/issues/32
- https://github.com/webtutsplus/ecommerce-backend/issues/19
- https://github.com/spring-petclinic/spring-petclinic-rest/issues/101
