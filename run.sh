#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

JAVAOPT="-Xmx4096m"
JAVACOPT="-Xlint:unchecked"

PROXYOPT=""
CLASSPATH=""

# Connection options
PROXYOPT="${PROXYOPT} -DHosts=5050:127.0.0.1:3306,5051:127.0.0.1:3306"

# Logging Config File
PROXYOPT="${PROXYOPT} -DlogConf=conf/log.conf"

# Plugins
#PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Debug,Plugin_Example"
#PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Debug"
PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Debug,Plugin_Ehcache"

# Ehcache for OS X. Disable on linux
# PROXYOPT="${PROXYOPT} -Dnet.sf.ehcache.pool.sizeof.AgentSizeOf.bypass=true"

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
javac ${JAVACOPT} -classpath "${CLASSPATH}" `find . -name \*.java | tr \\\n " "`

# Run
java ${JAVAOPT} -classpath "${CLASSPATH}" ${PROXYOPT} JMP
