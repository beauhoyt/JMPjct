#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

JAVAOPT="-Xmx4096m"

PROXYOPT=""
CLASSPATH=""

# Connection options
PROXYOPT="${PROXYOPT} -DmysqlHost=127.0.0.1 -DmysqlPort=3306 -Dport=5050"

# Logging Config File
PROXYOPT="${PROXYOPT} -DlogConf=conf/log.conf"

# Plugins
#PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Debug,Plugin_Example"
#PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Debug"
PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Debug,Plugin_Ehcache"

# Core components
CLASSPATH="${CLASSPATH}:core"

# Locate the plugins
for path in `find plugins -type d`
do
    CLASSPATH="${CLASSPATH}:${path}"
done

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
javac -classpath "${CLASSPATH}" `find . -name \*.java | tr \\\n " "`

# Run
java ${JAVAOPT} -classpath "${CLASSPATH}" ${PROXYOPT} JMP
