/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.webkit;

import java.util.Arrays;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class WebKitResult {

    private String[] values;

    public WebKitResult(String... params) {
        values = params;
    }
    
    public WebKitResult(){
        values = new String[]{""};
    }

    public String[] values() {
        return values;
    }
    
    public Object[] getAsArray(){
        Object[] array = new Object[values.length];
        System.arraycopy(values, 0, array, 0, array.length);
        return array;
    }

    @Override
    public String toString() {
        return "Result{" + "values=" + Arrays.toString(values) + '}';
    }
    
    
}
