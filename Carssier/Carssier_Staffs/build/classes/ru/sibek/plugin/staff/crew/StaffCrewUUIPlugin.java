/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.staff.crew;

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
 * Copyright by Pechenko Anton, created 16.12.2010
 */
public class StaffCrewUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    private StaffCrewPanel staffCrewPanel;
    private ArrayList<RightPanel> panels;

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getWorkPanelName() {
        return "Бригады";
    }

    @Override
    public String getSelectorGroupName() {
        return "Персонал";
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_crew.png";
    }

    public boolean isHandlesPermissions() {
        return false;
    }

    public boolean isDeclaresPermissions() {
        return false;
    }

    public boolean isNeedPermissionToLoad() {
        return true;
    }

    @Override
    public String getWorkPanelClassName() {
        return StaffCrewPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (staffCrewPanel == null) {
            staffCrewPanel = new StaffCrewPanel(session);
        }

        return staffCrewPanel;
    }

    @Override
    public int getSelectorGroupPosition() {
        return 300;
    }

    @Override
    public int getSelectorLabelPosition() {
        return 200;
    }

    @Override
    public String getGroupDescription() {
        return "";
    }

    @Override
    public List<RightPanel> getRightPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new CrewInfoPanel(session));
        }
        return panels;
    }

    @Override
    public String getPluginName() {
        return "Бригады";
    }

    @Override
    public String getPluginDescription() {
        return "Создание, редактирование, удаление бригад";
    }
}
