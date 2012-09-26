/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

import java.math.BigDecimal;
import java.util.Objects;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ReceiptRow {

    private String itemName;
    private BigDecimal price, quantity;

    public ReceiptRow(String itemName, BigDecimal quantity, BigDecimal price) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ReceiptRow other = (ReceiptRow) obj;
        if (!Objects.equals(this.itemName, other.itemName)) {
            return false;
        }
        if (!Objects.equals(this.price, other.price)) {
            return false;
        }
        if (!Objects.equals(this.quantity, other.quantity)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.itemName);
        hash = 17 * hash + Objects.hashCode(this.price);
        hash = 17 * hash + Objects.hashCode(this.quantity);
        return hash;
    }

    public String toString() {
        return "ReceiptRow{" + "itemName=" + itemName + ", quantity=" + quantity + ", price=" + price + '}';
    }
}
