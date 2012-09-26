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
public class Workers implements Serializable, KnowsId {

    public final long serialVersionUID = 2L;
    private long id;
    private String status = "";
    private String category="";
    private float price= 0.0f;
    //private String time="";
    public Workers() {
        //
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

      public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
    
    /*public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
        return "Workers{" + "serialVersionUID=" + serialVersionUID + ", id=" + id + ", status=" + status + ", category=" + category + ", price=" + price + '}';
    }

    
}
