/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.salary.report;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.uui.component.RightPanel;
import ru.sibek.business.ui.JSMediator;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class SalaryReportRightPanel extends RightPanel {

    private SalaryDistributionPanel salaryDistributionPanel;
    private SalaryInfoPanel salaryInfoPanel;

    public SalaryReportRightPanel(String sessionId) {
        super(sessionId);
        salaryDistributionPanel = new SalaryDistributionPanel(sessionId);
        salaryInfoPanel = new SalaryInfoPanel(sessionId);
    }

    @Override
    public String getName() {
        return "Аналитика";
    }

    @Override
    public void fireEvent(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);

            setSession(jsonObject.getString("session"));
            salaryDistributionPanel.setSession(jsonObject.getString("session"));
            salaryInfoPanel.setSession(jsonObject.getString("session"));

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showSalaryDistributionPanel")) {
                    JSMediator.showLockPanel(getSession());
                    salaryDistributionPanel.setReportShift(jsonObject.getLong("reportShiftId"));
                    JSMediator.setRightPanel(getSession(), salaryDistributionPanel.getModel());
                    JSMediator.hideLockPanel(getSession());
                }

                if (jsonObject.getString("action").equals("showSalaryInfoPanel")) {
                    JSMediator.showLockPanel(getSession());
                    salaryInfoPanel.setReportShift(jsonObject.getLong("reportShiftId"));
                    JSMediator.setRightPanel(getSession(), salaryInfoPanel.getModel());
                    JSMediator.hideLockPanel(getSession());
                }

                if (jsonObject.getString("action").equals("updateSalaryDistributionPanel")) {
                    JSMediator.showLockPanel(getSession());
                    JSMediator.setRightPanel(getSession(), salaryDistributionPanel.getModel());
                    JSMediator.hideLockPanel(getSession());
                }
            }
        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, json, e);
        }

    }

    @Override
    public String getIdentificator() {
        return SalaryReportRightPanel.class.getName();
    }
}
