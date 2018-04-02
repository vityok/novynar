#!/bin/sh

DERBY_HOME="$JAVA_HOME/db"
DERBY_JAR="$DERBY_HOME/lib/derby.jar"

java -classpath "$DERBY_JAR:build/libs/novinar-0.1.jar" org.bb.vityok.novinar.Main
