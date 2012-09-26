#!/bin/bash

curdir=`dirname $0`;
cd $curdir;

OPTS="-Djava.awt.headless=true -Xms64m -XX:MaxNewSize=24m -XX:NewSize=24m -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC";

id=`ps aux | awk '/Carssier_Loader/ && !/awk/ {print $2}'`
kill -9 $id

log=`date +"%Y-%m-%d-%H-%M-%S"`
log="$log.log";
log="/home/$USER/.saas/app/logs/$log"

jre/bin/java $OPTS -jar Carssier_Loader.jar server 1572 > $log 2>&1
