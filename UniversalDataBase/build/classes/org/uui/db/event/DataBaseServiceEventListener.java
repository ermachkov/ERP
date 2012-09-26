/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.event;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface DataBaseServiceEventListener extends EventListener{
    
    public void serviceEvent(DataBaseServiceEvent evt);
    
}
