
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.techcard.db;
import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;
import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;
import java.util.ArrayList;
/**
 *
 * @author developer
 */
public class DocumentState implements Serializable, KnowsId {

    public final long serialVersionUID = 165450L;
    private long id;
    private long DocumentsId=0,DeviceId=0,TechCardId;
    private boolean state=false;
    

    public DocumentState() {
        //
    }

    public long getDocumentsId() {
        return DocumentsId;
    }

    public void setDocumentsId(long DocumentsId) {
        this.DocumentsId = DocumentsId;
    }

    public long getTechCardId() {
        return TechCardId;
    }

    public void setTechCardId(long TechCardId) {
        this.TechCardId = TechCardId;
    }

    public boolean isState() {
        return state;
    }

    public long getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(long DeviceId) {
        this.DeviceId = DeviceId;
    }

    public void setState(boolean state) {
        this.state = state;
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
        return "DocumentState{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", DocumentsId=" + DocumentsId + ", DeviceId=" + DeviceId + ", TechCardId=" + TechCardId + ", state=" + state + '}';
    }

   

  

 
}
