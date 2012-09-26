/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.event;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class DataBaseEventAdapter implements DataBaseEventListener{
    
    @Override
    public void objectAdded(DataBaseEvent evt){}
    
    @Override
    public void objectModifyed(DataBaseEvent evt){}
    
    @Override
    public void objectDeleted(DataBaseEvent evt){}
    
    @Override
    public void allEvent(DataBaseEvent evt){}
    
}
