/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.rules.Rule;
import org.ubo.rules.Rules;
import org.ubo.utils.Result;
import org.uui.component.Button;
import org.uui.component.ComboBox;
import org.uui.component.Component;
import org.uui.component.PasswordTextField;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.plugin.Plugin;
import org.uui.plugin.RightPanelPlugin;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.users.User;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class LoginHandler extends Component {

    private Button btnOk, btnCancel;
    private ComboBox cboLogin;
    private PasswordTextField txtPassword;
    private Map<Long, ArrayList<Plugin>> userPlugins = new HashMap<>();
    private Map<Long, ArrayList<UIPlugin>> ribbonButtonsMap = new HashMap<>();
    private int pluginsCount, pluginsCounter;

    public LoginHandler(String sessionId) {
        super(sessionId);
        btnOk = new Button(getSession(), "Ok");
        btnOk.setCssClass("btnLoginOk");

        btnOk.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                doLogin();
            }
        });

        btnCancel = new Button(getSession(), "Cancel");
        btnCancel.setCssClass("btnLoginCancel");
        btnCancel.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                doCancel();
            }
        });

        cboLogin = new ComboBox(getSession());
        cboLogin.setStyle("width:99%;");
        
        txtPassword = new PasswordTextField(getSession());
        txtPassword.setStyle("width:99%;");
    }

    private void init() {
        ArrayList<User> users = CarssierDataBase.getDataBase().getAllObjectsList(User.class.getName());
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getLogin().compareToIgnoreCase(o2.getLogin());
            }
        });

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (User user : users) {
            map.put(user.getLogin() + "(" + user.getName() + ")", "" + user.getId());
        }
        cboLogin.setItems(map);
    }

    private void doLogin() {
        try {
            long idUser = Long.parseLong(cboLogin.getSelectedValue());
            ArrayList<User> users = CarssierDataBase.getDataBase().getAllObjectsList(User.class.getName());
            boolean isLogin = false;
            for (User user : users) {
                if (user.getId() == idUser && user.getPassword().equals(txtPassword.getPassword())) {
                    isLogin = true;
                }
            }

            if (!isLogin) {
                JSMediator.alert(getSession(), "Неправильный пароль");
                JSMediator.showLoginPanel(getSession(), getModel());

            } else {
                loadModules(idUser);
                JSMediator.setIndicator(getSession(), new UIIndicator(getSession()).getModel());
            }

        } catch (NumberFormatException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private void doCancel() {
        System.out.println("TODO Cancel");
        System.exit(0);
    }

    private String getModuleLoaderScreen() {
        String model = ""
                + "<div id='dataBaseHandlerPanel'>"
                + "<table width='100%' height='100%'>"
                + "<tr>"
                + "<td valign='middle' align='center'>"
                + "<img src='img/logo/logo.jpg' />"
                + "<div id='dbEventDescrition'></div>"
                + "<div id='progressBarDBContainer' align='left'>"
                + "<div id='progressBarDB'></div>"
                + "</div>"
                + "<div id='dbEventFullDescrition'></div>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</div>";

        return model;
    }

    private void loadModules(long userId) {
        if (!userPlugins.containsKey(userId)) {

            pluginsCounter = 0;
            JSMediator.showSplashPanel(getSession(), getModuleLoaderScreen());
            JSMediator.refreshElement(getSession(), "dbEventFullDescrition", "Поиск модулей");
            JSMediator.setSplashProgressBar(getSession(), 0);

            PluginLoader pluginLoader = new PluginLoader();
            pluginLoader.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (evt.getJSONObject().getString("eventName").equals("findPlugins")) {
                            JSMediator.refreshElement(
                                    LoginHandler.this.getSession(),
                                    "dbEventDescrition",
                                    evt.getJSONObject().getString("data"));
                        }

                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, null, e);
                    }
                }
            });

            pluginLoader.findPlugins();

            pluginLoader.loadPlugins(getSession(), pluginLoader.getFoundPlugins());

            userPlugins.put(userId, pluginLoader.getLoadedPlugins());

            JSMediator.refreshElement(getSession(), 
                                    "dbEventFullDescrition", 
                                    "Включение модулей");
            pluginsCount = userPlugins.get(userId).size();
            ArrayList<UIPlugin> ribbonButtons = new ArrayList<>();
            for (Plugin plugin : userPlugins.get(userId)) {
                UIPlugin ribbonButton = new UIPlugin(plugin);
                ribbonButtons.add(ribbonButton);
                
                System.out.println("############### LOAD " + ribbonButton);

                try {
                    ribbonButton.getWorkPanelPlugin().setSession(getSession());
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.OFF, Objects.toString(ribbonButton), e);
                    continue;
                }
                
                Object objPanel = ribbonButton.getWorkPanelPlugin().getWorkPanel();
                System.out.println(">>>>>>>>>>>>>------------ " + objPanel + ", " + objPanel.hashCode());
                ribbonButton.setWorkPanel(objPanel);
                setProgressBar();
                JSMediator.refreshElement(getSession(), "dbEventDescrition", plugin.getPluginName());
            }
            ribbonButtonsMap.put(userId, ribbonButtons);
        }

        JSMediator.restoreBody(getSession());
        JSMediator.setTopButtonsModel(getSession(), getTopButtonModel(userId));
        
        
        User user = (User)CarssierDataBase.getDataBase().getObject(User.class.getName(), "getId", userId);
        CarssierCore.getInstance().setLoggedUser(getSession(), user);
    }
    
    private void setProgressBar(){
        pluginsCounter++; 
        double value = (double)pluginsCounter / (double)pluginsCount;
        value = value * 100;
        JSMediator.setSplashProgressBar(getSession(), (int)value);
    }

    public String getTopButtonModel(final long userId) {
        String model = "<table height=\"100%\" border=\"0\" cellpadding=\"0\" "
                + "cellspacing=\"0\" id=\"ribbonTopPanel\">"
                + "<tbody><tr>";

        ArrayList<UIPlugin> ribbonButtons = ribbonButtonsMap.get(userId);
        Collections.sort(ribbonButtons, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                UIPlugin uip1 = (UIPlugin) o1;
                UIPlugin uip2 = (UIPlugin) o2;
                return ((Integer) uip1.getWorkPanelPlugin().getSelectorGroupPosition())
                        .compareTo(uip2.getWorkPanelPlugin().getSelectorGroupPosition());
            }
        });

        Set<String> topButtonNames = new LinkedHashSet<>();
        for (UIPlugin ribbonButton : ribbonButtons) {
            topButtonNames.add(ribbonButton.getTopButtonName());
        }

        for (String name : topButtonNames) {
            TopButton topButton = new TopButton(getSession(), name) {
                @Override
                public void selected(String json) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        JSMediator.setOperationButton(
                                getSession(),
                                getOperationButtonModel(userId, jsonObject.getString("name")));
                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.SEVERE, json, e);
                    }

                }
            };

            model += topButton.getModel();
        }

        model += "</tr></tbody></table>";
        return model;
    }

    private String getOperationButtonModel(long userId, String topButtonName) {
        String model = "<table height=\"100%\" border=\"0\" id=\"ribbon_bottom_panel_modules\"><tr>";
        ArrayList<UIPlugin> list = new ArrayList<>();
        for (UIPlugin ribbonButton : ribbonButtonsMap.get(userId)) {
            if (ribbonButton.getTopButtonName().equals(topButtonName)) {
                list.add(ribbonButton);
            }
        }

        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                UIPlugin p1 = (UIPlugin) o1;
                UIPlugin p2 = (UIPlugin) o2;
                return ((Integer) p1.getWorkPanelPlugin().getSelectorLabelPosition()).compareTo(p2.getWorkPanelPlugin().getSelectorLabelPosition());
            }
        });

        int colspan = 0;
        String description = "";

        Rules rules = null;
        Result r = CarssierCore.getInstance().getRulesForUser(userId);
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());

        } else {
            rules = (Rules) r.getObject();
        }

        for (UIPlugin ribbonButton : list) {
            boolean isAllowToUse = false;
            for (Rule rule : rules.getRules()) {
                if (ribbonButton.getClassName().equals(rule.getModuleWorkPanelClassName())) {
                    isAllowToUse = rule.isAllowToUse();
                    break;
                }
            }

            if (!isAllowToUse) {
                continue;
            }

            boolean hasWorkpanelToolbar = false;
            if (ribbonButton.getWorkPanelPlugin().getWorkPanel() instanceof HasWorkPanelToolbars) {
                HasWorkPanelToolbars hwptb = (HasWorkPanelToolbars) ribbonButton.getWorkPanelPlugin().getWorkPanel();
                if (hwptb.getWorkpanelToolbars().isEmpty()) {
                    hasWorkpanelToolbar = false;
                } else {
                    hasWorkpanelToolbar = true;
                }
            }

            if (ribbonButton.getWorkPanelPlugin() instanceof RightPanelPlugin) {
                model += "<td align=\"center\" valign=\"top\" class=\"ribbon_bottom_button\" identificator=\""
                        + ribbonButton.getIdentificator() + "\" rightPanels=\""
                        + ribbonButton.getRightPanelsIdentificators() + "\">"
                        + "<img src=\"img/" + ribbonButton.getImagePath() + "\"><br/>"
                        + "<span class=\"ribbon_bottom_button_text\">"
                        + ribbonButton.getOperationButton()
                        + "</span>"
                        + "</td>";

                if (hasWorkpanelToolbar) {
                    model += "<td class='arrowInactive' align='center' valign='middle'>"
                            + "<div class='vlineInactive'></div>"
                            + "<div class='arrow-leftInactive'></div>"
                            + "</td>";
                } else {
                    model += "<td></td>";
                }

            } else {
                model += "<td align=\"center\" valign=\"top\" class=\"ribbon_bottom_button\" identificator=\""
                        + ribbonButton.getIdentificator() + "\" rightPanels=\"\">"
                        + "<img src=\"img/" + ribbonButton.getImagePath() + "\"><br/>"
                        + "<span class=\"ribbon_bottom_button_text\">"
                        + ribbonButton.getOperationButton()
                        + "</span>"
                        + "</td>";

                if (hasWorkpanelToolbar) {
                    model += "<td class='arrowInactive' align='center' valign='middle'>"
                            + "<div class='vlineInactive'></div>"
                            + "<div class='arrow-leftInactive'></div>"
                            + "</td>";
                } else {
                    model += "<td></td>";
                }
            }

            String _description = "" + ribbonButton.getWorkPanelPlugin().getGroupDescription();
            if (!_description.equals("")) {
                description = _description;
            }

            colspan++;
        }

        colspan = colspan * 2;
        model += "</tr><tr><td colspan='" + colspan + "' align='center' class='ribbonPanelDescription'>"
                + description + "</td></tr></table>";
        return model;
    }

    @Override
    public String getModel() {

        init();
        txtPassword.setPassword("");

        String model = "<div class='loginPanel'>"
                + "<table width='100%' cellpadding='3' class='loginTable'>"
                + "<tr>"
                + "<td colspan='2'><img src='img/logo/logo_small.png'></td>"
                + "</tr>"
                + "<tr>"
                + "<td align='right'>Логин</td>"
                + "<td>" + cboLogin.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td align='right'>Пароль</td>"
                + "<td>" + txtPassword.getModel() + "</td>"
                + "</tr>"
                + "<tr><td colspan='2' align='right'>"
                + btnOk.getModel() + "&nbsp;" + btnCancel.getModel()
                + "<td>"
                + "</tr>"
                + "</table>"
                + "</div>";
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

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showLoginPanel")) {
                    JSMediator.showLoginPanel(getSession(), getModel());
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(LoginHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
