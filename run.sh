#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

JAVAOPT="-Xmx4096m"

# Connection options
PROXYOPT="-DmysqlHost=127.0.0.1 -DmysqlPort=3306 -Dport=5050"
# Plugins
#PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Debug,Plugin_Example"
PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Debug"

CLASSPATH=""
CLASSPATH="${CLASSPATH}:lib/commons-io-2.4.jar"
CLASSPATH="${CLASSPATH}:."

set CLASSPATH="${CLASSPATH}"
export CLASSPATH="${CLASSPATH}"

javac -classpath "${CLASSPATH}" *.java
java ${JAVAOPT} -classpath "${CLASSPATH}" ${PROXYOPT} JMP
