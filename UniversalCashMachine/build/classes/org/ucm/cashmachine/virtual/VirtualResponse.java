/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.virtual;

import org.ucm.cashmachine.CashMachineResponse;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class VirtualResponse {
    
    public static CashMachineResponse getCashMachineResponse(){
        CashMachineResponse cashMachineResponse = new CashMachineResponse();
        cashMachineResponse.addResponseItem(new ResponseItemVirtual());
        return cashMachineResponse;
    }
    
}
