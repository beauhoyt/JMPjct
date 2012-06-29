#!/bin/bash

javac *.java
java -DmysqlHost=127.0.0.1 -DmysqlPort=3306 -Dport=5050 JMTC
