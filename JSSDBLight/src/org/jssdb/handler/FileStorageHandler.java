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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.DBProperties;
import org.jssdb.core.FileStorage;
import org.jssdb.utils.FileInfo;

/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 08.05.2010
 */
public class FileStorageHandler implements FileStorage {

    private static FileStorageHandler self = null;
    private static final Logger logger = Logger.getLogger(FileStorageHandler.class.getName());
    private ConcurrentMap<String, Long> mBoost = new ConcurrentHashMap<>();
    private int autoStart = 0, autoIncrement = 0, autoOffset = 1;
    private Properties pLastestId;
    private Path lastestidPath;

    private FileStorageHandler() {
        pLastestId = new Properties();
        lastestidPath = Paths.get(DBProperties.getInstance().getPathToDB() + "lastestid");
    }

    public static synchronized FileStorageHandler getDefault() {
        if (self == null) {
            self = new FileStorageHandler();
        }

        return self;
    }

    public synchronized void setIdOffset(int autoStart, int autoIncrement, int autoOffset) {
        this.autoStart = autoStart;
        this.autoIncrement = autoIncrement;
        this.autoOffset = autoOffset;
    }
    
    public synchronized void updateBoost(File file){
        Path root = file.toPath().getRoot();
        Path sub = file.toPath().subpath(0, file.toPath().getNameCount() - 1);
        Path p = Paths.get(root.toString(), sub.toString());
        
        if(mBoost.get(p.toFile().getPath()) == null){
            mBoost.put(
                p.toFile().getPath(), 
                Long.parseLong(file.toPath().getName(file.toPath().getNameCount() - 1).toString()));
            
        } else {
            long oldId = mBoost.get(p.toFile().getPath());
            long newId = Long.parseLong(file.toPath().getName(file.toPath().getNameCount() - 1).toString());
            if(oldId < newId){
                mBoost.put(p.toFile().getPath(), newId);
            }
        }
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public synchronized FileInfo getSaveFileInfo(File f) {

        long number;
        if (mBoost.get(f.getPath()) == null) {
            number = getMax(f);
            
        } else {
            number = mBoost.get(f.getPath()) + 1;
        } 

        // get max file number
//        if (mBoost.get(f.getPath()) == null) {
//            try {
//                try (InputStream is = Files.newInputStream(lastestidPath)) {
//                    pLastestId.load(is);
//                    String lastNumber = pLastestId.getProperty(
//                            f.toPath().getName(f.toPath().getNameCount() - 1).toString());
//
//                    if (lastNumber == null) {
//                        number = getMax(f);
//
//                    } else {
//                        number = Long.parseLong(lastNumber.trim());
//                    }
//                }
//
//            } catch (IOException | NumberFormatException ex) {
//                Logger.getGlobal().log(Level.SEVERE, null, ex);
//
//            } finally {
//                if (number == -1) {
//                    number = getMax(f);
//
//                } else {
//                    number++;
//                }
//                
//                mBoost.put(f.getPath(), number);
//            }
//            
//        } else {
//            number = mBoost.get(f.getPath());
//            number++;
//        }

        long newId = 0;
        if (number < autoStart) {
            newId = autoStart;

        } else if (number == autoStart) {
            newId = autoStart + autoIncrement + autoOffset;

        } else if (number > autoStart) {
            if ((number % autoOffset) == 0) {
                newId = (number - (number % autoOffset)) + autoIncrement;
            } else {
                newId = (number - (number % autoOffset)) + autoIncrement + autoOffset;
            }
        }
        
        mBoost.put(f.getPath(), newId);

//        try {
//            try (OutputStream out = Files.newOutputStream(lastestidPath)) {
//                pLastestId.setProperty(
//                        f.toPath().getName(f.toPath().getNameCount() - 1).toString(),
//                        "" + newId);
//                pLastestId.store(out, "");
//            }
//        } catch (IOException ex) {
//            Logger.getGlobal().log(Level.SEVERE, null, ex);
//        }

        return new FileInfo(f + File.separator + newId, newId);
    }

    public synchronized long getMax(File f) {
        File files[] = f.listFiles();
        long number = 0;

        if (files.length > 0) {
            for (File file : files) {
                if(file.isDirectory()){
                    continue;
                }
                
                try {
                    long i = Long.valueOf(file.getName());
                    if (i > number) {
                        number = i;
                    }
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, file.getName(), e);
                }
            }
        }

        number = number + 1;

        return number;
    }

    @Override
    public String getLoadFile(long id, Class cls) {
        String fileName = null;
        if (cls == null) {
            return fileName;
        }

        File f = new File(DBProperties.getInstance().getPathToDB() + cls.getName() + File.separator + id);

        if (!f.isFile()) {
            return null;
        }

        try {
            fileName = f.getCanonicalPath();
        } catch (Exception e) {
            logger.log(Level.SEVERE, cls.getName(), e);
        }

        return fileName;
    }

