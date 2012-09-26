/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.event;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface DataBaseSyncEventListener extends EventListener{
    
    public void sync(SyncEvent evt);
    
}
