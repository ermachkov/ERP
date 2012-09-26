/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.order.unpaid;

import ru.sibek.plugin.neworder.GoodsBasketPanel;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class UnpaidOrderPanel extends GoodsBasketPanel {
    
    public UnpaidOrderPanel(String sessionId){
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
