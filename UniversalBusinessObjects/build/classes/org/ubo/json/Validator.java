/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.json;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class Validator {
    
    public static boolean isValid(JSONObject jsonObject, String ... keys){
        boolean result = true;
        for(String key : keys){
            if(!jsonObject.has(key)){
                result = false;
                break;
            }
        }
        
        return result;
    }
    
}
