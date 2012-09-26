/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class RuleItemTypeException extends Exception{
    
    private String message;
    
    public RuleItemTypeException(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return message + "\n" + super.toString();
    }
    
    
    
}
