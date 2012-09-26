/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ubo.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import org.jssdb.core.proxy.KnowsId;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 29.03.2011
 */
public class Transfer implements Serializable, KnowsId {

    static final long serialVersionUID = 1L;
    
    private long id;
    
    private long idSource; // HasPartner
    private long idDest; // HasPartner
    private BigDecimal sum;
    private Date date;
    private String note;
    private int type;
    private int through;
    private BigDecimal throughPercent;
    private long idPay = -1;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public long getIdDest() {
        return idDest;
    }

    public void setIdDest(long idDest) {
        this.idDest = idDest;
    }

    public long getIdSource() {
        return idSource;
    }

    public void setIdSource(long idSource) {
        this.idSource = idSource;
    }

    public long getIdPay() {
        return idPay;
    }

    public void setIdPay(long idPay) {
        this.idPay = idPay;
    }

    public int getThrough() {
        return through;
    }

    public void setThrough(int through) {
        this.through = through;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public BigDecimal getThroughPercent() {
        return throughPercent;
    }

    public void setThroughPercent(BigDecimal throughPercent) {
        this.throughPercent = throughPercent;
    }

    public String toString() {
        return "Transfer{" + "id=" + id + ", idSource=" + idSource + ", idDest=" + idDest + ", sum=" + sum + ", date=" + date + ", note=" + note + ", type=" + type + ", through=" + through + ", idPay=" + idPay + '}';
    }
    
}
