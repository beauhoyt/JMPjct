#!/bin/bash -x 

set -o errexit
set -o nounset
set -o pipefail

CLASSPATH=""
CLASSPATH="${CLASSPATH}:lib/commons-io-2.4.jar"
CLASSPATH="${CLASSPATH}:."

set CLASSPATH="${CLASSPATH}"
export CLASSPATH="${CLASSPATH}"

javac -classpath "${CLASSPATH}" *.java
java -classpath "${CLASSPATH}" -DmysqlHost=127.0.0.1 -DmysqlPort=3306 -Dport=5050 JMTC
