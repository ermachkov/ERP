/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.webpriceeditor;

import org.uui.component.WorkPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.WorkPanelPlugin;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class WebPriceEditorUUIPlugin implements Plugin, WorkPanelPlugin {

    private String session;

    @Override
    public void setSession(String session) {
        this.session = session;
    }
    private WebPriceEditorPanel webPriceEditorPanel = null;

    @Override
    public String getPluginName() {
        return "WWW редактор прайс-листов";
    }

    @Override
    public String getPluginDescription() {
        return "WWW редактор прайс-листов для размещения на сайте";
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
        return WebPriceEditorPanel.class.getName();
    }

    @Override
    public WorkPanel getWorkPanel() {
        if (webPriceEditorPanel == null) {
            webPriceEditorPanel = new WebPriceEditorPanel(session);
        }

        return webPriceEditorPanel;
    }

    @Override
    public String getWorkPanelName() {
        return "WWW прайс-лист";
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
