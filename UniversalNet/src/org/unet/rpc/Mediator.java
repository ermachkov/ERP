/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unet.rpc;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Date;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Mediator implements Serializable {

    private String className, methodName;
    private Object[] arguments;
    private Object result;
    public static int NOT_INVOKED = 0, INVOKED = 1;
    public static int ADD = 0, GET_RESULT = 1;
    private int status = 0;
    private int type = 0;
    private String mark;

    public Mediator(String className, String methodName, Object[] arguments) {
        this.className = className;
        this.methodName = methodName;
        this.arguments = arguments;
        
    }

    public Mediator(String mark) {
        this.mark = mark;
        type = Mediator.GET_RESULT;
    }
    
    public void setClientAddress(InetSocketAddress clientAddress){
        mark = "" + new Date().getTime() + clientAddress;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Mediator{" + "className=" + className + ", methodName=" + methodName + ", arguments=" + arguments + ", result=" + result + ", status=" + status + ", type=" + type + ", mark=" + mark + '}';
    }
}
