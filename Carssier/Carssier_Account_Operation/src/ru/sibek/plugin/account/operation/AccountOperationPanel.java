package ru.sibek.plugin.account.operation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.uui.component.ComboBox;
import org.uui.component.Component;
import org.uui.component.WorkPanel;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

public class AccountOperationPanel extends WorkPanel {

    private ComboBox cboPanels;
    private Component selectedPanel;
    private Map<String, Component> panels = new HashMap<>();

    public AccountOperationPanel() {
        Path p = Paths.get(System.getProperty("user.home"), new String[]{".saas", "app", "panels"});
        for (File f : p.toFile().listFiles()) {
            if (f.isDirectory()) {
                continue;
            }
            if (!f.toString().endsWith(".jar")) {
                continue;
            }
            try {
                JarFile jf = new JarFile(f);
                Enumeration jes = jf.entries();
                while (jes.hasMoreElements()) {
                    String je = ((JarEntry) jes.nextElement()).getName();
                    if ((je.endsWith(".class")) && (je.indexOf("$") == -1)) {
                        String classname = je.replaceAll("/", ".").substring(0, je.lastIndexOf(".class"));
                        Class cls = Class.forName(classname);
                        Component componet = (Component) cls.newInstance();
                        panels.put(componet.getComponentName(), componet);
                    }
                }
            } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                Logger.getGlobal().log(Level.SEVERE, null, e);
            }

        }

        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(panels.keySet());
        Collections.sort(keys);

        LinkedHashMap map = new LinkedHashMap();
        map.put("Выберите операцию", "");
        for (String key : keys) {
            map.put(key, key);
        }
        this.cboPanels = new ComboBox(map);
        this.cboPanels.addUIEventListener(getPanelsListener());
    }

    private UIEventListener getPanelsListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                try {
                    if (!cboPanels.getSelectedValue().equals("")) {
                        selectedPanel = panels.get(cboPanels.getSelectedValue());
                    }

                    WebKitFrame.getInstance().browserExecutor(
                            "getUICore().setWorkPanel('" 
                            + WebKitUtil.prepareToJS(AccountOperationPanel.this.getModel()) 
                            + "');");
                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
                }
            }
        };
        return listener;
    }

    @Override
    public String getModel() {
        String _panel = "";
        if (this.selectedPanel != null) {
            _panel = this.selectedPanel.getModel();
        }

        String model = "<table width='100%' height='100%' border='1'><tr height='30'><td valign='middle' style='font-size:80%;'><b>Операция:</b>" + this.cboPanels.getModel() + "</td>" + "</tr>" + "<tr>" + "<td valign='top'>" + _panel + "</td>" + "</tr>" + "</table>";

        return model;
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
            if (jsonObject.getString("eventType").equals("click")) {
                WebKitFrame.getInstance().browserExecutor("getUICore().setWorkPanel('" + WebKitUtil.prepareToJS(getModel()) + "');");

                JSMediator.hideNavigationPanel(getSession());
            }

            if ((jsonObject.getString("eventType").equals("push"))
                    && (jsonObject.getString("action").equals("refreshSelectedPanel"))) {
                WebKitFrame.getInstance().browserExecutor("getUICore().setWorkPanel('" + WebKitUtil.prepareToJS(getModel()) + "');");
            }

        } catch (JSONException ex) {
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    @Override
    public String getIdentificator() {
        return AccountOperationPanel.class.getName();
    }
}

/* Location:           /home/developer/bin/decompilers/jd-gui-0.3.3.linux.i686/Carssier/Carssier_Account_Operation.jar
 * Qualified Name:     ru.sibek.plugin.account.operation.AccountOperationPanel
 * JD-Core Version:    0.6.0
 */