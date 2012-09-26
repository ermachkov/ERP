/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.staff.employee;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.employee.Employee;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.utils.Result;
import org.uui.component.MenuItem;
import org.uui.component.WorkPanel;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerLeaf;
import org.uui.plugin.HasRules;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.ribbon.RibbonButtonEventListener;
import org.uui.ribbon.RibbonEvent;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.core.ui.PopupPanel;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class EmployeePanel extends WorkPanel implements HasWorkPanelToolbars, HasRules {

    private ArrayList<RibbonButton> toolbarButtons;
    private CarssierCore core = CarssierCore.getInstance();

    public EmployeePanel(String sessionId) {
        super(sessionId);
        initToolbar();
    }

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();

        LinkedList<SelectorRuleItem> selectorRuleItemList = new LinkedList<>();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("deleteEmployeeDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("deleteEmployeeAllow");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 1 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canDeleteEmployee",
                "Удаление работников:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        return listRulesItem;
    }

    private void initToolbar() {
        toolbarButtons = new ArrayList<>();

        RibbonButton rbAddPerson = RibbonButton.createDefaultRibbonButton(getSession(),
                "img/subbuttons/add_person.png", "Добавить персону", "addPerson");
        rbAddPerson.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                addPerson();
            }
        });
        toolbarButtons.add(rbAddPerson);

        RibbonButton rbRemovePerson = RibbonButton.createDefaultRibbonButton(getSession(),
                "img/subbuttons/remove_person.png", "Удалить персону", "removePerson");
        rbRemovePerson.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                //TODO
            }
        });
        toolbarButtons.add(rbRemovePerson);
    }

    public void addPerson() {
        Employee employee = new Employee();
        employee.setName("Новенький");
        Result r = core.addEmployee(employee);
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());

        } else {
            JSMediator.setWorkPanel(getSession(), getModel());
        }
    }

    @Override
    public String getModel() {
        ArrayList<Employee> list = core.getEmployeeList();
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

        if (model.equals("")) {
            model = "<div identificator='" + getIdentificator() + "' style='width:100%; height:100%;'>";
        }

        return model;
    }

    private String getLeafMenu(final Employee employee) {
        String model = "";
        MenuItem mnuProperties = new MenuItem(getSession(), "img/subbuttons/settings.png", "Свойства");
        mnuProperties.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                String json = "{eventType:push, session:" + getSession() + ", action:showRightPanel, ";
                json = json + "data:[{dbid:" + employee.getId() + ", className:" + Employee.class.getName() + "}]}";
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                        EmployeeCard.class.getName(), json);
            }
        });

        model += mnuProperties.getModel();

        MenuItem itemDelete = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить");
        itemDelete.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                deleteEmployee(employee);
            }
        });

        if (!core.isRadioButtonRuleAllow(getSession(), "canDeleteEmployee", "deleteEmployeeDeny")) {
            model += itemDelete.getModel();
        }

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    private void deleteEmployee(final Employee employee) {
        if (core.canDeleteEmployee(employee)) {
            ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                @Override
                public void pressed(int button) {
                    if (button == ConfirmPanel.YES) {
                        Result r = core.removeEmployee(employee);
                        if (r.isError()) {
                            JSMediator.alert(getSession(), r.getReason());
                        } else {
                            JSMediator.setWorkPanel(getSession(), EmployeePanel.this.getModel());
                        }
                    }
                }
            };
            confirmPanel.setTitle("Вопрос");
            confirmPanel.setMessage("Попытаться удалить персону?");
            confirmPanel.showPanel();

        } else {
            PopupPanel popupPanel = new PopupPanel(getSession());
            popupPanel.setTitle("Предупреждение");
            String message = "<img src='img/info/warning.png' "
                    + "align='left' hspace='5'>Удалить "
                    + employee.getShortName() + " не представляется возможным, "
                    + "потому что он зафиксирован в первичных документах.";
            popupPanel.setPanel(message);
            popupPanel.showPanel();
        }
    }

    @Override
    public String getIdentificator() {
        return EmployeePanel.class.getName();
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
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Персоны");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.hideNavigationPanel(getSession());
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showWorkPanel")) {
                    JSMediator.setWorkPanel(getSession(), getModel());
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), EmployeeCard.class.getName(), json);
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

            if (core.isRadioButtonRuleAllow(getSession(), "canDeleteEmployee", "deleteEmployeeDeny")
                    && rb.getActionName().equals("removePerson")) {
                continue;

            } else {
                model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
            }
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

        MenuItem mnuAddEmployee = new MenuItem(getSession(), "img/subbuttons/add_person.png", "Добавить персону");
        mnuAddEmployee.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                addPerson();
            }
        });
        model += mnuAddEmployee.getModel();

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }
}
