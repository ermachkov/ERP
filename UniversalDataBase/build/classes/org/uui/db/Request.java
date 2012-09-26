/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Request {

    private String methodName;
    private Condition conditions[];

    private Request(String methodName, Condition... conditions) {
        this.methodName = methodName;
        this.conditions = conditions;
    }
    
    public static Request newRequest(String methodName, Condition... conditions){
        return new Request(methodName, conditions);
    }

    public Condition[] getConditions() {
        return conditions;
    }

    public String getMethodName() {
        return methodName;
    }
}
