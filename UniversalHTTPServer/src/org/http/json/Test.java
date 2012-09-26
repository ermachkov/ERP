/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.http.json;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class Test {
    
    public static void main(String args[]) throws JSONException{
        String s = "{'src':'workPanel','eventType':'click','eventElement':'explorerTable',"
                + "'eventData':{'row':'6','column':'1','value':'321321'},"
                + "'topButton':'Заказы','operationButton':'Неоплаченные заказы','rightPanel':'none'}";
        JSONObject json = new JSONObject();
        json.put("src", "workPanel");
        json.put("eventType", "click");
        Map<String, Object> m = new HashMap();
        m.put("row", 6);
        m.put("column", 2);
        m.put("value", "qwerty");
        json.put("eventElement", m);
        json.put("topButton", "Заказы");
        json.put("operationButton", "Неоплаченные заказы");
        json.put("rightPanel", "none");
        System.out.println(json.toString());
        
        JSONObject jsonTest = new JSONObject(json.toString());
        System.out.println(jsonTest.get("src"));
        
        //JSONObject jsonTest = new JSONObject();
        //System.out.println(jsonTest.get("src"));
    }
    
}
