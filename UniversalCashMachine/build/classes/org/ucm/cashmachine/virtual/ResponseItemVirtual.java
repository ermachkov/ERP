/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.virtual;

import org.ucm.cashmachine.ResponseItem;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class ResponseItemVirtual implements ResponseItem{

    @Override
    public String getHumanError() {
        return "No error";
    }

    @Override
    public String getHumanCommand() {
        return "Virtual command";
    }

    @Override
    public String getSystemCommandName() {
        return "Virtual system command";
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public boolean isError() {
        return false;
    }
    
}
