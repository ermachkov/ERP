/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.rules;

import java.io.File;
import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.WorkPanelPlugin;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class GlobalRulesUUIPlugin implements Plugin, WorkPanelPlugin {

    private String session;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    
    private GlobalRulesPanel globalRulesPanel;

    public GlobalRulesUUIPlugin() {
        //
    }

    @Override
    public String getPluginName() {
        return "Общие бизнес-правила";
    }

    @Override
    public String getPluginDescription() {
        return "Установка общих бизнес-правил для всех пользователей";
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
        return "icons" + File.separator + "selector_global_rule.png";
    }

    @Override
    public int getSelectorLabelPosition() {
        return 20;
    }

    @Override
    public String getWorkPanelClassName() {
        return GlobalRulesPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (globalRulesPanel == null) {
            globalRulesPanel = new GlobalRulesPanel(session);
        }
        return globalRulesPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "Правила";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getGroupDescription() {
        return "";
    }
}
