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
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 29.05.2010 (C) Copyright by Zubanov Dmitry
 */
package org.jssdb.handler;

import org.jssdb.core.proxy.ImageIconHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jssdb.core.DBProperties;
import org.jssdb.core.Serializabler;
import org.jssdb.core.proxy.KnowsId;
import org.jssdb.core.proxy.Local;
import org.jssdb.core.proxy.ProxyHolder;
import org.jssdb.core.proxy.ProxyIO;
import org.jssdb.core.proxy.ProxyImageIcon;
import org.jssdb.revision.Revision;
import org.jssdb.utils.FileInfo;

/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 08.05.2010
 */
public class SerializableHandler implements Serializabler {

    private static SerializableHandler self = null;

    private SerializableHandler() {
    }

    public static synchronized SerializableHandler getDefault() {
        if (self == null) {
            self = new SerializableHandler();
        }

        return self;
    }

    @Override
    public long serialaizable(FileInfo fInfo, Object obj) {
        long id = -1;

        if (fInfo == null || obj == null) {
            return id;
        }

        obj = knowsIdHandler(obj, fInfo.getNumber());

        try {
            try (OutputStream fos = Files.newOutputStream(Paths.get(fInfo.getFileName()),
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING)) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(obj);
                oos.flush();
                fos.flush();
            }

            boolean isLocal = obj instanceof Local;
            if (!isLocal) {
                Revision.getInstance().incrementRevision(fInfo.getFileName());
            }

            id = fInfo.getNumber();

        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, fInfo.toString(), ex);
            id = -1;

        } finally {

            return id;
        }
    }

    public boolean serialaizableUpdate(String fileName, byte[] data) {
        try {
            try (OutputStream os = Files.newOutputStream(Paths.get(fileName),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.TRUNCATE_EXISTING)) {
                os.write(data);
                os.flush();
            }

            Revision.getInstance().incrementRevision(fileName);

            return true;

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, fileName, e);
            return false;
        }

    }

    public boolean serialaizableUpdate(String fileName, Object obj) {
        boolean result = false;

        try {
            File tmpFile = File.createTempFile("jssdb", "ser");
            try (OutputStream os = Files.newOutputStream(tmpFile.toPath(), StandardOpenOption.WRITE);
                    ObjectOutputStream out = new ObjectOutputStream(os)) {
                out.writeObject(obj);
                out.flush();
                os.flush();
            }

            try (OutputStream osm = Files.newOutputStream(Paths.get(fileName),
                            StandardOpenOption.WRITE)) {
                Files.copy(tmpFile.toPath(), osm);
                Files.deleteIfExists(tmpFile.toPath());
            }


            boolean isLocal = obj instanceof Local;
            if (!isLocal) {
                Revision.getInstance().incrementRevision(fileName);
            }

            result = true;

        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, fileName, ex);

        } finally {
            return result;
        }
    }

    private Object knowsIdHandler(Object object, long id) {
        if (object instanceof KnowsId) {
            try {
                Method method = object.getClass().getMethod("setId", long.class);
                method.invoke(object, id);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getGlobal().log(Level.WARNING, Objects.toString(object), ex);
            }
        }
        return object;
    }

    private Object imageProxyHandler(Object obj, String serilaizableFileName) {
        ImageIcon storedIcon = null;
        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equals("getImage")) {
                if (method.getReturnType() == null) {
                    continue;
                }

                if (!method.getReturnType().getName().equals(ImageIcon.class.getName())) {
                    continue;
                }

                if (method.getParameterTypes().length > 0) {
                    return obj;
                }

                try {
                    storedIcon = (ImageIcon) method.invoke(obj, new Object[]{});
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    if (ex.getClass().getName().equals(InvocationTargetException.class.getName())
                            && ex.getCause().getClass().getName().equals(NullPointerException.class.getName())) {
                        return obj;
                    }
                    Logger.getGlobal().log(Level.WARNING, Objects.toString(obj.getClass()), ex);
                }
            }
        }

        if (storedIcon == null) {
            return obj;
        }

        // TODO
        ProxyImageIcon proxyImageIcon = ImageIconHandler.getInstance().handle(storedIcon);
        String strProxyDir = DBProperties.getInstance().getPathToDB()
                + ProxyHolder.class.getName();
        File f = new File(strProxyDir);
        if (!f.isDirectory()) {
            f.mkdir();
        }

        String strPath = DBProperties.getInstance().getPathToDB()
                + ProxyHolder.class.getName() + File.separator + "1";
        Path p = Paths.get(strPath);
        try {
            if (!p.toFile().exists()) {
                ProxyHolder ph = new ProxyHolder();
                ph.setImageToObject(serilaizableFileName, proxyImageIcon.getId());
                DBProperties.getInstance().getImageHolderMap().put(serilaizableFileName, proxyImageIcon.getId());
                ProxyIO.write(strPath, ph);

            } else {
                ProxyHolder ph = (ProxyHolder) ProxyIO.read(strPath);
                ph.setImageToObject(serilaizableFileName, proxyImageIcon.getId());
                DBProperties.getInstance().getImageHolderMap().put(serilaizableFileName, proxyImageIcon.getId());
                ProxyIO.write(strPath, ph);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, strPath, e);
        }


        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equals("setImage")) {
                if (method.getParameterTypes().length != 1) {
                    continue;
                }

                if (!method.getParameterTypes()[0].getName().equals(ImageIcon.class.getName())) {
                    continue;
                }

                try {
                    method.invoke(obj, new Object[]{null});
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    if (ex.getClass().getName().equals(InvocationTargetException.class.getName())
                            && ex.getCause().getClass().getName().equals(NullPointerException.class.getName())) {
                        return obj;
                    }
                    Logger.getGlobal().log(Level.WARNING, Objects.toString(obj), ex);
                }
            }
        }

        return obj;
    }
}
