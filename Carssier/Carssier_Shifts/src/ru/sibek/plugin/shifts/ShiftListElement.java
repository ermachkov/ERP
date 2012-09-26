/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.shifts;

import java.math.BigDecimal;
import org.ubo.json.JSONObject;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ShiftListElement implements Comparable {

    private BigDecimal sum, count;
    long id;

    public ShiftListElement(JSONObject json) {
        try {
            sum = new BigDecimal(json.getString("sum"));
            count = new BigDecimal(json.getString("count"));
            id = json.getLong("id");

        } catch (Exception e) {
        }

    }

    public BigDecimal getCount() {
        return count;
    }

    public long getId() {
        return id;
    }

    public BigDecimal getSum() {
        return sum;
    }

    @Override
    public int compareTo(Object o) {
        if(o == null){
            return -1;
        }
        
        if (o instanceof ShiftListElement) {
            return ((Double)((ShiftListElement)o).getSum().doubleValue()).compareTo(sum.doubleValue());
        } else {
            return -1;
        }
    }
}
