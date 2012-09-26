/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.webkit;

import org.uui.event.EventListener;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface WebKitEventListener extends EventListener{
    
    public Object wkEvent(WebKitEvent evt);
    
}
