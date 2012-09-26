/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.rules;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.employee.Employee;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.rules.Rule;
import org.ubo.rules.RuleItem;
import org.ubo.rules.Rules;
import org.ubo.utils.Result;
import org.uui.component.*;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.users.User;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class RulesRightPanel extends RightPanel {

    private CarssierCore core = CarssierCore.getInstance();
    private User selectedUser = null;
    private TextField txtLogin, txtPassword;
    private Map<String, String> rolesMap = new HashMap<>();

    public RulesRightPanel(String sessionId) {
        super(sessionId);
        txtLogin = new TextField(getSession());
        txtLogin.setStyle("width:96%");
        txtPassword = new TextField(getSession());
        txtPassword.setStyle("width:96%");


        rolesMap.put("Работник", "employee");
        rolesMap.put("Бригадир", "foreman");
        rolesMap.put("Бригадир/Кассир", "foreman/cashier");
        rolesMap.put("Кассир", "cashier");
        rolesMap.put("Бухгалтер", "accountant");
        rolesMap.put("Менеджер", "manager");
        rolesMap.put("Владелец", "owner");
    }

    @Override
    public String getName() {
        return "Свойства";
    }

    @Override
    public String getModel() {
        String _model;
        if (selectedUser == null) {
            _model = "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td align='center' valign='middle'>"
                    + "<div style='width:50%;height:50%;border-radius:18px;border-color:gray;"
                    + "border-style:dotted;border-width:3px;' align='center'>"
                    + "<table width='100%' height='100%'><tr><td align='center' valign='middle'>"
                    + "Для просмотра и редактирования свойств бросайте сюда пользователей"
                    + "</td></tr></table>"
                    + "</div>"
                    + "<td>"
                    + "</tr>"
                    + "</table>";
        } else {

            Result result = core.getRulesForUser(selectedUser.getId());
            if (result.isError()) {
                _model = result.getReason();

            } else {
                //txtUserName.setText(selectedUser.getName());
                txtLogin.setText(selectedUser.getLogin());
                txtPassword.setText(selectedUser.getPassword());

                _model = "<div class='userBlock'>"
                        + "<table width='100%' cellpadding='3' class='userBlockTable'>"
                        + "<tr><td width='96' valign='middle' align='center'>"
                        + "<img src='" + selectedUser.getImageFileName() + "'>"
                        + "<br/>"
                        + "<span>Для смены картинки кликните на ней</span>"
                        + "</td>"
                        + "<td><table width='100%'>"
                        + "<tr><td align='right'>Логин</td>"
                        + "<td>" + txtLogin.getModel() + "</td></tr>"
                        + "<tr><td align='right'>Пароль</td>"
                        + "<td>" + txtPassword.getModel() + "</td></tr>"
                        + "<tr><td align='right' width='15%'>Персона</td>"
                        + "<td>" + getUserInfo() + "</td></tr>"
                        + "</table></td></tr>"
                        + "</table></div>";

                _model += "<div align='right' style='width:100%; margin-right:15px;'>"
                        + "<button class='btnRulesAllSelector'>"
                        + "select/unselect all"
                        + "</button>"
                        + "</div>";

                final Rules rules = (Rules) result.getObject();
                for (final Rule rule : rules.getRules()) {
                    String ruleBlock = "<div class='ruleBlock'>";
                    final CheckBox checkBox = new CheckBox(getSession(), "Вкл.");
                    checkBox.setName("ruleBlockCheckBox");
                    checkBox.setChecked(rule.isAllowToUse());
                    checkBox.addUIEventListener(new UIEventListener() {
                        @Override
                        public void event(UIEvent evt) {
                            rule.setAllowToUse(checkBox.isChecked());
                            Result r = core.modifyRules(rules);
                            if (r.isError()) {
                                JSMediator.alert(getSession(), r.getReason());
                            }
                        }
                    });

                    String color = "color:gray;";
                    if (rule.isAllowToUse()) {
                        color = "color:black;";
                    }

                    ruleBlock += "<table width='100%' class='font-size:85%;'><tr>"
                            + "<td>"
                            + "<span style='font-weight:bold;" + color + "'>" + rule.getModuleName()
                            + "</span>"
                            + "</td>"
                            + "<td width='10%'>" + checkBox.getModel()
                            + "</td></tr></table>";
                    ruleBlock += "<div class='ruleBlockDescription'>"
                            + "<span style='" + color + "'>"
                            + rule.getModuleDescription()
                            + "</span></div>";

                    String section = "";

                    try {
                        if (!rule.getRuleItems().isEmpty()) {
                            RulePanel rulePanel = new RulePanel(getSession(), rule) {
                                @Override
                                public void change(RuleItem ruleItem, boolean isChecked) {
                                    Result r = core.modifyRules(rules);
                                    Logger.getGlobal().log(Level.INFO, r.toString());
                                    if (r.isError()) {
                                        JSMediator.alert(getSession(), r.getReason());
                                    }
                                }
                            };

                            section = rulePanel.getModel();

                        }

                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, rule.toString(), e);
                    }

                    ruleBlock += section + "</div>";
                    _model += ruleBlock;
                }
            }
        }

        String model = "<div style='width:100%;height:100%;overflow:auto;' "
                + "identificator='" + getIdentificator() + "' class='rightPanel'>"
                + _model + "</div>";

        return model;
    }

    private String getUserInfo() {
        String info = "";
        if (selectedUser.getExtraInfo().containsKey("employeeId")) {
            long id = Long.parseLong(("" + selectedUser.getExtraInfo().get("employeeId")).trim());
            Result r = core.getEmployeeById(id);
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());
                return selectedUser.getName() + "<br/>";
            }

            Employee employee = (Employee) r.getObject();
            info += employee.getName() + " / " + employee.getShortName() + "<br/>";

            Iterator<String> it = rolesMap.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (rolesMap.get(key).equals(employee.getRole())) {
                    info += "Роль: " + key + "<br/>";
                }
            }
            return info;

        } else {
            info = selectedUser.getName() + "<br/>";
            return info;
        }
    }

    @Override
    public String getIdentificator() {
        return RulesRightPanel.class.getName();
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

            if (jsonObject.getString("eventType").equals("drop")) {
                dropHandler(jsonObject.getJSONArray("data"));
                JSMediator.setRightPanel(getSession(), getModel());
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    dropHandler(jsonObject.getJSONArray("data"));
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
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                if (jsonObject.getString("className").indexOf("User") != -1) {
                    Result result = core.getUser(jsonObject.getLong("dbid"));
                    if (result.isError()) {
                        JSMediator.alert(getSession(), result.getReason());

                    } else {
                        selectedUser = (User) result.getObject();
                    }
                }
            }

        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, Objects.toString(jsonArray), e);
        }

    }
}
