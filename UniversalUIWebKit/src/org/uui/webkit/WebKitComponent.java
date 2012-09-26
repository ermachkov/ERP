/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.webkit;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public interface WebKitComponent {
    
    public void setModel(String html);
    
    public String getModel();
    
    public String getIdentificator();
    
    public void setIdentificator(String identificator);
    
    public void fireEvent(String json);
    
    public String getComponentName();
    
    public void setComponentName(String name);
    
}
