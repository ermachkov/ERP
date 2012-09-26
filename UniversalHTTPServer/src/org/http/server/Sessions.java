/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.http.server;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Sessions {
    
    private static Sessions self = null;
    private CopyOnWriteArraySet<String> sessions = new CopyOnWriteArraySet<>();
    
    public synchronized static Sessions getInstance(){
        if(self == null){
            self = new Sessions();
        }
        
        return self;
    }
    
    public boolean addSession(String sessionId){
        return sessions.add(sessionId);
    }
    
}
