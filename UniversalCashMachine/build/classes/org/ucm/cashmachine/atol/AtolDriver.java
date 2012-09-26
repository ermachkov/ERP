/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.atol;

import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Dmitry Zubanov (zubanov@gmail.com) date 30.07.2009
 */
public class AtolDriver implements SerialPortEventListener {

    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    private SerialPort serialPort;
    private InputStream in;
    private OutputStream out;
    final ArrayList<Byte> responseByteList = new ArrayList();
    private boolean isConnected = false;
    //private static AtolDriver self = null;
    private int speed = 115200;
    private int passwd = 0;
    private String port;
    private AtomicLong atomic = new AtomicLong(0);
    private long aLong = 0;
    private Document commandXML, responseXML;
    private XPath xPath = XPathFactory.newInstance().newXPath();
    private static final Logger logger = Logger.getGlobal();

    public AtolDriver(int speed, String port, String pathToConfig) {
        System.out.println("New AtolDriver: speed = " + speed + ", port = " + port + ", pathToConfig = " + pathToConfig);
        this.speed = speed;
        this.port = port;

        try {
            commandXML = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(Paths.get(pathToConfig, "Atol.xml").toString());
            responseXML = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(Paths.get(pathToConfig, "AtolResponse.xml").toString());

        } catch (ParserConfigurationException | SAXException | IOException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }
    }

//    public static AtolDriver getInstance(int speed, String port, String pathToConfig) {
//        if (self == null) {
//            self = new AtolDriver(speed, port, pathToConfig);
//        }
//
//        return self;
//    }
//    public static String getPort() {
//        return port;
//    }
    public boolean isConnected() {
        return isConnected;
    }

