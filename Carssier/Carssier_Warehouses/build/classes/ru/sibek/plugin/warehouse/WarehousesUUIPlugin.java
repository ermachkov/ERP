/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.warehouse;

import java.io.File;
import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.WorkPanelPlugin;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com (C)
 * Copyright by Pechenko Anton, created 16.12.2010
 */
public class WarehousesUUIPlugin implements Plugin, WorkPanelPlugin {

    private String session;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    private WarehousesPanel warehousesPanel;

    public WarehousesUUIPlugin() {
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String getWorkPanelName() {
        return "Склады";
    }

    @Override
    public String getSelectorGroupName() {
        return "Склад";
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_warehouse.png";
    }

    @Override
    public String getWorkPanelClassName() {
        return WarehousesPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (warehousesPanel == null) {
            warehousesPanel = new WarehousesPanel(session);
        }
        return warehousesPanel;
    }

    @Override
    public int getSelectorGroupPosition() {
        return 200;
    }

    @Override
    public int getSelectorLabelPosition() {
        return 100;
    }

    @Override
    public String getGroupDescription() {
        return "Складские операции";
    }

    @Override
    public String getPluginName() {
        return "Склады";
    }

    @Override
    public String getPluginDescription() {
        return "Создание, редактрование, удаление складов.";
    }
}
