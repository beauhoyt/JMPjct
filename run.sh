#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

JAVAOPT="-Xmx4096m"
JAVAOPT="${JAVAOPT} -Xshare:off"

JAVACOPT="-Xlint:unchecked"

PROXYOPT=""
CLASSPATH=""

# Connection options
PROXYOPT="${PROXYOPT} -Dports=5050"

# Logging Config File
PROXYOPT="${PROXYOPT} -DlogConf=conf/log.conf"

# Plugins
#PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Proxy,Plugin_Debug,Plugin_Example"
#PROXYOPT="${PROXYOPT} -Dplugins=Plugin_Proxy,Plugin_Debug"
PROXYOPT="${PROXYOPT} -Dplugins=com.github.jmpjct.plugin.proxy.Proxy,com.github.jmpjct.plugin.cache.eh.Eh"

# Proxy
PROXYOPT="${PROXYOPT} -DproxyHosts=5050:127.0.0.1:3306,5051:127.0.0.1:2000,5052:127.0.0.1:5050"

# Ehcache
PROXYOPT="${PROXYOPT} -DehcacheConf=conf/ehcache.xml"
PROXYOPT="${PROXYOPT} -DehcacheCacheName=MySQL"

# Ehcache for OS X. Disable on linux
# PROXYOPT="${PROXYOPT} -Dnet.sf.ehcache.pool.sizeof.AgentSizeOf.bypass=true"

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
