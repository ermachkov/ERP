/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class PluginInitException extends Exception{
    
    private String className;
    
    public PluginInitException(String className){
        this.className = className;
    }
    
    @Override
    public String toString(){
        return "Can't initialize plugin founded by path " + className;
    }
    
}
