/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.core;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class Clipboard {
    
    private static Clipboard self = null;
    public static int CUT = 0, COPY = 1;
    private AtomicInteger refMode = new AtomicInteger(-1);
    private CopyOnWriteArraySet objects = new CopyOnWriteArraySet();
    
    public static synchronized Clipboard getInstance(){
        if(self == null){
            self = new Clipboard();
        }
        
        return self;
    }
    
    public void addObject(int mode, Object object){
        if(mode < 0 && mode > 1){
            return;
        }
        
        if(object == null){
            return;
        }
        
        refMode.set(mode);
        objects.add(object);
    }
    
    public ArrayList getObjects(){
        ArrayList list =  new ArrayList();
        for(Object o : objects){
            list.add(o);
        }
        return list;
    }
    
    public ArrayList getAndClear(){
        ArrayList list =  new ArrayList();
        for(Object o : objects){
            list.add(o);
        }
        
        objects.clear();
        refMode.set(-1);
        return list;
    }
    
    public int getMode(){
        return refMode.get();
    }
    
    public boolean isEmpty(){
        return objects.isEmpty();
    }
    
    public void clear(){
        objects.clear();
        refMode.set(-1);
    }
    
    @Override
    public String toString(){
        return "Clipboard{object = " + Objects.toString(objects, "null") 
                + ", mode = " + refMode + "}";
    }
    
}
