/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.utils;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Debug {
    
    public static boolean isDebug(String propertyName){
        boolean result = false;
        String val = System.getProperty(propertyName);
        if(val != null){
            if(val.equals("true")){
                result = true;
            }
        }
        return result;
    }
    
}
