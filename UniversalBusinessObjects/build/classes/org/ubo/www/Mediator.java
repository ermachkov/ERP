/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.www;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ContentHandler;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Mediator implements Serializable {

    static final long serialVersionUID = 1L;
    private Map<String, Object> attributes;

    public Mediator() {
        //
    }
    
    public static Mediator getMediatorError(int errorCode, String message){
        message = Objects.toString(message, "");
        Mediator mediator = new Mediator();
        Map<String, Object> map = new HashMap<>();
        map.put("status", errorCode);
        map.put("statusHuman", message);
        mediator.setAttributes(map);
        return mediator;
    }
    
    public static Mediator getMediator(int errorCode, String message, Map<String, Object> mData){
        message = Objects.toString(message, "");
        Mediator mediator = new Mediator();
        Map<String, Object> map = new HashMap<>();
        map.put("status", errorCode);
        map.put("statusHuman", message);
        map.putAll(mData);
        mediator.setAttributes(map);
        return mediator;
    }
    
    public static Mediator getMediatorOk(){
        Mediator mediator = new Mediator();
        Map<String, Object> map = new HashMap<>();
        map.put("status", 0);
        map.put("statusHuman", "Everything ok");
        mediator.setAttributes(map);
        return mediator;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    public byte[] getSerailData() {
        byte buf[] = null;
        try {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(this);
                oos.flush();
                baos.flush();
                buf = baos.toByteArray();
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);

        } finally {
            return buf;
        }
    }

    @Override
    public String toString() {
        return "Mediator{" + "attributes=" + attributes + '}';
    }
}
