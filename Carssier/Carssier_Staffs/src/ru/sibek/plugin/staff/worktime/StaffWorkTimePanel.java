/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.staff.worktime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.employee.Employee;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.uui.component.MenuItem;
import org.uui.component.WorkPanel;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerLeaf;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class StaffWorkTimePanel extends WorkPanel implements HasWorkPanelToolbars {

    private ArrayList<RibbonButton> toolbarButtons = new ArrayList<>();
    private CarssierCore core = CarssierCore.getInstance();

    public StaffWorkTimePanel(String sessionId) {
        super(sessionId);
    }

    @Override
    public String getModel() {
        ArrayList<Employee> list = core.getAllEmployeeNotAtWork();

        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Employee e1 = (Employee) o1;
                Employee e2 = (Employee) o2;
                return e1.getShortName().compareToIgnoreCase(e2.getShortName());
            }
        });

        ArrayList<ExplorerLeaf> listLeaf = new ArrayList<>();
        for (final Employee employee : list) {
            if (employee.getShortName().equals("Бригадир")) {
                continue;
            }
            String image = employee.getImageFileName();
            if (employee.getRole().indexOf("foreman") != -1) {
                image = "img/icons/master.png";
            }

            if (image == null) {
                image = "img/icons/employee64.png";
            } else if (image.equals("")) {
                image = "img/icons/employee64.png";
            }

            ExplorerLeaf label = new ExplorerLeaf(getSession(), 
                    Employee.class.getName(),
                    employee.getId(),
                    employee.getShortName(), image);
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
            listLeaf.add(label);
        }

        String model = "";
        for (ExplorerLeaf el : listLeaf) {
            model += el.getModel();
        }

        return model;
    }

    private String getLeafMenu(final Employee employee) {
        String model = "";
        MenuItem mnuProperties = new MenuItem(getSession(), "img/subbuttons/import.png", "Зарегистрировать");
        mnuProperties.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                String json = "{eventType:push, session:" + getSession() + ", action:showRightPanel, ";
                json = json + "data:[{dbid:" + employee.getId()
                        + ", className:" + Employee.class.getName() + "}]}";
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        StaffWorktimeRightPanel.class.getName(), json);
            }
        });
        model += mnuProperties.getModel();

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    @Override
    public String getIdentificator() {
        return StaffWorkTimePanel.class.getName();
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
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Рабочее время");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.hideNavigationPanel(getSession());

                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        StaffWorktimeRightPanel.class.getName(),
                        "{eventType:click, session:" + getSession() + ", action:showRightPanel}");
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showWorkPanel")) {
                    JSMediator.setWorkPanel(getSession(), getModel());
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            StaffWorktimeRightPanel.class.getName(), json);
                }
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
