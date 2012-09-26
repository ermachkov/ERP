/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.webkit;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class WebKitEvent {

    private Object[] event;

    public WebKitEvent(Object[] event) {
        this.event = event;
    }

    public Object[] getEvent() {
        return event;
    }
}
