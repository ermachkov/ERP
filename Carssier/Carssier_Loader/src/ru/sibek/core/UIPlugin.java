/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core;

import java.util.List;
import java.util.Objects;
import org.ubo.json.JSONObject;
import org.uui.component.RightPanel;
import org.uui.plugin.Plugin;
import org.uui.plugin.RightPanelPlugin;
import org.uui.plugin.WorkPanelPlugin;
import org.uui.webkit.WebKitComponent;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class UIPlugin {

    private Plugin plugin;
    private Object workPanel;

    public UIPlugin(Plugin wpp) {
        this.plugin = Objects.requireNonNull(wpp);
    }

    public Object getRightPanels() {
        return ((RightPanelPlugin) plugin).getRightPanels();
    }

    public Object getWorkPanel() {
        return workPanel;
    }

    public String getIdentificator() {
        String identificator = "undefined";

        if (workPanel instanceof WebKitComponent) {
            identificator = ((WebKitComponent) workPanel).getIdentificator();
        }

        return identificator;
    }

    public String getRightPanelsIdentificators() {
        String str = "{";
        List<RightPanel> rppList = ((RightPanelPlugin) plugin).getRightPanels();
        int i = 0;
        for (RightPanel rp : rppList) {
            String comma = ",";
            if (i == 0) {
                comma = "";
            }
            i++;

            str += comma + rp.getName() + ":" + rp.getIdentificator();
        }
        return str + "}";
    }

    public void setWorkPanel(Object workPanel) {
        this.workPanel = workPanel;
    }

    public String getImagePath() {
        return ((WorkPanelPlugin) plugin).getSelectorGroupImagePath();
    }

    public String getClassName() {
        return ((WorkPanelPlugin) plugin).getWorkPanelClassName();
    }

    public String getOperationButton() {
        return ((WorkPanelPlugin) plugin).getWorkPanelName();
    }

    public String getTopButtonName() {
        //TopButton topButton = new TopButton(((WorkPanelPlugin) plugin).getSelectorGroupName());
        //return topButton;
        return ((WorkPanelPlugin) plugin).getSelectorGroupName();
    }

    public WorkPanelPlugin getWorkPanelPlugin() {
        return (WorkPanelPlugin) plugin;
    }

    @Override
    public String toString() {
        return "RibbonButton{" + "wpp=" + plugin + '}';
    }
}
