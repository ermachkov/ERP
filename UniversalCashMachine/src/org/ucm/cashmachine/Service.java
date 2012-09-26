/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

import java.util.List;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface Service {
    
    public String getDriver(String cashMachineName);
    
    public boolean addCashMachine(String cashMachineName, String driverName);
    
    public boolean setDriver(String cashMachineName, String driverName);
    
    public String[] getAvialableDrivers();
    
    public boolean setDefaultCashMachine(String cashMachineName);
    
    public String getDefaultCashMachine();
    
    public List<String> getCashMachines();
    
    public boolean setSpeed(String cashMachineName, int speed);
    
    public boolean setPort(String cashMachineName, String port);
    
    public boolean setPassword(String cashMachineName, int password);
    
    public int getSpeed(String cashMachineName);
    
    public String getPort(String cashMachineName);
    
    public int getPassword(String cashMachineName);
    
    public int getMaxTextFieldLenght(String cashMachineName);
    
    public String getConfigDir(String cashMachineName);
    
    public boolean setConfigDir(String cashMachineName, String folderName);
    
}
