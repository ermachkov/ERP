/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.event;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class EventAdapter implements JSSDBEventListener{
    
    @Override
    public void objectAdded(Event evt){}
    
    @Override
    public void objectModifyed(Event evt){}
    
    @Override
    public void objectDeleted(Event evt){}
    
    @Override
    public void allEvent(Event evt){}
    
}
