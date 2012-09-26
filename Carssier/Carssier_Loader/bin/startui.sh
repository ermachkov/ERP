#!/bin/bash

curdir=`dirname $0`;
cd $curdir;

CHROME="/usr/bin/google-chrome";

case "$1" in
    app)
       $CHROME --window-position=0,0 --window-size=1366,768 --app=http://localhost:$2/index.html
       echo "start" > /home/$USER/.saas/app/bin/log.txt
       #exit 0;
    ;;

    kiosk)
       $CHROME --kiosk http://localhost:$2/index.html
       #exit 0;
    ;;

    kiosk-printer)
       $CHROME --kiosk --kiosk-printing http://localhost:$2/index.html
       #exit 0;
    ;;
    
    *)
       echo "use app port | kiosk port | kiosk-printer port";
       exit 1;
    ;;
esac
