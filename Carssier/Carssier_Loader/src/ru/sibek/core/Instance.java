/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core;

import java.util.concurrent.Executors;

/**
 *
 * @author developer
 */
public class Instance {
    
    public Instance(){
        //
    }
    
    public void startNewInstance(final String session){
        Runnable r = new Runnable() {

            @Override
            public void run() {
                Thread.currentThread().setName("Carssier-Instance-" + session);
                //DataBaseHandler dataBaseHandler = new DataBaseHandler(session);
            }
        };
        
        Executors.newSingleThreadExecutor().execute(r);
    }
    
}
