/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.staff.employee;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.employee.Employee;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.utils.Result;
import org.uui.component.RightPanel;
import org.uui.db.DataBase;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author anton
 */
public class EmployeeCard extends RightPanel {

    private EmployeePropertiesPanel employeePropertiesPanel;
    private DataBase dataBase;
    private EmployeePanel spp;
    private CarssierCore core = CarssierCore.getInstance();

    public EmployeeCard(String sessionId) {
        super(sessionId);
        dataBase = CarssierDataBase.getDataBase();
        employeePropertiesPanel = new EmployeePropertiesPanel(sessionId, dataBase);
    }

    private void setPanel(Employee employee) {
        employeePropertiesPanel.setEmployee(employee);
    }

    @Override
    public String getName() {
        return "Свойства";
    }

    public EmployeePanel getStaffPersonPanel() {
        return spp;
    }

    public void setStaffPersonPanel(EmployeePanel spp) {
        this.spp = spp;
    }

    @Override
    public String getModel() {
        return "<div style='width:100%;height:100%;overflow:hidden;' "
                + "identificator='" + getIdentificator() + "' class='rightPanel'>"
                + employeePropertiesPanel.getModel()
                + "</div>";
    }

    @Override
    public String getIdentificator() {
        return EmployeeCard.class.getName();
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
            employeePropertiesPanel.setSession(jsonObject.getString("session"));

            if (jsonObject.getString("eventType").equals("drop")) {
                dropHandler(jsonObject.getJSONArray("data"));
                        JSMediator.setRightPanel(getSession(), getModel());
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    dropHandler(jsonObject.getJSONArray("data"));
                }
                
                        JSMediator.setRightPanel(getSession(), getModel());
            }

            if (jsonObject.getString("eventType").equals("click")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    JSMediator.setRightPanel(getSession(), getModel());
                }
            }

        } catch (JSONException  ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    private void dropHandler(JSONArray jsonArray) {
        try {
            if(jsonArray.length() > 0){
                JSONObject jsonObject = jsonArray.optJSONObject(0);
                if (jsonObject.getString("className").indexOf("Employee") != -1) {
                    Result r = core.getEmployeeById(jsonObject.getLong("dbid"));
                    if(r.isError()){
                        JSMediator.alert(getSession(), r.getReason());
                    } else {
                        setPanel((Employee)r.getObject());
                    }
                }
            }

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, Objects.toString(jsonArray), e);
        }

    }
}
