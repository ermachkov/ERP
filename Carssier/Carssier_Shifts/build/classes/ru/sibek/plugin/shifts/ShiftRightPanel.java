/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.shifts;

import org.uui.component.RightPanel;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class ShiftRightPanel extends RightPanel{
    
    public ShiftRightPanel(String sessionId){
        super(sessionId);
    }

    @Override
    public String getName() {
        return "Свойства";
    }
}
