/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.pos;

import org.ucm.cashmachine.ResponseItem;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class PosResponseItem implements ResponseItem {

    private String humanError = "", humanCommand = "", systemCommandName = "", value = "";
    private boolean isError = false;

    public PosResponseItem() {
        //
    }

    public PosResponseItem(String humanError, String humanCommand, String systemCommandName, boolean isError) {
        this.humanCommand = humanCommand;
        this.humanError = humanError;
        this.systemCommandName = systemCommandName;
        this.isError = isError;
    }

    public void setHumanCommand(String humanCommand) {
        this.humanCommand = humanCommand;
    }

    public void setHumanError(String humanError) {
        this.humanError = humanError;
    }

    public void setIsError(boolean isError) {
        this.isError = isError;
    }

    public void setSystemCommandName(String systemCommandName) {
        this.systemCommandName = systemCommandName;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getHumanError() {
        return humanError;
    }

    @Override
    public String getHumanCommand() {
        return humanCommand;
    }

    @Override
    public String getSystemCommandName() {
        return systemCommandName;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isError() {
        return isError;
    }

    @Override
    public String toString() {
        return "PosResponseItem{" + "humanError=" + humanError + ", humanCommand=" + humanCommand + ", systemCommandName=" + systemCommandName + ", value=" + value + ", isError=" + isError + '}';
    }
}
