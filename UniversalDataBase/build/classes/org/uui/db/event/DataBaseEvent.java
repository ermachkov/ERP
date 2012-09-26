/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia, created 11.03.2011
 * (C) Copyright by Zubanov Dmitry
 */
package org.uui.db.event;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class DataBaseEvent implements Serializable{

    private long id = -1;
    private String className;
    private File file;
    private int action = -1;
    private Map eventMap;
    private Serializable object;
    public static int ADD = 0, MODIIFY = 1, DELETE = 2;

    public DataBaseEvent(Map eventMap) throws Exception {
        this.eventMap = eventMap;
        if (eventMap != null) {
            if (!eventMap.isEmpty()) {
                id = (Long) eventMap.get("id");
                className = Objects.toString(eventMap.get("class"));
                file = (File) eventMap.get("file");
                action = (int) eventMap.get("action");
                if(eventMap.get("object") instanceof Serializable){
                    object = (Serializable) eventMap.get("object");
                }
            }
        }
    }

    public int getAction() {
        return action;
    }

    public String getClassName() {
        return className;
    }

    public File getFile() {
        return file;
    }

    public long getId() {
        return id;
    }

    public long getEventTime() {
        long val = -1;

        if (file.canRead()) {
            val = file.lastModified();
        }

        return val;
    }

    public boolean isPresent(String... classesName) {
        boolean result = false;
        for (String str : classesName) {
            if (str.equals(className)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return Objects.toString(eventMap, "eventMap is null");
    }
}
