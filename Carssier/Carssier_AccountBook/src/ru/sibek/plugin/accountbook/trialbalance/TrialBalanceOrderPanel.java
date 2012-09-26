/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.accountbook.trialbalance;

import ru.sibek.plugin.neworder.GoodsBasketPanel;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class TrialBalanceOrderPanel extends GoodsBasketPanel {
    
    public TrialBalanceOrderPanel(String sessionId){
        super(sessionId);
        setEditable(false);
    }

    @Override
    public String getName() {
        return "TrialBookOrders";
    }
    
    @Override
    public void setSession(String session) {
        super.setSession(session);
        super.getGoodsOrderPanel().setSession(session);
    }
    
}
