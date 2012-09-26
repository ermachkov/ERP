/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.ordercomplete;

import ru.sibek.plugin.neworder.GoodsBasketPanel;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class OrdersCompleteOrderPanel extends GoodsBasketPanel {
    
    public OrdersCompleteOrderPanel(String sessionId){
        super(sessionId);
        setEditable(false);
    }

    @Override
    public String getName() {
        return "OrdersComplete";
    }
    
    @Override
    public void setSession(String session) {
        super.setSession(session);
        super.getGoodsOrderPanel().setSession(session);
    }
    
}
