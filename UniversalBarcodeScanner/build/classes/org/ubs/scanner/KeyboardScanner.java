/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubs.scanner;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.TooManyListenersException;
import javax.swing.Timer;

/**
 *
 * @author developer
 */
public class KeyboardScanner implements BarcodeScanner {

    private static KeyboardScanner self = null;
    private Timer timer;
    private String strCode = "";
    private boolean isEnable = false, isDebug = true;
    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    private KeyboardScanner() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
                addKeyEventDispatcher(getKeyEventDispatcher());
        timer = new Timer(250, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (strCode.length() == 13) {
                    fireScannerEvent(new ScannerEvent(strCode));
                }

                strCode = "";
            }
        });

        timer.setRepeats(false);
    }

    private KeyEventDispatcher getKeyEventDispatcher() {
        KeyEventDispatcher dispatcher = new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_RELEASED) {
                    if (!isEnable) {
                        return false;
                    }

                    if (!timer.isRunning()) {
                        timer.start();
                    }

                    strCode += e.getKeyChar();
                    if (isDebug) {
                        System.out.println(e.getKeyChar());
                    }
                }

                return false;
            }
        };

        return dispatcher;
    }

    public synchronized static KeyboardScanner getScanner() {
        if (self == null) {
            self = new KeyboardScanner();
        }

        return self;
    }

    public void addScannerEventListener(ScannerEventListener listener) {
        listenerList.add(ScannerEventListener.class, listener);
    }

    private void fireScannerEvent(ScannerEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ScannerEventListener.class) {
                ((ScannerEventListener) listeners[i + 1]).catchCode(evt);
            }
        }
    }

    @Override
    public void setEnable(boolean enable) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, TooManyListenersException, IOException {
        isEnable = enable;
    }

    @Override
    public void setDebugMode(boolean isDebug) {
        this.isDebug = isDebug;
    }
}
