/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.DBProperties;
import org.jssdb.core.proxy.KnowsId;
import org.jssdb.event.Event;
import org.jssdb.event.EventAdapter;
import org.jssdb.event.ServiceEvent;
import org.jssdb.event.ServiceEventListener;
import org.jssdb.query.Query;
import org.jssdb.query.Request;
import org.jssdb.query.RequestCondition;
import org.jssdb.utils.MetaInfoData;
import org.uui.db.event.*;
import org.uui.db.net.Client;
import org.uui.db.net.Server;

/**
 *
 * @author anton
 */
public class DataBase implements Serializable {

    private static DataBase self = null;
    private DBProperties dataBase = DBProperties.getInstance();
    private Query query;
    private EventListenerList listenerList = new EventListenerList();
    public static int ADD = 0, MODIFY = 1, DELETE = 2;
    private AtomicBoolean isBinded = new AtomicBoolean();
    private Client client;

    private DataBase(String confPath) {
        dataBase.setProperties(confPath);
        dataBase.initMe();
        bindServiceListener();

        if (dataBase.isNetworkMode()) {
            if (dataBase.isServerMode()) {
                Server.startServer(dataBase.getServerHost(), dataBase.getServerPort());
            } else {
                client = Client.getInstance(dataBase.getServerHost(), dataBase.getServerPort());
            }
        }

        bindCustomListener();
    }

    public static synchronized DataBase getInstance(String confPath) {
        if (self == null) {
            self = new DataBase(confPath);
        }

        return self;
    }

    public static synchronized DataBase getDataBase() {
        return self;
    }

    public String getPathToDB() {
        return dataBase.getPathToDB();
    }

    public void init() {
        query = Query.getInstance();
        query.init();
    }

    private void bindServiceListener() {
        dataBase.addServiceEventListener(new ServiceEventListener() {
            @Override
            public void serviceEvent(ServiceEvent evt) {
                fireServiceEvent(new DataBaseServiceEvent(evt.getEvent()));
            }
        });
    }

