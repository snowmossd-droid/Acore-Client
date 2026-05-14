#!/bin/bash
exec java -Xmx4G -jar "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" "$@"
