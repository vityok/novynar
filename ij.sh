#!/bin/sh

# script to launch the ij Apache Derby command-line tool

if [ -z "${DERBY_HOME}" ]; then
    DERBY_HOME="${JAVA_HOME}/db"
fi

DERBY_JAR="${DERBY_HOME}/lib/derby.jar:$DERBY_HOME/lib/derbytools.jar"

echo "Using Derby jars location: ${DERBY_JAR}"

OPML_FILE="$HOME/.novinar/feeds.opml"
FEEDS_DB="$HOME/.novinar/feeds_db"

echo "To connect, run the following command:"
echo "CONNECT 'jdbc:derby:$FEEDS_DB';"
java -cp $DERBY_JAR org.apache.derby.tools.ij
