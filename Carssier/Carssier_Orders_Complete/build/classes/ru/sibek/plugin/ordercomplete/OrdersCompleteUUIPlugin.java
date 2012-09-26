/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.ordercomplete;

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
public class OrdersCompleteUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;
    private OrdersComplete ordersComplete;
    private ArrayList<RightPanel> panels;

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public OrdersCompleteUUIPlugin() {
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String getSelectorGroupName() {
        return "Заказы";
    }

    @Override
    public String getWorkPanelName() {
        return "Выполненные";
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_orders_completed.png";
    }

    @Override
    public String getWorkPanelClassName() {
        return OrdersComplete.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (ordersComplete == null) {
            ordersComplete = new OrdersComplete(session, CarssierDataBase.getDataBase());
        }

        return ordersComplete;
    }

    @Override
    public int getSelectorGroupPosition() {
        return 100;
    }

    @Override
    public int getSelectorLabelPosition() {
        return 40;
    }

    @Override
    public List<RightPanel> getRightPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new OrdersCompleteRightPanel(session));
        }
        return panels;
    }

    public String getRightPanelWorkPanelClassName() {
        return OrdersCompleteRightPanel.class.getName();
    }

    @Override
    public String getGroupDescription() {
        return "Работа с заказами";
    }

    @Override
    public String getPluginName() {
        return "Список выполненых заказов";
    }

    @Override
    public String getPluginDescription() {
        return "Просмотр выполненых заказов";
    }
}
