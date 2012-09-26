package org.uui.table;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.EventListener;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
// Declare the listener class. It must extend EventListener.
// A class must implement this interface to get MyEvents.
public interface TableEventListener extends EventListener {

    public void tableEventOccurred(MacTableEvent evt);
}

