/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.orderinwork;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.uui.component.RightPanel;
import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.RightPanelPlugin;
import org.uui.plugin.WorkPanelPlugin;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com (C)
 * Copyright by Pechenko Anton, created 16.12.2010
 */
public class OrdersInWorkUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    private List<RightPanel> panels;
    private OrdersInWork ordersInWork;

    public OrdersInWorkUUIPlugin() {
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getSelectorGroupName() {
        return "Заказы";
    }

    @Override
    public String getWorkPanelName() {
        return "В работе";
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_orders_in_work.png";
    }

    @Override
    public String getWorkPanelClassName() {
        return OrdersInWork.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (ordersInWork == null) {
            ordersInWork = new OrdersInWork(session, CarssierDataBase.getDataBase());
        }

        return ordersInWork;
    }

    @Override
    public int getSelectorGroupPosition() {
        return 100;
    }

    @Override
    public int getSelectorLabelPosition() {
        return 30;
    }

    @Override
    public List<RightPanel> getRightPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            OrdersInWorkRightPanel ordersInWorkRightPanel = new OrdersInWorkRightPanel(session);
            panels.add(ordersInWorkRightPanel);
        }

        return panels;
    }

    public String getRightPanelWorkPanelClassName() {
        return OrdersInWorkRightPanel.class.getName();
    }

    @Override
    public String getGroupDescription() {
        return "Работа с заказами";
    }

    @Override
    public String getPluginName() {
        return "Список заказов в работе";
    }

    @Override
    public String getPluginDescription() {
        return "Просмотр заказов в работе";
    }
}
