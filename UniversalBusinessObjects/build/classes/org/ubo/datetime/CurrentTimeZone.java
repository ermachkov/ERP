package org.ubo.datetime;

import java.io.Serializable;
import java.util.Calendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class CurrentTimeZone implements Serializable {

    static final long serialVersionUID = 100000000000010001L;
    private static CurrentTimeZone self = null;

    public static CurrentTimeZone getDefault(){
        if(self == null){
            self = new CurrentTimeZone();
        }

        return self;
    }

    public SimpleTimeZone getSimpleTimeZone(){
        SimpleTimeZone stz = new SimpleTimeZone(
         TimeZone.getDefault().getRawOffset()
         ,TimeZone.getDefault().getID()
         ,Calendar.MARCH
         ,-1
         ,Calendar.SUNDAY
         ,getTime(2,0,0,0)
         ,Calendar.OCTOBER
         ,-1
         ,Calendar.SUNDAY
         ,getTime(3,0,0,0)
         );

        return stz;
    }
   
   private int getTime(int hour,int min,int sec,int ms){
      return hour * 3600000 + min * 60000 + sec * 1000 + ms;
   }
}