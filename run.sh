#!/bin/sh

if [ -z "${DERBY_HOME}" ]; then
    DERBY_HOME="${JAVA_HOME}/db"
fi

DERBY_JAR="${DERBY_HOME}/lib/derby.jar"

echo "Info: Using derby.jar location: ${DERBY_JAR}"

if [ ! -f "${DERBY_JAR}" ]; then
    echo "Error: Could not find Apache DB/Derby JAR file at the specified location. Aborting startup"
    exit 1
fi

if [ ! -d "${PATH_TO_FX}" ]; then
    echo "Warning: PATH_TO_FX environment variable not set or set to a wrong location"
fi

echo "Info: Using JavaFX location: ${PATH_TO_FX}"

OPML_FILE="${HOME}/.novinar/feeds.opml"
FEEDS_DB="${HOME}/.novinar/feeds_db"

# JAVA=~/local/jdk-11.0.1/bin/java
JAVA=${JAVA_HOME}/bin/java

${JAVA} --module-path "${PATH_TO_FX}" \
	--add-modules javafx.controls,javafx.web \
	-cp "${DERBY_JAR}:build/jar/novinar.jar" \
	"-Dorg.bb.vityok.novinar.opml_file=${OPML_FILE}" \
	"-Dorg.bb.vityok.novinar.db_dir=${FEEDS_DB}" \
	org.bb.vityok.novinar.Main
