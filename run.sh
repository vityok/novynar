#!/bin/sh

DERBY_HOME="$JAVA_HOME/db"
DERBY_JAR="$DERBY_HOME/lib/derby.jar"

echo "Derby is located at: $DERBY_JAR"

OPML_FILE="$HOME/.novinar/feeds.opml"
FEEDS_DB="$HOME/.novinar/feeds_db"

java -cp $DERBY_JAR:build/jar/novinar.jar \
     "-Dorg.bb.vityok.novinar.opml_file=$OPML_FILE" \
     "-Dorg.bb.vityok.novinar.db_dir=$FEEDS_DB" \
     org.bb.vityok.novinar.Main

     
