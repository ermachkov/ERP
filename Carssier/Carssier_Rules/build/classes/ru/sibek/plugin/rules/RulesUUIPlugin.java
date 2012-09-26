/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.rules;

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
public class RulesUUIPlugin implements Plugin, WorkPanelPlugin, RightPanelPlugin {

    private String session;
    private RulesPanel rulesPanel;
    private ArrayList<RightPanel> rightPanels;

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public RulesUUIPlugin() {
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
        return "icons/selector_users.png";
    }

    @Override
    public int getSelectorLabelPosition() {
        return 100;
    }

    @Override
    public String getWorkPanelClassName() {
        return RulesPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (rulesPanel == null) {
            rulesPanel = new RulesPanel(session);
        }
        return rulesPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "Модули";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getGroupDescription() {
        return "Бизнес-правила, права доступа, пользователи";
    }

    @Override
    public List<RightPanel> getRightPanels() {
        if (rightPanels == null) {
            rightPanels = new ArrayList<>();
            rightPanels.add(new RulesRightPanel(session));
        }
        return rightPanels;
    }

    @Override
    public String getPluginName() {
        return "Установка бизнес-правил для модулей";
    }

    @Override
    public String getPluginDescription() {
        return "Установка прав доступа пользователей к модулям и установка бизнес-правил для них";
    }
}
