/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.orderdeferred;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.document.Order;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.utils.Result;
import org.uui.component.Button;
import org.uui.component.RightPanel;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class OrdersDeferredRightPanel extends RightPanel {

    private OrdersDeferredView orderPanel;
    private Button btnEdit, btnSave;
    private CarssierCore core = CarssierCore.getInstance();

    public OrdersDeferredRightPanel(String sessionId) {
        super(sessionId);
        orderPanel = new OrdersDeferredView(sessionId);

        orderPanel.getFooterOrderPanel().clearComponents();
        btnEdit = new Button(getSession(), "Редактировать");
        btnEdit.setStyle("font-size:80%;");
        btnEdit.addUIEventListener(getEditButtonListener());
        orderPanel.getFooterOrderPanel().addComponent(btnEdit);

        btnSave = new Button(getSession(), "Сохранить");
        btnSave.addUIEventListener(getSaveButtonListener());
    }

    private UIEventListener getSaveButtonListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                try {
                    CarssierCore.getInstance().modifyOrder(orderPanel.getOrder());
                    orderPanel.getFooterOrderPanel().clearComponents();
                    orderPanel.getFooterOrderPanel().addComponent(btnEdit);
                    orderPanel.setEditable(false);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("eventType", "switchToView");
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            OrdersDeferred.class.getName(), jsonObject.toString());
                            JSMediator.setRightPanel(getSession(), getModel());

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    private UIEventListener getEditButtonListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                try {
                    orderPanel.getFooterOrderPanel().clearComponents();
                    orderPanel.getFooterOrderPanel().addComponent(btnSave);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("eventType", "switchToEdit");
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            OrdersDeferred.class.getName(), jsonObject.toString());

                    orderPanel.setEditable(true);
                    JSMediator.setRightPanel(getSession(), getModel());

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }

            }
        };

        return listener;
    }

    @Override
    public String getIdentificator() {
        return OrdersDeferredRightPanel.class.getName();
    }

    @Override
    public String getName() {
        return "Заказ";
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
            
            if (jsonObject.getString("eventType").equals("click")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                            JSMediator.setRightPanel(getSession(), getModel());

                    String s = orderPanel.getDescriptions();
                    if (!s.equals("")) {
                        JSMediator.setOrderDescription(getSession(), s);
                    }
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                Result r = core.getOrder(jsonObject.getLong("dbid"));
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());

                } else {
                    orderPanel.setOrder((Order) r.getObject());
                            JSMediator.setRightPanel(getSession(), getModel());
                }
            }

        } catch (JSONException ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    @Override
    public String getModel() {
        return orderPanel.getModel();
    }
}
