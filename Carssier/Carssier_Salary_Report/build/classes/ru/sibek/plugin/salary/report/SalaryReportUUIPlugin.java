/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.salary.report;

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
public class SalaryReportUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin{
    
    private SalaryReportPanel salaryReportPanel = null;
    private ArrayList<RightPanel> rightPanels = null;
    private String session;

    @Override
    public String getPluginName() {
        return "Зарплата отчеты/распределение";
    }

    @Override
    public String getPluginDescription() {
        return "Зарплата отчеты/распределение";
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
        return 300;
    }

    @Override
    public String getWorkPanelClassName() {
        return SalaryReportPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if(salaryReportPanel == null){
            salaryReportPanel = new SalaryReportPanel(session);
        }
        
        return salaryReportPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "Зарплата";
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
        if(rightPanels == null){
            rightPanels = new ArrayList<>();
            rightPanels.add(new SalaryReportRightPanel(session));
        }
        
        return rightPanels;
    }

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    
}
