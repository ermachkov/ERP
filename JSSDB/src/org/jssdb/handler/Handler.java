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
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.DBHandler;
import org.jssdb.core.DBProperties;
import org.jssdb.event.Event;
import org.jssdb.event.EventListenerList;
import org.jssdb.event.JSSDBEventListener;
import org.jssdb.filesystem.WatchDir;
import org.jssdb.query.Query;
import org.jssdb.replicator.TCPNode;
import org.jssdb.revision.Revision;
import org.jssdb.utils.EventHashUtil;
import org.jssdb.utils.FileInfo;
import org.jssdb.utils.HashUtil;
import org.jssdb.utils.MetaInfoData;

/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 08.05.2010
 */
public class Handler implements DBHandler, Serializable {

    private static Handler self = null;
    private FileStorageHandler fsh;
    private SerializableHandler sh;
    private DeserializableHandler dh;
    private WeakHashMap<Long, String> mapFilter = new WeakHashMap();
    private static final Logger logger = Logger.getLogger(Handler.class.getName());
    protected EventListenerList listenerList = new EventListenerList();
    public static int ADD = 0, MODIFY = 1, DELETE = 2;
    private AtomicReference atomicReference = new AtomicReference();
    private ConcurrentMap<String, byte[]> eventMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, byte[]> incomeMap = new ConcurrentHashMap<>();
    private UpdateQueue updateQueue;
    private ExecutorService addService;

    private Handler() {
        addService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        atomicReference.set("");
        fsh = FileStorageHandler.getDefault();
        sh = SerializableHandler.getDefault();
        dh = DeserializableHandler.getDefault();
    }

    public static synchronized Handler getInstance() {
        if (self == null) {
            self = new Handler();
        }

        return self;
    }

    public void initWatcher(String dbDir) {
        Executors.newSingleThreadExecutor().execute(watchDir(dbDir));
    }

