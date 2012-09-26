/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia, created 16.02.2011
 * (C) Copyright by Zubanov Dmitry
 */
package org.uui.table;

import java.io.Serializable;
import java.util.Objects;

public class MacHeaderColumn implements Serializable{

    private String text, width = "";
    private boolean isSortable;
    private Class cls;

    public MacHeaderColumn(String text, Class cls, boolean isSortable) {
        this.text = text;
        this.cls = cls;
        this.isSortable = isSortable;
    }
    
    /**
     * 
     * @param width 
     * Should be in pixels or precentage (e.q. "100", "15%")
     */
    public void setColumnWidth(String width){
        this.width = width;
    }
    
    public String getColumnWidth(){
        return Objects.toString(width, "");
    }

    public Class getHeaderClass(){
        return cls;
    }

    public String getText() {
        return text;
    }

    public boolean isSortable() {
        return isSortable;
    }
}
