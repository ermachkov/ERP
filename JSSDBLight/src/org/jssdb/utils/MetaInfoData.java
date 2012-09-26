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

import java.io.File;
import java.util.Date;

public class MetaInfoData {

    private long freeSpace;
    private long totalSpace;
    private long lastModified;
    private long usableSpace;
    private long fileLenght;
    private ConvertDate cd;

    public MetaInfoData(File file) {
        if (file != null) {
            if (file.isFile()) {
                freeSpace = file.getFreeSpace();
                lastModified = file.lastModified();
                totalSpace = file.getTotalSpace();
                usableSpace = file.getUsableSpace();
                fileLenght = file.length();
            }
        }

        cd = new ConvertDate();
    }

    public String getModifiedDataString() {
        Date date = new Date();
        date.setTime(lastModified);
        return cd.getDateFromTime("yyyy-MM-dd HH:mm:ss", date);
    }

    public Date getModifiedData() {
        Date date = new Date();
        date.setTime(lastModified);
        return date;
    }

    public long getFileLenght() {
        return fileLenght;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public long getUsableSpace() {
        return usableSpace;
    }
}
