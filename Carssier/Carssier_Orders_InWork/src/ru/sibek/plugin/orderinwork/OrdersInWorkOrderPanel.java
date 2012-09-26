/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.orderinwork;

import ru.sibek.plugin.neworder.GoodsBasketPanel;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class OrdersInWorkOrderPanel extends GoodsBasketPanel{
    
    public OrdersInWorkOrderPanel(String sessionId){
        super(sessionId);
        setTableViewMode();
        setEditable(false);
    }

    @Override
    public String getName() {
        return "OrdersInWork";
    }

    @Override
    public void setSession(String session) {
        super.setSession(session);
        super.getGoodsOrderPanel().setSession(session);
    }
    
    
    
}
