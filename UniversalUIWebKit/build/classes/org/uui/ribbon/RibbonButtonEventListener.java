package org.uui.ribbon;

import org.uui.event.EventListener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
// Declare the listener class. It must extend EventListener.
// A class must implement this interface to get MyEvents.
public interface RibbonButtonEventListener extends EventListener {

    public void event(RibbonEvent evt);
}

