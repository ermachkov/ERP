
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package db;
import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;
import java.io.Serializable;
import org.jssdb.core.proxy.KnowsId;
import java.util.ArrayList;
/**
 *
 * @author developer
 */
public class Stanki implements Serializable, KnowsId {

    public final long serialVersionUID = 3L;
    private long id;
    private String number = "";

  

    public String getNumber() {
        return number;
    }

    public void setNumber(String name) {
        this.number = name;
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
        return "Stanki{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", name=" + number + '}';
    }

    


 
}
