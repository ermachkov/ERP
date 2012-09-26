/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ubo.credit;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import org.jssdb.core.proxy.KnowsId;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 29.03.2011
 */
public class Credit implements Serializable, KnowsId {
    
    static final long serialVersionUID = 1L;
    
    private long id;
    
    private long idSource; // HasPartner
    private long idDest; // HasPartner
    private BigDecimal sum;
    private BigDecimal percent;
    private Date beginDate;
    private Date endDate;
    private Date closeDate;
    private String note;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
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
    
    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public void setPercent(BigDecimal percent) {
        this.percent = percent;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public String toString() {
        return "Credit{" + "id=" + id + ", idSource=" + idSource + ", idDest=" + idDest + ", sum=" + sum + ", percent=" + percent + ", beginDate=" + beginDate + ", endDate=" + endDate + ", closeDate=" + closeDate + ", note=" + note + '}';
    }
    
}
