/*
 * Copyright (C) 2011 developer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jssdb.revision;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.DBProperties;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Revision {

    private static Revision self = null;
    //private Properties properties;
    private Path pathToProperties;
    private AtomicBoolean isLocked = new AtomicBoolean(false);
    private ConcurrentHashMap<String, Long> properties = new ConcurrentHashMap<>();
    private ExecutorService writeService;

    private Revision() {
        String revPath = DBProperties.getInstance().getPathToDB() + "revision";
        pathToProperties = Paths.get(revPath);
        if (!pathToProperties.toFile().exists()) {
            try {
                pathToProperties.toFile().createNewFile();
            } catch (IOException ex) {
                Logger.getGlobal().log(Level.SEVERE, revPath, ex);
            }
        }

        writeService = Executors.newSingleThreadExecutor();

        Properties p = new Properties();
        InputStream is = null;
        try {
            is = Files.newInputStream(pathToProperties);
            p.load(is);
            is.close();

            Iterator it = p.keySet().iterator();
            while (it.hasNext()) {
                String key = "" + it.next();
                String value = p.getProperty(key);
                if (value == null) {
                    value = "0";
                }

                if (value.equals("")) {
                    value = "0";
                }

                try {
                    properties.put(key, Long.parseLong(value));
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, revPath, e);
                }

            }

        } catch (IOException | NumberFormatException e) {
            Logger.getGlobal().log(Level.SEVERE, revPath, e);
        }

    }

    public synchronized static Revision getInstance() {
        if (self == null) {
            self = new Revision();
        }

        return self;
    }

    public synchronized boolean incrementRevision(String fileName) {
        if (fileName == null) {
            return false;
        }

        if (fileName.equals("")) {
            return false;
        }

        Path path = Paths.get(fileName);
        String className = Objects.toString(path.getName(path.getNameCount() - 2));

        long id = 0;
        try {
            id = Long.parseLong("" + path.getName(path.getNameCount() - 1));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, fileName, e);
            return false;
        }

        long revision = getRevision(className, id);
        if (revision == -1000) {
            return false;
        }

        if (revision == -1) {
            return false;
        }

        if (revision == -100) {
            revision = 0;

        } else {
            revision = revision + 1;
        }

        boolean success = setRevision(className, id, revision);
        return success;
    }

    public long getRevision(String className, long id) {
        long revision = -100;
        String key = className + "_" + id;
        if (properties.containsKey(key)) {
            revision = properties.get(key);
        }

        return revision;
    }

    private Runnable saveProperties() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                OutputStream os = null;
                Path pathLock = Paths.get(DBProperties.getInstance().getPathToDB(), "revision_lock");
                while(pathLock.toFile().exists()){
                    LockSupport.parkNanos(1000);
                }
                
                try {
                    Properties p = new Properties();
                    Iterator<String> it = properties.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        long value = properties.get(key);
                        p.setProperty(key, "" + value);
                    }

                    if(!pathLock.toFile().exists()){
                        pathLock.toFile().createNewFile();
                    }

                    os = Files.newOutputStream(pathToProperties);
                    p.store(os, "");
                    os.flush();
                    os.close();
                    
                    pathLock.toFile().delete();

                } catch (Exception e) {
                    if(pathLock.toFile().exists()){
                        pathLock.toFile().delete();
                    }
                    
                    if (os != null) {
                        try {
                            os.close();
                        } catch (Exception ex) {
                        }
                    }
                    
                } finally{
                    if(pathLock.toFile().exists()){
                        pathLock.toFile().delete();
                    }
                }
            }
        };

        return r;
    }

    public boolean setRevision(String className, long id, long revision) {
        String key = className + "_" + id;
        properties.put(key, revision);
        writeService.execute(saveProperties());
        return true;
    }

    public synchronized void setDeleted(String fileName) {
        Path path = Paths.get(fileName);
        String className = Objects.toString(path.getName(path.getNameCount() - 2));
        long id = Long.parseLong(Objects.toString(path.getName(path.getNameCount() - 1)));
        setRevision(className, id, -1);
    }

    public synchronized ArrayList<Long> getDeletedList(String className) {
        ArrayList<Long> list = new ArrayList<>();

        Set set = properties.keySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            String key = "" + it.next();
            if (key.split("_")[0].equals(className)) {
                if (properties.get(key) == -1) {
                    try {
                        list.add(Long.parseLong(key.split("_")[1].trim()));
                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, key, e);
                    }
                }
            }
        }

        return list;
    }
}
