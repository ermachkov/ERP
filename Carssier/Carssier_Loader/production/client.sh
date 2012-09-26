#!/bin/bash

curdir=`dirname $0`;
cd $curdir;

OPTS="-XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC";

id=`ps aux | awk '/Carssier_Loader/ && !/awk/ {print $2}'`
kill -9 $id

log=`date +"%Y-%m-%d-%H-%M-%S"`
log="$log.log";
log="/home/$USER/.saas/app/logs/$log"

splash=/home/$USER/.saas/app/ui/img/logo/carssier_car.png

jre/bin/java -splash:$splash $OPTS -jar Carssier_Loader.jar client 1572 > $log 2>&1
