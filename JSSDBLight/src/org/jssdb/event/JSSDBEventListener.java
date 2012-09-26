package org.jssdb.event;

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
public interface JSSDBEventListener extends EventListener {

    public void objectAdded(Event evt);
    
    public void objectModifyed(Event evt);
    
    public void objectDeleted(Event evt);
    
    public void allEvent(Event evt);
    
    public void sync(Event evt);
    
}

