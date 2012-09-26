/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.staff.employee;

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
public class EmployeeUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    private ArrayList<RightPanel> panels;
    private EmployeePanel employeePanel;

    public EmployeeUUIPlugin() {
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String getWorkPanelName() {
        return "Персоны";
    }

    @Override
    public String getSelectorGroupName() {
        return "Персонал";
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons/selector_person.png";
    }

    @Override
    public String getWorkPanelClassName() {
        return EmployeePanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (employeePanel == null) {
            employeePanel = new EmployeePanel(session);
        }

        return employeePanel;
    }

    @Override
    public int getSelectorGroupPosition() {
        return 300;
    }

    @Override
    public int getSelectorLabelPosition() {
        return 100;
    }

    @Override
    public List getRightPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new EmployeeCard(session));
        }

        return panels;
    }

    public String getRightPanelWorkPanelClassName() {
        return EmployeePanel.class.getName();
    }

    @Override
    public String getGroupDescription() {
        return "Персоны, специальности, рабочее время";
    }

    @Override
    public String getPluginName() {
        return "Персоны";
    }

    @Override
    public String getPluginDescription() {
        return "Создание, редактирование, удаление рабочих персон.";
    }
}
