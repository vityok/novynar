#!/bin/sh

if [ -z "${DERBY_HOME}" ]; then
    DERBY_HOME="${JAVA_HOME}/db"
fi

DERBY_JAR="${DERBY_HOME}/lib/derby.jar"

echo "Using derby.jar location: ${DERBY_JAR}"

if [ ! -f "${DERBY_JAR}" ]; then
    echo "Could not find Apache DB/Derby JAR file at the specified location. Aborting startup"
    exit 1
fi

OPML_FILE="${HOME}/.novinar/feeds.opml"
FEEDS_DB="${HOME}/.novinar/feeds_db"

java -cp "${DERBY_JAR}:build/jar/novinar.jar" \
     "-Dorg.bb.vityok.novinar.opml_file=${OPML_FILE}" \
     "-Dorg.bb.vityok.novinar.db_dir=${FEEDS_DB}" \
     org.bb.vityok.novinar.Main
