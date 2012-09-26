/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.plugin;

import org.uui.component.WorkPanel;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 16.12.2010
 */
public interface WorkPanelPlugin extends Plugin {

    public String getSelectorGroupName();

    public int getSelectorGroupPosition();

    public String getSelectorGroupImagePath();//path relative to images directory in .saas

    public int getSelectorLabelPosition();

    public String getWorkPanelClassName();

    public WorkPanel getWorkPanel();

    public String getWorkPanelName();

    public boolean isSingle();
    
    public String getGroupDescription();
    
    public void setSession(String session);
    
}
