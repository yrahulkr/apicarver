#!/bin/bash
BASEDIR=$1
#sed -i -e "s++/home//git+g" $BASEDIR/src/test/java/generated/GeneratedTests.java
#sed -i -e "s+BrowserType.CHROME,+BrowserType.CHROME_HEADLESS,+g" $BASEDIR/src/test/java/generated/GeneratedTests.java
sed -i -e "s+private final boolean mutate = true;+private final boolean mutate = false;+g" $BASEDIR/src/test/java/generated/GeneratedTests.java
sed -i -e "s+StateEquivalenceAssertionMode.HYBRID;+StateEquivalenceAssertionMode.DOM;+g" $BASEDIR/src/test/java/generated/GeneratedTests.java
sed -i -e "s+StateEquivalenceAssertionMode.DOM)+StateEquivalenceAssertionMode.HYBRID)+g" $BASEDIR/src/test/java/generated/GeneratedTests.java
sed -i -e "s+FragmentationPlugin+CrawlOverview+g" $BASEDIR/config.json


if [ $# -ge 2 ]
then
  plugin=$2
  echo $plugin
  sed -i -e "s+package generated;+package generated;import tests.${plugin}.${plugin}ManualPlugin;+g" $BASEDIR/src/test/java/generated/GeneratedTests.java
  sed -i -e "s+return builder.build();+builder.addPlugin(new ${plugin}ManualPlugin());return builder.build();+g" $BASEDIR/src/test/java/generated/GeneratedTests.java
fi

echo "Compiling generated tests source files..."
javac -cp "/TestCarving/testCarver/target/testCarver-0.0.1-SNAPSHOT-jar-with-dependencies.jar" "$BASEDIR/src/test/java//generated/GeneratedTests.java"
echo "Running generated tests..."
java -cp "/TestCarving/testCarver/target/testCarver-0.0.1-SNAPSHOT-jar-with-dependencies.jar":"$BASEDIR/src/test/java/" org.testng.TestNG $BASEDIR/testng.xml
