/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class EventCollector implements Serializable{
    
    private static EventCollector self = null;
    private LinkedHashSet<EventItem> eventSet = new LinkedHashSet<>();
    
    public synchronized static EventCollector getInstance(){
        if(self == null){
            self = new EventCollector();
        }
        
        return self;
    }
    
    public boolean addEvent(DataBaseEvent evt){
        //DataBaseEventLight evtl = new DataBaseEventLight(evt.getClassName(), evt.getId(), evt.getAction());
//        EventItem eventItem = new EventItem(evt);
//        boolean s = eventSet.add(eventItem);
//        Logger.getGlobal().log(Level.INFO, "add event {0} result = {1}", new Object[]{evt, s});
//        return s;
        return true;
    }
    
    public ArrayList<EventItem> getEvents(long timestamp){
        ArrayList<EventItem> list = new ArrayList<>();
        for(EventItem ei : eventSet){
            if(ei.getTimestamp() > timestamp){
                list.add(ei);
            }
        }
        
        return list;
    }
    
}
