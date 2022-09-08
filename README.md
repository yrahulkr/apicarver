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
