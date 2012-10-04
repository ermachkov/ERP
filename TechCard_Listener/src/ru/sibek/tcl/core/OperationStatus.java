
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
/**
 *
 * @author developer
 */
public class OperationStatus implements Serializable, KnowsId {

    public final long serialVersionUID = 5864L;
    private long id;
    private String status = "";
    private long detailid;
    private long operationid;
 

    //ArrayList<String> operations = null;
    public OperationStatus() {
        //
    }

    public long getDetailid() {
        return detailid;
    }

    public void setDetailid(long detailid) {
        this.detailid = detailid;
    }

    public long getOperationid() {
        return operationid;
    }

    public void setOperationid(long operationid) {
        this.operationid = operationid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        return "OperationStatus{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", status=" + status + ", detailid=" + detailid + ", operationid=" + operationid + '}';
    }

    
    

 
}
