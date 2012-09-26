/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia, created 19.02.2011
 * (C) Copyright by Zubanov Dmitry
 */
package org.uui.table;

import java.io.Serializable;
import java.util.ArrayList;

public class MacTableSummator implements Serializable{

    public static int ALL = 0, SELECT = 1;
    private int type = 0;
    private int[] sumColumns;

    public MacTableSummator(int... columnIndexes) {
        sumColumns = new int[columnIndexes.length];
        System.arraycopy(columnIndexes, 0, sumColumns, 0, sumColumns.length);
    }

    public int[] getSumColumns() {
        return sumColumns;
    }
    
    public ArrayList<Integer> getSumColumnsList(){
        ArrayList<Integer> list = new ArrayList();
        for(int col : sumColumns){
            list.add(col);
        }
        return list;
    }
}
