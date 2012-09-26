/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.orderinwork;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.document.Order;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.partner.Account;
import org.ubo.partner.Address;
import org.ubo.partner.Agent;
import org.ubo.partner.Contacts;
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
public class OrdersInWorkRightPanel extends RightPanel {

    private OrdersInWorkOrderPanel orderPanel;
    private Button btnToDone, btnEdit, btnSave;
    private CarssierCore core = CarssierCore.getInstance();
    private boolean isPanelEnabled = true;

    public OrdersInWorkRightPanel(String sessionId) {
        super(sessionId);
        orderPanel = new OrdersInWorkOrderPanel(sessionId);
        orderPanel.setHelpDropMessage("Для редактирования заказа в работе сделайте двойной щелчок на нужной строчке таблицы");

        orderPanel.getFooterOrderPanel().clearComponents();
        btnToDone = new Button(getSession(), "В выполненные");
        btnToDone.addUIEventListener(getButtonDoneEventListener());
        orderPanel.getFooterOrderPanel().addComponent(btnToDone);

        btnEdit = new Button(getSession(), "Редактировать");
        btnEdit.setStyle("font-size:80%;");
        btnEdit.addUIEventListener(getEditButtonListener());
        orderPanel.getFooterOrderPanel().addComponent(btnEdit);

        btnSave = new Button(getSession(), "Сохранить");
        btnSave.setStyle("font-size:80%;");
        btnSave.addUIEventListener(getSaveButtonListener());
    }

    private UIEventListener getSaveButtonListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    JSMediator.showLockPanel(getSession());
                    
                    boolean isAddNewCustomer = false;
                    Agent selectedAgent = null;
                    if (orderPanel.getHeaderOrderPanel().getSelectedCustomerId() != -1) {
                        Result r = core.getAgent(orderPanel.getHeaderOrderPanel().getSelectedCustomerId());
                        if (r.isError()) {
                            JSMediator.alert(getSession(), r.getReason());
                            return;
                        }
                        Agent a = (Agent) r.getObject();
                        if (!a.getShortName().equals(orderPanel.getHeaderOrderPanel().getCustomerName())) {
                            isAddNewCustomer = true;
                        } else {
                            selectedAgent = a;
                        }
                    } else {
                        selectedAgent = (Agent) core.getAgent(12).getObject();
                    }

                    if (!isAddNewCustomer) {
                        orderPanel.getOrder().setCustomer(selectedAgent);

                    } else {
                        Agent a = new Agent();
                        a.setShortName(orderPanel.getHeaderOrderPanel().getCustomerName());
                        a.setFullName(orderPanel.getHeaderOrderPanel().getCustomerName());

                        Address address = new Address();
                        a.setAddress("default", address);

                        Contacts contact = new Contacts();
                        a.setContacts("default", contact);

                        Account account = new Account();
                        a.setAccount("default", account);

                        Result r = core.addAgent(a);
                        if (!r.isError()) {
                            orderPanel.getOrder().setCustomer((Agent) r.getObject());
                        } else {
                            JSMediator.alert(getSession(), r.getReason());
                            return;
                        }
                    }

                    CarssierCore.getInstance().modifyOrder(orderPanel.getOrder());
                    orderPanel.getFooterOrderPanel().clearComponents();
                    orderPanel.getFooterOrderPanel().addComponent(btnToDone);
                    orderPanel.getFooterOrderPanel().addComponent(btnEdit);
                    orderPanel.setEditable(false);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("eventType", "switchToView");
                    jsonObject.put("session", getSession());
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                            OrdersInWork.class.getName(), jsonObject.toString());
                    JSMediator.setRightPanel(getSession(), getModel());
                    
                    JSMediator.hideLockPanel(getSession());

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                    JSMediator.hideLockPanel(getSession());
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
                    jsonObject.put("session", getSession());
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                            OrdersInWork.class.getName(), jsonObject.toString());

                    orderPanel.setEditable(true);
                    JSMediator.setRightPanel(getSession(), getModel());

                    String s = orderPanel.getCustomrsList();
                    if (!s.equals("")) {
                        JSMediator.setCustomersSelector(getSession(), s);
                    }

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }

            }
        };

        return listener;
    }

    private UIEventListener getButtonDoneEventListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    JSMediator.showLockPanel(getSession());
                    
                    Result r = CarssierCore.getInstance().closeOrder(orderPanel.getOrder());
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());

                    } else {
                        JSMediator.hideRightPanel(getSession());
                        WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                                OrdersInWork.class.getName(),
                                "{eventType:showWorkPanel, session:\"" + getSession() + "\"}");
                    }
                    
                    JSMediator.hideLockPanel(getSession());

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                    JSMediator.hideLockPanel(getSession());
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    @Override
    public String getName() {
        return "Свойства";
    }

    @Override
    public String getModel() {
        if (!core.isRadioButtonRuleAllow(getSession(), "canEditOrderInWork", "editOrderInWorkAllow")) {
            orderPanel.getFooterOrderPanel().removeComponent(btnEdit);
        }
        return orderPanel.getModel();
    }

    @Override
    public String getIdentificator() {
        return getClass().getName();
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
                if (jsonObject.has("dbid")) {
                    if (isPanelEnabled) {
                        Result r = core.getOrder(jsonObject.getLong("dbid"));
                        if (r.isError()) {
                            JSMediator.alert(getSession(), r.getReason());

                        } else {
                            orderPanel.setOrder((Order) r.getObject());
                            JSMediator.setRightPanel(getSession(), getModel());
                        }
                    }
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.has("isPanelEnabled")) {
                    isPanelEnabled = jsonObject.getBoolean("isPanelEnabled");
                    if (!isPanelEnabled) {
                        JSMediator.hideRightPanel(getSession());
                    }
                }
            }

            if (jsonObject.getString("eventType").equals("click")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    if (isPanelEnabled) {
                        JSMediator.setRightPanel(getSession(), getModel());
                    }
                }
            }

        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }
}
