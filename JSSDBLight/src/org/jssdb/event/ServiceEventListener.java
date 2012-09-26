/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.event;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface ServiceEventListener extends EventListener{
    
    public void serviceEvent(ServiceEvent evt);
    
}
