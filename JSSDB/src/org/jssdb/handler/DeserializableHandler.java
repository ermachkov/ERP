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
package org.jssdb.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jssdb.core.DBProperties;
import org.jssdb.core.Deserializabler;
import org.jssdb.core.proxy.ProxyIO;
import org.jssdb.core.proxy.ProxyImageIcon;

/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia,
 * created 08.05.2010
 */
public class DeserializableHandler implements Deserializabler {

    private static DeserializableHandler self = null;
    private ArrayList<Object> listClasses = new ArrayList();

    public static synchronized DeserializableHandler getDefault() {
        if (self == null) {
            self = new DeserializableHandler();
        }

        return self;
    }

    public void addClass(Class cls) throws Exception {
        listClasses.add(cls);
    }

    @Override
    public synchronized Object deserialaizable(String fileName) {
        Object object = null;
        if (Paths.get(fileName).toFile().length() == 0) {
            return object;
        }

        try {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(Files.readAllBytes(Paths.get(fileName)))) {
                object = new ObjectInputStream(bais).readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            Logger.getGlobal().log(Level.WARNING, fileName, e);

        } finally {
            return object;
        }
    }

    public ConcurrentMap<Long, Object> getCollection(ArrayList<File> fileList) {
        ConcurrentMap<Long, Object> list = new ConcurrentHashMap<>();
        if (fileList == null) {
            Logger.getGlobal().log(Level.WARNING, "FileList is null");
            return list;
        }

        if (fileList.isEmpty()) {
            Logger.getGlobal().log(Level.WARNING, "FileList is empty");
            return list;
        }

        for (File file : fileList) {
            Object obj = deserialaizable(file.getPath());
            if(obj != null){
                try {
                    long id = Long.parseLong(file.getName());
                    list.put(id, obj);
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, Objects.toString(file, "null"), e);
                }
            }
        }

        return list;
    }

    private Object imageProxyHandler(Object obj, String deserilaizableFileName) {
        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equals("setImage")) {
                if (method.getParameterTypes().length != 1) {
                    continue;
                }

                if (!method.getParameterTypes()[0].getName().equals(ImageIcon.class.getName())) {
                    continue;
                }

                try {
                    if (DBProperties.getInstance().getImageHolderMap().containsKey(deserilaizableFileName)) {
                        long idProxyImage = DBProperties.getInstance().getImageHolderMap().get(deserilaizableFileName);
                        ProxyImageIcon pio = (ProxyImageIcon) ProxyIO.read(DBProperties.getInstance().getPathToDB()
                                + ProxyImageIcon.class.getName() + File.separator + idProxyImage);
                        method.invoke(obj, new Object[]{pio.getImageIcon()});
                    }

                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getGlobal().log(Level.WARNING, Objects.toString(obj), ex);
                }
            }
        }

        return obj;
    }
}
