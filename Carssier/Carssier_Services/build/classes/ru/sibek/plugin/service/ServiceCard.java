/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.service.Service;
import org.ubo.tree.TreeFolder;
import org.ubo.tree.TreeLeaf;
import org.uui.component.RightPanel;
import org.uui.db.DataBase;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author anton
 */
public class ServiceCard extends RightPanel {

    private ServicePropertiesPanel servicePropertiesPanel;
    private DataBase dataBase;

    public ServiceCard(String sessionId) {
        super(sessionId);
        dataBase = CarssierDataBase.getDataBase();
        servicePropertiesPanel = new ServicePropertiesPanel(sessionId, dataBase);
    }

    @Override
    public String getName() {
        return "Свойства";
    }

    @Override
    public String getModel() {
        return "<div style='width:100%;height:100%;overflow:hidden;' "
                + "identificator='" + getIdentificator() + "' class='rightPanel'>"
                + servicePropertiesPanel.getModel()
                + "</div>";
    }

    private void setPanel(TreeLeaf treeLeaf) {
        Service service = (Service) dataBase.getObject(
                treeLeaf.getContainer().getClassName(),
                treeLeaf.getContainer().getId());
        servicePropertiesPanel.setService(service);
    }

    @Override
    public String getIdentificator() {
        return ServiceCard.class.getName();
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
            servicePropertiesPanel.setSession(jsonObject.getString("session"));

            if (jsonObject.getString("eventType").equals("drop")) {
                dropHandler(jsonObject.getJSONArray("data"));
                JSMediator.setRightPanel(getSession(), getModel());
            }

            if (jsonObject.getString("eventType").equals("push")
                    && jsonObject.getString("action").equals("showRightPanel")) {
                if (!jsonObject.isNull("data")) {
                    dropHandler(jsonObject.getJSONArray("data"));
                }
                JSMediator.setRightPanel(getSession(), getModel());
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("updateRightPanel")) {
                    servicePropertiesPanel.refresh();
                    JSMediator.setRightPanel(getSession(), getModel());
                }
            }

            if (jsonObject.getString("eventType").equals("click")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    JSMediator.setRightPanel(getSession(), getModel());
                }
            }

        } catch (JSONException ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    private void dropHandler(JSONArray jsonArray) {
        try {
            ArrayList<TreeLeaf> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                if (jsonObject.getString("className").indexOf("TreeLeaf") != -1) {
                    TreeLeaf treeLeaf = (TreeLeaf) dataBase.getObject(
                            jsonObject.getString("className"),
                            jsonObject.getLong("dbid"));
                    list.add(treeLeaf);
                }

                if (jsonObject.getString("className").indexOf("TreeFolder") != -1) {
                    TreeFolder treeFolder = (TreeFolder) dataBase.getObject(
                            jsonObject.getString("className"),
                            jsonObject.getLong("dbid"));
                    list.addAll(treeFolder.getAllDescendTreeLeaves());
                }
            }

            if (!list.isEmpty()) {
                setPanel(list.get(0));
            }

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, Objects.toString(jsonArray), e);
        }

    }
}
