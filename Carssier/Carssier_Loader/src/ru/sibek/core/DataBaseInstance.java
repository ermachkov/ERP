/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core;

import org.uui.db.DataBase;

/**
 *
 * @author developer
 */
public class DataBaseInstance {
    
    private static DataBaseInstance self = null;
    private DataBase dataBase;
    
    public synchronized static DataBaseInstance getInstance(){
        if(self == null){
            self = new DataBaseInstance();
        }
        
        return self;
    }
    
    public DataBase getDataBase(){
        return dataBase;
    }
    
    public void setDataBase(DataBase dataBase){
        this.dataBase = dataBase;
    }
    
}
