/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Debug {
    
    public static boolean isDebug(){
        boolean result = false;
        String val = System.getProperty("debug_udb");
        if(val != null){
            if(val.equals("true")){
                result = true;
            }
        }
        return result;
    }
    
}
