/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.ui;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author developer
 */
public class MainFrame {
    
    private static MainFrame self = null;
    private ConcurrentHashMap<String, String> operationButtons; 
    
    private MainFrame(){
        operationButtons = new ConcurrentHashMap<>();
    }
    
    public synchronized static MainFrame getInstance(){
        if(self == null){
            self = new MainFrame();
        }
        
        return self;
    }
    
    public void setSelectedOperationButton(String sessionId, String name){
        operationButtons.put(sessionId, name);
    }
    
    public String getSelectedOperationButton(String sessionId){
        return "" + operationButtons.get(sessionId);
    }
}
