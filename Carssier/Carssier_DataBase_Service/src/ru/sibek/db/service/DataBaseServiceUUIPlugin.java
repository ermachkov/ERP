/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.db.service;

import java.io.File;
import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.WorkPanelPlugin;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class DataBaseServiceUUIPlugin implements Plugin, WorkPanelPlugin {

    private String session;
    private DataBaseServicePanel rulesPanel;

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public DataBaseServiceUUIPlugin() {
    }

    @Override
    public String getSelectorGroupName() {
        return "Настройки";
    }

    @Override
    public int getSelectorGroupPosition() {
        return 1000;
    }

    @Override
    public String getSelectorGroupImagePath() {
        return "icons" + File.separator + "selector_users.png";
    }

    @Override
    public int getSelectorLabelPosition() {
        return 300;
    }

    @Override
    public String getWorkPanelClassName() {
        return DataBaseServicePanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (rulesPanel == null) {
            rulesPanel = new DataBaseServicePanel(session);
        }
        return rulesPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "База данных";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getGroupDescription() {
        return "Работа с базой данных";
    }

    @Override
    public String getPluginName() {
        return "Администрирование базы данных";
    }

    @Override
    public String getPluginDescription() {
        return "Администрирование базы данных, резервные копии";
    }
}
