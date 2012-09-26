/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unet.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class NetUtils {
    
    public static boolean isAlive(String host, int port, int timeout){
        boolean isAlive = false;
        try {
            InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
            Logger.getGlobal().log(Level.INFO, "remoteAddress = {0}", remoteAddress);
            try (Socket socket = new Socket()) {
                socket.connect(remoteAddress, timeout);
                isAlive = socket.isConnected();
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, host, e);
        } finally {
            Logger.getGlobal().log(Level.INFO, "Host {0} is alive = {1}", new Object[]{host, isAlive});
            return isAlive;
        }
    }
        
    public boolean isDeviceUp(String deviceIpAddress) {
        boolean isDeviceEnable = false;

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.getHostAddress().equals(deviceIpAddress)) {
                        continue;
                    }

                    if (ni.isUp()) {
                        isDeviceEnable = true;
                        Logger.getGlobal().log(Level.INFO, "interface {0} is up!", ni);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "" + deviceIpAddress, e);
        }

        return isDeviceEnable;
    }
    
    public static boolean isDevicePresent(String deviceName){
        boolean result = false;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                if(ni.getName().indexOf(deviceName) == 0){
                    result = true;
                    break;
                }
            }
            
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, deviceName, e);
            
        } finally {
            return result;
        }
        
    }
    
}
