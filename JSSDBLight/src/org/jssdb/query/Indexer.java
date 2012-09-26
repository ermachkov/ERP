/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.query;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.DBProperties;
import org.jssdb.core.proxy.IndexField;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Indexer {

    private Path rootPath;
    private Class[] allowIndexClasses = new Class[]{BigDecimal.class,
        byte.class, short.class, int.class, long.class, double.class, float.class,
        Byte.class, Short.class, Integer.class, Long.class, Double.class,
        Float.class, Date.class};

    public Indexer() {
        rootPath = Paths.get(DBProperties.getInstance().getPathToDB());
        Path root = rootPath.getRoot();
        rootPath = rootPath.subpath(0, rootPath.getNameCount() - 1);
        rootPath = rootPath.resolve(".index");
        rootPath = Paths.get(root.toString(), rootPath.toString());
        if (!rootPath.toFile().exists()) {
            rootPath.toFile().mkdir();
        }
    }

    public void createIndexses(CopyOnWriteArraySet<CacheData> cacheList) {
        String sValue;

        for (Iterator<CacheData> it = cacheList.iterator(); it.hasNext();) {
            CacheData cacheData = it.next();
            String className = cacheData.getClassName();
            Path p = rootPath.resolve(className);
            if (!p.toFile().exists()) {
                p.toFile().mkdir();
            }

            try {
                Class cls = Class.forName(className);
                for (Method method : cls.getMethods()) {
                    if (method.isAnnotationPresent(IndexField.class)) {
                        Class clsReturn = method.getReturnType();
                        if (clsReturn.isArray()) {
                            continue;
                        }

                        if (clsReturn.isEnum()) {
                            continue;
                        }

                        boolean isAllow = false;
                        for (Class clsAllow : allowIndexClasses) {
                            if (clsAllow.equals(clsReturn)) {
                                isAllow = true;
                                break;
                            }
                        }

                        if (!isAllow) {
                            continue;
                        }

                        Object value = method.invoke(cacheData.getObject(), new Object[]{});
                        Path pMethod = p.resolve(method.getName());
                        if (!pMethod.toFile().exists()) {
                            pMethod.toFile().mkdir();
                        }

                        if (value instanceof Date) {
                            sValue = "" + ((Date) value).getTime();

                        } else if (value instanceof BigDecimal) {
                            sValue = ((BigDecimal) value).toString();

                        } else {
                            sValue = convertNumberToString(value);
                        }

                        String sIndex = sValue + "_" + clsReturn.getSimpleName() + "_" + cacheData.getId();
                        Path pIndex = pMethod.resolve(sIndex);
                        if (pIndex.toFile().exists()) {
                            pIndex.toFile().delete();
                        }
                        pIndex.toFile().createNewFile();
                        Logger.getGlobal().log(Level.FINE, "{0}, {1}, {2}",
                                new Object[]{className, method.getName(), sIndex});
                    }
                }
            } catch (ClassNotFoundException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e) {
                Logger.getGlobal().log(Level.WARNING, className, e);
            }
        }
    }

    private String convertNumberToString(Object value) {
        String s = "null";

        if (value == null) {
            return s;
        }

        if (value.getClass().equals(byte.class)) {
            s = "" + (byte) value;

        } else if (value.getClass().equals(Byte.class)) {
            s = "" + ((Byte) value).byteValue();

        } else if (value.getClass().equals(short.class)) {
            s = "" + ((short) value);

        } else if (value.getClass().equals(Short.class)) {
            s = "" + ((Short) value).shortValue();

        } else if (value.getClass().equals(int.class)) {
            s = "" + ((int) value);

        } else if (value.getClass().equals(Integer.class)) {
            s = "" + ((Integer) value).intValue();

        } else if (value.getClass().equals(long.class)) {
            s = "" + ((long) value);

        } else if (value.getClass().equals(Long.class)) {
            s = "" + ((Long) value).longValue();

        } else if (value.getClass().equals(double.class)) {
            s = "" + ((double) value);

        } else if (value.getClass().equals(Double.class)) {
            s = "" + ((Double) value).doubleValue();

        } else if (value.getClass().equals(float.class)) {
            s = "" + ((float) value);

        } else if (value.getClass().equals(Float.class)) {
            s = "" + ((Float) value).floatValue();

        }

        return s;
    }

    public ArrayList<Long> getIdList(String className, String method, String value) {
        ArrayList<Long> list = new ArrayList<>();
        Path p = rootPath.resolve(className);
        p = p.resolve(method);
        for (File f : p.toFile().listFiles()) {
            String arr[] = f.getName().split("_");
            if (arr[0].equals(value)) {
                list.add(Long.parseLong(arr[2]));
            }
        }
        return list;
    }

    public ArrayList<Long> getIdList(RequestCondition requestConditions) {
        requestConditions.setRootPath(rootPath);
        return requestConditions.getResultSet();
    }
}
