/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.agents;

import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.partner.Agent;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.utils.Result;
import org.uui.component.*;
import org.uui.db.DataBase;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.plugin.HasRules;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.ribbon.RibbonButtonEventListener;
import org.uui.ribbon.RibbonEvent;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.core.ui.PopupPanel;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class AgentsPanel extends WorkPanel implements HasWorkPanelToolbars, HasRules {

    private DataBase dataBase;
    private CarssierCore core = CarssierCore.getInstance();
    private MacTableModel macTableModel;
    private ArrayList<RibbonButton> toolbarButtons;
    private TableSearchFilter tableSearchFilter;

    public AgentsPanel(String sessionId) {
        super(sessionId);
        dataBase = CarssierDataBase.getDataBase();

        macTableModel = new MacTableModel(getSession());
        macTableModel.setCssClass("leftMacTable");
        macTableModel.setId("agentsPanelTable");

        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Партнер", String.class, true));
        mth.addHeaderColumn(new MacHeaderColumn("Адрес", String.class, true));
        mth.addHeaderColumn(new MacHeaderColumn("Телефон", String.class, true));
        mth.addHeaderColumn(new MacHeaderColumn("Контактное лицо", String.class, true));
        macTableModel.setHeader(mth);
        macTableModel.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    if (evt.getJSONObject().getString("eventType").equals("click")) {
                        Agent a = (Agent) macTableModel.getRow(evt.getJSONObject().getInt("row")).getValue();
                        WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                                AgentsPropertiesPanel.class.getName(),
                                "{eventType:push,"
                                + "session:" + getSession() + ","
                                + "action:showRightPanel,"
                                + "dbid:" + a.getId() + "}");
                    }

                    if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                        Agent a = (Agent) macTableModel.getRow(evt.getJSONObject().getInt("row")).getValue();
                        JSMediator.setContextMenu(getSession(), getTableRowMenu(a));
                    }

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });

        tableSearchFilter = new TableSearchFilter(getSession()) {
            @Override
            public void searchAction(String searchPattern, int rowsCount) {
                refreshAgentsTable();
            }
        };

        initToolbar();
    }

    private void refreshAgentsTable() {
        try {
            updateTable();
            JSMediator.refreshElement(getSession(), "AgentsPanel", macTableModel.getModel());

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private String getTableRowMenu(final Agent a) {
        String model = "";
        MenuItem mnuToProp = new MenuItem(getSession(), "img/subbuttons/settings.png", "Свойства");
        mnuToProp.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        AgentsPropertiesPanel.class.getName(),
                        "{eventType:push,"
                        + "action:showRightPanel,"
                        + "dbid:" + a.getId() + "}");
            }
        });
        model += mnuToProp.getModel();

        MenuItem itemDelete = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить");
        itemDelete.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                    @Override
                    public void pressed(int button) {
                        if (button == ConfirmPanel.YES) {
                            AgentsPanel.this.deleteAgent(a);
                        }
                    }
                };
                confirmPanel.setTitle("Вопрос");
                confirmPanel.setMessage("Попытаться удалить партнера?");
                confirmPanel.showPanel();
            }
        });

        if (!core.isRadioButtonRuleAllow(getSession(), "canDeleteAgents", "deleteAgentsDeny")) {
            model += itemDelete.getModel();
        }

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    private String getPanelMenu() {
        String model = "";

        MenuItem mnuAddAgent = new MenuItem(getSession(), "img/icons/add_agent.png", "Добавить партнера");
        mnuAddAgent.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                addAgent();
            }
        });

        if (!core.isRadioButtonRuleAllow(getSession(), "canAddAgents", "addAgentsDeny")) {
            model += mnuAddAgent.getModel();
        }

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    private void deleteAgent(Agent a) {
        if (core.canDeleteAgent(a)) {
            Result r = core.removeAgent(a);
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());
            } else {
                try {
                    JSMediator.setWorkPanel(getSession(), getModel());

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }

        } else {
            PopupPanel popupPanel = new PopupPanel(getSession());
            popupPanel.setTitle("Предупреждение");
            String message = "<img src='img/info/warning.png' "
                    + "align='left' hspace='5'>Удалить партнера "
                    + a.getShortName() + " не представляется возможным, "
                    + "потому что он зафиксирован в певичных документах.";
            if (a.isDefaultSupplier()) {
                message = "<img src='img/info/warning.png' "
                        + "align='left' hspace='5'>Удалить поставщика по умолчанию нельзя.";

            } else if (a.getShortName().equals("Частное лицо")) {
                message = "<img src='img/info/warning.png' "
                        + "align='left' hspace='5'>Удалить партнера «Частное лицо» нельзя.";
            }
            popupPanel.setPanel(message);
            popupPanel.showPanel();
        }
    }

    private void initToolbar() {
        toolbarButtons = new ArrayList<>();

        // Add Agent
        RibbonButton rbAddAgent = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/add_agent.png",
                "Добавить партнера",
                "addAgent");
        rbAddAgent.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                addAgent();
            }
        });
        toolbarButtons.add(rbAddAgent);

        // Remove Agent
        RibbonButton rbRemoveAgent = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/delete.png",
                "Удалить партнера",
                "removeAgent");
        rbRemoveAgent.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                if (macTableModel.getSelectedRow() != -1) {
                    ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                        @Override
                        public void pressed(int button) {
                            if (button == ConfirmPanel.YES) {
                                Agent a = (Agent) macTableModel.getRow(macTableModel.getSelectedRow()).getValue();
                                deleteAgent(a);
                            }
                        }
                    };
                    confirmPanel.setTitle("Вопрос");
                    confirmPanel.setMessage("Попытаться удалить партнера?");
                    confirmPanel.showPanel();

                } else {
                    PopupPanel popupPanel = new PopupPanel(getSession());
                    popupPanel.setTitle("Подсказка");
                    popupPanel.setPanel("<img src='img/info/info.png' "
                            + "align='left' hspace='5'/>Для удаления партнера "
                            + "его необходимо сначала выделить.");
                    popupPanel.showPanel();
                }
            }
        });
        toolbarButtons.add(rbRemoveAgent);
    }

    private void addAgent() {
        try {
            Agent agent = new Agent();
            agent.setShortName("Новый партнер");
            agent.setFullName("Новый партнер");
            agent.setINN("");

            Result r = core.addAgent(agent);
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());

            } else {
                JSMediator.setWorkPanel(getSession(), getModel());
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

    }

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return toolbarButtons;
    }

    private void updateTable() {
        ArrayList<Agent> listAgents = dataBase.getAllObjectsList(Agent.class.getName());
        Collections.sort(listAgents, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Agent a1 = (Agent) o1;
                Agent a2 = (Agent) o2;

                return a1.getShortName().compareToIgnoreCase(a2.getShortName());
            }
        });

        ArrayList<MacTableRow> rows = new ArrayList<>();
        int counter = 0;
        for (Agent a : listAgents) {
            if (counter > tableSearchFilter.getRowsCount()) {
                continue;
            }

            if (a.getShortName().indexOf(tableSearchFilter.getSearchPattern()) == -1
                    && a.getFullName().indexOf(tableSearchFilter.getSearchPattern()) == -1) {
                if (!tableSearchFilter.getSearchPattern().trim().equals("")) {
                    continue;
                }
            }

            MacTableRow macTableRow = new MacTableRow();
            macTableRow.setValue(a);
            String name = "" + a.getShortName() + " / " + a.getFullName();
            if (a.isDefaultSupplier()) {
                name = "<span style='color:darkblue;font-weight:bold;'>" + name + "</span>";
            }
            macTableRow.addCell(new MacTableCell(getSession(), name, false));

            String address = "";
            if (a.getDefaultAddress() != null) {
                address = "" + a.getDefaultAddress().getFormatted("{zip} {country} {city} {street} {house}");
            }

            macTableRow.addCell(new MacTableCell(getSession(), address, false));

            String defaultPhone = "";
            if (a.getDefaultContacts() != null) {
                defaultPhone = a.getDefaultContacts().getDefaultPhone();
            }
            macTableRow.addCell(new MacTableCell(getSession(), defaultPhone, false));

            String defaultContactPerson = "";
            if (a.getDefaultContacts() != null) {
                defaultContactPerson = a.getDefaultContacts().getDefaultContactPerson();
            }
            macTableRow.addCell(new MacTableCell(getSession(), defaultContactPerson, false));

            rows.add(macTableRow);

            counter++;
        }

        macTableModel.setData(rows);
    }

    @Override
    public String getModel() {
        updateTable();
        StringBuilder sb = new StringBuilder();
        sb.append(tableSearchFilter.getModel());
        sb.append("<div id='AgentsPanel'>");
        sb.append(macTableModel.getModel());
        sb.append("</div>");

        return sb.toString();
    }

    @Override
    public String getIdentificator() {
        return AgentsPanel.class.getName();
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
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Партнеры");
                
                JSMediator.setWorkPanel(getSession(), getModel());

                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());

                JSMediator.hideNavigationPanel(getSession());
            }

            if (jsonObject.getString("eventType").equals("rightclick")) {
                JSMediator.setContextMenu(getSession(), getPanelMenu());
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showWorkPanel")) {
                    JSMediator.setWorkPanel(getSession(), getModel());
                }
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

            if (rb.getActionName().equals("removeAgent")
                    && !core.isRadioButtonRuleAllow(getSession(), "canDeleteAgents", "deleteAgentsDeny")) {
                model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
            }

            if (rb.getActionName().equals("addAgent")
                    && !core.isRadioButtonRuleAllow(getSession(), "canAddAgents", "addAgentsDeny")) {
                model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
            }

            col++;
        }
        model += "</tr></table>";
        return model;
    }

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();

        LinkedList<SelectorRuleItem> selectorRuleItemList = new LinkedList<>();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("deleteAgentsDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("deleteAgentsAllow");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 1 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canDeleteAgents",
                "Удаление партнеров:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("addAgentsDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("addAgentsAllow");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canAddAgents",
                "Добавление партнеров:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        return listRulesItem;
    }
}
