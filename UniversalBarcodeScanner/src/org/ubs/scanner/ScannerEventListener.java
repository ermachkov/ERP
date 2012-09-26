/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubs.scanner;

import java.util.EventListener;

/**
 *
 * @author developer
 */
public interface ScannerEventListener extends EventListener {
    
    public void catchCode(ScannerEvent evt);
    
}
