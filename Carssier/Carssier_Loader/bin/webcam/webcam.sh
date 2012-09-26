#!/bin/bash

curdir=`dirname $0`;
cd $curdir;

case "$1" in
start)
	killall ffmpeg

	id=`ps aux | awk '/converter/ && !/awk/ {print $2}'`
        kill -9 $id

	id=`ps aux | awk '/watcher/ && !/awk/ {print $2}'`
        kill -9 $id

        id=`ps aux | awk '/checker/ && !/awk/ {print $2}'`
        kill -9 $id

	nohup ./converter $2 </dev/null >/dev/null 2>/dev/null&
	nohup ./checker </dev/null >/dev/null 2>/dev/null&
        nohup ./watcher /home/$USER/.saas/app/ui/img/stream/ </dev/null >/dev/null 2>/dev/null&
;;

stop)

	killall ffmpeg

        id=`ps aux | awk '/converter/ && !/awk/ {print $2}'`
        kill -9 $id

	id=`ps aux | awk '/watcher/ && !/awk/ {print $2}'`
        kill -9 $id

	id=`ps aux | awk '/checker/ && !/awk/ {print $2}'`
        kill -9 $id
;;

*)
	echo "Use webcam.sh [start all|nosound|webcam] | stop";
        echo "option all - enable video + sound + webcam";
        echo "option nosound - enable video + webcam";
        echo "option webcam - enable webcam only";
;;
esac;