    private void bindCustomListener() {
        dataBase.addJSSDBEventListener(new EventAdapter() {
            @Override
            public void allEvent(Event evt) {
                Logger.getGlobal().log(Level.FINE, "Event from data base {0}", evt);
                try {
                    DataBaseEvent dbe = new DataBaseEvent(evt.getEventMap());
                    initTransients(evt.getObject());
                    fireDataBaseEvent(dbe);
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }

            @Override
            public void sync(Event evt) {
                SyncEvent syncEvent;
                if (evt.getAction() == Event.SYNC_START) {
                    syncEvent = new SyncEvent(SyncEvent.SYNC_START);
                } else {
                    syncEvent = new SyncEvent(SyncEvent.SYNC_COMPLITE);
                }
                fireSyncEvent(syncEvent);
            }
        });
    }

    public Map<Long, Object> getObjects(String className, int fromIndex, int toIndex) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (Map<Long, Object>) client.getResult(
                    getMethod("getObjects", String.class, int.class, int.class),
                    new Object[]{className, fromIndex, toIndex});

        } else {
            Map<Long, Object> map = new HashMap<>();
            if (fromIndex >= toIndex) {
                return map;
            }

            if (toIndex >= getObjectCount(className)) {
                return map;
            }

            map = query.getMapResultSet(className, fromIndex, toIndex);
            return map;
        }

    }

    /**
     *
     * @param map
     * @param method
     * @param conditions
     * @return
     */
    public Map<Long, Object> applyFilterMap(Map<Long, Object> map, String method, Condition conditions) {
        Map<Long, Object> mResult = new LinkedHashMap();
        org.uui.db.Request request = org.uui.db.Request.newRequest(method, conditions);
        try {
            mResult = DataBaseRequest.getResult(map, request);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        }

        return mResult;
    }

    //Monitor count = 27947
    //27937 27946
    /**
     *
     * @param map
     * @param method
     * @param conditions
     * @return
     */
    public ArrayList applyFilterList(Map<Long, Object> map, String method, Condition conditions) {
        Map<Long, Object> mResult = new LinkedHashMap();
        org.uui.db.Request request = org.uui.db.Request.newRequest(method, conditions);
        try {
            mResult = DataBaseRequest.getResult(map, request);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        }

        return DataBaseRequest.toArrayList(mResult);
    }

    /**
     *
     * @param className Полное имя класса
     * @param method Имя метода
     * @param conditionType Условие соответствия
     * @param valueCondition Сравниваемое значение<br/> Сравниваемое значение
     * всегда представляется в виде String<br/> Пример:<br> "1", "100.50",
     * "Петров", "2011-05-04 23:50:20"
     * @return Отфильтрованный Map используя простой запрос. Параметр
     * conditionType, может принимать значения:<br/> <strong> <ul>
     * <li>EQUAL</li> <li>LESS</li> <li>MORE</li> <li>LESS_OR_EQUAL</li>
     * <li>MORE_OR_EQUAL</li> <li>OBJECT_EQUAL</li> <li>REGEXP</li> </ul>
     * </strong>
     */
    public Map<Long, Object> getFilteredResultMap(String className, String method,
            int conditionType, String valueCondition) {

        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (Map<Long, Object>) client.getResult(
                    getMethod("getFilteredResultMap", String.class, String.class, int.class, String.class),
                    new Object[]{className, method, conditionType, valueCondition});

        } else {
            Map<Long, Object> map = new LinkedHashMap();
            Condition condition = Condition.newCondition(conditionType, valueCondition);
            org.uui.db.Request request = org.uui.db.Request.newRequest(method, condition);
            try {
                map = DataBaseRequest.getResult(className, request, this);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }

            initTransients(map);
            return map;
        }



    }

    /**
     *
     * @param className Полное имя класса
     * @param method Имя метода
     * @param conditions Набор условий Condition применяемый к значению
     * получаемому из method
     * @return Отфильтрованный Map используя комбинированный запрос.<br/>
     * Пример:<br/> Найти всех работников старше 25 и младше 60 лет<br/><br/>
     * <i> Condition c1 = Condition.newCondition(Condition.MORE_OR_EQUAL,
     * "25");<br/> Condition c2 =
     * Condition.newCondition(Condition.LESS_OR_EQUAL, "60");<br/> Map<Long,
     * Object> map = getFilteredResultMap("ru.saas.Person", "getAge", c1,
     * c2);<br/> </i>
     */
    public Map<Long, Object> getFilteredResultMap(String className, String method, Condition... conditions) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (Map<Long, Object>) client.getResult(
                    getMethod("getFilteredResultMap", String.class, String.class, Condition[].class),
                    new Object[]{className, method, conditions});

        } else {
            Map<Long, Object> map = new LinkedHashMap();
            org.uui.db.Request request = org.uui.db.Request.newRequest(method, conditions);
            try {
                map = DataBaseRequest.getResult(className, request, this);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }

            initTransients(map);
            return map;
        }
    }

    /**
     *
     * @param classNameWhoKnowsId Полное имя класса который реализует интерфейс
     * KnowsId
     * @param method Имя метода
     * @param conditionType Условие соответствия
     * @param valueCondition Сравниваемое значение<br/> Сравниваемое значение
     * всегда представляется в виде String<br/> Пример:<br> "1", "100.50",
     * "Петров", "2011-05-04 23:50:20"
     * @return Отфильтрованный ArrayList используя простой запрос. Параметр
     * conditionType, может принимать значения:<br/> <strong> <ul>
     * <li>EQUAL</li> <li>LESS</li> <li>MORE</li> <li>LESS_OR_EQUAL</li>
     * <li>MORE_OR_EQUAL</li> <li>OBJECT_EQUAL</li> <li>REGEXP</li> </ul>
     * </strong>
     */
    public ArrayList getFilteredResultList(String classNameWhoKnowsId, String method,
            int conditionType, String valueCondition) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (ArrayList) client.getResult(
                    getMethod("getFilteredResultList", String.class, String.class, int.class, String.class),
                    new Object[]{classNameWhoKnowsId, method,
                        conditionType, valueCondition});

        } else {
            Map<Long, Object> map = getFilteredResultMap(classNameWhoKnowsId, method, conditionType, valueCondition);
            initTransients(map);
            return DataBaseRequest.toArrayList(map);
        }
    }

    /**
     *
     * @param classNameWhoKnowsId Полное имя класса который реализует интерфейс
     * KnowsId
     * @param method Имя метода
     * @param conditions Набор условий Condition применяемый к значению
     * получаемому из method
     * @return Отфильтрованный ArrayList используя комбинированный запрос.<br/>
     * Пример:<br/> Найти всех работников старше 25 и младше 60 лет<br/><br/>
     * <i> Condition c1 = Condition.newCondition(Condition.MORE_OR_EQUAL,
     * "25");<br/> Condition c2 =
     * Condition.newCondition(Condition.LESS_OR_EQUAL, "60");<br/>
     * ArrayList<Person> personList = getFilteredResultList("ru.saas.Person",
     * "getAge", c1, c2);<br/> </i>
     */
    public ArrayList getFilteredResultList(String classNameWhoKnowsId,
            String method, Condition... conditions) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (ArrayList) client.getResult(
                    getMethod("getFilteredResultList", String.class, String.class, Condition[].class),
                    new Object[]{classNameWhoKnowsId, method, conditions});
        } else {
            Map<Long, Object> map = getFilteredResultMap(classNameWhoKnowsId, method, conditions);
            initTransients(map);
            return DataBaseRequest.toArrayList(map);
        }
    }

    public Object getObject(String className, long id) {
        Object value = null;
        try {
            if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
                value = client.getResult(
                        getMethod("getObject", String.class, long.class),
                        new Object[]{className, id});
                if (value != null) {
                    initTransients(value);
                }

                return value;

            } else {
                value = query.getObject(className, id);
                if (value != null) {
                    initTransients(value);
                }

                return value;
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, className, e);
            return value;
        }
    }

    @Deprecated
    public Object getObject(String className) {
        Object object = null;
        try {
            Request request = new Request(className);
            Map<Long, Object> resultMap = query.getMapResultSet(request);
            if (resultMap != null) {
                if (!resultMap.isEmpty()) {
                    Long[] idSet = resultMap.keySet().toArray(new Long[resultMap.keySet().size()]);
                    Arrays.sort(idSet);
                    object = resultMap.get(idSet[0]);
                }
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, className, e);

        } finally {
            initTransients(object);
            return object;
        }
    }

    public int getObjectCount(String className) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (int) client.getResult(
                    getMethod("getObjectCount", String.class),
                    new Object[]{className});

        } else {
            int count = -1;
            Path p = Paths.get(dataBase.getPathToDB(), className);
            if (Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)) {
                count = p.toFile().listFiles().length;
            }

            return count;
        }

    }

    public Object getObject(String className, String method, Object val) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return client.getResult(
                    getMethod("getObject", String.class, String.class, Object.class),
                    new Object[]{className, method, val});

        } else {
            Object findObject = null;

            try {
                Map<Long, Object> mapResult = query.getMapResultSet(new Request(className));
                Iterator<Long> it = mapResult.keySet().iterator();
                while (it.hasNext()) {
                    long id = it.next();
                    Object o = mapResult.get(id);

                    Method findMethod = null;
                    for (Method m : o.getClass().getMethods()) {
                        if (m.getName().equals(method)) {
                            findMethod = m;
                            break;
                        }
                    }

                    if (findMethod == null) {
                        continue;
                    }

                    Object result = findMethod.invoke(o, new Object[]{});
                    if (result != null) {
                        if (result.equals(val)) {
                            findObject = o;
                            break;
                        }
                    }
                }

            } catch (ClassNotFoundException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getGlobal().log(Level.INFO,
                        MessageFormat.format("className {0}\n, method {1}\n, val {2}\n\n",
                        new Object[]{className, method, val}), ex);

            } finally {
                //Logger.getGlobal().log(Level.INFO, "return object {0}", findObject);
                initTransients(findObject);
                return findObject;
            }
        }
    }

    public ArrayList<Object> getObjects(String className, String method, Object val) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (ArrayList<Object>) client.getResult(
                    getMethod("getObjects", String.class, String.class, Object.class),
                    new Object[]{className, method, val});

        } else {
            ArrayList<Object> found = new ArrayList<>();
            try {
                try {
                    Map<Long, Object> objs = query.getMapResultSet(new Request(className));
                    for (long id : objs.keySet()) {
                        Object o = objs.get(id);
                        Method m = o.getClass().getMethod(method);
                        if (m.invoke(o).equals(val)) {
                            found.add(o);
                        }
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                    Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
            }
            initTransients(found);
            return found;
        }
    }

    public ArrayList getObjects(String className) {
        ArrayList found = new ArrayList<>();
        try {
            if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
                found = (ArrayList) client.getResult(
                        getMethod("getObjects", String.class),
                        new Object[]{className});
                initTransients(found);

            } else {
                Map<Long, Object> objs = query.getMapResultSet(new Request(className));
                for (Iterator<Long> it = objs.keySet().iterator(); it.hasNext();) {
                    long id = it.next();
                    found.add(objs.get(id));
                }

                initTransients(found);
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, className, e);
            return found;
        }


        return found;
    }

    public long addObject(Object obj) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (long) client.getResult(getMethod("addObject", Object.class),
                    new Object[]{obj});

        } else {
            if (obj instanceof KnowsId) {
                return addObject((KnowsId) obj);

            } else {
                return query.addObject(obj);
            }
        }
    }

    public long addObject(KnowsId obj) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (long) client.getResult(getMethod("addObject", KnowsId.class),
                    new Object[]{obj});

        } else {
            long id = query.addObject(obj);
            obj.setId(id);
            //query.updateObject(id, obj);
            query.updateJournalObject(id, obj);
            return id;
        }
    }

    @Deprecated
    public void updateObject(Object obj) {
        if (Debug.isDebug()) {
            System.out.println("------------------------");
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : elements) {
                System.out.println(element);
            }
        }

        if (obj instanceof KnowsId) {
            updateObject((KnowsId) obj);
        }
        query.updateObject(1, obj);
    }

    public boolean updateObject(KnowsId obj) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (boolean) client.getResult(
                    getMethod("updateObject", new Class[]{KnowsId.class}),
                    new Object[]{obj});

        } else {
            return query.updateObject(obj.getId(), obj);
        }
    }

    public boolean updateObject(long id, Object object) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (boolean) client.getResult(
                    getMethod("updateObject", long.class, Object.class),
                    new Object[]{id, object});

        } else {
            return query.updateObject(id, object);
        }

    }

    public boolean deleteObject(KnowsId obj) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (boolean) client.getResult(
                    getMethod("deleteObject", KnowsId.class),
                    new Object[]{obj});

        } else {
            return deleteObject(obj.getClass().getName(), obj.getId());
        }

    }

    public boolean deleteObject(String className, long id) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (boolean) client.getResult(
                    getMethod("deleteObject", String.class, long.class),
                    new Object[]{className, id});

        } else {
            return query.deleteObject(id, className);
        }

    }

    public Map<Long, Object> getAllObjects(String className) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (Map<Long, Object>) client.getResult(
                    getMethod("getAllObjects", String.class),
                    new Object[]{className});

        } else {
            Map<Long, Object> map = new LinkedHashMap();
            try {
                Request request = new Request(className);
                Map<Long, Object> m = query.getMapResultSet(request);
                initTransients(m);

                Long idArray[] = m.keySet().toArray(new Long[map.size()]);
                Arrays.sort(idArray);
                for (long id : idArray) {
                    map.put(id, m.get(id));
                }

                return map;

            } catch (ClassNotFoundException e) {
                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, className, e);
                return new LinkedHashMap();
            }
        }
    }

    public ArrayList getAllObjectsList(String className, RequestCondition requestCondition) {
        return getAllObjectsList(className, query.getIdList(requestCondition));
    }

    public ArrayList getAllObjectsList(String className, ArrayList<Long> idList) {
        Map<Long, Object> map = query.getMapResultSet(className, idList);

        ArrayList list = new ArrayList();
        if (map != null) {
            ArrayList<Long> idsList = new ArrayList<>();
            idsList.addAll(map.keySet());
            Collections.sort(idsList);
            for (long id : idsList) {
                list.add(map.get(id));
            }
            initTransients(list);
        }

        return list;
    }

    public Map<Long, Object> getAllObjects(String classWhichKnowsId, Date dateStart, Date dateEnd) {
        try {
            Request request = new Request(classWhichKnowsId);
            Map<Long, Object> map = query.getMapResultSet(request, dateStart, dateEnd);
            initTransients(map);
            return map;

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, classWhichKnowsId, e);
            return new HashMap<>();
        }

    }

    public ArrayList getAllObjectsList(String classWhichKnowsId, Date dateStart, Date dateEnd) {
        ArrayList list = new ArrayList();
        try {
            Request request = new Request(classWhichKnowsId);
            Map<Long, Object> map = query.getMapResultSet(request, dateStart, dateEnd);

            if (map != null) {
                ArrayList<Long> idList = new ArrayList<>();
                idList.addAll(map.keySet());
                Collections.sort(idList);
                for (long id : idList) {
                    list.add(map.get(id));
                }
            }

            initTransients(list);
            return list;

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, classWhichKnowsId, e);
            return list;
        }

    }

    /**
     *
     * @param classWhichKnowsId
     * @return Always will be return ArrayList empty or not
     */
    public ArrayList getAllObjectsList(String classWhichKnowsId) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (ArrayList) client.getResult(
                    getMethod("getAllObjectsList", String.class),
                    new Object[]{classWhichKnowsId});

        } else {
            ArrayList list = new ArrayList();
            if (classWhichKnowsId == null) {
                return list;
            }

            Map<Long, Object> map;
            try {
                Request request = new Request(classWhichKnowsId);
                map = query.getMapResultSet(request);

                if (map != null) {
                    for (Long id : map.keySet()) {
                        list.add(map.get(id));
                    }
                }

                initTransients(list);
                return list;

            } catch (ClassNotFoundException e) {
                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, classWhichKnowsId, e);
                return new ArrayList<>();
            }
        }
    }

    public Map<Long, Boolean> deleteAll(String className) {

        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (Map<Long, Boolean>) client.getResult(
                    getMethod("deleteAll", String.class),
                    new Object[]{className});

        } else {
            Map<Long, Boolean> check = new HashMap();
            try {
                Request request = new Request(className);
                Map<Long, Object> map = query.getMapResultSet(request);
                for (long id : map.keySet()) {
                    boolean result = query.deleteObject(id, className);
                    check.put(id, result);
                }

            } catch (ClassNotFoundException e) {
                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, className, e);

            } finally {
                return check;
            }
        }
    }

    public void update(Observable o, Object arg) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addDataBaseSyncEventListener(DataBaseSyncEventListener listener) {
        listenerList.add(DataBaseSyncEventListener.class, listener);
    }

    private void fireSyncEvent(SyncEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == DataBaseSyncEventListener.class) {
                ((DataBaseSyncEventListener) listeners[i + 1]).sync(evt);
            }
        }
    }

    public void addDataBaseEventListener(DataBaseEventListener listener) {
        listenerList.add(DataBaseEventListener.class, listener);
    }

    public void addDataBaseServiceEventListener(DataBaseServiceEventListener listener) {
        listenerList.add(DataBaseServiceEventListener.class, listener);
    }

    private void fireServiceEvent(DataBaseServiceEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == DataBaseServiceEventListener.class) {
                ((DataBaseServiceEventListener) listeners[i + 1]).serviceEvent(evt);
            }
        }
    }

    private void fireDataBaseEvent(DataBaseEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == DataBaseEventListener.class) {

                if (evt.getAction() == 0) {
                    ((DataBaseEventListener) listeners[i + 1]).objectAdded(evt);
                }

                if (evt.getAction() == 1) {
                    ((DataBaseEventListener) listeners[i + 1]).objectModifyed(evt);
                }

                if (evt.getAction() == 2) {
                    ((DataBaseEventListener) listeners[i + 1]).objectDeleted(evt);
                }

                ((DataBaseEventListener) listeners[i + 1]).allEvent(evt);

                //EventCollector.getInstance().addEvent(evt);
            }
        }
    }

    private void initTransients(Map<Long, Object> objs) {
        initTransients(new ArrayList(objs.values()));
    }

    private void initTransients(ArrayList<Object> objs) {
        for (int i = 0; i < objs.size(); i++) {
            initTransients(objs.get(i));
        }
    }

    private void initTransients(Object obj) {
        if (obj instanceof KnowsDB) {
            ((KnowsDB) obj).setDataBase(this);
        }
    }

    public Date getObjectDateModify(KnowsId object) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (Date) client.getResult(
                    getMethod("getObjectDateModify", KnowsId.class),
                    new Object[]{object});

        } else {
            return getObjectDateModify(object.getId(), object.getClass().getName());
        }

    }

    public Date getObjectDateModify(long id, String className) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (Date) client.getResult(
                    getMethod("getObjectDateModify", long.class, String.class),
                    new Object[]{id, className});

        } else {
            Date date = null;
            MetaInfoData mif = query.getObjectDateModify(id, className);
            if (mif != null) {
                date = mif.getModifiedData();
            }
            return date;
        }

    }

    public MetaInfoData getObjectMetaInfo(KnowsId object) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (MetaInfoData) client.getResult(
                    getMethod("getObjectMetaInfo", KnowsId.class),
                    new Object[]{object});

        } else {
            return getObjectMetaInfo(object.getId(), object.getClass().getName());
        }

    }

    public MetaInfoData getObjectMetaInfo(long id, String className) {
        if (dataBase.isNetworkMode() && !dataBase.isServerMode()) {
            return (MetaInfoData) client.getResult(
                    getMethod("getObjectMetaInfo", long.class, String.class),
                    new Object[]{id, className});

        } else {
            MetaInfoData value = null;
            MetaInfoData mif = query.getObjectDateModify(id, className);
            if (mif != null) {
                value = mif;
            }
            return value;
        }
    }

    public DBProperties getDataBaseProperties() {
        return dataBase;
    }

    private String getMethod(String methodName, Class... paramClasses) {
        String s = null;
        try {
            s = getClass().getMethod(methodName, paramClasses).toString();
        } catch (NoSuchMethodException | SecurityException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }

        return s;
    }

    public ArrayList<Long> getIdList(String className, String method, String value) {
        return query.getIdList(className, method, value);
    }
}
