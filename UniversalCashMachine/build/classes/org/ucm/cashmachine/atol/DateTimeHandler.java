/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.atol;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DateTimeHandler {

    private static DateTimeHandler self = null;

    private DateTimeHandler(){
        
    }

    public static DateTimeHandler getInstance() {
        if (self == null) {
            self = new DateTimeHandler();
        }

        return self;
    }

    public int[] getTimeCommand(int hour, int minute, int second) {
        int cmd[] = new int[6];

        // check input
        if (hour > 59 || hour < 0 || minute > 59 || minute < 0
                || second > 59 || second < 0) {
            return null;
        }

        cmd[0] = 0;
        cmd[1] = 48;
        cmd[2] = 75;
        
        String str = "" + hour;
        if(str.length() == 1){
            str = "0" + str;
        }
        cmd[3] = Integer.valueOf(str, 16).intValue();

        str = "" + minute;
        if(str.length() == 1){
            str = "0" + str;
        }
        cmd[4] = Integer.valueOf(str, 16).intValue();

        str = "" + minute;
        if(str.length() == 1){
            str = "0" + str;
        }
        cmd[5] = Integer.valueOf(str, 16).intValue();

        return cmd;
    }

    public int[] getDateCommand(int day, int month, int year) {
        int cmd[] = new int[6];

        // check input
        if (day > 31 || day < 1 || month > 12 || month < 1 || year > 90
                || year < 0) {
            return null;
        }

        cmd[0] = 0;
        cmd[1] = 48;
        cmd[2] = 100;

        String str = "" + day;
        if(str.length() == 1){
            str = "0" + str;
        }
        cmd[3] = Integer.valueOf(str, 16).intValue();

        str = "" + month;
        if(str.length() == 1){
            str = "0" + str;
        }
        cmd[4] = Integer.valueOf(str, 16).intValue();

        str = "" + year;
        if(str.length() == 1){
            str = "0" + str;
        }
        cmd[5] = Integer.valueOf(str, 16).intValue();

        return cmd;
    }
}
