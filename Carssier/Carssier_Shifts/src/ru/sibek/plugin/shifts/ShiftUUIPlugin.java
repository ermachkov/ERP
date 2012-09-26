/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.shifts;

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
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class ShiftUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;
    private ShiftPanel shiftsPanel = null;
    private List<RightPanel> list = null;

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    @Override
    public String getPluginName() {
        return "Смена";
    }

    @Override
    public String getPluginDescription() {
        return "Просмотр и закрытие смены";
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
        return "icons" + File.separator + "selector_report.png";
    }

    @Override
    public int getSelectorLabelPosition() {
        return 200;
    }

    @Override
    public String getWorkPanelClassName() {
        return ShiftPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (shiftsPanel == null) {
            shiftsPanel = new ShiftPanel(session);
        }

        return shiftsPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "Смена";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getGroupDescription() {
        return "Работа с отчетами, сменами";
    }

    @Override
    public List<RightPanel> getRightPanels() {
        if (list == null) {
            list = new ArrayList<>();
            list.add(new ShiftRightPanel(session));
        }

        return list;
    }
}
