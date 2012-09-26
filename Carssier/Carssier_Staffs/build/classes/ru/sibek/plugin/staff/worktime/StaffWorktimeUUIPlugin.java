/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.staff.worktime;

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
public class StaffWorktimeUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    private ArrayList<RightPanel> panels;
    private StaffWorkTimePanel staffWorkTimePanel;

    public StaffWorktimeUUIPlugin() {
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getWorkPanelName() {
        return "Рабочее время";
    }

    @Override
    public String getSelectorGroupName() {
        return "Персонал";
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons/selector_worktime.png";
    }

    @Override
    public String getWorkPanelClassName() {
        return StaffWorkTimePanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (staffWorkTimePanel == null) {
            staffWorkTimePanel = new StaffWorkTimePanel(session);
        }

        return staffWorkTimePanel;
    }

    @Override
    public int getSelectorGroupPosition() {
        return 300;
    }

    @Override
    public int getSelectorLabelPosition() {
        return 300;
    }

    @Override
    public List getRightPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new StaffWorktimeRightPanel(session));
        }

        return panels;
    }

    public String getRightPanelWorkPanelClassName() {
        return StaffWorkTimePanel.class.getName();
    }

    @Override
    public String getGroupDescription() {
        return "";
    }

    @Override
    public String getPluginName() {
        return "Рабочее время";
    }

    @Override
    public String getPluginDescription() {
        return "Оформление прихода на работу работников.";
    }
}
