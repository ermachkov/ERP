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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ExternalClassLoader {

    private static final Class[] parameters = new Class[]{URL.class};
    private static ExternalClassLoader self = null;

    public void addJarFile(String jarName) {
        try {
            Logger.getGlobal().log(Level.INFO, "Try load jar {0}", jarName);
            File file = new File(jarName);
            URL u = file.toURI().toURL();
            URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class sysclass = URLClassLoader.class;

            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{u});
            
        } catch (MalformedURLException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Logger.getGlobal().log(Level.WARNING, jarName, e);
        }
    }

    public static ExternalClassLoader getInstance() {
        if(self == null){
            self = new ExternalClassLoader();
        }
        
        return self;
    }
}
