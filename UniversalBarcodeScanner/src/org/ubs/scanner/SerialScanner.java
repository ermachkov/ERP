/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubs.scanner;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.TooManyListenersException;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.utils.XPathUtil;

/**
 *
 * @author developer
 */
public class SerialScanner implements BarcodeScanner {

    private static SerialScanner self = null;
    private SerialPort serialPort;
    private InputStream in;
    private OutputStream out;
    private boolean isEnable = false, isDebug = true, isConnect = false;
    private int speed = 2400;
    private String port = "/dev/ttyUSB0";
    private int buffer[] = new int[15];
    private int count = 0;
    private boolean isRun = false;
    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    private SerialScanner(String scannerName, Path pathToSystemXML) {
        String val = XPathUtil.getNodeValue("/root/barcode_scanner[@name='" + scannerName + "']", 
                pathToSystemXML.toString(), "debug");
        if(val.equals("true")){
            isDebug = true;
        }
        
        val = XPathUtil.getNodeValue("/root/barcode_scanner[@name='" + scannerName + "']", 
                pathToSystemXML.toString(), "port");
        port = val;
        
        val = XPathUtil.getNodeValue("/root/barcode_scanner[@name='" + scannerName + "']", 
                pathToSystemXML.toString(), "speed");
        speed = Integer.parseInt(val);
    }

    public static synchronized SerialScanner getScanner(String scannerName, Path pathToSystemXML) {
        if (self == null) {
            self = new SerialScanner(scannerName, pathToSystemXML);
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

    private Runnable runner() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                while (true) {
                    LockSupport.parkNanos(100000000);
                }
            }
        };

        return r;
    }

    public void setEnable(boolean enable) {

        if (!isConnect) {
            try {
                isConnect = connect();

            } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | TooManyListenersException | IOException ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
            }
        }

        if (!isRun) {
            Executors.newSingleThreadExecutor().execute(runner());
            isRun = true;
        }

        isEnable = enable;
    }

    public boolean connect() throws NoSuchPortException, PortInUseException,
            UnsupportedCommOperationException, TooManyListenersException,
            IOException {
        boolean result = false;
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
        if (portIdentifier.isCurrentlyOwned()) {
            Logger.getGlobal().log(Level.INFO, "Error: Port is currently in use");
            result = false;

        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                serialPort.addEventListener(getSerialPortEventListener());

                serialPort.notifyOnDataAvailable(true);
                result = true;

                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();

            } else {
                Logger.getGlobal().log(Level.INFO, "Error: Only serial ports are handled by this example.");
                result = false;
            }
        }

        return result;
    }

    private SerialPortEventListener getSerialPortEventListener() {
        SerialPortEventListener listener = new SerialPortEventListener() {

            @Override
            public void serialEvent(SerialPortEvent spe) {
                switch (spe.getEventType()) {
                    case SerialPortEvent.BI:
                    case SerialPortEvent.OE:
                    case SerialPortEvent.FE:
                    case SerialPortEvent.PE:
                    case SerialPortEvent.CD:
                    case SerialPortEvent.CTS:
                    case SerialPortEvent.DSR:
                    case SerialPortEvent.RI:
                    case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                    case SerialPortEvent.DATA_AVAILABLE:
                        try {
                            int b;
                            while ((b = in.read()) != -1) {
                                //System.out.println("" + count + ", " + Integer.toHexString(b));
                                buffer[count] = b;
                                if (count == 14 && buffer[0] == 0x82 && buffer[14] == 0x03) {
                                    pushCode(buffer);
                                }

                                count++;
                                if (count > 14) {
                                    count = 0;
                                    pushError();
                                }
                            }

                        } catch (Exception ex) {
                            Logger.getGlobal().log(Level.SEVERE, "", ex);
                        }

                        break;
                }
            }
        };

        return listener;
    }

    private void clearBuffer() {
        for (int i = 0; i < buffer.length - 1; i++) {
            buffer[i] = 0;
        }
        count = -1;
    }

    private void pushError() {
        // TODO
    }

    private void pushCode(int b[]) {
        if (!isEnable) {
            clearBuffer();
            return;
        }

        String str = "";
        for (int i = 1; i < 14; i++) {
            int c = b[i];
            str += Integer.toHexString(c).substring(1);
        }

        long code = -1;
        try {
            code = Long.parseLong(str);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, str, e);
        }
        
        String strCode = "undefined";
        if(code != -1){
            strCode = "" + code;
            int l = 13 - strCode.length();
            for(int i= 0; i < l; i++){
                strCode = "0" + strCode;
            }
        }

        System.out.println("SerialScanner " + code);
        Logger.getGlobal().log(Level.INFO, "code = {0}", code);
        fireScannerEvent(new ScannerEvent(""+ code));
        clearBuffer();
    }

    @Override
    public void setDebugMode(boolean isDebug) {
        this.isDebug = isDebug;
    }
}
