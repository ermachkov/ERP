/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DataBaseRequest {

    public static Map<Long, Object> getResult(Class cls, Request request, DataBase dataBase) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return getResult(cls.getName(), request, dataBase);
    }

    public static Map<Long, Object> getResult(String selectClass, Request request, DataBase dataBase) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Map resultMap = new LinkedHashMap();
        Map res;

        if (selectClass == null || request == null) {
            return resultMap;
        }

        if (request.getConditions() == null || request.getConditions().length == 0) {
            return resultMap;
        }

        Map<Long, Object> map = dataBase.getAllObjects(selectClass);
        if (map.isEmpty()) {
            return resultMap;
        }

        Object cObj = map.values().toArray()[0];
        Class clazz = cObj.getClass();
        Method method = null;
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(request.getMethodName())) {
                if (m.getParameterTypes().length == 0) {
                    method = m;
                    break;
                }
            }
        }

        if (method == null) {
            return resultMap;
        }

        // first passage
        RequestHandler requestHandler = new RequestHandler();
        Condition condition = request.getConditions()[0];
        Iterator<Long> it = map.keySet().iterator();
        while (it.hasNext()) {
            long id = it.next();
            Object obj = map.get(id);
            Object val = method.invoke(obj, new Object[]{});
            if(val == null){
                continue;
            }
            
            if (requestHandler.isNumber(val)) {
                if (requestHandler.isPassedNumber(val, condition)) {
                    resultMap.put(id, map.get(id));
                }

            } else if (val.getClass().equals(BigDecimal.class)) {
                if (requestHandler.isPassedNumber(val, condition)) {
                    resultMap.put(id, map.get(id));
                }

            } else if (val.getClass().equals(String.class)) {
                if (condition.getType() == Condition.REGEXP) {
                    Pattern p = Pattern.compile(condition.getConditionValue());
                    Matcher m = p.matcher(Objects.toString(val, "null"));
                    while (m.find()) {
                        resultMap.put(id, map.get(id));
                    }

                } else if(condition.getType() == Condition.EQUAL) {
                    if (Objects.toString(val, "null").equals(condition.getConditionValue())) {
                        resultMap.put(id, map.get(id));
                    }
                    
                } else if(condition.getType() == Condition.NOT_EQUIAL) {
                    if (!Objects.toString(val, "null").equals(condition.getConditionValue())) {
                        resultMap.put(id, map.get(id));
                    }
                }

            } else if (val.getClass().equals(Date.class)) {
                if (requestHandler.isPassedDate(val, condition)) {
                    resultMap.put(id, map.get(id));
                }

            } else if (val.getClass().equals(Boolean.class) || val.getClass().equals(boolean.class)) {
                if (condition.getType() == Condition.EQUAL) {
                    if (("" + val).equals(condition.getConditionValue())) {
                        resultMap.put(id, map.get(id));
                    }
                }
            }
        }

        ArrayList<Map<Long, Object>> listMap = new ArrayList();
        listMap.add(resultMap);
        listMap.add(new LinkedHashMap<Long, Object>());

        if (request.getConditions().length > 1) {
            for (int i = 1; i < request.getConditions().length; i++) {
                condition = request.getConditions()[i];

                it = listMap.get(i - 1).keySet().iterator();
                while (it.hasNext()) {
                    long id = it.next();
                    Object obj = resultMap.get(id);
                    Object val = method.invoke(obj, new Object[]{});
                    if(val == null){
                        continue;
                    }
                    if (requestHandler.isNumber(val)) {
                        if (requestHandler.isPassedNumber(val, condition)) {
                            listMap.get(i).put(id, listMap.get(i - 1).get(id));
                        }

                    } else if (val.getClass().equals(String.class)) {
                        if (condition.getType() == Condition.REGEXP) {
                            Pattern p = Pattern.compile(condition.getConditionValue());
                            Matcher m = p.matcher(Objects.toString(val, "null"));
                            while (m.find()) {
                                listMap.get(i).put(id, listMap.get(i - 1).get(id));
                            }

                        } else if(condition.getType() == Condition.EQUAL){
                            if (Objects.toString(val, "null").equals(condition.getConditionValue())) {
                                listMap.get(i).put(id, listMap.get(i - 1).get(id));
                            }
                            
                        } else if(condition.getType() == Condition.NOT_EQUIAL){
                            if (!Objects.toString(val, "null").equals(condition.getConditionValue())) {
                                listMap.get(i).put(id, listMap.get(i - 1).get(id));
                            }
                        }

                    } else if (val.getClass().equals(Date.class)) {
                        if (requestHandler.isPassedDate(val, condition)) {
                            listMap.get(i).put(id, listMap.get(i - 1).get(id));
                        }

                    } else if (val.getClass().equals(Boolean.class) || val.getClass().equals(boolean.class)) {
                        if (condition.getType() == Condition.EQUAL) {
                            if (("" + val).equals(condition.getConditionValue())) {
                                listMap.get(i).put(id, listMap.get(i - 1).get(id));
                            }
                        }
                    }
                }

                listMap.add(new LinkedHashMap<Long, Object>());
            }

            res = listMap.get(listMap.size() - 2);

        } else {
            res = resultMap;
        }

        return res;
    }

    public static Map<Long, Object> getResult(Map<Long, Object> map, Request request) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Map resultMap = new LinkedHashMap();
        Map res;

        if (map == null || request == null) {
            return resultMap;
        }

        if (map.isEmpty()) {
            return resultMap;
        }

        if (request.getConditions() == null || request.getConditions().length == 0) {
            return resultMap;
        }

        Object cObj = map.values().toArray()[0];
        Class clazz = cObj.getClass();
        Method method = null;
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(request.getMethodName())) {
                if (m.getParameterTypes().length == 0) {
                    method = m;
                    break;
                }
            }
        }

        if (method == null) {
            return resultMap;
        }

        // first passage
        RequestHandler requestHandler = new RequestHandler();
        Condition condition = request.getConditions()[0];
        Iterator<Long> it = map.keySet().iterator();
        while (it.hasNext()) {
            long id = it.next();
            Object obj = map.get(id);
            Object val = method.invoke(obj, new Object[]{});
            if (requestHandler.isNumber(val)) {
                if (requestHandler.isPassedNumber(val, condition)) {
                    resultMap.put(id, map.get(id));
                }

            } else if (val.getClass().equals(String.class)) {
                if (condition.getType() == Condition.REGEXP) {
                    Pattern p = Pattern.compile(condition.getConditionValue());
                    Matcher m = p.matcher(Objects.toString(val, "null"));
                    while (m.find()) {
                        resultMap.put(id, map.get(id));
                    }

                } else {
                    if (condition.getType() == Condition.EQUAL) {
                        if (Objects.toString(val, "null").equals(condition.getConditionValue())) {
                            resultMap.put(id, map.get(id));
                        }

                    } else if (condition.getType() == Condition.NOT_EQUIAL) {
                        if (!Objects.toString(val, "null").equals(condition.getConditionValue())) {
                            resultMap.put(id, map.get(id));
                        }
                    }
                }

            } else if (val.getClass().equals(Date.class)) {
                if (requestHandler.isPassedDate(val, condition)) {
                    resultMap.put(id, map.get(id));
                }
            }
        }

        ArrayList<Map<Long, Object>> listMap = new ArrayList();
        listMap.add(resultMap);
        listMap.add(new LinkedHashMap<Long, Object>());

        if (request.getConditions().length > 1) {
            for (int i = 1; i < request.getConditions().length; i++) {
                condition = request.getConditions()[i];

                it = listMap.get(i - 1).keySet().iterator();
                while (it.hasNext()) {
                    long id = it.next();
                    Object obj = resultMap.get(id);
                    Object val = method.invoke(obj, new Object[]{});
                    if (requestHandler.isNumber(val)) {
                        if (requestHandler.isPassedNumber(val, condition)) {
                            listMap.get(i).put(id, listMap.get(i - 1).get(id));
                        }

                    } else if (val.getClass().equals(String.class)) {
                        if (condition.getType() == Condition.REGEXP) {
                            Pattern p = Pattern.compile(condition.getConditionValue());
                            Matcher m = p.matcher(Objects.toString(val, "null"));
                            while (m.find()) {
                                listMap.get(i).put(id, listMap.get(i - 1).get(id));
                            }

                        } else {
                            if (condition.getType() == Condition.EQUAL) {
                                if (Objects.toString(val, "null").equals(condition.getConditionValue())) {
                                    listMap.get(i).put(id, listMap.get(i - 1).get(id));
                                }

                            } else if (condition.getType() == Condition.NOT_EQUIAL) {
                                if (!Objects.toString(val, "null").equals(condition.getConditionValue())) {
                                    listMap.get(i).put(id, listMap.get(i - 1).get(id));
                                }
                            }
                        }

                    } else if (val.getClass().equals(Date.class)) {
                        if (requestHandler.isPassedDate(val, condition)) {
                            listMap.get(i).put(id, listMap.get(i - 1).get(id));
                        }
                    }
                }

                listMap.add(new LinkedHashMap<Long, Object>());
            }

            res = listMap.get(listMap.size() - 2);

        } else {
            res = resultMap;
        }

        return res;
    }

    public static ArrayList toArrayList(Map<Long, Object> map) {
        ArrayList list = new ArrayList();
        if (map == null) {
            return list;
        }
        
        ArrayList<Long> listMapKeys = new ArrayList<>();
        listMapKeys.addAll(map.keySet());
        Collections.sort(listMapKeys);
        
        Iterator<Long> it = listMapKeys.iterator();
        while(it.hasNext()){
            Long key = it.next();
            list.add(map.get(key));
        }

        return list;
    }
}
