/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.event;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class EventFilter {
    
    private String className;
    private long lastEventDate;
    private DataBaseEvent lastDataBaseEvent;
    private AtomicBoolean isReady = new AtomicBoolean(false);
    
    public EventFilter(String className){
        this.className = className;
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(checker(), 0, 100, TimeUnit.MILLISECONDS);
    }
    
    private Runnable checker(){
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    if((System.currentTimeMillis() - lastEventDate) > 1000){
                        if(isReady.get()){
                            fireEvent(lastDataBaseEvent);
                            isReady.set(false);
                        }
                    }
                } catch (Exception e) {
                }
            }
        };
        
        return r;
    }
    
    public abstract void fireEvent(DataBaseEvent dataBaseEvent);
    
    public void addEvent(DataBaseEvent dataBaseEvent){
        if(dataBaseEvent.isPresent(className)){
            lastEventDate = System.currentTimeMillis();
            lastDataBaseEvent = dataBaseEvent;
            isReady.set(true);
        }
    }
    
}
