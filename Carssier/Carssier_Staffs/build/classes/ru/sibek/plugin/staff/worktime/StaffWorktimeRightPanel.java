/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.staff.worktime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.employee.Crew;
import org.ubo.employee.Employee;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.utils.Result;
import org.uui.component.MenuItem;
import org.uui.component.RightPanel;
import org.uui.db.DataBase;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerLeaf;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author anton
 */
public class StaffWorktimeRightPanel extends RightPanel {

    private DataBase dataBase;
    private CarssierCore core = CarssierCore.getInstance();

    public StaffWorktimeRightPanel(String sessionId) {
        super(sessionId);
        dataBase = CarssierDataBase.getDataBase();
    }

    @Override
    public String getName() {
        return "Регистрация";
    }

    @Override
    public String getModel() {
        String _model;

        boolean isEmpty = true;
        for (Crew crew : CarssierCore.getInstance().getCrewsList()) {
            if (!crew.getIdsEmployees().isEmpty()) {
                isEmpty = false;
            }
        }

        if (isEmpty) {
            _model = "<div style='width:98%;height:98%;overflow:hidden;' "
                    + "identificator='" + getIdentificator() + "' class='rightPanel'>"
                    + "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td align='center' valign='middle'>"
                    + "<div style='width:50%;height:50%;border-radius:18px;border-color:gray;"
                    + "border-style:dotted;border-width:3px;' align='center'>"
                    + "<table width='100%' height='100%'><tr><td align='center' valign='middle'>"
                    + "Для регистрации работников просто бросайте их сюда"
                    + "</td></tr></table>"
                    + "</div>"
                    + "<td>"
                    + "</tr>"
                    + "</table>"
                    + "</div>";
        } else {

            ArrayList<Employee> list = CarssierCore.getInstance().getAllEmployeeAtWork();
            Collections.sort(list, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    Employee e1 = (Employee) o1;
                    Employee e2 = (Employee) o2;
                    return e1.getShortName().compareToIgnoreCase(e2.getShortName());
                }
            });

            String employeePanel = "";
            for (final Employee employee : list) {
                String img = employee.getImageFileName();
                if (employee.getRole().indexOf("foreman") != -1) {
                    img = "img/icons/master.png";
                }

                ExplorerLeaf label = new ExplorerLeaf(getSession(), 
                        Employee.class.getName(),
                        employee.getId(),
                        employee.getShortName(), img);
                label.addUIEventListener(new UIEventListener() {
                    @Override
                    public void event(UIEvent evt) {
                        try {
                            if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                                JSMediator.setContextMenu(getSession(), getLeafMenu(employee));
                            }
                        } catch (JSONException e) {
                            JSMediator.alert(getSession(), e.toString());
                        }
                    }
                });
                employeePanel += label.getModel();
            }
            _model = "<div style='width:98%;height:98%;overflow:hidden;padding:5px;' "
                    + "identificator='" + getIdentificator() + "' class='rightPanel'>"
                    + "<div align='center'>"
                    + "<div style='width:94%;padding:5px;font-size:85%;font-weight:bold;"
                    + "border-radius:8px;border-color:gray;border-style:dotted;"
                    + "border-width:1px;background-color:#CACACA;margin:5px;' align='center'>"
                    + "Зарегистрированный персонал"
                    + "</div>"
                    + "</div>"
                    + "<div style='width:100%;'>"
                    + employeePanel
                    + "</div>"
                    + "</div>";
        }

        return _model;
    }

    private String getLeafMenu(final Employee employee) {
        String model = "";
        MenuItem mnuProperties = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить из смены");
        mnuProperties.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {

                Result r = core.removeEmployeeFromCrew(employee.getId(), employee.getDefaultCrew().getId());
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());
                    JSMediator.setRightPanel(getSession(), getModel());
                    String json = "{eventType:push, session:" + getSession() + ", action:showWorkPanel}";
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            StaffWorkTimePanel.class.getName(), json);
                    
                } else {
                    JSMediator.setRightPanel(getSession(), getModel());
                    String json = "{eventType:push, session:" + getSession() + ", action:showWorkPanel}";
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            StaffWorkTimePanel.class.getName(), json);

                }
            }
        });
        model += mnuProperties.getModel();

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    @Override
    public String getIdentificator() {
        return StaffWorktimeRightPanel.class.getName();
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

            if (jsonObject.getString("eventType").equals("click")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    JSMediator.setRightPanel(getSession(), getModel());
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    dropHandler(jsonObject.getJSONArray("data"));
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
            ArrayList<Employee> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                if (jsonObject.getString("className").indexOf("Employee") != -1) {
                    Employee employee = (Employee) dataBase.getObject(
                            jsonObject.getString("className"),
                            jsonObject.getLong("dbid"));
                    list.add(employee);
                }
            }

            if (!list.isEmpty()) {
                registerPerson(list);
            }

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, Objects.toString(jsonArray), e);
        }

    }

    private void registerPerson(ArrayList<Employee> list) {
        for (Employee employee : list) {
            Result r = core.putEmployeeToCrew(employee.getId(), employee.getDefaultCrew().getId());
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());
                return;
            }
        }

        String json = "{eventType:push, session:" + getSession() + ", action:showWorkPanel}";
        WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                StaffWorkTimePanel.class.getName(), json);
    }
}
