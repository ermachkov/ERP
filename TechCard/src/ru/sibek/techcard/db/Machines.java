/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.techcard.db;

import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;
import java.util.ArrayList;
/**
 *
 * @author developer
 */
public class Machines implements Serializable, KnowsId {

    public final long serialVersionUID = 4L;
    private long id;
    private String name = "";
    private String internalPartNumber="";
    private String externalPartNumber="";
    public Machines() {
        //
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

      public String getInternalPartNumber() {
        return internalPartNumber;
    }

    public void setInternalPartNumber(String int_partnumber) {
        this.internalPartNumber = int_partnumber;
    }
    
    public String getExternalPartNumber() {
        return externalPartNumber;
    }

    public void setExternalPartNumber(String ext_partnumber) {
        this.externalPartNumber = ext_partnumber;
    }
    
    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Machines{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", name=" + name + ", int_partnumber=" + internalPartNumber + ", ext_partnumber=" + externalPartNumber + '}';
    }

  
}
