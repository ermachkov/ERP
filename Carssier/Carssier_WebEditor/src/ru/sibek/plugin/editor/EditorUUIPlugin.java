/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.editor;

import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.WorkPanelPlugin;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class EditorUUIPlugin implements Plugin, WorkPanelPlugin {

    private String session;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    private EditorPanel editorPanel = null;

    @Override
    public String getPluginName() {
        return "WWW редактор текста";
    }

    @Override
    public String getPluginDescription() {
        return "WWW редактор текста для размещения на сайте";
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
        return 10;
    }

    @Override
    public String getWorkPanelClassName() {
        return EditorPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (editorPanel == null) {
            editorPanel = new EditorPanel(session);
        }

        return editorPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "WWW редактор";
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
