
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
public class TechnologyCard implements Serializable, KnowsId {

    public final long serialVersionUID = 5L;
    private long id;
    private String name = "";
    private String number = "";
    private long documentid;
    private ArrayList<Long> documents = new ArrayList();

    public long getDocumentId() {
        return documentid;
    }

    public void setDocumentId(long documentid) {
        this.documentid = documentid;
    }

    public void addDocument(long id) {
        this.documents.add(id);
    }
    public ArrayList<Long> getDocuments() {
        return documents;
    }

    public void setDocuments(ArrayList<Long> documents) {
        this.documents = documents;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
    //ArrayList<String> operations = null;
    public TechnologyCard() {
        //
    }

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
        return "TechnologyCard{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", name=" + name + ", number=" + number + ", documentid=" + documentid + ", documents=" + documents + '}';
    }

    

 
}
