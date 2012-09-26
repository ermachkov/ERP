/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.query;

import java.io.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class CacheData implements Serializable{

    private byte[] data;
    private String className;
    private long id;
    private final static Object sync = new Object();
    static final long serialVersionUID = 1L;

    public CacheData() {
        //
    }

    public CacheData(Object object, long id) {
        init(object, id);
    }

    private void init(Object object, long id) {
        data = new byte[]{};
        this.id = id;
        this.className = object.getClass().getName();
        updateCacheData(object);
    }

    public static CacheData createCacheData(String className, long id, byte[] data) {
        CacheData cacheData = new CacheData();
        cacheData.setClassName(className);
        cacheData.setId(id);
        cacheData.setData(data);

        return cacheData;
    }

    public void updateCacheData(Object object) {
        synchronized (sync) {
            try {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(object);
                    oos.flush();
                    baos.flush();
                    data = baos.toByteArray();
                }

            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE, null, e);

            }
        }
    }

    public Object getObject() {
        synchronized (sync) {
            try {
                Object object;
                try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        ObjectInputStream ois = new ObjectInputStream(bais)) {
                    object = ois.readObject();
                }
                return object;

            } catch (IOException | ClassNotFoundException e) {
                Logger.getGlobal().log(Level.WARNING, className, e);
                return null;
            }
        }
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CacheData{" + "data=" + data + ", className=" + className + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CacheData other = (CacheData) obj;
        if (!Objects.equals(this.className, other.className)) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.className);
        hash = 79 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
