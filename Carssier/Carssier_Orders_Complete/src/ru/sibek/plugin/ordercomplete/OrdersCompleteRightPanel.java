/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.ordercomplete;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.document.Order;
import org.ubo.json.JSONObject;
import org.ubo.utils.Result;
import org.uui.component.RightPanel;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class OrdersCompleteRightPanel extends RightPanel {

    private OrdersCompleteOrderPanel orderPanel;
    private CarssierCore core = CarssierCore.getInstance();

    public OrdersCompleteRightPanel(String sessionId) {
        super(sessionId);
        orderPanel = new OrdersCompleteOrderPanel(sessionId);
        orderPanel.setHelpDropMessage("Для просмотра свойств выполненного заказа сделайте двойной щелчок на нужной строчке таблицы");
        orderPanel.getFooterOrderPanel().clearComponents();
    }

    @Override
    public String getName() {
        return "Свойства";
    }

    @Override
    public String getModel() {
        return orderPanel.getModel();
    }

    @Override
    public String getIdentificator() {
        return OrdersCompleteRightPanel.class.getName();
    }

    @Override
    public void fireEvent(String json) {
        if (json == null) {
            return;
        }
        if (json.equals("")) {
            return;
        }
        if (json.equals("{}")) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            
            setSession(jsonObject.getString("session"));
            orderPanel.setSession(jsonObject.getString("session"));

            if (jsonObject.getString("eventType").equals("push")) {
                Result r = core.getOrder(jsonObject.getLong("dbid"));
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());

                } else {
                    orderPanel.setOrder((Order) r.getObject());
                    JSMediator.setRightPanel(getSession(), getModel());
                }
            }

            if (jsonObject.getString("eventType").equals("click")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    JSMediator.setRightPanel(getSession(), getModel());
                }
            }
            

        } catch (Exception ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }
}