    public boolean deleteFile(long id, String className) {
        boolean result = false;

        if (className == null) {
            return result;
        }

        File f = new File(DBProperties.getInstance().getPathToDB() + className + File.separator + id);
        try {
            result = Files.deleteIfExists(f.toPath());

        } catch (IOException ex) {
            logger.log(Level.SEVERE, className, ex);
        }

        return result;
    }

    @Override
    public boolean deleteFile(long id, Class cls) {
        if (cls == null) {
            return false;
        }
        return deleteFile(id, cls.getName());
    }

    public synchronized long getMinId(String className) {
        if (className == null) {
            return -1;
        }

        File f = new File(DBProperties.getInstance().getPathToDB() + className);

        if (!f.isDirectory()) {
            return -1;
        }

        // get max file number
        long number = 0;
        File files[] = f.listFiles();

        if (files.length > 0) {
            ArrayList<Long> a = new ArrayList<>();
            for (File file : files) {
                a.add(Long.parseLong(file.getName()));
            }
            number = Collections.min(a);
        }

        return number;
    }

    public synchronized long getMinId(Class cls) {
        if (cls == null) {
            return -1;
        }

        return getMinId(cls.getName());
    }

    public synchronized long getMaxId(String className) {
        if (className == null) {
            return -1;
        }

        File f = new File(DBProperties.getInstance().getPathToDB() + className);

        if (!f.isDirectory()) {
            return -1;
        }

        // get max file number
        long number = 0;
        File files[] = f.listFiles();

        if (files.length > 0) {
            ArrayList<Long> a = new ArrayList<>();
            for (File file : files) {
                a.add(Long.parseLong(file.getName()));
            }
            number = Collections.max(a);
        }

        return number;
    }

    public synchronized long getMaxId(Class cls) {
        if (cls == null) {
            return -1;
        }

        return getMaxId(cls.getName());
    }

    public synchronized ArrayList<File> getFileList(long idFrom, long idTo, String className) {
        if (className == null) {
            return null;
        }

        File f = new File(DBProperties.getInstance().getPathToDB() + className);

        if (!f.isDirectory()) {
            return null;
        }
        File files[] = f.listFiles();
        if (files.length == 0) {
            return null;
        }

        ArrayList<File> fileList = new ArrayList();
        fileList.addAll(Arrays.asList(files));
        Collections.sort(fileList, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                int val;
                File f1 = (File) o1;
                File f2 = (File) o2;
                long l1 = Long.parseLong(f1.getName());
                long l2 = Long.parseLong(f2.getName());
                if (l1 > l2) {
                    val = 1;
                } else {
                    val = -1;
                }
                return val;
            }
        });

        ArrayList<File> fileListOut = new ArrayList();
        for (File fCheck : fileList) {
            long val = Long.parseLong(fCheck.getName());
            if (val >= idFrom && val <= idTo) {
                fileListOut.add(fCheck);
            }
        }

        return fileListOut;
    }

    @Override
    public synchronized ArrayList<File> getAllFilesList(String className) {
        ArrayList<File> fileList = new ArrayList();
        if (className == null) {
            Logger.getGlobal().log(Level.WARNING, "Class name is null");
            return fileList;
        }

        File f = new File(DBProperties.getInstance().getPathToDB() + className);

        if (!f.isDirectory()) {
            Logger.getGlobal().log(Level.WARNING, "{0} is not directory", f);
            return fileList;
        }

        File files[] = f.listFiles();
        if (files.length == 0) {
            Logger.getGlobal().log(Level.WARNING, "Files list in dir {0} = 0", f);
            return fileList;
        }

        fileList.addAll(Arrays.asList(files));
        File[] lockFiles = Paths.get(DBProperties.getInstance().getPathToDB()).toFile().listFiles();
        for (File lockFile : lockFiles) {
            if (lockFile.isDirectory()) {
                continue;
            }

            Path pl = Paths.get(lockFile.getPath());
            if (pl.getName(pl.getNameCount() - 1).toString().indexOf("_lock") != -1) {
                String arr[] = lockFile.getPath().split("_");
                Path p = Paths.get(arr[0]);
                String pClass = p.getName(p.getNameCount() - 1).toString();
                pClass = pClass + File.separator + arr[1];
                fileList.remove(Paths.get(pClass).toFile());
            }
        }

        Collections.sort(fileList, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                int val;
                File f1 = (File) o1;
                File f2 = (File) o2;
                long l1 = Long.parseLong(f1.getName());
                long l2 = Long.parseLong(f2.getName());
                if (l1 > l2) {
                    val = 1;
                } else {
                    val = -1;
                }
                return val;
            }
        });

        return fileList;
    }

    public synchronized ArrayList<File> getFileList(long idFrom, long idTo, Class cls) {
        if (cls == null) {
            return null;
        }

        return getFileList(idFrom, idTo, cls.getName());
    }
}
