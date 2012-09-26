/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.webservice;

import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.WorkPanelPlugin;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class WebServiceUUIPlugin implements Plugin, WorkPanelPlugin {

    private String session;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    private WebServicePanel webServicePanel = null;

    @Override
    public String getPluginName() {
        return "WWW служба";
    }

    @Override
    public String getPluginDescription() {
        return "WWW служба для обмена данными сервер <-> монтажка";
    }

    @Override
    public String getSelectorGroupName() {
        return "WWW";
    }

    @Override
    public int getSelectorGroupPosition() {
        return 1100;
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons/selector_users.png";
    }

    @Override
    public int getSelectorLabelPosition() {
        return 20;
    }

    @Override
    public String getWorkPanelClassName() {
        return WebServicePanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (webServicePanel == null) {
            webServicePanel = new WebServicePanel(session);
        }

        return webServicePanel;
    }

    @Override
    public String getWorkPanelName() {
        return "WWW служба";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getGroupDescription() {
        return "Работа с web-службами и сервисами";
    }
}
