/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 16.02.2011 (C) Copyright by Zubanov Dmitry
 */
package org.uui.table;

import java.io.Serializable;
import java.util.ArrayList;

public class MacTableRow implements Serializable{

    private ArrayList<MacTableCell> cellList = new ArrayList();
    private Object value;
    public static int DEFAULT = 0, SUM = 1;
    private int rowType = 0;
    private String style = "";
    private boolean isSelected = false;

    public MacTableRow() {
        //
    }

    public MacTableRow(ArrayList<MacTableCell> cellList) {
        this.cellList = (ArrayList<MacTableCell>) cellList.clone();
        cellList = null;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getRowType() {
        return rowType;
    }

    public void setRowType(int rowType) {
        this.rowType = rowType;
    }

    public ArrayList<MacTableCell> getCells() {
        return cellList;
    }

    public MacTableCell getCell(int column) {
        return cellList.get(column);
    }

    public void addCell(int position, MacTableCell cell) {
        cellList.add(position, cell);
    }

    public void addCell(MacTableCell cell) {
        cellList.add(cell);
    }

    public void setCell(int column, Object value) {
        MacTableCell cell = cellList.get(column);
        cell.setValue(value);
        cellList.set(column, cell);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public MacTableRow setRowData(ArrayList<MacTableCell> cellList) {
        this.cellList = (ArrayList<MacTableCell>) cellList.clone();
        cellList = null;
        return this;
    }

    public int getSize() {
        return cellList.size();
    }

    @Override
    public String toString() {
        return "MacTableRow{" + "cellList=" + cellList + ",value=" + value + '}';
    }
}
