/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unet.tester;

import java.util.Map;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class Test {
    
    public Test(){
        //
    }
    
    public String setInfo(Map<String, Object> info){
        return info.toString();
    }
    
    public String setInfo(Map<String, Object> info, String str){
        return str;
    }
    
}
