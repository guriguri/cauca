#!/bin/bash
export LANG=en_US.UTF-8

LOG=crawler-`date +%Y%m%d`.log

JARFILE=../target/courtauction-0.1.0-SNAPSHOT-standalone.jar
CONF=../src/resources
CLASSPATH=.:$CONF:$JARFILE

JAVA=java

echo "START `date` $0 $@" >> $LOG
$JAVA -cp $CLASSPATH courtauction.component.crawler $@ >> $LOG
echo "END `date` $0 $@" >> $LOG
