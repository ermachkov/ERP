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
package org.jssdb.query;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.DBProperties;
import org.jssdb.core.proxy.KnowsId;
import org.jssdb.event.ServiceEvent;
import org.jssdb.filesystem.ConvertToSingle;
import org.jssdb.filesystem.DataFileChannel;
import org.jssdb.handler.Handler;
import org.jssdb.utils.MetaInfoData;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Query implements Serializable {

    private Handler handler = Handler.getInstance();
    private int mode;
    private static Query self = null;
    private CopyOnWriteArraySet<CacheData> cacheList = new CopyOnWriteArraySet<>();
    private ConcurrentHashMap<String, ByteArrayOutputStream> cachedMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, DataFileChannel> dataFileChannelMap = new ConcurrentHashMap<>();
    private AtomicBoolean isCreateSnapshot = new AtomicBoolean(false);
    private Indexer indexer;

    private Query() {
        mode = DBProperties.getInstance().getMemoryModel();
        indexer = new Indexer();
    }

    public synchronized static Query getInstance() {
        if (self == null) {
            self = new Query();
        }

        return self;
    }

    public void init() {
        long start = new Date().getTime();
        String event = "{message:\"try load from snapshot\"}";
        DBProperties.getInstance().fireServiceEvent(new ServiceEvent(event));

        Path p = Paths.get(DBProperties.getInstance().getPathToDB());
        String s = "";
        boolean isFirst = true;
        for (File dir : p.toFile().listFiles()) {
            if (dir.isFile()) {
                continue;
            }

            if (isFirst) {
                isFirst = false;
                s += "\"" + dir.getName() + "\"";
            } else {
                s += ", \"" + dir.getName() + "\"";
            }
        }
        event = "{message:\"restoreClassList\", value:{list:[" + s + "]}}";
        DBProperties.getInstance().fireServiceEvent(new ServiceEvent(event));

        for (File dir : p.toFile().listFiles()) {

            if (dir.isFile()) {
                continue;
            }

            boolean isForceConvert = false;
            long multiSize = 0, singleSize = 0;
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    File fs = f.toPath().resolve("data.jsdb").toFile();
                    if (fs.exists()) {
                        try {
                            event = "{message:\"checking objects " + dir.getName() + "...\"}";
                            DBProperties.getInstance().fireServiceEvent(new ServiceEvent(event));
                            DataFileChannel dataFileChannel = new DataFileChannel(dir.getName());
                            singleSize = dataFileChannel.getDataSize();
                        } catch (Exception e) {
                            Logger.getGlobal().log(Level.SEVERE, dir.getName(), e);
                        }

                    } else {
                        isForceConvert = true;
                    }

                } else {
                    multiSize += f.length();
                }
            }

            if (isForceConvert || multiSize != singleSize) {
                try {
                    ConvertToSingle convertToSingle = new ConvertToSingle(
                            DBProperties.getInstance().getPathToDB(), dir.getName(), false);
                    convertToSingle.fastConvert();
                } catch (IOException ex) {
                    Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            event = "{message:\"result for checking objects " + dir.getName() + " is Ok!\""
                    + ", value:{class:" + dir.getName() + "}}";
            DBProperties.getInstance().fireServiceEvent(new ServiceEvent(event));
        }

        event = "{message:\"reset progress bar\"}";
        DBProperties.getInstance().fireServiceEvent(new ServiceEvent(event));

        for (File f : p.toFile().listFiles()) {
            if (f.isFile()) {
                continue;
            }

            try {
                event = "{message:\"init cache for " + f.getName() + "\"}";
                DBProperties.getInstance().fireServiceEvent(new ServiceEvent(event));

                dataFileChannelMap.put(f.getName(), new DataFileChannel(f.getName()));

                event = "{message:\"init cache complete for " + f.getName() + "\", "
                        + "value:{time:" + (System.currentTimeMillis() - start) + ", "
                        + "class:\"" + f.getName() + "\"}}";
                DBProperties.getInstance().fireServiceEvent(new ServiceEvent(event));

            } catch (Exception ex) {
                Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        event = "{message:\"load from snapshot complete\", "
                + "value:{time:" + (System.currentTimeMillis() - start) + "}}";
        DBProperties.getInstance().fireServiceEvent(new ServiceEvent(event));
    }

    public void eventHandler(File f, int action) {
        try {
            Path p = f.toPath();
            String className = p.getName(p.getNameCount() - 2).toString();
            long id = Long.parseLong(p.getName(p.getNameCount() - 1).toString());

            if (action == Handler.ADD) {
                if (mode == DBProperties.RAM) {
                    cacheList.add(CacheData.createCacheData(className, id, Files.readAllBytes(p)));
                }

            } else if (action == Handler.MODIFY) {
                if (mode == DBProperties.RAM) {
                    Iterator<CacheData> it = cacheList.iterator();
                    while (it.hasNext()) {
                        CacheData cd = it.next();
                        if (cd.getClassName().equals(className) && cd.getId() == id) {
                            cd.setData(Files.readAllBytes(p));
                            break;
                        }
                    }
                }

            } else if (action == Handler.DELETE) {
                if (mode == DBProperties.RAM) {
                    removeFromCache(className, id);
                }
            }

            cachedMap.remove(className);

        } catch (NumberFormatException | IOException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

    }

    public MetaInfoData getObjectDateModify(long id, String className) {
        return handler.getMetaInfo(id, className);
    }

    private synchronized byte[] addCacheData(Object object, long id) {
        CacheData cd = new CacheData(object, id);
        cacheList.add(cd);
        return cd.getData();
    }

    private synchronized byte[] updateCacheData(Object object, long id) {
        byte data[] = null;
        String className = object.getClass().getName();
        for (CacheData cd : cacheList) {
            if (cd.getClassName().equals(className) && cd.getId() == id) {
                cd.updateCacheData(object);
                data = cd.getData();
                break;
            }
        }

        return data;
    }

    private synchronized void removeFromCache(String className, long id) {
        CacheData cdRemove = null;
        Iterator<CacheData> it = cacheList.iterator();
        while (it.hasNext()) {
            CacheData cd = it.next();
            if (cd.getClassName().equals(className) && cd.getId() == id) {
                cdRemove = cd;
                break;
            }
        }

        if (cdRemove != null) {
            cacheList.remove(cdRemove);
        }
    }

    private synchronized Object getCacheObject(String className, long id) {
        CacheData cacheData = null;
        Iterator<CacheData> it = cacheList.iterator();
        while (it.hasNext()) {
            CacheData cd = it.next();
            if (cd.getClassName().equals(className) && cd.getId() == id) {
                cacheData = cd;
                break;
            }
        }

        if (cacheData == null) {
            return null;

        } else {
            return cacheData.getObject();
        }

    }

    public synchronized long addObject(Object object) {
        long ds = new Date().getTime();

        try {
            long id = handler.add(object);
            if (id < 0) {
                return id;
            }

            if (!dataFileChannelMap.containsKey(object.getClass().getName())) {
                dataFileChannelMap.put(object.getClass().getName(), new DataFileChannel(object.getClass().getName()));
            }
            DataFileChannel dfc = dataFileChannelMap.get(object.getClass().getName());

            if(object instanceof KnowsId){
                ((KnowsId)object).setId(id);
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            dfc.add(id, baos.toByteArray(), object);

            System.out.println("Query add: " + (new Date().getTime() - ds) + "ms.");

            return id;

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return -1l;
        }
    }
    
    public synchronized boolean updateJournalObject(long id, Object object) {
        return handler.update(id, object);
    }

    public synchronized boolean updateObject(long id, Object object) {
        long ds = new Date().getTime();

        boolean result;

        try {
            boolean resultHandler = handler.update(id, object);

            DataFileChannel dfc = dataFileChannelMap.get(object.getClass().getName());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            boolean resultChannel = dfc.update(id, baos.toByteArray(), object);
            result = resultHandler && resultChannel ? true : false;

            System.out.println("Query updateObject: " + (new Date().getTime() - ds) + "ms.");

            return result;

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return false;
        }
    }

    public synchronized boolean deleteObject(long id, Class cls) {
        boolean result = false;
        if (cls == null) {
            return result;
        }

        result = deleteObject(id, cls.getName());
        return result;
    }

    public synchronized boolean deleteObject(long id, Object object) {
        boolean result = false;
        if (object == null) {
            return result;
        }

        result = deleteObject(id, object.getClass().getName());
        return result;
    }

    public synchronized boolean deleteObject(long id, String className) {
        try {
            handler.delete(id, className);
            DataFileChannel dfc = dataFileChannelMap.get(className);
            return dfc.delete(id);

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return false;
        }
    }

    public synchronized Object getObject(String className, long id) {
        if (className == null || id < 0) {
            return null;
        }

        if (className.equals("")) {
            return null;
        }

        DataFileChannel dfc = dataFileChannelMap.get(className);
        if (dfc == null) {
            return null;

        } else {
            Object obj = dfc.getObject(id);
            return obj;
        }
    }

    public synchronized Map<Long, Object> getMapResultSet(Request request, Date dateStart, Date dateEnd) {
        DataFileChannel dfc = dataFileChannelMap.get(request.getClazz().getName());
        if (dfc == null) {
            return new HashMap<>();
        }

        return dfc.getCollection(dateStart, dateEnd);
    }

    public synchronized Map<Long, Object> getMapResultSet(Request request) {
        DataFileChannel dfc = dataFileChannelMap.get(request.getClazz().getName());
        if (dfc == null) {
            return new HashMap<>();
        }

        return dfc.getCollection();
    }

    public synchronized Map<Long, Object> getMapResultSet(String className, ArrayList<Long> idList) {
        Map<Long, Object> resultSet = new HashMap<>();
        return resultSet;
    }

    public synchronized Map<Long, Object> getMapResultSet(String className, int indexFrom, int indexTo) {
        Map<Long, Object> mapResultSet = new HashMap<>();
        return mapResultSet;
    }

    public Map<Method, Expression> getFilter(Request request) {
        LinkedHashMap<Method, Expression> mf = new LinkedHashMap();
        if (request.getFilter() != null) {
            Iterator it = request.getFilter().keySet().iterator();
            String methodName = "" + it.next();
            for (Method method : request.getClazz().getMethods()) {
                if (method.getName().equals(methodName)) {
                    mf.put(method, request.getFilter().get(methodName));
                }
            }
        }

        return mf;
    }

    public ArrayList<Long> getIdList(String className, String method, String value) {
        return indexer.getIdList(className, method, value);
    }

    public ArrayList<Long> getIdList(RequestCondition requestConditions) {
        return indexer.getIdList(requestConditions);
    }
}
