/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.core.orders.paid;

import java.util.Date;
import org.ubo.document.Order;
import org.ubo.tree.treedatesorted.TreeDateGetter;

/**
 *
 * @author anton
 */
public class PaidOrdersDateGetter implements TreeDateGetter {

    @Override
    public Date getDate(Object obj) {
        Order ord = (Order) obj;
        return ord.getPaidDate();
    }
    
}
