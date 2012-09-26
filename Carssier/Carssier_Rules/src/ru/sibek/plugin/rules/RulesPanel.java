/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.rules;

import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.employee.Employee;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.utils.Result;
import org.uui.component.*;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerLeaf;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.ribbon.RibbonButtonEventListener;
import org.uui.ribbon.RibbonEvent;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.business.users.User;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.core.ui.PopupPanel;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class RulesPanel extends WorkPanel implements HasWorkPanelToolbars {

    private ArrayList<RibbonButton> toolbarButtons;
    private CarssierCore core = CarssierCore.getInstance();
    private TextField txtNewUserName, txtNewUserLogin, txtNewUserPassword;
    private ComboBox cboEmployee;

    public RulesPanel(String sessionId) {
        super(sessionId);
        txtNewUserName = new TextField(getSession());
        txtNewUserName.setStyle("width:96%;");

        txtNewUserLogin = new TextField(getSession());
        txtNewUserLogin.setStyle("width:96%;");

        txtNewUserPassword = new TextField(getSession());
        txtNewUserPassword.setStyle("width:96%;");

        cboEmployee = new ComboBox(getSession());

        initToolbar();
    }

    private void initToolbar() {
        toolbarButtons = new ArrayList<>();

        // Add user
        RibbonButton rbAddUser = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/add_user.png",
                "Добавить пользователя",
                "addUser");
        rbAddUser.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                addUser();
            }
        });
        toolbarButtons.add(rbAddUser);

        // Remove user
        RibbonButton rbRemoveUser = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/remove_user.png",
                "Удалить пользователя",
                "removeUser");
        rbRemoveUser.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                JSMediator.requestAllSelectedNodes(getSession(), "removeUser", getIdentificator());
            }
        });
        toolbarButtons.add(rbRemoveUser);
    }

    private void addUser() {
        try {
            ArrayList<Employee> employees = core.getEmployeeList();
            if (employees.isEmpty()) {
                PopupPanel popupPanel = new PopupPanel(getSession());
                popupPanel.setTitle("Предупреждение");
                popupPanel.setPanel("Создать учетную запись не "
                        + "представляется возможным. "
                        + "Не обнаружено ни одной персоны."
                        + "Вам необходимо создать новую персону в разделе "
                        + "<strong>Персонал >> Персоны</strong>");
                popupPanel.showPanel();
                return;
            }

            LinkedHashMap<String, String> m = new LinkedHashMap<>();
            for (Employee employee : employees) {
                String name = employee.getFullName();
                if (name.equals("")) {
                    name = employee.getShortName();
                }
                m.put(name, "" + employee.getId());
            }
            cboEmployee.setItems(m);

            String panel = "<table width='100%'>"
                    + "<tr>"
                    + "<td align='right' width='10%'>Логин</td>"
                    + "<td>" + txtNewUserLogin.getModel() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='right' width='10%'>Пароль</td>"
                    + "<td>" + txtNewUserPassword.getModel() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='right' width='10%'>Персона</td>"
                    + "<td>" + cboEmployee.getModel() + "</td>"
                    + "</tr>"
                    + "</table>";

            PopupPanel popupPanel = new PopupPanel(getSession());
            popupPanel.setPanel(panel);
            popupPanel.setTitle("Новый пользователя");
            popupPanel.showPanel(500, -1);

            if (txtNewUserLogin.getText().equals("") || txtNewUserPassword.getText().equals("")) {
                String msg;
                msg = txtNewUserLogin.getText().equals("") ? "Логин пользователя не может быть пустым<br/>" : "";
                msg += txtNewUserPassword.getText().equals("") ? "Пароль пользователя не может быть пустым<br/>" : "";

                popupPanel = new PopupPanel(getSession());
                popupPanel.setTitle("<span style='color:red;'>Ошибка</span>");
                popupPanel.setPanel(msg);
                popupPanel.showPanel();

                return;
            }

            Result r = core.isUserExist(txtNewUserLogin.getText());
            if (!r.isError()) {
                popupPanel = new PopupPanel(getSession());
                popupPanel.setTitle("<span style='color:red;'>Ошибка</span>");
                popupPanel.setPanel("'Пользователь с логином "
                        + txtNewUserLogin.getText() + " "
                        + "уже существует<br/>придумайте другой'");
                popupPanel.showPanel();

            } else {
                r = core.addUser(cboEmployee.getSelectedKey(), txtNewUserLogin.getText(),
                        txtNewUserPassword.getText(), cboEmployee.getSelectedValue());
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());

                } else {
                    JSMediator.setWorkPanel(getSession(), getModel());
                }
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

    }

    private void removeUsers(JSONArray jsonArray) {
        final ArrayList<User> removeUserList = new ArrayList<>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                if (jsonObject.getString("className").indexOf("User") != -1) {
                    Result result = core.getUser(jsonObject.getLong("dbid"));
                    if (result.isError()) {
                        JSMediator.alert(getSession(), result.getReason());

                    } else {
                        removeUserList.add((User) result.getObject());
                    }
                }
            }

            if (!removeUserList.isEmpty()) {
                String userList = "";
                for (User u : removeUserList) {
                    userList += u.getName() + " (" + u.getLogin() + ")<br/>";
                }

                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                    @Override
                    public void pressed(int button) {
                        if (button == ConfirmPanel.YES) {
                            for (User u : removeUserList) {
                                core.removeUser(u);
                            }

                            JSMediator.setWorkPanel(getSession(), RulesPanel.this.getModel());
                        }
                    }
                };
                confirmPanel.setTitle("Вопрос");
                confirmPanel.setMessage(
                        "Вы действительно желаете удалить пользовтелей:<br/>" + userList);
                confirmPanel.showPanel("getUICore().showConfirmPanel");
            }

        } catch (JSONException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

    }

    @Override
    public String getModel() {
        ArrayList<User> list = core.getUsers();
        Collections.sort(list, new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                return u1.getName().compareToIgnoreCase(u2.getName());
            }
        });

        ArrayList<ExplorerLeaf> listLeaf = new ArrayList<>();
        for (final User user : list) {
            String image = user.getImageFileName();
            if (image == null) {
                image = "img/icons/user64.png";
            } else if (image.equals("")) {
                image = "img/icons/user64.png";
            }

            ExplorerLeaf label = new ExplorerLeaf(getSession(), 
                    User.class.getName(),
                    user.getId(),
                    user.getLogin(), image);
            label.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                            JSMediator.setContextMenu(getSession(), getLeafMenu(user));
                        }
                    } catch (JSONException e) {
                        JSMediator.alert(getSession(), e.toString());
                    }
                }
            });
            listLeaf.add(label);
        }

        String model = "";
        for (ExplorerLeaf el : listLeaf) {
            model += el.getModel();
        }

        return model;
    }

    private String getLeafMenu(final User user) {
        String model = "";
        MenuItem mnuProperties = new MenuItem(getSession(), "img/subbuttons/settings.png", "Свойства");
        mnuProperties.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                String json = "{eventType:push, session:"+getSession()+", action:showRightPanel, ";
                json = json + "data:[{dbid:" + user.getId() + ", className:" + User.class.getName() + "}]}";
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        RulesRightPanel.class.getName(), json);
            }
        });
        model += mnuProperties.getModel();

        MenuItem itemDelete = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить");
        itemDelete.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("className", User.class.getName());
                    jsonObject.put("dbid", user.getId());
                    jsonArray.put(jsonObject);
                    removeUsers(jsonArray);

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });
        model += itemDelete.getModel();

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    @Override
    public String getIdentificator() {
        return RulesPanel.class.getName();
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
            JSMediator.setExplorerEditableMode(getSession(), false);

            if (jsonObject.getString("eventType").equals("removeUser")) {
                removeUsers(jsonObject.getJSONArray("data"));
            }

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Модули");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.hideNavigationPanel(getSession());

            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            RulesRightPanel.class.getName(), json);
                }
            }

            if (jsonObject.getString("eventType").equals("rightclick")) {
                JSMediator.setContextMenu(getSession(), getPanelMenu());
            }

        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }

    private String getSubOperationButtonModel() {
        int cols = getWorkpanelToolbars().size() / 2;
        int col = 0;
        String model = "<table class='subButtonsTable'><tr>";

        for (org.uui.ribbon.RibbonButton rb : getWorkpanelToolbars()) {
            if (col == cols) {
                model += "</tr><tr>";
            }

            model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
            col++;
        }

        model += "</tr></table>";

        return model;
    }

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return toolbarButtons;
    }

    private String getPanelMenu() {
        String model = "";

        MenuItem mnuAddUser = new MenuItem(getSession(), "img/subbuttons/add_user.png", "Добавить пользователя");
        mnuAddUser.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                addUser();
            }
        });
        model += mnuAddUser.getModel();

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }
}
