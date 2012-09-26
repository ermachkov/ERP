/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.pos;

import org.ucm.cashmachine.CashMachineResponse;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class PosResponse {
    
    public static CashMachineResponse getNoErrorsCashMachineResponse(){
        CashMachineResponse cashMachineResponse = new CashMachineResponse();
        cashMachineResponse.addResponseItem(new ResponseItemPos());
        return cashMachineResponse;
    }
    
}
