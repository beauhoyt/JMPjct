#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

JAVAOPT=""
JAVAOPT="${JAVAOPT} -Xmx2g"
#JAVAOPT="${JAVAOPT} -Xshare:off"

JAVACOPT="-Xlint:unchecked"

PROXYOPT="-Dconfig=conf/jmp.properties"
CLASSPATH=""

# Core components
CLASSPATH="${CLASSPATH}:src"

# Locate the conf files (Required for ehcache)
CLASSPATH="${CLASSPATH}:conf"

# Add any jar files
for file in `find lib -name *.jar`
do
    CLASSPATH="${CLASSPATH}:${file}"
done

# Add cwd
CLASSPATH="${CLASSPATH}:."

set CLASSPATH="${CLASSPATH}"
export CLASSPATH="${CLASSPATH}"

# Compile
javac ${JAVACOPT} -classpath "${CLASSPATH}" `find src -name \*.java | tr \\\n " "`

# Run
java ${JAVAOPT} -classpath "${CLASSPATH}" ${PROXYOPT} com.github.jmpjct.JMP
