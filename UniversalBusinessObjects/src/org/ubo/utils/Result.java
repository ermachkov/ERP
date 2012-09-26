/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.utils;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Result {

    private boolean result;
    private String reason;
    private Object returnObject;

    private Result(boolean result, String reason) {
        this.result = result;
        this.reason = reason == null ? "" : reason;
    }

    private Result(boolean result, String reason, Object object) {
        this.result = result;
        this.reason = reason == null ? "" : reason;
        returnObject = object;
    }
    
    public static Result newResult(boolean status, String message){
        return new Result(status, message);
    }
    
    public static Result newResult(boolean status, String message, Object value){
        return new Result(status, message, value);
    }
    
    public static Result newEmptySuccess(){
        return new Result(true, "Success");
    }
    
    public static Result newResultSuccess(Object value){
        return new Result(true, "Success", value);
    }
    
    public static Result newResultError(String message){
        return new Result(false, message);
    }
    
    public static Result newStackTraceResultError(Thread t){
        String message = "";
        message += "----------Stack trace------------\n";
        for (StackTraceElement ste : t.getStackTrace()) {
            message += "\n" + ste.toString();
        }
        return new Result(false, message);
    }
    
    public static Result newStackTraceResultError(String message, Thread t){
        message += "----------Stack trace------------\n";
        for (StackTraceElement ste : t.getStackTrace()) {
            message += "\n" + ste.toString();
        }
        return new Result(false, message);
    }

    public String getReason() {
        return reason;
    }

    public boolean isError() {
        return !result;
    }

    public Object getObject() {
        return returnObject;
    }

    public String toString() {
        return "Result{" + "result=" + result + ", reason=" + reason + ", returnObject=" + returnObject + '}';
    }
}
