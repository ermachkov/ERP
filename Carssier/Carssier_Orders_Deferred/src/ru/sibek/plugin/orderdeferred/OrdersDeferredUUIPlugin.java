/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.orderdeferred;

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
public class OrdersDeferredUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;
    private OrdersDeferred ordersDeferred;
    private ArrayList<RightPanel> list;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    

    public OrdersDeferredUUIPlugin() {
    }

    @Override
    public String getPluginName() {
        return "Отложенные заказы";
    }

    @Override
    public String getPluginDescription() {
        return "Просмотр удаление и редактирование отложенных заказов.";
    }

    @Override
    public String getSelectorGroupName() {
        return "Заказы";
    }

    @Override
    public int getSelectorGroupPosition() {
        return 100;
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_orders_delayed.png";
    }

    @Override
    public int getSelectorLabelPosition() {
        return 50;
    }

    @Override
    public String getWorkPanelClassName() {
        return OrdersDeferred.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (ordersDeferred == null) {
            ordersDeferred = new OrdersDeferred(session, CarssierDataBase.getDataBase());
        }

        return ordersDeferred;
    }

    @Override
    public String getWorkPanelName() {
        return "Отложенные";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getGroupDescription() {
        return "";
    }

    @Override
    public List<RightPanel> getRightPanels() {
        if (list == null) {
            list = new ArrayList<>();
            list.add(new OrdersDeferredRightPanel(session));
        }

        return list;
    }
}
