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
 * Omsk, Russia, created 22.09.2010
 * (C) Copyright by Zubanov Dmitry
 */
package org.jssdb.filesystem;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import org.jssdb.utils.FileSeparator;

public class ModifyAction implements Serializable{

    private long time;
    private String className, fileName;
    private int modifyType;
    public static int ADD = 0, MODIFY = 1, DELETE = 2;
    private static final long serialVersionUID = 344690125605722781L;

    public ModifyAction(long time, String className, String fileName, int modifyType) {
        this.time = time;
        this.className = className;
        this.fileName = fileName;
        this.modifyType = modifyType;
    }

    public ModifyAction(long time, File file, int modifyType) {
        this.time = time;
        this.modifyType = modifyType;
        String arr[] = file.getAbsolutePath().split(FileSeparator.getInstance().separator);
        if (arr.length >= 2) {
            this.className = arr[arr.length - 2];
            this.fileName = arr[arr.length - 1];
        }
    }

    public ModifyAction(long time, String fullPath, int modifyType) {
        this.time = time;
        this.modifyType = modifyType;
        Path p = Paths.get(fullPath); 
        this.fileName = p.getName(p.getNameCount() - 1).toString(); 
        this.className = p.getName(p.getNameCount() - 2).toString(); 
//        String arr[] = fullPath.split(File.separator);
//        if (arr.length >= 2) {
//            this.className = arr[arr.length - 2];
//            this.fileName = arr[arr.length - 1];
//        }
    }

    public String getClassName() {
        return className;
    }

    public String getFileName() {
        return fileName;
    }

    public int getModifyType() {
        return modifyType;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Time {0}, Class {1}, File {2}, "
                + "Modify type {3}", new Object[]{time, className,
                    fileName, modifyType});
    }
}
