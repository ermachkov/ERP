/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core;

import org.uui.component.Component;
import org.uui.component.WorkPanel;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class OperationButton extends Component{
    
    private String text, className;
    private WorkPanel workPanel;
    
    public OperationButton(String sessionId, String text, String className, WorkPanel workPanel){
        super(sessionId);
        this.text = text;
        this.className = className;
        this.workPanel = workPanel;
    }
}
