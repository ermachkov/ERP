
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
public class DocumentsTb implements Serializable, KnowsId {

    public final long serialVersionUID = 2L;
    private long id;
    private String name = "";

  

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "DocumentsTb{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", name=" + name + '}';
    }

   


 
}