    public boolean connect() throws Exception {
        boolean result;
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
        if (portIdentifier.isCurrentlyOwned()) {
            logger.log(Level.INFO, "Error: Port is currently in use");
            result = false;
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);
                isConnected = true;
                result = true;

                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();

            } else {
                logger.log(Level.INFO, "Error: Only serial ports are handled by this example.");
                result = false;
            }
        }

        return result;
    }

    public void disconnect() {
        if (serialPort != null) {
            try {
                logger.info("out.close()");
                out.close();
                logger.info("in.close()");
                in.close();

            } catch (IOException ex) {
                logger.info(ex.getMessage());
            }
            // Close the port.
            logger.info("try serial port close");
            serialPort.close();
            logger.info("serial port closed");
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
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

                byte[] buffer = new byte[1];
                try {
                    while (in.read(buffer) != -1) {
                        responseByteList.add(buffer[0]);
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "", ex);
                }

                break;
        }
    }

    public ArrayList<Byte> executeCommand(String commandName, int password) {
        int timeout = getTimeOut(commandName);
        int[] commandArray = getCommandArray(commandName, password);
        System.out.println("commandArray: " + Arrays.toString(commandArray));
        return executeCommand(commandArray, timeout);
    }

    public ArrayList<Byte> executeCommand(int[] commandArray, int timeout) {
        ArrayList<Byte> list = new ArrayList<>();

        try {
            //ENQ
            out.write(0x05);

            atomic.set(0);
            while (!isACK()) {
                LockSupport.parkNanos(1000000);
                atomic.incrementAndGet();
                if (atomic.get() > 5000) {
                    logger.info("Break isACK() 1 ");
                    break;
                }
            }

            String cmdOut = "Send execute array to cashmachine: ";
            int[] executeArray = DataHandler.getDefault().getDataBlock(commandArray);
            for (int i = 0; i < executeArray.length; i++) {
                out.write(executeArray[i]);
                cmdOut += "" + executeArray[i] + " ";
            }
            logger.info(cmdOut);
            System.out.println("cmdOut: " + cmdOut);

            atomic.set(0);
            while (!isACK()) {
                LockSupport.parkNanos(1000000);
                atomic.incrementAndGet();
                if (atomic.get() > 5000) {
                    logger.info("Break isACK() 2 ");
                    break;
                }
            }

            //EOT
            out.write(0x04);

            atomic.set(0);
            while (!isENQ()) {
                LockSupport.parkNanos(1000000);
                atomic.incrementAndGet();
                if (atomic.get() > 20000) {
                    logger.info("Break isENQ()");
                    break;
                }
            }

            //ACK
            out.write(0x06);

            atomic.set(0);
            boolean isETXCorrect = true;
            while (isETX()) {
                LockSupport.parkNanos(1000000);
                atomic.incrementAndGet();
                if (atomic.get() > timeout) {
                    logger.info("break");
                    isETXCorrect = false;
                    break;
                }
            }
            if (isETXCorrect) {
                list.addAll(responseByteList);
            }

            //ACK
            out.write(0x06);

            atomic.set(0);
            while (!isEOT()) {
                LockSupport.parkNanos(1000000);
                atomic.incrementAndGet();
                if (atomic.get() > 2000) {
                    logger.info("Break isEOT()");
                    break;
                }
            }

        } finally {
            return list;
        }
    }

    private boolean isEOT() {
        boolean result = false;
        for (int i = 0; i < responseByteList.size(); i++) {
            if (((Byte) responseByteList.get(i)) == 0x04) {
                result = true;
                responseByteList.clear();
            }
        }

        return result;
    }

    private boolean isENQ() {
        boolean result = false;
        for (int i = 0; i < responseByteList.size(); i++) {
            if (((Byte) responseByteList.get(i)) == 0x05) {
                result = true;
                responseByteList.clear();
            }
        }

        return result;
    }

    private boolean isACK() {
        boolean result = false;
        for (int i = 0; i < responseByteList.size(); i++) {
            if (((Byte) responseByteList.get(i)) == 0x06) {
                result = true;
                responseByteList.clear();
            }
        }

        return result;
    }

    private boolean isETX() {
        boolean result = true;
        for (int i = 0; i < responseByteList.size(); i++) {
            if (((Byte) responseByteList.get(i)) == 0x03) {
                result = false;
            }
        }
        return result;
    }

    public int[] getCommandArray(String commandName, int password) {
        String str = "";
        try {
            String xPathString;
            if (commandName.startsWith("mode_")) {
                xPathString = "/commands/modes/" + commandName + "/request";

            } else {
                xPathString = "/commands/requests/" + commandName + "/request";
            }

            XPathExpression xPathExpression = xPath.compile(xPathString);
            Node node = (Node) xPathExpression.evaluate(commandXML, XPathConstants.NODE);
            if (node != null) {
                str = node.getAttributes().getNamedItem("command").getNodeValue();
            }

        } catch (XPathExpressionException | DOMException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }

        String arr[] = str.split(",");
        int cmd[] = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            cmd[i] = Integer.parseInt(arr[i].trim());
            if (i == 1) {
                cmd[i] = 0;
            }
        }

        if (commandName.indexOf("mode_") != -1 && !commandName.equals("mode_out")) {
            //00 00 00 00
            String s = "" + password;
            for (int i = s.length(); i < 8; i++) {
                s = "0" + s;
            }

            cmd[4] = Integer.parseInt(s.substring(0, 2));
            cmd[5] = Integer.parseInt(s.substring(2, 4));
            cmd[6] = Integer.parseInt(s.substring(4, 6));
            cmd[7] = Integer.parseInt(s.substring(6, 8));
        }

        String info = "";
        for (int c : cmd) {
            info += Integer.toHexString(c).toUpperCase() + " ";
        }
        logger.log(Level.INFO, "Send command to cashmachine: {0}, command name: {1}", new Object[]{info, commandName});
        return cmd;
    }

    public int getTimeOut(String commandName) {
        int timeout = 2000;
        try {
            String xPathString;
            if (commandName.startsWith("mode_")) {
                xPathString = "/commands/modes/" + commandName + "/request";

            } else {
                xPathString = "/commands/requests/" + commandName + "/request";
            }

            XPathExpression xPathExpression = xPath.compile(xPathString);
            Node node = (Node) xPathExpression.evaluate(commandXML, XPathConstants.NODE);
            if (node != null) {
                timeout = Integer.parseInt(node.getAttributes().getNamedItem("timeout").getNodeValue());
            }

        } catch (XPathExpressionException | DOMException | NumberFormatException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }


        return timeout;
    }
}
