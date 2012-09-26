/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.uui.component.RightPanel;
import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.RightPanelPlugin;
import org.uui.plugin.WorkPanelPlugin;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com (C)
 * Copyright by Pechenko Anton, created 16.03.2011
 */
public class ServicesUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;
    private ServicesPanel servicesPanel;
    private ArrayList<RightPanel> panels;

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    @Override
    public String getSelectorGroupName() {
        return "Справочники";
    }

    @Override
    public int getSelectorGroupPosition() {
        return 950;
    }

    @Override
    public int getSelectorLabelPosition() {
        return 200;
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_service.png";
    }

    @Override
    public String getWorkPanelClassName() {
        return ServicesPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (servicesPanel == null) {
            servicesPanel = new ServicesPanel(session);
        }
        return servicesPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "Услуги";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public List<RightPanel> getRightPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new ServiceCard(session));
        }

        return panels;
    }

    @Override
    public String getGroupDescription() {
        return "";
    }

    @Override
    public String getPluginName() {
        return "Услуги";
    }

    @Override
    public String getPluginDescription() {
        return "Создание, редактирование, удаление услуг. Распределение услуг по группам.";
    }
}
