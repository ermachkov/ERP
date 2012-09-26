
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
public class Documents implements Serializable, KnowsId {

    public final long serialVersionUID = 10L;
    private long id;
    private String firmname = "OOO Sibek";
    private long marshrutnayaCardId=0;
    private long operationCardId;
    
    public long getMarshrutnayaCardId() {
        return marshrutnayaCardId;
    }

    public void setMarshrutnayaCardId(long marshrutnayaCardId) {
        this.marshrutnayaCardId = marshrutnayaCardId;
    }

    public String getFirmname() {
        return firmname;
    }

    public void setFirmname(String firmname) {
        this.firmname = firmname;
    }

   
 
    public Documents() {
        //
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
        return "Documents{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", firmname=" + firmname + ", MarshrutnayaCardId=" + marshrutnayaCardId + '}';
    }

  

 
}
