/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.agents;

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
 * Copyright by Pechenko Anton, created 16.12.2010
 */
public class AgentsUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;
    private ArrayList<RightPanel> panels;
    private AgentsPanel agentsPanel;

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public AgentsUUIPlugin() {
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String getWorkPanelName() {
        return "Партнеры";
    }

    @Override
    public String getSelectorGroupName() {
        return "Справочники";
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons/selector_agents.png";
    }

    @Override
    public String getWorkPanelClassName() {
        return AgentsPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (agentsPanel == null) {
            agentsPanel = new AgentsPanel(session);
        }
        return agentsPanel;
    }

    @Override
    public int getSelectorGroupPosition() {
        return 950;
    }

    @Override
    public int getSelectorLabelPosition() {
        return 300;
    }

    @Override
    public List<RightPanel> getRightPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new AgentsPropertiesPanel(session));
        }

        return panels;
    }

    public String getRightPanelWorkPanelClassName() {
        return AgentsPanel.class.getName();
    }

    @Override
    public String getGroupDescription() {
        return "";
    }

    @Override
    public String getPluginName() {
        return "Партнеры, агенты, контрагенты";
    }

    @Override
    public String getPluginDescription() {
        return "Создание, редактирование, удаление партнеров.";
    }
}
