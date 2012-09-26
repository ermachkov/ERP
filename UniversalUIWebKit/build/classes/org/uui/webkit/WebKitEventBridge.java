/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.webkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uui.table.MacTableCell;
import org.uui.table.MacTableModel;
import org.uui.table.MacTableRow;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class WebKitEventBridge {

    private static WebKitEventBridge self = null;
    private ConcurrentHashMap<String, Set<WebKitComponent>> components = new ConcurrentHashMap<>();

    private WebKitEventBridge() {
        //refSet.set(webKitComponentSet);
    }

    public static synchronized WebKitEventBridge getInstance() {
        if (self == null) {
            self = new WebKitEventBridge();
        }

        return self;
    }
    
    public boolean registerWebKitComponent(String sessionId, WebKitComponent component) {
        if(components.containsKey(sessionId)){
            return components.get(sessionId).add(component);
            
        } else {
            Set<WebKitComponent> set = new HashSet<>();
            set.add(component);
            components.put(sessionId, set);
            return true;
        }
    }

    public void cleanMacTable(String sessionId, MacTableModel macTableModel) {
        for (MacTableRow row : macTableModel.getRows()) {
            for (MacTableCell cell : row.getCells()) {
                unregisterWebKitComponent(sessionId, cell);
            }
        }
    }

    public boolean unregisterWebKitComponent(String sessionId, WebKitComponent component) {
        return components.get(sessionId).remove(component);
    }

    public boolean unregisterWebKitComponents(String sessionId, ArrayList<WebKitComponent> _components) {
        return components.get(sessionId).removeAll(_components);
    }

    public void pushEventToComponent(String sessionId, String identificator, String json) {
        Logger.getGlobal().log(Level.INFO, "Try pushEventToComponent sessionId {0}, identificator {1}, "
                + "json {2}", new Object[]{sessionId, identificator, json});
        Iterator<WebKitComponent> it = components.get(sessionId).iterator();
        
        boolean isFind = false;
        while(it.hasNext()){
            WebKitComponent wc = it.next();
            if(!wc.getIdentificator().equals(identificator)){
                continue;
            }
            
            wc.fireEvent(json);
            Logger.getGlobal().log(Level.INFO, "Fire event to WebKitComponent {3}, sessionId {0}, identificator {1}, "
                + "json {2}", new Object[]{sessionId, identificator, json, wc});
            isFind = true;
            break;
        }
        
        if(!isFind){
            Logger.getGlobal().log(Level.WARNING, "Can not pushEventToComponent sessionId {0}, identificator {1}, "
                + "json {2}", new Object[]{sessionId, identificator, json});
        }
        
    }

    public Object lookupInvoke(String sessionId, String className, String methodName, Object... params) {
        Object result = null;

        for (WebKitComponent component : components.get(sessionId)) {
            if (!component.getClass().getName().equals(className)) {
                continue;
            }

            for (Method method : component.getClass().getMethods()) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }

                try {
                    result = method.invoke(component, params);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    Logger.getGlobal().log(Level.SEVERE, methodName, e);
                    return result;
                }

            }
        }

        return result;
    }
}
