/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.accountbook.trialbalance;

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
public class TrialBalanceUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;
    private ArrayList<RightPanel> rightPanels;
    private WorkPanel workPanel;

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    @Override
    public String getPluginName() {
        return "Оборотная ведомость";
    }

    @Override
    public String getPluginDescription() {
        return "Работа с отчетами";
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
        return 100;
    }

    @Override
    public String getWorkPanelClassName() {
        return TrialBalancePanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (workPanel == null) {
            workPanel = new TrialBalancePanel(session);
        }

        return workPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "<div style='font-size:60%;'>Оборотная </div>"
                + "<div style='font-size:60%;'>ведомость</div>";
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
        if (rightPanels == null) {
            rightPanels = new ArrayList<>();
            rightPanels.add(new TrialBalanceRightPanel(session));
        }

        return rightPanels;
    }
}
