package org.uui.db.event;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
// Declare the listener class. It must extend EventListener.
// A class must implement this interface to get MyEvents.
public interface DataBaseEventListener extends EventListener {

    public void objectAdded(DataBaseEvent evt);
    
    public void objectModifyed(DataBaseEvent evt);
    
    public void objectDeleted(DataBaseEvent evt);
    
    public void allEvent(DataBaseEvent evt);
}

