/*
 *  Copyright (C) 2010 Zubanov Dmitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia, created 29.05.2010
 * (C) Copyright by Zubanov Dmitry
 */

package org.jssdb.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConvertDate {

        public ConvertDate() {
        }

    /**
     *
     * @param format
     * @param date
     * @return
     */
    public Date getTimeStamp(String format, String date) {
        Date d = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            d = sdf.parse(date);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        return d;
    }

    /**
     * 
     * @param full
     * if full is true, then return full MySQL date format eq. yyyy-MM-dd HH:mm:ss
     * @return
     * 
     */
    public String getSQLDateNow(boolean full) {
        String format = "";
        if (full) {
            format = "yyyy-MM-dd HH:mm:ss";
        } else {
            format = "yyyy-MM-dd";
        }
        String sdate = null;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdate = sdf.format(date);
        return sdate;
    }

    /**
     *
     * @param format
     * @param date
     * @return
     */
    public String getDateFromTime(String format, Date date){
        String strDate = "";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try{
            strDate = sdf.format(date);
        } catch (Exception ex){
            System.err.println(ex.getMessage());
        }

        return strDate;
    }

    public String sqlConvert(String sDate, String from, String to) {
        String str = "";
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat(from);
        try {
            date = (Date) formatter.parse(sDate);
            SimpleDateFormat sdf = new SimpleDateFormat(to);
            str = sdf.format(date);
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
        }

        return str;
    }
}
