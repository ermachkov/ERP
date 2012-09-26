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
import java.util.ArrayList;

public class MacTableHeaderModel implements Serializable{

    private ArrayList<MacHeaderColumn> headers = new ArrayList();

    public void addHeaderColumn(MacHeaderColumn macHeader){
        headers.add(macHeader);
    }

    public ArrayList<MacHeaderColumn> getHeaders(){
        return headers;
    }

}
