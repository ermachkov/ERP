/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

import java.util.LinkedList;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class CashMachineResponse {

    private LinkedList<ResponseItem> responseItemList = new LinkedList<>();

    public CashMachineResponse() {
        //
    }
    
    public boolean isError(){
        boolean isError = false;
        for(ResponseItem item : responseItemList){
            if(item.isError()){
                isError = true;
                break;
            }
        }
        
        return isError;
    }
    
    public String getErrorInfo(){
        String info = "";
        boolean isStart = true;
        for(ResponseItem item : responseItemList){
            if(item.isError()){
                if(isStart){
                    info += item.getHumanCommand() + "->" + item.getHumanError();
                    isStart = false;
                    
                } else {
                    info += ", " + item.getHumanCommand() + "->" + item.getHumanError();
                }
            }
        }
        
        return info;
    }

    public CashMachineResponse(ResponseItem item) {
        responseItemList.add(item);
    }

    public void addResponseItem(ResponseItem item) {
        responseItemList.add(item);
    }

    public LinkedList<ResponseItem> getResponseItemList() {
        return responseItemList;
    }

    @Override
    public String toString() {
        return "CashMachineResponse{" + "responseItemList=" + responseItemList + '}';
    }
}
