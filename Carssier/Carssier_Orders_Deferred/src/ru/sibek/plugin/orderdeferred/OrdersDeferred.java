/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 19.03.2011 (C) Copyright by Zubanov Dmitry
 */
package ru.sibek.plugin.orderdeferred;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.document.Order;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.tree.Tree;
import org.ubo.tree.TreeBasic;
import org.ubo.tree.TreeFolder;
import org.ubo.tree.TreeLeaf;
import org.ubo.utils.Result;
import org.uui.component.HasRightPanels;
import org.uui.component.MenuItem;
import org.uui.db.DataBase;
import org.uui.db.event.DataBaseEvent;
import org.uui.db.event.DataBaseEventAdapter;
import org.uui.event.NavigatorChangeListener;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerPanel;
import org.uui.explorer.TreeExplorerPanel;
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

public class OrdersDeferred extends TreeExplorerPanel implements HasWorkPanelToolbars, HasRightPanels, HasRules {

    private DataBase dataBase;
    private ArrayList<RibbonButton> toolbarButtons;
    private CarssierCore core = CarssierCore.getInstance();
    private long selectedOrderId = -1;
    private MacTableModel macTableModel;
    private FilterPanel filterPanel;

    public OrdersDeferred(String sessionId, DataBase dataBase) {
        super(sessionId, dataBase, (Tree) CarssierCore.getInstance().getTreeOrdersDeffered().getObject());
        this.dataBase = dataBase;
        switchView(ExplorerPanel.PANEL_TABLE);
        initToolbar();

        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Дата оформления", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Заказ", String.class, true));
        mth.addHeaderColumn(new MacHeaderColumn("Получатель", String.class, true));
        mth.addHeaderColumn(new MacHeaderColumn("Сумма", Number.class, true));

        macTableModel = new MacTableModel(getSession(), true, new MacTableSummator(4));
        macTableModel.setCssClass("leftMacTable");
        macTableModel.setId("orderUnpaidTable");
        macTableModel.setHeader(mth);
        macTableModel.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("eventType", "push");
                    jsonObject.put("eventName", "showOrder");
                    jsonObject.put("className", Order.class.getName());
                    jsonObject.put("session", getSession());
                    Order o = (Order) macTableModel.getRow(evt.getJSONObject().getInt("row")).getValue();
                    jsonObject.put("dbid", o.getId());
                    selectedOrderId = o.getId();

                    if (evt.getJSONObject().getString("eventType").equals("click")) {
                        WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                                OrdersDeferredRightPanel.class.getName(),
                                jsonObject.toString());
                    }

                    if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                        //JSMediator.setContextMenu(getSession(), getLeafMenu());
                    }

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });

        dataBase.addDataBaseEventListener(new DataBaseEventAdapter() {
            @Override
            public void allEvent(DataBaseEvent evt) {
                try {
                    if (evt.getClassName().equals(Order.class.getName())) {
                        String panelName = MainFrame.getInstance().getSelectedOperationButton(OrdersDeferred.this.getSession());
                        if (!panelName.equals("Отложенные")) {
                            return;
                        }

                        macTableModel.getMacTableNavigator().resetPage();
                        JSMediator.showLockPanel(OrdersDeferred.this.getSession());
                        JSMediator.setWorkPanel(OrdersDeferred.this.getSession(), refreshAndGetHTMLModel());
                        JSMediator.hideLockPanel(OrdersDeferred.this.getSession());

                        if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                            setMacTableFilterVisible(true);
                        }
                    }
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        });

        macTableModel.addNavigatorChangeListener(new NavigatorChangeListener() {
            @Override
            public void event(int event) {
                try {
                    if (event == MacTableNavigator.FILTER) {
                        setMacTableFilterVisible(true);

                    } else if (event == MacTableNavigator.REFRESH) {
                        macTableModel.getMacTableNavigator().resetPage();

                        JSMediator.showLockPanel(getSession());
                        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                        JSMediator.hideLockPanel(getSession());

                        if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                            setMacTableFilterVisible(true);
                        }

                    } else if (event == MacTableNavigator.CALENDAR) {
                        //
                    } else {
                        JSMediator.showLockPanel(getSession());
                        JSMediator.setWorkPanel(getSession(), macTableModel.getModel());
                        JSMediator.hideLockPanel(getSession());
                        if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                            setMacTableFilterVisible(true);
                        }
                    }

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });

        macTableModel.setNavigatorShowingAlways(true);
        macTableModel.setNavigatorDateSelectorEnabled(false);
        filterPanel = new FilterPanel(getSession()) {
            @Override
            public void dateSearchEnable(boolean isEnabled) {
                try {
                    macTableModel.setNavigatorDateSelectorEnabled(isEnabled);

                    JSMediator.showLockPanel(getSession());
                    JSMediator.setWorkPanel(getSession(), macTableModel.getModel());
                    JSMediator.hideLockPanel(getSession());

                    if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                        setMacTableFilterVisible(true);
                    }

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        };
        macTableModel.getMacTableNavigator().setFilterPanel(filterPanel);
    }

    private void setMacTableFilterVisible(boolean isVisible) {
        try {
            if (isVisible) {
                LockSupport.parkNanos(1000000000); // !!!!!!!!!!!! HACK
                String filterModel = macTableModel.getMacTableNavigator().getFilterPanel().getModel();
                String identificator = macTableModel.getMacTableNavigator().getIdentificator();
                JSMediator.showFilterPanel(getSession(), filterModel, identificator);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

    }

    @Override
    public String treeTableWalker(final TreeFolder treeFolder) {
        WebKitEventBridge.getInstance().cleanMacTable(getSession(), macTableModel);

        ArrayList<MacTableRow> rows = new ArrayList<>();

        ArrayList<Order> orders = core.getOrdersByWorkStatus(Order.WORK_DEFERRED);
        for (Order order : orders) {

            if (filterPanel.isDateSearchEnable()) {
                if (order.getDate().getTime() > macTableModel.getMacTableNavigator().getDateEnd().getTime()) {
                    continue;
                }

                if (order.getDate().getTime() < macTableModel.getMacTableNavigator().getDateStart().getTime()) {
                    continue;
                }
            }

            if (filterPanel.getSum().doubleValue() > 0) {
                if (order.getTotalWithTotalDiscount().doubleValue() != filterPanel.getSum().doubleValue()) {
                    continue;
                }
            }

            if (!filterPanel.getCustomerName().equals("")) {
                if (order.getCustomer().getShortName().toLowerCase().indexOf(filterPanel.getCustomerName().toLowerCase()) == -1) {
                    continue;
                }
            }

            MacTableRow macTableRow = new MacTableRow();
            //ArrayList<MacTableCell> r = new ArrayList<>();
            macTableRow.addCell(new MacTableCell(getSession(), "<div align='center'>" + DateTime.getFormatedDate("dd.MM.yyyy HH:mm", order.getDate()) + "</div>", false));

            macTableRow.addCell(new MacTableCell(getSession(), "<img src='img/icons/order_payment24.png' "
                    + "style='vertical-align: middle;'/>&nbsp;"
                    + "№ " + order.getId() + ", " + order.getDescription(),
                    false));

            String parnterName = order.getCustomer() == null ? "" : order.getCustomer().getShortName();
            macTableRow.addCell(new MacTableCell(getSession(), parnterName, false));
            macTableRow.addCell(new MacTableCell(getSession(), order.getTotalWithTotalDiscount(), false));
            //macTableRow.setRowData(r);
            macTableRow.setValue(order);

            rows.add(macTableRow);
        }

        macTableModel.setData(rows);

        //setNavigatorPanel();

        htmlModel = macTableModel.getModel();
        return htmlModel;
    }

    private void initToolbar() {
        toolbarButtons = new ArrayList<>();

        RibbonButton rbDelete = RibbonButton.createDefaultRibbonButton(getSession(),
                "img/subbuttons/delete.png",
                "Удалить",
                "delete");
        rbDelete.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                removeOrder();
            }
        });

        RibbonButton rbMoveToWork = RibbonButton.createDefaultRibbonButton(getSession(),
                "img/subbuttons/move_to_work.png",
                "В работу",
                "moveToWork");
        rbMoveToWork.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                moveOrderToWork();
            }
        });

        toolbarButtons.add(rbMoveToWork);
        toolbarButtons.add(rbDelete);
    }

    private void moveOrderToWork() {
        if (selectedOrderId == -1) {
            PopupPanel popupPanel = new PopupPanel(getSession());
            popupPanel.setTitle("Подсказка");
            popupPanel.setPanel("Чтобы перенести заказ в работу необходимо его выделить");
            popupPanel.showPanel();
            return;
        }

        ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
            @Override
            public void pressed(int button) {
                if (button == ConfirmPanel.YES) {
                    Result r = core.modifyOrderWorkStatus(selectedOrderId, Order.WORK_INWORK);
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                        return;
                    }

                    r = core.getOrder(selectedOrderId);
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                        return;
                    }

                    r = core.moveOrder((Order) r.getObject(),
                            (TreeBasic) core.getTreeOrdersDeffered().getObject(),
                            (TreeBasic) core.getTreeOrdersInWork().getObject());
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                        return;
                    }

                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

                }
            }
        };
        confirmPanel.setTitle("Вопрос");
        confirmPanel.setMessage("Перенести заказ в <strong>«Работу»</strong>?");
        confirmPanel.showPanel();
    }

    private void removeOrder() {
        try {
            Result result = core.getRulesItemByKey(getSession(), "canDeleteDeferredOrder");
            if (result.isError()) {
                JSMediator.alert(getSession(), result.getReason());

            } else {
                boolean isAllow = false;
                RuleItem ruleItem = (RuleItem) result.getObject();
                for (SelectorRuleItem item : (LinkedList<SelectorRuleItem>) ruleItem.getValue()) {
                    if (item.isSelected()) {
                        if (item.getKey().equals("deleteOrdersDeferred")) {
                            if (item.isSelected()) {
                                isAllow = true;
                            }
                        }
                    }
                }

                if (isAllow) {
                    Result r = core.getOrderWorkStatus(selectedOrderId);
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());

                    } else if ((int) r.getObject() == Order.WORK_DEFERRED) {
                        ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                            @Override
                            public void pressed(int button) {
                                if (button == ConfirmPanel.YES) {
                                    Result r = core.removeOrderDeffered(selectedOrderId);
                                    if (r.isError()) {
                                        JSMediator.alert(getSession(), r.getReason());

                                    } else {
                                        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                    }
                                }
                            }
                        };
                        confirmPanel.setTitle("Вопрос");
                        confirmPanel.setMessage(
                                "Удалить заказ безвозвратно?");
                        confirmPanel.showPanel();
                    }
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, htmlModel, e);
        }
    }

    @Override
    public String getLeafMenu(final TreeLeaf leaf) {
        String model = "";

        MenuItem itemProperties = new MenuItem(getSession(), "img/subbuttons/document.png", "Редактировать заказ");
        itemProperties.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                        OrdersDeferredRightPanel.class.getName(),
                        "{eventType:push, dbid:" + leaf.getContainer().getId() + "}");
            }
        });
        model += itemProperties.getModel();

        MenuItem itemMoveToWork = new MenuItem(getSession(), "img/subbuttons/import.png", "В работу");
        itemMoveToWork.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                moveOrderToWork();
            }
        });
        if (isCanMoveToWork()) {
            model += itemMoveToWork.getModel();
        }

        MenuItem itemDelete = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить");
        itemDelete.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                removeOrder();
            }
        });

        if (isCanDelete()) {
            model += itemDelete.getModel();
        }

        return model;
    }

    @Override
    public String getFolderMenu(TreeFolder folder) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPanelMenu() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return toolbarButtons;
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

            if (jsonObject.getString("eventType").equals("showWorkPanel")) {
                super.switchView(ExplorerPanel.PANEL_TABLE);
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.hideNavigationPanel(getSession());
            }

            if (jsonObject.getString("eventType").equals("switchToEdit")) {
                super.switchView(ExplorerPanel.PANEL_ICON);
                super.replaceTree((Tree) CarssierCore.getInstance().getTreeGoodsAndService().getObject());

                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());
            }

            if (jsonObject.getString("eventType").equals("switchToView")) {
                super.replaceTree((Tree) CarssierCore.getInstance().getTreeOrdersDeffered().getObject());
                super.switchView(ExplorerPanel.PANEL_TABLE);
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.hideNavigationPanel(getSession());
            }

            if (jsonObject.getString("eventType").equals("rightclick")) {
                JSMediator.setContextMenu(getSession(), getPanelMenu());
            }

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Отложенные");
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.hideNavigationPanel(getSession());
            }

        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }

    @Override
    public void navigatorButtonAction(UIEvent evt) {
        try {
            TreeFolder tfb = (TreeFolder) dataBase.getObject(
                    evt.getJSONObject().getString("className"),
                    evt.getJSONObject().getLong("dbid"));
            currentTreeFolder = tfb;

            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

            setNavigatorPanel();
            JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());

        } catch (JSONException e) {
            Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
        }
    }

    @Override
    public String getIdentificator() {
        return OrdersDeferred.class.getName();
    }

    private boolean isCanDelete() {
        boolean isShowDelete = true;
        Result result = core.getRulesItemByKey(getSession(), "canDeleteDeferredOrder");
        if (result.isError()) {
            JSMediator.alert(getSession(), result.getReason());

        } else {
            RuleItem ruleItem = (RuleItem) result.getObject();
            for (SelectorRuleItem item : (LinkedList<SelectorRuleItem>) ruleItem.getValue()) {
                if (item.isSelected()) {
                    if (item.getKey().equals("deleteOrdersDeny")) {
                        isShowDelete = false;
                        break;
                    }
                }
            }
        }

        return isShowDelete;
    }

    private boolean isCanMoveToWork() {
        boolean isShowMoveToWork = true;
        Result result = core.getRulesItemByKey(getSession(), "canMoveOrderToWork");
        if (result.isError()) {
            JSMediator.alert(getSession(), result.getReason());

        } else {
            RuleItem ruleItem = (RuleItem) result.getObject();
            for (SelectorRuleItem item : (LinkedList<SelectorRuleItem>) ruleItem.getValue()) {
                if (item.isSelected()) {
                    if (item.getKey().equals("moveOrderToWorkDeny")) {
                        isShowMoveToWork = false;
                        break;
                    }
                }
            }
        }

        return isShowMoveToWork;
    }

    private String getSubOperationButtonModel() {
        int cols = getWorkpanelToolbars().size() / 2;
        int col = 0;
        String model = "<table class='subButtonsTable'><tr>";

        for (org.uui.ribbon.RibbonButton rb : getWorkpanelToolbars()) {
            if (col == cols) {
                model += "</tr><tr>";
            }

            if (rb.getActionName().equals("delete") && isCanDelete()) {
                model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
            }

            if (rb.getActionName().equals("moveToWork") && isCanMoveToWork()) {
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
        selectorRuleItem.setKey("deleteOrdersDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("deleteOrdersDeferred");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 1 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canDeleteDeferredOrder",
                "Удаление заказов:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("moveOrderToWorkDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("moveOrderToWork");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canMoveOrderToWork",
                "Перемещение заказов в работу:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        return listRulesItem;
    }
}
