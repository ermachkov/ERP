/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.io.Serializable;
import org.uui.webkit.WebKitComponent;
import org.uui.webkit.WebKitEventBridge;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class Component implements WebKitComponent, Serializable {

    private String componentName, identificator = null;
    public String session = "";

    public Component(String session) {
        this.session = session;
        WebKitEventBridge.getInstance().registerWebKitComponent(session, this);
    }
    
//    public Component() {
//        WebKitEventBridge.getInstance().registerWebKitComponent(this);
//    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    @Override
    public void setModel(String html) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getModel() {
        return "undefined";
    }

    @Override
    public String getIdentificator() {
        if(identificator == null){
            return "" + hashCode();
        } else {
            return identificator;
        }
    }

    @Override
    public void setIdentificator(String identificator) {
        this.identificator = identificator;
    }

    @Override
    public void fireEvent(String json) {
        if (json == null) {
            return;
        }
        if (json.equals("")) {
            return;
        }
        if (json.equals("{}")) {
            return;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getComponentName() {
        return componentName;
    }

    @Override
    public void setComponentName(String name) {
        componentName = name;
    }
}
