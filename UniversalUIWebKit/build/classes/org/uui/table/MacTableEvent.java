/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia, created 25.03.2011
 * (C) Copyright by Zubanov Dmitry
 */

package org.uui.table;

public class MacTableEvent {
    
    private int row, column;
    private Object value;
    private MacTableModel model;
    
    public MacTableEvent(MacTableModel model, int row, int column, Object value){
        this.model = model;
        this.row = row;
        this.column = column;
        this.value = value;
    }
    
    public Class getClassValue(){
        return model.getColumnClass(column);
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public Object getValue() {
        return value;
    }
    
    public String toString(){
        return "row=" + row + ", column=" + column + ", value=" + value 
                + ", " + model.getColumnClass(column);
    }

}
