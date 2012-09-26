/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface ResponseItem {
    
    public String getHumanError();
    
    public String getHumanCommand();
    
    public String getSystemCommandName();
    
    public String getValue();
    
    public boolean isError();
    
    @Override
    public String toString();
    
}
