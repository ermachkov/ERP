/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.uui.component.WorkPanel;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.webkit.WebKitComponent;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class GlobalRulesPanel extends WorkPanel implements WebKitComponent, HasWorkPanelToolbars {

    private ArrayList<RibbonButton> toolbarButtons;

    public GlobalRulesPanel(String sessionId) {
        super(sessionId);
        initWPToolbar();
    }

    private void initWPToolbar() {
        toolbarButtons = new ArrayList<>();
    }

    @Override
    public String getModel() {
        return "???";
    }

    @Override
    public String getIdentificator() {
        return GlobalRulesPanel.class.getName();
    }

    @Override
    public void fireEvent(String json) {
        if (json == null) {
            return;
        }
        if (json.equals("")) {
            return;
        }
        if (json.equals("{}")) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);

            setSession(jsonObject.getString("session"));

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Правила");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.hideNavigationPanel(getSession());

            }

        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }

    private String getSubOperationButtonModel() {
        String model = "<div>";
        for (org.uui.ribbon.RibbonButton rb : getWorkpanelToolbars()) {
            model += rb.getModel();
        }
        model += "</div>";
        return model;
    }

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return toolbarButtons;
    }
}
