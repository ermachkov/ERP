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
package org.jssdb.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.handler.FileStorageHandler;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class JSSDB {

    public static void main(String args[]) {
        String err = "For start db use config file as\n "
                + "java -jar JSSDB.jar config=path/to/config/file\n";

        if (args.length == 0) {
            System.err.println(err);
            System.exit(1);
        }

        if (args[0].indexOf("config=") != -1) {
            String arr[] = args[0].split("=");
            DBProperties prop = DBProperties.getInstance();
            prop.setProperties(arr[1]);

            FileStorageHandler.getDefault().setIdOffset(prop.getAutoStart(),
                    prop.getAutoIncrement(), prop.getAutoOffset());

            for (String jar : prop.getExternalJarFiles()) {
                try {
                    ExternalClassLoader.getInstance().addJarFile(jar);
                } catch (Exception ex) {
                    Logger.getLogger(JSSDB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else {
            System.err.println(err);
            System.exit(1);
        }

    }
}
