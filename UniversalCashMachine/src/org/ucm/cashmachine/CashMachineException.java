/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class CashMachineException extends Exception{
    
    String exceptionMessage = "";
    
    public CashMachineException(String exception){
        exceptionMessage = exception;
    }

    @Override
    public String getMessage() {
        return exceptionMessage;
    }

    @Override
    public String toString() {
        return exceptionMessage;
    }
    
    
    
}
