/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubs.scanner;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.TooManyListenersException;

/**
 *
 * @author developer
 */
public interface BarcodeScanner {
    
    public void addScannerEventListener(ScannerEventListener listener);
    
    public void setEnable(boolean enable) throws NoSuchPortException, PortInUseException, 
            UnsupportedCommOperationException, TooManyListenersException, 
            IOException;
    
    public void setDebugMode(boolean isDebug);
    
}
