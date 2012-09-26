/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.event;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class EventItem implements Comparable, Serializable{

    private DataBaseEvent evt;
    private long timestamp;

    public EventItem(DataBaseEvent evt) {
        this.evt = evt;
        timestamp = new Date().getTime();
    }

    public String toString() {
        return "EventItem{" + "evt=" + evt + ", timestamp=" + timestamp + '}';
    }

    public DataBaseEvent getEvt() {
        return evt;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Object o) {
        int compare = 0;
        if(o instanceof EventItem){
            if(!o.equals(this)){
                EventItem eventItem = (EventItem)o;
                compare = eventItem.getTimestamp().compareTo(timestamp);
            }
        }
        
        return compare;
    }
}
