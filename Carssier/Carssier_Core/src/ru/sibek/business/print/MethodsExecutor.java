/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.print;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class MethodsExecutor {

    private LinkedList<String> methodsList, parametersList;
    private Object rootObject, lastObject;
    private int count = 0;

    private MethodsExecutor(LinkedList<String> methodsList, 
            LinkedList<String> parametersList, Object rootObject) {
        this.methodsList = methodsList;
        this.parametersList = parametersList;
        this.rootObject = rootObject;
    }
    
    public static String execute(LinkedList<String> methodsList, 
            LinkedList<String> parametersList, Object rootObject){
        return Objects.toString(new MethodsExecutor(methodsList, parametersList, rootObject).listWalker());
    }

    private Object listWalker() {
        String methodName = methodsList.get(count);
        count++;

        Object[] params = parametersList.toArray();
        Object object = executeMethod(methodName, params, rootObject);
        if (methodsList.size() == count) {
            return object;
            
        } else {
            return walker(object);
        }

    }

    private Object walker(Object object) {
        while (count != methodsList.size()) {
            String methodName = methodsList.get(count);
            count++;
            lastObject = executeMethod(methodName, null, object);
            walker(lastObject);
        }

        return lastObject;
    }

    private Object executeMethod(String methodName, Object[] params, Object object) {
        if(params == null){
            params = new Object[]{};
        }
        
        Object result = null;
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equals(methodName)) {
                try {
                    result = method.invoke(object, params);
                    
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    System.err.println("Object " + object + ", Method " + methodName);
                    Logger.getGlobal().log(Level.WARNING, "Object " + object + ", Method " + methodName, e);
                }

            }
        }

        return result;
    }
}
