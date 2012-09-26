/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubs.scanner;

/**
 *
 * @author developer
 */
public class ScannerEvent {

    private String code = "undefined";

    public ScannerEvent(String code) {
        if(code != null){
            if(!code.equals("")){
                this.code = code;
            }
        }
    }

    public String getCode() {
        return code;
    }

    public String toString() {
        return "ScannerEvent{" + "code=" + code + '}';
    }
}
