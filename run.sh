#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

JAVAOPT="-Xmx4096m"

PROXYOPT=""

# Connection options
PROXYOPT="${PROXYOPT} -DmysqlHost=127.0.0.1 -DmysqlPort=3306 -Dport=5050"

# Logging Config File
PROXYOPT="${PROXYOPT} -DlogConf=log.conf"

# Plugins
#PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Debug,Plugin_Example"
PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Debug"

# Load up the class path automagically
CLASSPATH=""
for file in `find lib -name *.jar`
do
    CLASSPATH="${CLASSPATH}${file}:"
done

CLASSPATH="${CLASSPATH}."

set CLASSPATH="${CLASSPATH}"
export CLASSPATH="${CLASSPATH}"

javac -classpath "${CLASSPATH}" `find . -name \*.java | tr \\\n " "`
java ${JAVAOPT} -classpath "${CLASSPATH}" ${PROXYOPT} JMP
