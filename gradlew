#!/bin/bash
GRADLE_WRAPPER_JAR="$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    echo "ERROR: gradle-wrapper.jar not found!"
    exit 1
fi
exec java -Xmx4G -jar "$GRADLE_WRAPPER_JAR" "$@"
