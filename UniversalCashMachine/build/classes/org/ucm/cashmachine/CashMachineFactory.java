/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

import java.nio.file.Path;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class CashMachineFactory {
    
    private static CashMachineFactory self = null;
    
    private CashMachineFactory(){
        //sXML = SystemXML.newSystemXML(pathToSystemXML);
    }
    
    public synchronized static CashMachineFactory getInstance(){
        if(self == null){
            self = new CashMachineFactory();
        }
        
        return self;
    }
    
    public synchronized CashMachine getCashMachine(Path pathToSystemXML, 
            String cashMachineName, String pathToKKMConfig){
        CashMachine cm = null;
        CashMachineService service = CashMachineService.getInstance(pathToSystemXML);
        String port = service.getPort(cashMachineName);
        int password = service.getPassword(cashMachineName);
        int speed = service.getSpeed(cashMachineName);
        int textLen = service.getMaxTextFieldLenght(cashMachineName);
        String driver = service.getDriver(cashMachineName);
        switch (driver) {
            case "atol":
                //cm = CashMachineAtol.getInstance(speed, password, port, textLen, pathToKKMConfig);
                cm = new CashMachineAtol(speed, port, textLen, pathToKKMConfig);
                break;
                
            case "shtrih":
                break;
                
            case "virtual":
                cm = CashMachineVirtual.getInstance();
                break;
                
            case "pos":
                cm = new CashMachinePos(speed, port, textLen, pathToKKMConfig);
                break;
        }
        
        return cm;
    }
    
    public synchronized CashMachine getAtolCashMachine(String port, int speed, int maxTextLength, String pathToKKMConfig){
        return new CashMachineAtol(speed, port, maxTextLength, pathToKKMConfig);
    }
    
}
