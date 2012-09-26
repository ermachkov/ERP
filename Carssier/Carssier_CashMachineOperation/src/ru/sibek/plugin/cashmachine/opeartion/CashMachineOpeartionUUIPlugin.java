/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.cashmachine.opeartion;

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
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class CashMachineOpeartionUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;
    private CashMachineOperationPanel cashMachineOperationPanel;

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public CashMachineOpeartionUUIPlugin() {
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getSelectorGroupName() {
        return "Расчеты";
    }

    @Override
    public String getWorkPanelName() {
        return "Касса";
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_cashmachine.png";
    }

    @Override
    public String getWorkPanelClassName() {
        return CashMachineOperationPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (cashMachineOperationPanel == null) {
            cashMachineOperationPanel = new CashMachineOperationPanel(session);
        }

        return cashMachineOperationPanel;
    }

    @Override
    public int getSelectorGroupPosition() {
        return 120;
    }

    @Override
    public int getSelectorLabelPosition() {
        return 20;
    }

    @Override
    public String getGroupDescription() {
        return "Настройки, устройства, обслуживание";
    }

    @Override
    public List<RightPanel> getRightPanels() {
        return new ArrayList<>();
    }

    @Override
    public String getPluginName() {
        return "Обслуживание кассовых аппаратов";
    }

    @Override
    public String getPluginDescription() {
        return "Настройка кассовых аппаратов и работа с их служебными функциями.";
    }
}
