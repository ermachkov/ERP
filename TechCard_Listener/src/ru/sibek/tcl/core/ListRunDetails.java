
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.tcl.core;
import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;
import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;
import java.util.ArrayList;
import java.util.Date;
/**
 *
 * @author developer
 */
public class ListRunDetails implements Serializable, KnowsId {

    public final long serialVersionUID = 5254L;
    private long id;
    private String name = "";
    private String number = "";
    private Date date=null;//; = new Date();
    private ArrayList<Long> rundetails = new ArrayList();

     public ListRunDetails() {
        //
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ArrayList<Long> getRundetails() {
        return rundetails;
    }

    public void setRundetails(ArrayList<Long> rundetails) {
        this.rundetails = rundetails;
    }
    

    public void addDetailtoRun(long id) {
        this.rundetails.add(id);
    }
    public ArrayList<Long> getDocuments() {
        return rundetails;
    }

    public void setDocuments(ArrayList<Long> rundetails) {
        this.rundetails = rundetails;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
    //ArrayList<String> operations = null;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

   /*   public ArrayList getOperations() {
        return operations;
    }

    public void setOperations(ArrayList operations) {
        this.operations = operations;
    }*/
    
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
        return "ListRunDetails{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", name=" + name + ", number=" + number + ", rundetails=" + rundetails + '}';
    }

   
    

 
}
