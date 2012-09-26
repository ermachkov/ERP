/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.orderinwork;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.proxy.KnowsId;
import org.ubo.datetime.DateTime;
import org.ubo.document.Order;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.tree.Tree;
import org.ubo.tree.TreeFolder;
import org.ubo.tree.TreeLeaf;
import org.ubo.tree.TreeLeafBasic;
import org.ubo.utils.Result;
import org.uui.component.*;
import org.uui.db.DataBase;
import org.uui.db.event.DataBaseEvent;
import org.uui.db.event.DataBaseEventAdapter;
import org.uui.event.NavigatorChangeListener;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerFolder;
import org.uui.explorer.ExplorerLeaf;
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
import ru.sibek.core.ui.Callback;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.core.ui.PopupPanel;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class OrdersInWork extends TreeExplorerPanel implements HasWorkPanelToolbars, HasRightPanels, HasRules {

    private ArrayList<RibbonButton> toolbarButtons;
    private CarssierCore core = CarssierCore.getInstance();
    private DataBase dataBase = CarssierDataBase.getDataBase();
    private long selectedOrderId = -1;
    private boolean isSendEvent = false, isCheckBoxSelected = false;
    private MacTableModel macTableModel;
    private FilterPanel filterPanel;

    public OrdersInWork(String sessionId, DataBase dataBase) {
        super(sessionId, dataBase, (Tree) CarssierCore.getInstance().getTreeOrdersInWork().getObject());
        switchView(ExplorerPanel.PANEL_TABLE);
        initToolbar();

        dataBase.addDataBaseEventListener(new DataBaseEventAdapter() {
            @Override
            public void allEvent(DataBaseEvent evt) {
                if (evt.getClassName().equals(Order.class.getName())) {
                    Logger.getGlobal().log(Level.INFO, "allEvent {0}", Objects.toString(evt));
                    String panelName = MainFrame.getInstance().getSelectedOperationButton(OrdersInWork.this.getSession());
                    if (!panelName.equals("В работе")) {
                        return;
                    }

                    if (isSendEvent) {
                        macTableModel.getMacTableNavigator().resetPage();
                        JSMediator.showLockPanel(OrdersInWork.this.getSession());
                        JSMediator.setWorkPanel(OrdersInWork.this.getSession(), refreshAndGetHTMLModel());
                        JSMediator.hideLockPanel(OrdersInWork.this.getSession());
                        if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                            setMacTableFilterVisible(true);
                        }
                    }
                }
            }
        });

        macTableModel = new MacTableModel(getSession(), true, new MacTableSummator(4));
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Дата", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Заказ", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Получатель", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Сумма", Number.class, false));

        macTableModel.setCssClass("leftMacTable");
        macTableModel.setId("ordersInWorkTable");
        macTableModel.setHeader(mth);
        macTableModel.setNavigatorEnable(true);
        macTableModel.setNavigatorShowingAlways(true);
        macTableModel.setNavigatorDateSelectorEnabled(true);
        filterPanel = new FilterPanel(sessionId);
        macTableModel.getMacTableNavigator().setFilterPanel(filterPanel);

        macTableModel.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("eventType", "push");
                    jsonObject.put("eventName", "showOrder");
                    jsonObject.put("className", Order.class.getName());
                    jsonObject.put("session", evt.getJSONObject().getString("session"));
                    Order o = (Order) macTableModel.getRow(evt.getJSONObject().getInt("row")).getValue();
                    jsonObject.put("dbid", o.getId());
                    selectedOrderId = o.getId();

                    if (evt.getJSONObject().getString("eventType").equals("click")) {
                        WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                                OrdersInWorkRightPanel.class.getName(),
                                jsonObject.toString());
                    }

                    if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                        JSMediator.setContextMenu(getSession(), getLeafMenu());
                    }

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
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
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        });
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

        ArrayList<MacTableRow> list = new ArrayList<>();

        ArrayList<Order> orderList = core.getOrdersInWork();
        for (Order order : orderList) {
            if (order.getWorkStatus() != Order.WORK_INWORK) {
                continue;
            }

            if (order.getDate().getTime() > macTableModel.getMacTableNavigator().getDateEnd().getTime()) {
                continue;
            }

            if (order.getDate().getTime() < macTableModel.getMacTableNavigator().getDateStart().getTime()) {
                continue;
            }

            if (!filterPanel.getCustomer().equals("")) {
                if (order.getCustomer().getShortName().toLowerCase().indexOf(filterPanel.getCustomer().toLowerCase()) == -1) {
                    continue;
                }
            }

            if (filterPanel.getSum().doubleValue() != 0) {
                if (order.getTotalWithTotalDiscount().doubleValue() != filterPanel.getSum().doubleValue()) {
                    continue;
                }
            }

            MacTableRow macTableRow = new MacTableRow();
            ArrayList<MacTableCell> r = new ArrayList<>();
            r.add(new MacTableCell(getSession(), DateTime.getFormatedDate("dd.MM.yyyy HH:mm",
                    order.getDate()), false));
            r.add(new MacTableCell(getSession(), "<img src='img/icons/orderinwork24.png' "
                    + "style='vertical-align: middle;'/>&nbsp;"
                    + "№ " + order.getId() + ", " + order.getDescription(),
                    false));

            String parnterName = order.getCustomer() == null ? "" : order.getCustomer().getShortName();
            r.add(new MacTableCell(getSession(), parnterName, false));
            r.add(new MacTableCell(getSession(), order.getTotalWithTotalDiscount(), false));
            macTableRow.setRowData(r);
            macTableRow.setValue(order);
            list.add(macTableRow);
        }

        macTableModel.setData(list);
        setNavigatorPanel();

        selectedOrderId = -1;
        htmlModel = macTableModel.getModel();
        return htmlModel;
    }

    private void removeOrder() {
        if (selectedOrderId == -1) {
            PopupPanel popupPanel = new PopupPanel(getSession());
            popupPanel.setTitle("Подсказка");
            popupPanel.setPanel("Для того чтобы удалить заказ его необходимо выделить");
            popupPanel.showPanel();
            return;
        }

        Result r = core.getOrder(selectedOrderId);
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
        }

        final Order order = (Order) r.getObject();
        if (order.getPaidStatus() == Order.PAID || order.getPaidStatus() == Order.CHUNK_PAID) {
            if (core.isRadioButtonRuleAllow(getSession(), "canDeletePaidOrder", "deletePaidOrderDeny")) {
                PopupPanel popupPanel = new PopupPanel(getSession());
                popupPanel.setTitle("Предупреждение");
                popupPanel.setPanel("У вас нет прав на удаление оплаченного заказа");
                popupPanel.showPanel();

            } else {
                final RadioButton rbToComplite = new RadioButton(
                        getSession(), "select", "Забить и отправить в Выполненные", true);
                final RadioButton rbReturnPayAll = new RadioButton(
                        getSession(), "select", "Вернуть все деньги клиенту", false);
                final RadioButton rbReturnPayChunk = new RadioButton(
                        getSession(), "select", "Вернуть часть денег клиенту", false);
                RadioButtonGroup radioButtonGroup =
                        new RadioButtonGroup(rbToComplite, rbReturnPayAll, rbReturnPayChunk);

                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                    @Override
                    public void pressed(int button) {
                        if (rbToComplite.isChecked()) {
                            moveToComplite();
                            JSMediator.showLockPanel(getSession());
                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                            JSMediator.hideLockPanel(getSession());

                        } else if (rbReturnPayAll.isChecked()) {
                            //
                        } else if (rbReturnPayChunk.isChecked()) {
                            RemoveOrderPanel removeOrderPanel = new RemoveOrderPanel(getSession(), order, dataBase) {
                                @Override
                                public void close(LinkedHashSet<RemoveOrderPanel.GoodsOrderRow> goodsSet,
                                        LinkedHashSet<RemoveOrderPanel.ServiceOrderRow> serviceSet) {
                                    Result r = core.removeOrderInWorkChunkPayed(selectedOrderId, goodsSet, serviceSet);
                                    if (r.isError()) {
                                        JSMediator.alert(getSession(), r.getReason());
                                        JSMediator.showLockPanel(getSession());
                                        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                        JSMediator.hideLockPanel(getSession());

                                    } else {
                                        JSMediator.showLockPanel(getSession());
                                        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                        JSMediator.hideLockPanel(getSession());
                                    }
                                }
                            };

                            JSMediator.showRemoveOrderPanel(getSession(), removeOrderPanel.getModel());
                        }
                    }
                };

                confirmPanel.setTitle("Вопрос");
                confirmPanel.setMessage("Вы "
                        + "действительно желаете удалить заказ, "
                        + "который Оплачен?<br/>"
                        + "<b>Предупреждение!</b><br/>"
                        + "1. Не все товары можно вернуть на склад.<br/>"
                        + "2. Вам будет необходимо определить, "
                        + "будет ли клиент оплачивать товары которые "
                        + "можно вернуть на склад.<br/>"
                        + "3. Вам также будет необходимо определить "
                        + "за какие из уже оказанных услуг с клиента "
                        + "необходимо получить оплату.<br/>"
                        + "4. Все неоплаченные, но использованные "
                        + "товары/услуги будут списаны в убытки<br/>"
                        + "<hr/>"
                        + "<b>Для продолжения процедуры удаления выберите "
                        + "один из следующих вариантов:</b><br>"
                        + rbToComplite.getModel() + "<br/>"
                        + rbReturnPayAll.getModel() + "<br/>"
                        + rbReturnPayChunk.getModel() + "<br/>");
                confirmPanel.showPanel();
            }

        } else {
            final RadioButton rbNothing = new RadioButton(
                    getSession(), "select", "Ничего не делать, оставить все как есть", true);
            final RadioButton rbChunkPay = new RadioButton(
                    getSession(), "select", "Клиент оплатит частично", false);
            final RadioButton rbNoPay = new RadioButton(
                    getSession(), "select", "Клиент не оплатит ничего", false);
            RadioButtonGroup radioButtonGroup =
                    new RadioButtonGroup(rbNothing, rbChunkPay, rbNoPay);

            r = core.getOrderWorkStatus(selectedOrderId);
            String status = "находиться <b>«В работе»</b>";
            if ((int) r.getObject() == Order.WORK_COMPLETE) {
                status = "имеет статус <b>«Выполнен»</b>";
            }

            ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                @Override
                public void pressed(int button) {
                    if (rbNothing.isChecked()) {
                        //
                    } else if (rbChunkPay.isChecked()) {
                        RemoveOrderPanel removeOrderPanel = new RemoveOrderPanel(getSession(), order, dataBase) {
                            @Override
                            public void close(LinkedHashSet<RemoveOrderPanel.GoodsOrderRow> goodsSet,
                                    LinkedHashSet<RemoveOrderPanel.ServiceOrderRow> serviceSet) {
                                Result r = core.removeOrderInWorkChunk(selectedOrderId, goodsSet, serviceSet);
                                if (r.isError()) {
                                    JSMediator.alert(getSession(), r.getReason());

                                    JSMediator.showLockPanel(getSession());
                                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                    JSMediator.hideLockPanel(getSession());

                                } else {
                                    JSMediator.showLockPanel(getSession());
                                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                    JSMediator.hideLockPanel(getSession());
                                }
                            }
                        };

                        JSMediator.showRemoveOrderPanel(getSession(), removeOrderPanel.getModel());

                    } else if (rbNoPay.isChecked()) {
                        Result r = core.removeOrderInWorkAll(selectedOrderId);
                        if (r.isError()) {
                            JSMediator.alert(getSession(), r.getReason());
                            JSMediator.showLockPanel(getSession());
                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                            JSMediator.hideLockPanel(getSession());

                        } else {
                            JSMediator.showLockPanel(getSession());
                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                            JSMediator.hideLockPanel(getSession());
                        }
                    }
                }
            };

            confirmPanel.setTitle("Вопрос");
            confirmPanel.setMessage("Вы "
                    + "действительно желаете удалить заказ, "
                    + "который " + status + "?<br/>"
                    + "<b>Предупреждение!</b><br/>"
                    + "1. Не все товары можно вернуть на склад.<br/>"
                    + "2. Вам будет необходимо определить, "
                    + "будет ли клиент оплачивать товары которые "
                    + "можно вернуть на склад.<br/>"
                    + "3. Вам также будет необходимо определить "
                    + "за какие из уже оказанных услуг с клиента "
                    + "необходимо получить оплату.<br/>"
                    + "4. Все неоплаченные, но использованные "
                    + "товары/услуги будут списаны в убытки<br/>"
                    + "<hr/>"
                    + "<b>Для продолжения процедуры удаления выберите "
                    + "один из следующих вариантов:</b><br>"
                    + rbNothing.getModel() + "<br/>"
                    + rbChunkPay.getModel() + "<br/>"
                    + rbNoPay.getModel() + "<br/>");
            confirmPanel.showPanel();
        }
    }

    @Override
    public String getLeafMenu(TreeLeaf tl) {
        return "";
    }

    public String getLeafMenu() {
        String model = "";

        MenuItem itemProperties = new MenuItem(getSession(), "img/subbuttons/document.png", "Редактировать заказ");
        itemProperties.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                        OrdersInWorkRightPanel.class.getName(),
                        "{eventType:push, dbid:" + selectedOrderId + "}");
            }
        });
        model += itemProperties.getModel();

        if (!isCheckBoxSelected) {
            MenuItem itemAllToComplite = new MenuItem(getSession(), "img/subbuttons/import.png", "Все в выполненные");
            itemAllToComplite.addMenuEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    moveAllToComplite();
                }
            });
            if (core.isRadioButtonRuleAllow(getSession(), "canMoveAllOrderToComplite", "moveAllOrderToComplite")) {
                model += itemAllToComplite.getModel();
            }

            MenuItem itemToComplite = new MenuItem(getSession(), "img/subbuttons/import.png", "В выполненные");
            itemToComplite.addMenuEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    moveToComplite();
                }
            });
            if (core.isRadioButtonRuleAllow(getSession(), "canMoveOrderToComplite", "moveOrderToComplite")) {
                model += itemToComplite.getModel();
            }

            MenuItem itemToDefferd = new MenuItem(getSession(), "img/subbuttons/import.png", "В отложенные");
            itemToDefferd.addMenuEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    moveToDeferred();
                }
            });
            if (core.isRadioButtonRuleAllow(getSession(), "canMoveOrderToDeferred", "moveOrderToDeferred")) {
                model += itemToDefferd.getModel();
            }

        } else {
            MenuItem itemSelectAll = new MenuItem(getSession(), "img/subbuttons/import.png", "Выделить все");
            itemSelectAll.addMenuEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        JSMediator.setAllTableCheckBoxSelected(getSession(), "ordersInWorkTable", true);

                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, null, e);
                    }
                }
            });
            model += itemSelectAll.getModel();

            MenuItem itemDeselectAll = new MenuItem(getSession(), "img/subbuttons/import.png", "Снять выделение");
            itemDeselectAll.addMenuEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        JSMediator.setAllTableCheckBoxSelected(getSession(), "ordersInWorkTable", false);

                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, null, e);
                    }
                }
            });
            if (!macTableModel.getCheckedRows().isEmpty()) {
                model += itemDeselectAll.getModel();
            }

            MenuItem itemToComplite = new MenuItem(getSession(), "img/subbuttons/import.png", "В выполненные");
            itemToComplite.addMenuEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    moveAllSelectedToComplite();
                }
            });
            if (core.isRadioButtonRuleAllow(getSession(), "canMoveOrderToComplite", "moveOrderToComplite")) {
                if (!macTableModel.getCheckedRows().isEmpty()) {
                    model += itemToComplite.getModel();
                }
            }

            MenuItem itemToDefferd = new MenuItem(getSession(), "img/subbuttons/import.png", "В отложенные");
            itemToDefferd.addMenuEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    moveAllSelectedToDeferred();
                }
            });
            if (core.isRadioButtonRuleAllow(getSession(), "canMoveOrderToDeferred", "moveOrderToDeferred")) {
                if (!macTableModel.getCheckedRows().isEmpty()) {
                    model += itemToDefferd.getModel();
                }
            }
        }

        String s = isCheckBoxSelected ? "Снять выделение галочками" : "Выделение галочками";
        MenuItem itemMode = new MenuItem(getSession(), "img/subbuttons/settings.png", s);
        itemMode.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                changeTableModeSelector();
            }
        });
        model += itemMode.getModel();

        MenuItem itemDelete = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить");
        itemDelete.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                removeOrder();
            }
        });
        if (core.isRadioButtonRuleAllow(getSession(), "canDeleteInWorkOrder", "deleteOrdersInWork")) {
            model += itemDelete.getModel();
        }

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    private void changeTableModeSelector() {
        // TODO
        if (!isCheckBoxSelected) {
            macTableModel.setMode(MacTableModel.MODE_EDIT);
            isCheckBoxSelected = true;
            WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                    OrdersInWorkRightPanel.class.getName(),
                    "{eventType:\"push\", isPanelEnabled:\"false\"}");

        } else {
            macTableModel.setMode(MacTableModel.MODE_VIEW);
            isCheckBoxSelected = false;
            WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                    OrdersInWorkRightPanel.class.getName(),
                    "{eventType:\"push\", isPanelEnabled:\"true\"}");
        }

        try {
            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            JSMediator.alert(getSession(), e.toString());
        }
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
    public String getIdentificator() {
        return OrdersInWork.class.getName();
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

            if (jsonObject.getString("eventType").equals("callback")) {
                if (jsonObject.getString("requestMethod").equals("getSelectedWorkPanel")) {
                    String selectedPanel = jsonObject.getString("value");
                    if (selectedPanel.equals("В работе")) {
                        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                    }
                }
            }

            if (jsonObject.getString("eventType").equals("showWorkPanel")) {
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                selectedOrderId = -1;
            }

            if (jsonObject.getString("eventType").equals("switchToEdit")) {
                super.switchView(ExplorerPanel.PANEL_ICON);
                super.replaceTree((Tree) CarssierCore.getInstance().getTreeGoodsAndService().getObject());

                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());
            }

            if (jsonObject.getString("eventType").equals("switchToView")) {
                super.replaceTree((Tree) CarssierCore.getInstance().getTreeOrdersInWork().getObject());
                super.switchView(ExplorerPanel.PANEL_TABLE);
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.hideNavigationPanel(getSession());
                selectedOrderId = -1;
            }

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "В работе");
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.hideNavigationPanel(getSession());
                selectedOrderId = -1;

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

        try {
            for (org.uui.ribbon.RibbonButton rb : getWorkpanelToolbars()) {
                if (col == cols) {
                    model += "</tr><tr>";
                }

                if (core.isRadioButtonRuleAllow(getSession(), "canDeleteInWorkOrder", "deleteOrdersInWork")
                        && rb.getActionName().equals("deleteOrdersInWork")) {
                    model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
                }

                if (core.isRadioButtonRuleAllow(getSession(), "canMoveAllOrderToComplite", "moveAllOrderToComplite")
                        && rb.getActionName().equals("moveAllOrderToComplite")) {
                    model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
                }

                if (core.isRadioButtonRuleAllow(getSession(), "canMoveOrderToComplite", "moveOrderToComplite")
                        && rb.getActionName().equals("moveOrderToComplite")) {
                    model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
                }

                if (core.isRadioButtonRuleAllow(getSession(), "canMoveOrderToDeferred", "moveOrderToDeferred")
                        && rb.getActionName().equals("moveOrderToDeferred")) {
                    model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
                }

                col++;
            }

            model += "</tr></table>";

        } catch (Exception e) {
            //
        } finally {
            return model;
        }
    }

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();

        // DELETE
        LinkedList<SelectorRuleItem> selectorRuleItemList = new LinkedList<>();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("deleteOrdersDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("deleteOrdersInWork");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 1 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canDeleteInWorkOrder",
                "Удаление заказов:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        // DELETE ALL OR ONLY NOT PAID
        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("deletePaidOrderDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("deletePaidOrderAllow");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canDeletePaidOrder",
                "Удаление оплаченных заказов:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        // MOVE ALL TO COMPLITE
        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("moveAllOrderToCompliteDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("moveAllOrderToComplite");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canMoveAllOrderToComplite",
                "Перемещение всех заказов в Выполненные:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        // MOVE TO COMPLITE
        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("moveOrderToCompliteDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("moveOrderToComplite");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canMoveOrderToComplite",
                "Перемещение заказа в Выполненные:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        // MOVE TO DEFERRED
        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("moveOrderToDeferredDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("moveOrderToDeferred");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canMoveOrderToDeferred",
                "Перемещение заказа в Отложенные:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        // Allow edit order
        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("editOrderInWorkDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("editOrderInWorkAllow");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canEditOrderInWork",
                "Редактирование заказов, находящихся в работе:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        return listRulesItem;
    }

    private void initToolbar() {
        toolbarButtons = new ArrayList<>();
        RibbonButton rbDelete = RibbonButton.createDefaultRibbonButton(getSession(),
                "img/subbuttons/delete.png",
                "Удалить",
                "deleteOrdersInWork");
        rbDelete.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                removeOrder();
            }
        });

        RibbonButton rbMoveAllToComplite = RibbonButton.createDefaultRibbonButton(getSession(),
                "img/subbuttons/move_all_to_complite.png",
                "Всё в выполненные",
                "moveAllOrderToComplite");
        rbMoveAllToComplite.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                moveAllToComplite();
            }
        });

        RibbonButton rbMoveToComplite = RibbonButton.createDefaultRibbonButton(getSession(),
                "img/subbuttons/move_to_complite.png",
                "В выполненные",
                "moveOrderToComplite");
        rbMoveToComplite.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                moveToComplite();
            }
        });

        RibbonButton rbMoveToDeferred = RibbonButton.createDefaultRibbonButton(getSession(),
                "img/subbuttons/move_to_deferred.png",
                "В отложенные",
                "moveOrderToDeferred");
        rbMoveToDeferred.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                moveToDeferred();
            }
        });

        toolbarButtons.add(rbMoveAllToComplite);
        toolbarButtons.add(rbMoveToComplite);
        toolbarButtons.add(rbMoveToDeferred);
        toolbarButtons.add(rbDelete);
    }

    private void moveAllSelectedToDeferred() {
        ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
            @Override
            public void pressed(int button) {
                if (button == ConfirmPanel.YES) {
                    JSMediator.showLockPanel(getSession());

                    isSendEvent = false;
                    ArrayList<String> errors = new ArrayList<>();
                    ArrayList<MacTableRow> rows = macTableModel.getCheckedRows();
                    for (MacTableRow row : rows) {
                        Result r = core.modifyOrderWorkStatus(((Order) row.getValue()).getId(), Order.WORK_DEFERRED);
                        if (r.isError()) {
                            errors.add(r.getReason());
                        }
                    }

                    JSMediator.hideLockPanel(getSession());
                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

                    if (!errors.isEmpty()) {
                        PopupPanel popupPanel = new PopupPanel(getSession());
                        popupPanel.setTitle("Внимание!");
                        popupPanel.setPanel("При переносе зазказов "
                                + "возникли ошибки:<br/>" + errors);
                        popupPanel.showPanel();
                    }

                    isSendEvent = true;
                }
            }
        };
        confirmPanel.setTitle("Вопрос");
        confirmPanel.setMessage("Перенести отмеченные заказы в Отложенные?");
        confirmPanel.showPanel();
    }

    private void moveToDeferred() {
        if (selectedOrderId == -1) {
            PopupPanel popupPanel = new PopupPanel(getSession());
            popupPanel.setTitle("Подсказка");
            popupPanel.setPanel("Для переноса заказа в Отложенные "
                    + "его нужно выбрать.");
            popupPanel.showPanel();
            return;
        }

        ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
            @Override
            public void pressed(int button) {
                if (button == ConfirmPanel.YES) {
                    Result r = core.modifyOrderWorkStatus(selectedOrderId, Order.WORK_DEFERRED);
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                    }
                }
            }
        };
        confirmPanel.setTitle("Вопрос");
        confirmPanel.setMessage("Перенести заказ в Отложенные?");
        confirmPanel.showPanel();
    }

    private void moveToComplite() {
        try {
            if (selectedOrderId == -1) {
                PopupPanel popupPanel = new PopupPanel(getSession());
                popupPanel.setTitle("Подсказка");
                popupPanel.setPanel("Для переноса заказа в Выполненные "
                        + "его нужно выбрать.");
                popupPanel.showPanel();
                return;
            }

            final Result r = core.getOrder(selectedOrderId);
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());
                return;
            }

            ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                @Override
                public void pressed(int button) {
                    if (button == ConfirmPanel.YES) {
                        JSMediator.showLockPanel(getSession());
                        Result rr = core.closeOrder((Order) r.getObject());
                        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                        JSMediator.hideLockPanel(getSession());
                        if (rr.isError()) {
                            JSMediator.alert(getSession(), r.getReason());
                        }
                    }
                }
            };
            confirmPanel.setTitle("Вопрос");
            confirmPanel.setMessage("Перенести заказ в Выполненные?");
            confirmPanel.showPanel();

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
        }
    }

    private void moveAllSelectedToComplite() {
        try {
            ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                @Override
                public void pressed(int button) {
                    if (button == ConfirmPanel.YES) {
                        JSMediator.showLockPanel(getSession());

                        isSendEvent = false;
                        ArrayList<String> errors = new ArrayList<>();
                        ArrayList<MacTableRow> rows = macTableModel.getCheckedRows();
                        for (MacTableRow row : rows) {
                            Result r = core.closeOrder((Order) row.getValue());
                            if (r.isError()) {
                                errors.add(r.getReason());
                            }
                        }
                        isSendEvent = true;

                        JSMediator.hideLockPanel(getSession());
                        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

                        if (!errors.isEmpty()) {
                            PopupPanel popupPanel = new PopupPanel(getSession());
                            popupPanel.setTitle("Внимание!");
                            popupPanel.setPanel("При переносе зазказов "
                                    + "возникли ошибки:<br/>" + errors);
                            popupPanel.showPanel();
                        }
                    }
                }
            };
            confirmPanel.setTitle("Вопрос");
            confirmPanel.setMessage("Перенести отмеченные заказы из Работы в Выполненные?");
            confirmPanel.showPanel();

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private void moveAllToComplite() {
        try {
            ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                @Override
                public void pressed(int button) {
                    JSMediator.showLockPanel(getSession());

                    isSendEvent = false;
                    Result r = core.moveAllOrderInWorkToComplite(getSession());
                    isSendEvent = true;

                    JSMediator.hideLockPanel(getSession());

                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                    }
                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                }
            };
            confirmPanel.setTitle("Вопрос");
            confirmPanel.setMessage("Перенести все заказы из Работы в Выполненные?");
            confirmPanel.showPanel();

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    @Override
    public String treeWalker(TreeFolder treeFolder) {
        StringBuilder sb = new StringBuilder();

        Set<TreeFolder> folders = treeFolder.getSetTreeFolder();
        Set<TreeLeaf> leaves = treeFolder.getSetTreeLeaf();

        Set<ExplorerFolder> eFolders = new LinkedHashSet<>();
        Set<ExplorerLeaf> eLabels = new LinkedHashSet<>();

        for (TreeFolder tf : folders) {
            ExplorerFolder folder = new ExplorerFolder(getSession(), tf.getClass().getName(),
                    ((KnowsId) tf).getId(), tf.getName(), tf.getImageFileName());

            folder.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                            TreeFolder selectedTreeFolder = (TreeFolder) db.getObject(
                                    evt.getJSONObject().getString("className"),
                                    evt.getJSONObject().getLong("dbid"));

                            JSMediator.setContextMenu(getSession(), getFolderMenu(selectedTreeFolder));
                        }

                        if (evt.getJSONObject().getString("eventType").equals("click")) {
                            TreeFolder selectedTreeFolder = (TreeFolder) db.getObject(
                                    evt.getJSONObject().getString("className"),
                                    evt.getJSONObject().getLong("dbid"));

                            moveDown(selectedTreeFolder);

                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                        }

                        if (evt.getJSONObject().getString("eventType").equals("drop")) {
                            cutAndPaste(evt.getJSONObject());
                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                        }

                        if (evt.getJSONObject().getString("eventType").equals("keypress")) {
                            rename(evt.getJSONObject());
                        }
                    } catch (JSONException ex) {
                        Logger.getGlobal().log(Level.WARNING, evt.toString(), ex);
                    }
                }
            });
            eFolders.add(folder);
        }

        for (TreeLeaf tl : leaves) {
            boolean isContinue = false;
            if ((this.filterClasses != null)
                    && (this.isFilterEnable)) {
                String contClass = tl.getContainer().getClassName();
                for (String cls : this.filterClasses) {
                    if (cls.equals(contClass)) {
                        isContinue = true;
                    }
                }

            }

            if (isContinue) {
                continue;
            }
            String img = "img/icons/service.png";
            if (!tl.getImageFileName().trim().equals("")) {
                img = tl.getImageFileName();
            }

            String info = core.getPriceCountForSales((TreeLeafBasic) tl);
            ExplorerLeaf label = new ExplorerLeaf(getSession(), tl.getClass().getName(), ((KnowsId) tl).getId(), tl.getName(), img);

            label.setExtraText(info);
            label.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                            TreeLeaf selectedTreeLeaf = (TreeLeaf) db.getObject(evt.getJSONObject().getString("className"), evt.getJSONObject().getLong("dbid"));

                            JSMediator.setContextMenu(getSession(), getLeafMenu(selectedTreeLeaf));
                        }

                        if (evt.getJSONObject().getString("eventType").equals("keypress")) {
                            rename(evt.getJSONObject());
                        }
                    } catch (JSONException e) {
                        Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
                    }
                }
            });
            eLabels.add(label);
        }

        Logger.getGlobal().log(Level.INFO, "htmlModel = {0}", this.htmlModel);

        for (ExplorerFolder ef : eFolders) {
            sb.append(ef.getModel());
        }

        for (ExplorerLeaf el : eLabels) {
            sb.append(el.getModel());
        }

        setNavigatorPanel();

        return sb.toString();
    }
}
