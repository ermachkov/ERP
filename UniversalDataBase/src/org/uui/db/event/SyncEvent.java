/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.event;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class SyncEvent {
    
    private int syncStatus;
    public static final int SYNC_START = 0, SYNC_COMPLITE = 0;
    
    public SyncEvent(int syncStatus){
        this.syncStatus = syncStatus;
    }
    
    
    public int getSyncStatus(){
        return syncStatus;
    }
}
