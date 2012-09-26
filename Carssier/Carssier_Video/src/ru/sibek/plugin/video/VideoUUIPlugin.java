/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.video;

import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.WorkPanelPlugin;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class VideoUUIPlugin implements Plugin, WorkPanelPlugin {

    private String session;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    private VideoPanel videoPanel;

    @Override
    public String getPluginName() {
        return "Видео";
    }

    @Override
    public String getPluginDescription() {
        return "Видео наблюдение";
    }

    @Override
    public String getSelectorGroupName() {
        return "Видео";
    }

    @Override
    public int getSelectorGroupPosition() {
        return 1200;
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
        return VideoPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (videoPanel == null) {
            videoPanel = new VideoPanel(session);
        }

        return videoPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "Видео";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String getGroupDescription() {
        return "Модуль видеонаблюдения";
    }
}
