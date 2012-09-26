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
package org.jssdb.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.jssdb.core.DBProperties;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public final class ModifyLogger extends ArrayList<ModifyAction> implements Serializable {

    private static final long serialVersionUID = 344690125605722780L;
    private static ModifyLogger self = null;
    private static final Logger logger = Logger.getLogger("ModifyLogger");

    private ModifyLogger() {
        String dbDir = DBProperties.getInstance().getPathToDB();
        try {
            FileHandler fh = new FileHandler(dbDir + "modify.log", true);
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException | SecurityException e) {
            logger.log(Level.SEVERE, dbDir + "modify.log", e);
        }

        clear();
        File file = new File(dbDir + "modify.log");
        if (!file.isFile()) {
            return;
        }

        try {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                String str;
                while ((str = in.readLine()) != null) {
                    if (!str.startsWith("INFO:")) {
                        continue;
                    }

                    String arr[] = str.split("\\|");
                    if (arr.length < 2) {
                        continue;
                    }

                    String a[] = arr[1].split(",");

                    long time = Long.parseLong(a[0].trim());
                    File f = new File(a[1].trim());
                    int action = Integer.parseInt(a[2].trim());
                    ModifyAction modifyAction = new ModifyAction(time, f, action);
                    add(modifyAction);
                }

                logger.log(Level.FINE, "Modify logger size = {0}", this.size());
            }

        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(ModifyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addModify(File file, int action) {
        long time = new Date().getTime();
        ModifyAction modifyAction = new ModifyAction(time, file, action);
        add(modifyAction);
        logger.log(Level.INFO, "|{0},{1},{2}", new Object[]{"" + time, file, action});
    }

    public ModifyAction findModifyAction(String className, String fileName, int typeAction) {
        ModifyAction modifyAction = null;
        for (ModifyAction ma : this) {
            if (ma.getClassName().equals(className)
                    && ma.getFileName().equals(fileName)
                    && ma.getModifyType() == typeAction) {
                modifyAction = ma;
                break;
            }
        }
        return modifyAction;
    }

    public ArrayList<ModifyAction> findAction(int modifyType) {
        ArrayList<ModifyAction> modifyActionList = new ArrayList();
        for (ModifyAction ma : this) {
            if (ma.getModifyType() == modifyType) {
                modifyActionList.add(ma);
            }
        }
        return modifyActionList;
    }

    public static ModifyLogger getInstance() {
        if (self == null) {
            self = new ModifyLogger();
        }

        return self;
    }
}
