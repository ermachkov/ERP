/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.shifts;

import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.WorkPanelPlugin;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ShiftsListUUIPlugin implements Plugin, WorkPanelPlugin {

    private String session;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    private ShiftsListPanel shiftsListPanel;

    @Override
    public String getPluginName() {
        return "Смены";
    }

    @Override
    public String getPluginDescription() {
        return "Просмотр и закрых смен";
    }

    @Override
    public String getSelectorGroupName() {
        return "Отчеты";
    }

    @Override
    public int getSelectorGroupPosition() {
        return 1000;
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons/selector_report.png";
    }

    @Override
    public int getSelectorLabelPosition() {
        return 250;
    }

    @Override
    public String getWorkPanelClassName() {
        return ShiftsListPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (shiftsListPanel == null) {
            shiftsListPanel = new ShiftsListPanel(session);
        }

        return shiftsListPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "Смены";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getGroupDescription() {
        return "Работа с отчетами, сменами";
    }
}