    private Runnable watchDir(final String dbDir) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    new WatchDir(dbDir) {

                        @Override
                        public void onChange(File file, int action) {
                            if (DBProperties.getInstance().isDebug()) {
                                Logger.getGlobal().log(Level.INFO,
                                        "File {0}, action = {1}",
                                        new Object[]{file, action});
                            }

                            Path p = file.toPath();

                            long id = 0;
                            try {
                                id = Long.parseLong("" + p.getName(p.getNameCount() - 1));

                            } catch (NumberFormatException e) {
                                Logger.getGlobal().log(Level.WARNING, "id = " + id, e);
                                return;
                            }

                            String className = "" + p.getName(p.getNameCount() - 2);

                            Object object = null;
                            ConcurrentMap<String, Object> evtMap = new ConcurrentHashMap<>();
                            if (action != WatchDir.DELETE) {
                                Path path = Paths.get(DBProperties.getInstance().getPathToDB()
                                        + className + "_" + id + "_lock");
                                //max timeout 5 sec.
                                int timeout = 0, maxIdleTime = 500;
                                while (path.toFile().exists()) {
                                    LockSupport.parkNanos(10000000);
                                    timeout++;
                                    if (timeout > maxIdleTime) {
                                        break;
                                    }
                                }

                                object = get(id, className);
                                if (object == null) {
                                    return;
                                }
                                
                                if(action == Handler.ADD){
                                    FileStorageHandler.getDefault().updateBoost(file);
                                }

                                evtMap.put("id", id);
                                evtMap.put("class", className);
                                evtMap.put("file", file);
                                evtMap.put("action", action);
                                evtMap.put("object", object);

                            } else {
                                evtMap.put("id", id);
                                evtMap.put("class", className);
                                evtMap.put("file", file);
                                evtMap.put("action", action);
                                evtMap.put("object", new Object());

                                try {
                                    fireCustomEvent(new Event(evtMap));
                                } catch (Exception e) {
                                    Logger.getGlobal().log(Level.WARNING, Objects.toString(evtMap), e);
                                }

                                // DELETE FROM Cache
                                Query.getInstance().eventHandler(file, action);

                                ///////////////////// DELETE REPLICA
                                if (DBProperties.getInstance().isReplicationEnabled()) {
                                    if (DBProperties.getInstance().isSuperNode()) {
                                        long revision = Revision.getInstance().
                                                getRevision(className, id);
                                        //SuperNode.getInstance().circuitReplication(
                                        //        file, className, id, revision, action);
                                        //UDPSuperNode.getInstance().circuitReplication(file, className, id, revision, action);
                                        TCPNode.getSuperNode().circuitReplication(file, className, id, revision, action);

                                    } else {
                                        long revision = Revision.getInstance().
                                                getRevision(className, id);
                                        //Node.getInstance().replicationToSuperNode(
                                        //        file, className, id, revision, action);
                                        //UDPNode.getInstance().replicationToSuperNode(file, className, id, revision, action);
                                        TCPNode.getNode().replicationToSuperNode(file, className, id, revision, action);
                                    }
                                }
                            }

                            if (object == null) {
                                return;
                            }

                            boolean isSendEvent = true;
                            String key = className + "_" + id;
                            if (DBProperties.getInstance().isDebug()) {
                                System.out.println("try send event from db for object " + object);
                            }

                            byte[] bytesHash = EventHashUtil.getHash(file);
                            if (eventMap.containsKey(key) && bytesHash != null) {
                                if (Arrays.equals(eventMap.get(key), bytesHash)) {
                                    isSendEvent = false;
                                    if (DBProperties.getInstance().isDebug()) {
                                        System.out.println("BLOCKED EVENT ON WATCHER FOR OBJECT " + object);
                                    }
                                }
                            }

                            if (bytesHash != null) {
                                eventMap.put(key, bytesHash);
                            }


                            if (isSendEvent) {
                                Query.getInstance().eventHandler(file, action);
                                //if (action != WatchDir.DELETE) {
                                mapFilter.put(file.lastModified(), className);
                                try {
                                    fireCustomEvent(new Event(evtMap));
                                } catch (Exception e) {
                                    Logger.getGlobal().log(Level.WARNING, Objects.toString(evtMap), e);
                                }

                                ////////////////////// REPLICA 
                                if (DBProperties.getInstance().isReplicationEnabled()) {
                                    if (DBProperties.getInstance().isSuperNode()) {
                                        long revision = Revision.getInstance().
                                                getRevision(className, id);
                                        TCPNode.getSuperNode().circuitReplication(file, className, id, revision, action);

                                    } else {
                                        long revision = Revision.getInstance().
                                                getRevision(className, id);
                                        TCPNode.getNode().replicationToSuperNode(file, className, id, revision, action);
                                    }
                                }
                            }
                        }
                    };

                } catch (Exception ex) {
                    System.err.println("Exception watchDir = " + ex);
                    Logger.getGlobal().log(Level.SEVERE, dbDir, ex);
                }
            }
        };

        return r;
    }

    //public synchronized FileInfo getOptimisticFileInfo(Object object) {
    public synchronized FileInfo getOptimisticFileInfo(String className) {
        File f = new File(DBProperties.getInstance().getPathToDB() + className);

        if (!f.isDirectory()) {
            f.mkdir();
        }

        FileInfo fInfo = fsh.getSaveFileInfo(f);
        return fInfo;
    }

    @Override
    public long add(Object obj) {
        atomicReference.set(obj.getClass().getName());
        synchronized (atomicReference.get()) {
            Path lockPath = null;
            File f = new File(DBProperties.getInstance().getPathToDB() + obj.getClass().getName());
            if (!f.isDirectory()) {
                f.mkdir();
            }

            FileInfo fInfo = fsh.getSaveFileInfo(f);

            try {
                Path pathId = Paths.get(fInfo.getFileName());
                String idFile = pathId.getName(pathId.getNameCount() - 1).toString();
                Path path = Paths.get(DBProperties.getInstance().getPathToDB()
                        + obj.getClass().getName() + "_" + idFile + "_lock");

                if (!path.toFile().exists()) {
                    //lockPath = Files.createFile(path,
                    //        PosixFilePermissions.asFileAttribute(
                    //        PosixFilePermissions.fromString("rw-rw-rw-")));
                    path.toFile().createNewFile();
                    lockPath = path;
                }

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }

            if (DBProperties.getInstance().isDebug()) {
                System.out.println("try add object " + obj);
            }

            String key = obj.getClass().getName() + "_" + fInfo.getNumber();
            if (incomeMap.containsKey(key)) {
                if (Arrays.equals(incomeMap.get(key), HashUtil.getHash(obj))) {
                    lockPath.toFile().delete();
                    if (DBProperties.getInstance().isDebug()) {
                        System.out.println("not added object " + obj + " because hash is present");
                    }
                    return -1;
                }
            }
            incomeMap.put(key, HashUtil.getHash(obj));

            long id = sh.serialaizable(fInfo, obj);
            if (DBProperties.getInstance().isDebug()) {
                System.out.println("success added object " + obj);
            }

            lockPath.toFile().delete();

            return id;
        }
    }

    private Callable<Long> addConcurrent(final long id, final Object obj) {
        Callable<Long> call = new Callable() {

            @Override
            public Long call() throws Exception {
                atomicReference.set(obj.getClass().getName());
                synchronized (atomicReference.get()) {

                    Path lockPath = null;
                    String strDir = DBProperties.getInstance().getPathToDB() + obj.getClass().getName();
                    File f = new File(strDir);
                    if (!f.isDirectory()) {
                        f.mkdir();
                    }

                    FileInfo fi = new FileInfo(strDir + File.separator + id, id);

                    try {
                        Path path = Paths.get(DBProperties.getInstance().getPathToDB()
                                + obj.getClass().getName() + "_" + id + "_lock");

                        if (!path.toFile().exists()) {
                            path.toFile().createNewFile();
                            lockPath = path;
                        }

                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, null, e);
                    }

                    if (DBProperties.getInstance().isDebug()) {
                        System.out.println("try add object " + Objects.toString(obj) + " with id " + id);
                    }

                    long result = -1;
                    String key = obj.getClass().getName() + "_" + id;
                    if (incomeMap.containsKey(key)) {
                        if (Arrays.equals(incomeMap.get(key), HashUtil.getHash(obj))) {
                            lockPath.toFile().delete();
                            if (DBProperties.getInstance().isDebug()) {
                                System.out.println("not added object " + obj + " with id "
                                        + id + " because hash is present");
                            }
                            return result;
                        }
                    }
                    incomeMap.put(key, HashUtil.getHash(obj));

                    result = sh.serialaizable(fi, obj);
                    if (DBProperties.getInstance().isDebug()) {
                        System.out.println("success added object " + obj + " with id " + id);
                    }

                    lockPath.toFile().delete();

                    return result;
                }
            }
        };

        return call;
    }

    public void add(long id, String className, byte[] data) {
        atomicReference.set(className);
        synchronized (atomicReference.get()) {
            Path lockPath = null;

            try {
                Path path = Paths.get(DBProperties.getInstance().getPathToDB()
                        + className + "_" + id + "_lock");

                if (!path.toFile().exists()) {
                    path.toFile().createNewFile();
                    lockPath = path;
                }

                String key = className + "_" + id;
                byte md5[] = HashUtil.getHash(data);
                if (incomeMap.containsKey(key)) {
                    if (Arrays.equals(incomeMap.get(key), md5)) {
                        lockPath.toFile().delete();
                        if (DBProperties.getInstance().isDebug()) {
                            System.out.println("not added object " + className + " with id "
                                    + id + " because hash is present");
                        }
                        return;
                    }
                }
                incomeMap.put(key, md5);

                try (OutputStream fos = Files.newOutputStream(Paths.get(
                                DBProperties.getInstance().getPathToDB(), className, "" + id),
                                StandardOpenOption.WRITE,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING)) {
                    fos.write(data);
                    fos.flush();
                }

                lockPath.toFile().delete();

            } catch (Exception e) {
                if (lockPath != null) {
                    lockPath.toFile().delete();
                }
                Logger.getGlobal().log(Level.WARNING, className, e);
            }
        }
    }

    public long add(long id, Object obj) {
        atomicReference.set(obj.getClass().getName());
        synchronized (atomicReference.get()) {

            Path lockPath = null;
            String strDir = DBProperties.getInstance().getPathToDB() + obj.getClass().getName();
            File f = new File(strDir);
            if (!f.isDirectory()) {
                f.mkdir();
            }

            FileInfo fi = new FileInfo(strDir + File.separator + id, id);

            try {
                Path path = Paths.get(DBProperties.getInstance().getPathToDB()
                        + obj.getClass().getName() + "_" + id + "_lock");

                if (!path.toFile().exists()) {
                    path.toFile().createNewFile();
                    lockPath = path;
                }

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }

            if (DBProperties.getInstance().isDebug()) {
                System.out.println("try add object " + Objects.toString(obj) + " with id " + id);
            }

            long result = -1;
            String key = obj.getClass().getName() + "_" + id;
            if (incomeMap.containsKey(key)) {
                if (Arrays.equals(incomeMap.get(key), HashUtil.getHash(obj))) {
                    lockPath.toFile().delete();
                    if (DBProperties.getInstance().isDebug()) {
                        System.out.println("not added object " + obj + " with id "
                                + id + " because hash is present");
                    }
                    return result;
                }
            }
            incomeMap.put(key, HashUtil.getHash(obj));

            result = sh.serialaizable(fi, obj);
            if (DBProperties.getInstance().isDebug()) {
                System.out.println("success added object " + obj + " with id " + id);
            }

            lockPath.toFile().delete();

            return result;
        }

    }

    public synchronized void add(long id, long lastModified, Object obj) {
        atomicReference.set(obj.getClass().getName());
        synchronized (atomicReference.get()) {
            Path lockPath = null;
            String strDir = DBProperties.getInstance().getPathToDB() + obj.getClass().getName();
            File f = new File(strDir);
            if (!f.isDirectory()) {
                f.mkdir();
            }

            FileInfo fi = new FileInfo(strDir + File.separator + id, id);

            try {
                Path path = Paths.get(DBProperties.getInstance().getPathToDB()
                        + obj.getClass().getName() + "_" + id + "_lock");

                if (!path.toFile().exists()) {
                    //lockPath = Files.createFile(path,
                    //        PosixFilePermissions.asFileAttribute(
                    //        PosixFilePermissions.fromString("rw-rw-rw-")));
                    path.toFile().createNewFile();
                    lockPath = path;
                }

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }

            String key = obj.getClass().getName() + "_" + id;
            if (incomeMap.containsKey(key)) {
                if (Arrays.equals(incomeMap.get(key), HashUtil.getHash(obj))) {
                    lockPath.toFile().delete();
                    return;
                }
            }
            incomeMap.put(key, HashUtil.getHash(obj));

            sh.serialaizable(fi, obj);

            lockPath.toFile().delete();

            File fUpdate = new File(fi.getFileName());
            fUpdate.setLastModified(lastModified);
        }


    }

    @Override
    public boolean update(long id, Object obj) {

        atomicReference.set(obj.getClass().getName());
        synchronized (atomicReference.get()) {
            Path lockPath = null;
            try {
                Path path = Paths.get(DBProperties.getInstance().getPathToDB()
                        + obj.getClass().getName() + "_" + id + "_lock");

                if (!path.toFile().exists()) {
                    path.toFile().createNewFile();
                    lockPath = path;
                }

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }

            boolean result = false;
            if (obj == null) {
                return result;
            }

            //String fileName = fsh.getLoadFile(id, obj.getClass());

            if (DBProperties.getInstance().isDebug()) {
                System.out.println("try update object " + obj + " with id = " + id);
            }

            String key = obj.getClass().getName() + "_" + id;
            if (incomeMap.containsKey(key)) {
                if (Arrays.equals(incomeMap.get(key), HashUtil.getHash(obj))) {
                    if (lockPath != null) {
                        lockPath.toFile().delete();
                    }

                    if (DBProperties.getInstance().isDebug()) {
                        System.out.println("not updated object " + obj
                                + " with id " + id + " because hash is present");
                    }
                    return true;
                }
            }
            incomeMap.put(key, HashUtil.getHash(obj));

            String fileName = Paths.get(DBProperties.getInstance()
                    .getPathToDB(), obj.getClass().getName(), "" + id)
                    .toString();
            result = sh.serialaizableUpdate(fileName, obj);
            
            if (DBProperties.getInstance().isDebug()) {
                System.out.println("success updated object " + obj + " with id " + id);
            }

            if (lockPath != null) {
                lockPath.toFile().delete();
            }

            return result;
        }
    }
    
    public boolean update(long id, String className, byte data[]) {

        atomicReference.set(className);
        synchronized (atomicReference.get()) {
            Path lockPath = null;
            try {
                Path path = Paths.get(DBProperties.getInstance().getPathToDB()
                        + className + "_" + id + "_lock");

                if (!path.toFile().exists()) {
                    path.toFile().createNewFile();
                    lockPath = path;
                }

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }

            if (data == null) {
                return false;
            }

            if (DBProperties.getInstance().isDebug()) {
                System.out.println("try update object " + className + " with id = " + id);
            }

            byte[] md5 = HashUtil.getHash(data);
            String key = className + "_" + id;
            if (incomeMap.containsKey(key)) {
                if (Arrays.equals(incomeMap.get(key), md5)) {
                    if (lockPath != null) {
                        lockPath.toFile().delete();
                    }

                    if (DBProperties.getInstance().isDebug()) {
                        System.out.println("not updated object " + className
                                + " with id " + id + " because hash is present");
                    }
                    return true;
                }
            }
            incomeMap.put(key, md5);
            
            String fileName = Paths.get(DBProperties.getInstance()
                    .getPathToDB(), className, "" + id)
                    .toString();
            sh.serialaizableUpdate(fileName, data);
            
            if (DBProperties.getInstance().isDebug()) {
                System.out.println("success updated object " + className + " with id " + id);
            }

            if (lockPath != null) {
                lockPath.toFile().delete();
            }

            return true;
        }
    }

    public boolean updateFromQueue(long id, Object obj) {
//        atomicReference.set(obj.getClass().getName());
//        synchronized (atomicReference.get()) {
//            Path lockPath = null;
//            try {
//                Path path = Paths.get(DBProperties.getInstance().getPathToDB()
//                        + obj.getClass().getName() + "_" + id + "_lock");
//
//                if (!path.toFile().exists()) {
//                    lockPath = Files.createFile(path,
//                            PosixFilePermissions.asFileAttribute(
//                            PosixFilePermissions.fromString("rw-rw-rw-")));
//                }
//
//            } catch (Exception e) {
//                Logger.getGlobal().log(Level.WARNING, null, e);
//            }
//            boolean result = false;
//            if (obj == null) {
//                return result;
//            }
//
//            String fileName = fsh.getLoadFile(id, obj.getClass());
//            if (fileName == null) {
//                return result;
//            }
//
//            if (DBProperties.getInstance().isDebug()) {
//                System.out.println("try update object " + obj + " with id = " + id);
//            }
//
//            String key = obj.getClass().getName() + "_" + id;
//            if (incomeMap.containsKey(key)) {
//                if (Arrays.deepEquals(incomeMap.get(key), HashUtil.computeHash(obj))) {
//                    lockPath.toFile().delete();
//                    if (DBProperties.getInstance().isDebug()) {
//                        System.out.println("not updated object " + obj 
//                                + " with id " + id + " because hash is present");
//                    }
//                    return false;
//                }
//            }
//            incomeMap.put(key, HashUtil.computeHash(obj));
//
//            result = sh.serialaizableUpdate(fileName, obj);
//
//            if (DBProperties.getInstance().isDebug()) {
//                System.out.println("success updated object " + obj + " with id " + id);
//            }
//
//            if (result) {
//                FilesListInfo.getInstance().modifyFileInfo(fileName);
//            }
//
//            lockPath.toFile().delete();
//
//            return result;
//        }
        return true;
    }

    @Override
    public Object get(long id, Class cls) {
        synchronized (atomicReference.get()) {

            Path path = Paths.get(DBProperties.getInstance().getPathToDB()
                    + cls.getName() + "_" + id + "_lock");
            while (path.toFile().exists()) {
                LockSupport.parkNanos(10000);
            }

            String fileName = fsh.getLoadFile(id, cls);
            if (fileName == null) {
                return null;
            }

            Object obj = dh.deserialaizable(fileName);

            return obj;
        }
    }

    @Override
    public Object get(long id, String className) {
        synchronized (atomicReference.get()) {
            Path path = Paths.get(DBProperties.getInstance().getPathToDB()
                    + className + "_" + id + "_lock");
            while (path.toFile().exists()) {
                LockSupport.parkNanos(10000);
            }

            Class cls = null;
            try {
                cls = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                logger.log(Level.SEVERE, className, ex);
            }

            String fileName = fsh.getLoadFile(id, cls);
            if (fileName == null) {
                return null;
            }

            Object obj = get(id, cls);

            return obj;
        }
    }

    @Override
    public synchronized boolean delete(long id, String className) {
        if (className == null) {
            return false;
        }

        boolean result = fsh.deleteFile(id, className);
        if (result) {
            Revision.getInstance().setDeleted(DBProperties.getInstance().getPathToDB()
                    + className + File.separator + id);
        }

        return result;
    }

    @Override
    public synchronized boolean delete(long id, Class cls) {
        boolean result = false;
        if (cls == null) {
            return result;
        }

        result = fsh.deleteFile(id, cls);
        if (result) {
            Revision.getInstance().setDeleted(DBProperties.getInstance().getPathToDB()
                    + cls.getName() + File.separator + id);
        }

        return result;
    }

    @Override
    public synchronized ConcurrentMap<Long, Object> getCollection(String className) {
        return dh.getCollection(fsh.getAllFilesList(className));
    }

    @Override
    public synchronized Map<Long, Object> getCollection(Class cls) {
        Map<Long, Object> mw = new HashMap<>();
        ConcurrentMap<Long, Object> m = getCollection(cls.getName());
        mw.putAll(m);
        return mw;
    }

    public synchronized ConcurrentMap<Long, Object> getCollection(long idFrom, long idTo, Class cls) {
        return dh.getCollection(fsh.getFileList(idFrom, idTo, cls));
    }

    public synchronized ConcurrentMap<Long, Object> getCollection(long idFrom, long idTo, String className) {
        return dh.getCollection(fsh.getFileList(idFrom, idTo, className));
    }

    @Override
    public synchronized long getMinId(String className) {
        return fsh.getMinId(className);
    }

    @Override
    public synchronized long getMinId(Class cls) {
        return fsh.getMinId(cls);
    }

    @Override
    public synchronized long getMaxId(String className) {
        return fsh.getMaxId(className);
    }

    @Override
    public synchronized long getMaxId(Class cls) {
        return fsh.getMaxId(cls);
    }

    @Override
    public MetaInfoData getMetaInfo(long id, Class cls) {
        return MetaInfo.getDefault().getMetaInfo(id, cls);
    }

    public MetaInfoData getMetaInfo(long id, String className) {
        return MetaInfo.getDefault().getMetaInfo(id, className);
    }

    public void addJSSDBEventListener(JSSDBEventListener listener) {
        listenerList.add(JSSDBEventListener.class, listener);
    }

    private void fireCustomEvent(Event evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == JSSDBEventListener.class) {

                if (evt.getAction() == 0) {
                    ((JSSDBEventListener) listeners[i + 1]).objectAdded(evt);
                }

                if (evt.getAction() == 1) {
                    ((JSSDBEventListener) listeners[i + 1]).objectModifyed(evt);
                }

                if (evt.getAction() == 2) {
                    ((JSSDBEventListener) listeners[i + 1]).objectDeleted(evt);
                }

                ((JSSDBEventListener) listeners[i + 1]).allEvent(evt);
            }
        }
    }
}
