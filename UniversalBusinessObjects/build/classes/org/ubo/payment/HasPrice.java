/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.payment;

import java.math.BigDecimal;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 30.03.2011
 */
public interface HasPrice {

    public BigDecimal getPrice();

    public void setPrice(BigDecimal price);

    public long getIdPriceReason(); // may be id of object that define price

    public void setIdPriceReason(long id);
}
