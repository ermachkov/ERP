/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Receipt {

    private ArrayList<ReceiptRow> itemList = new ArrayList<>();
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    public Receipt() {
        //
    }

    public void addReceiptRow(String itemName, BigDecimal quantity, BigDecimal price) {
        itemList.add(new ReceiptRow(itemName, quantity, price));
    }

    public void addReceiptRow(ReceiptRow row) {
        itemList.add(row);
    }

    public ArrayList<ReceiptRow> getReceiptRows() {
        return itemList;
    }

    public BigDecimal getTotalDiscountSum() {
        return totalDiscount;
    }

    public boolean setTotalDiscount(BigDecimal totalDiscountSum) {
        double totalSum = 0;
        for(ReceiptRow rr : itemList){
            totalSum += rr.getPrice().doubleValue() * rr.getQuantity().doubleValue();
        }
        
        if(totalSum < totalDiscountSum.doubleValue()){
            return false;
            
        } else {
            this.totalDiscount = totalDiscountSum;
            return true;
        }
        
    }

    public String toString() {
        String str = "";
        if (itemList != null) {
            for (ReceiptRow row : itemList) {
                str += row.toString() + "\n";
            }
        }

        return str;
    }
}
