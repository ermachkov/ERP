/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ups.print;

/**
 *
 * @author developer
 */
public class PrintResult {

    private boolean result;
    private String description;

    public PrintResult(boolean result, String description) {
        this.result = result;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isError() {
        return result;
    }

    @Override
    public String toString() {
        return "PrintResult{" + "result=" + result + ", description=" + description + '}';
    }
}
