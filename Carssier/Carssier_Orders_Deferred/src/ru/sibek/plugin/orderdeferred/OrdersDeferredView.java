/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia, created 09.01.2011
 * (C) Copyright by Zubanov Dmitry
 */

package ru.sibek.plugin.orderdeferred;

import ru.sibek.plugin.neworder.GoodsBasketPanel;

public class OrdersDeferredView extends GoodsBasketPanel {
    
    public OrdersDeferredView(String sessionId){
        super(sessionId);
        setTableViewMode();
        setEditable(false);
    }

    @Override
    public String getName() {
        return "Order";
    }
    
    @Override
    public void setSession(String session) {
        super.setSession(session);
        super.getGoodsOrderPanel().setSession(session);
    }

}
