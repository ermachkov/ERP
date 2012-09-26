/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.techcard.ui;

import org.uui.webkit.WebKitEventBridge;
import org.uui.webkit.WebKitResult;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class JSHandler {

    public JSHandler() {
    }

    public WebKitResult debug(String message) {
        //Logger.getGlobal().log(Level.INFO, "DEBUG: {0}", message);
        return new WebKitResult();
    }
    
    public WebKitResult sendEvent(String json){
        //WebKitEventBridge.getInstance().execute(new Object[]{json});
        return new WebKitResult();
    }
}
