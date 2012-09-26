/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.plugin;

import java.awt.Component;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 08.06.2011
 */
public interface RibbonTopWidgetPlugin extends Plugin {
    
    public Component getWidget();
    public int getPosition();
    
}
