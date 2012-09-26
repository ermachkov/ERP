/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.net;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Request implements Serializable {

    private String method;
    private Object params[];
    private Object result;
    static final long serialVersionUID = 1L;

    public Request() {
        //
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
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
        String s = null;
        if(params !=  null){
            s = Arrays.toString(params);
        }
        return "Request{" + "method=" + method + ", params=" + s + '}';
    }
}
