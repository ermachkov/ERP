/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 19.03.2011 (C) Copyright by Zubanov Dmitry
 */
package ru.sibek.plugin.ordercomplete;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.ubo.utils.Result;
import org.uui.component.*;
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

public class OrdersComplete extends TreeExplorerPanel implements HasWorkPanelToolbars, HasRightPanels, HasRules {

    private ArrayList<RibbonButton> toolbarButtons;
    private CarssierCore core = CarssierCore.getInstance();
    private DataBase dataBase;
    private long selectedOrderId = -1;
    private MacTableModel macTableModel;
    private FilterPanel filterPanel;

    public OrdersComplete(String sessionId, DataBase dataBase) {
        super(sessionId, dataBase, (Tree) CarssierCore.getInstance().getTreeOrdersComplete().getObject());
        this.dataBase = dataBase;
        initToolbar();

        dataBase.addDataBaseEventListener(new DataBaseEventAdapter() {
            @Override
            public void allEvent(DataBaseEvent evt) {
                try {
                    if (evt.getClassName().equals(Order.class.getName())) {
                        String panelName = MainFrame.getInstance().getSelectedOperationButton(OrdersComplete.this.getSession());
                        if (!panelName.equals("Выполненные")) {
                            return;
                        }

                        macTableModel.getMacTableNavigator().resetPage();

                        JSMediator.showLockPanel(OrdersComplete.this.getSession());
                        JSMediator.setWorkPanel(OrdersComplete.this.getSession(), refreshAndGetHTMLModel());
                        JSMediator.hideLockPanel(OrdersComplete.this.getSession());

                        if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                            setMacTableFilterVisible(true);
                        }
                    }
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        });

        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Дата оформления", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Дата закрытия", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Заказ", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Получатель", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Сумма", Number.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Вид оплаты", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Статус оплаты", String.class, false));

        macTableModel = new MacTableModel(getSession(), true, new MacTableSummator(5));
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
                                OrdersCompleteRightPanel.class.getName(),
                                jsonObject.toString());
                    }

                    if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                        JSMediator.setContextMenu(getSession(), getLeafMenu());
                    }

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });

        macTableModel.addNavigatorChangeListener(new NavigatorChangeListener() {
            @Override
            public void event(int event) {
                try {
                    switch (event) {
                        case MacTableNavigator.FILTER:
                            setMacTableFilterVisible(true);
                            break;

                        case MacTableNavigator.REFRESH:
                            macTableModel.getMacTableNavigator().resetPage();
                            JSMediator.showLockPanel(getSession());
                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                            JSMediator.hideLockPanel(getSession());
                            if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                                setMacTableFilterVisible(true);
                            }
                            break;

                        case MacTableNavigator.CALENDAR:
                            break;

                        default:
                            JSMediator.showLockPanel(getSession());
                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
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
        macTableModel.setNavigatorDateSelectorEnabled(true);
        filterPanel = new FilterPanel(getSession());
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
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();

        LinkedList<SelectorRuleItem> selectorRuleItemList = new LinkedList<>();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("deleteCompliteOrdersDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("deleteCompliteOrdersAllow");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 1 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canDeleteCompliteOrder",
                "Удаление заказов:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        // DELETE ALL OR ONLY NOT PAID
        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("deleteComplitePaidOrderDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("deleteComplitePaidOrderAllow");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canDeleteComplitePaidOrder",
                "Удаление оплаченных заказов:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        return listRulesItem;
    }

    @Override
    public String refreshAndGetHTMLModel() {
        String model = "";
        if (currentTreeFolder instanceof KnowsId) {
            currentTreeFolder = (TreeFolder) db.getObject(
                    currentTreeFolder.getClass().getName(),
                    ((KnowsId) currentTreeFolder).getId());
        }

        if (currentTreeFolder.getSetTreeFolder().isEmpty()) {
            viewMode = ExplorerPanel.PANEL_TABLE;

        } else {
            viewMode = ExplorerPanel.PANEL_ICON;
        }

        TreeFolder treeFolder = currentTreeFolder;
        if (viewMode == ExplorerPanel.PANEL_ICON) {
            if (treeFolder == null) {
                treeFolder = getRoot();
                model = treeWalker(treeFolder);
            } else {
                model = treeWalker(treeFolder);
            }

        } else if (viewMode == ExplorerPanel.PANEL_TABLE) {
            if (treeFolder == null) {
                model = treeTableWalker(getRoot());
            } else {
                model = treeTableWalker(treeFolder);
            }

        } else if (viewMode == ExplorerPanel.PANEL_MAC) {
            if (treeFolder == null) {
                model = treeMacWalker(getRoot());
            } else {
                model = treeMacWalker(treeFolder);
            }
        }

        return model;
    }

    @Override
    public String treeTableWalker(final TreeFolder treeFolder) {
        WebKitEventBridge.getInstance().cleanMacTable(getSession(), macTableModel);

        ArrayList<MacTableRow> list = new ArrayList<>();
        ArrayList<Order> orderList = core.getOrdersComplete();

        for (Order order : orderList) {
            if (filterPanel.getPaidStatus() > 0) {
                if (filterPanel.getPaidStatus() != order.getPaidStatus()) {
                    continue;
                }
            }

            if (filterPanel.getPaidType() > 0) {
                if (filterPanel.getPaidType() != order.getPaidType()) {
                    continue;
                }
            }

            if (order.getDate() == null || order.getClosedDate() == null) {
                continue;
            }

            if (filterPanel.isFindByDateClosed()) {
                if (order.getClosedDate().getTime() > macTableModel.getMacTableNavigator().getDateEnd().getTime()) {
                    continue;
                }

                if (order.getClosedDate().getTime() < macTableModel.getMacTableNavigator().getDateStart().getTime()) {
                    continue;
                }

            } else {
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
            macTableRow.addCell(new MacTableCell(getSession(), DateTime.getFormatedDate("dd.MM.yyyy HH:mm", order.getDate()), false));
            macTableRow.addCell(new MacTableCell(getSession(), DateTime.getFormatedDate("dd.MM.yyyy HH:mm", order.getClosedDate()), false));
            macTableRow.addCell(new MacTableCell(getSession(), "<img src='img/icons/ordercomplete24.png' "
                    + "style='vertical-align: middle;'/>&nbsp;"
                    + "№ " + order.getId() + ", " + order.getDescription(),
                    false));

            String parnterName = order.getCustomer() == null ? "" : order.getCustomer().getShortName();
            macTableRow.addCell(new MacTableCell(getSession(), parnterName, false));
            macTableRow.addCell(new MacTableCell(getSession(), order.getTotalWithTotalDiscount(), false));

            // pay
            String sType = "";
            switch (order.getPaidType()) {
                case Order.BANK_ACCOUNT:
                    sType = "Банк";
                    break;

                case Order.CARD:
                    sType = "Карточка";
                    break;

                case Order.CASH:
                    sType = "Наличные";
                    break;

                case Order.LEFT:
                    sType = "Левый";
                    break;
            }
            macTableRow.addCell(new MacTableCell(getSession(), "<div align='center'>" + sType + "</div>", false));

            String sStatus = "";
            switch (order.getPaidStatus()) {
                case Order.UNPAID:
                    sStatus = "<span style='color:red;'>Не оплачен</span>";
                    break;

                case Order.PAID:
                    sStatus = "<span style='color:green;'>Оплачен</span>";
                    break;

                case Order.CHUNK_PAID:
                    sStatus = "<span style='color:orange;'>Оплачен частично</span>";
                    break;

                case Order.WAIT_PAY_BANK:
                    sStatus = "<span style='color:red;'>Неподтвержден (ч/з банк)</span>";
                    break;
            }
            macTableRow.addCell(new MacTableCell(getSession(), "<div align='center'>" + sStatus + "</div>", false));

            macTableRow.setValue(order);
            list.add(macTableRow);
        }

        macTableModel.setData(list);

        setNavigatorPanel();

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

        toolbarButtons.add(rbDelete);
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
            if (core.isRadioButtonRuleAllow(getSession(), "canDeleteComplitePaidOrder", "deleteComplitePaidOrderDeny")) {
                PopupPanel popupPanel = new PopupPanel(getSession());
                popupPanel.setTitle("Предупреждение");
                popupPanel.setPanel("У вас нет прав на удаление оплаченного заказа");
                popupPanel.showPanel();

            } else {
                final RadioButton rbReturnPayAll = new RadioButton(
                        getSession(),
                        "select", "Вернуть все деньги клиенту", false);
                final RadioButton rbReturnPayChunk = new RadioButton(
                        getSession(),
                        "select", "Вернуть часть денег клиенту", false);
                RadioButtonGroup radioButtonGroup =
                        new RadioButtonGroup(rbReturnPayAll, rbReturnPayChunk);

                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                    @Override
                    public void pressed(int button) {
                        if (rbReturnPayAll.isChecked()) {
                            Result r = core.modifyOrderPayStatus(selectedOrderId, Order.RETURN_ALL_PAID);
                            if (r.isError()) {
                                JSMediator.alert(getSession(), r.getReason());
                                return;
                            }

                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

                        } else if (rbReturnPayChunk.isChecked()) {
                            RemoveOrderPanel removeOrderPanel = new RemoveOrderPanel(getSession(), order, dataBase) {
                                @Override
                                public void close(LinkedHashSet<RemoveOrderPanel.GoodsOrderRow> goodsSet,
                                        LinkedHashSet<RemoveOrderPanel.ServiceOrderRow> serviceSet) {
                                    Result r = core.removeOrderCompliteChunkPayed(selectedOrderId, goodsSet, serviceSet);
                                    if (r.isError()) {
                                        JSMediator.alert(getSession(), r.getReason());

                                    } else {
                                        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
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
                        + rbReturnPayAll.getModel() + "<br/>"
                        + rbReturnPayChunk.getModel() + "<br/>");
                confirmPanel.showPanel();
            }

        } else {
            final RadioButton rbNothing = new RadioButton(
                    getSession(),
                    "select", "Ничего не делать, оставить все как есть", true);
            final RadioButton rbChunkPay = new RadioButton(
                    getSession(),
                    "select", "Клиент оплатит частично", false);
            final RadioButton rbNoPay = new RadioButton(
                    getSession(),
                    "select", "Клиент не оплатит ничего", false);
            RadioButtonGroup radioButtonGroup =
                    new RadioButtonGroup(rbNothing, rbChunkPay, rbNoPay);

            ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                @Override
                public void pressed(int button) {
                    Result r = core.getOrder(selectedOrderId);
                    if (r.isError()) {
                        return;
                    }

                    if (rbNothing.isChecked()) {
                        // What ???
                    } else if (rbChunkPay.isChecked()) {
                        RemoveOrderPanel removeOrderPanel = new RemoveOrderPanel(getSession(), (Order) r.getObject(), dataBase) {
                            @Override
                            public void close(LinkedHashSet<RemoveOrderPanel.GoodsOrderRow> goodsSet,
                                    LinkedHashSet<RemoveOrderPanel.ServiceOrderRow> serviceSet) {
                                Result r = core.removeOrderInWorkChunk(selectedOrderId, goodsSet, serviceSet);
                                if (r.isError()) {
                                    JSMediator.alert(getSession(), r.getReason());
                                } else {
                                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                }
                            }
                        };

                        JSMediator.showRemoveOrderPanel(getSession(), removeOrderPanel.getModel());

                    } else if (rbNoPay.isChecked()) {
                        r = core.removeOrderInWorkAll(selectedOrderId);
                        if (r.isError()) {
                            JSMediator.alert(getSession(), r.getReason());

                        } else {
                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                        }
                    }
                }
            };
            confirmPanel.setTitle("Вопрос");
            confirmPanel.setMessage("Вы "
                    + "действительно желаете удалить заказ?<br/>"
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
    public String getLeafMenu(final TreeLeaf leaf) {
        return "";
    }

    public String getLeafMenu() {
        String model = "";
        MenuItem itemProperties = new MenuItem(getSession(), "img/subbuttons/document.png", "Заказ");
        itemProperties.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                        OrdersCompleteRightPanel.class.getName(),
                        "{eventType:push, dbid:" + selectedOrderId + "}");
            }
        });
        model += itemProperties.getModel();

        MenuItem itemDelete = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить");
        itemDelete.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                removeOrder();
            }
        });

        if (core.isRadioButtonRuleAllow(getSession(), "canDeleteCompliteOrder", "deleteCompliteOrdersAllow")) {
            model += itemDelete.getModel();
        }

        return model;
    }

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return toolbarButtons;
    }

    @Override
    public String getIdentificator() {
        return OrdersComplete.class.getName();
    }

    @Override
    public void moveDown(TreeFolder treeFolder) {
        currentTreeFolder = treeFolder;
        setNavigatorPanel();
        try {
            JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
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
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
            }

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Выполненные");
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.hideNavigationPanel(getSession());

            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showWorkPanel")) {
                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
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

            if (rb.getActionName().equals("delete")
                    && core.isRadioButtonRuleAllow(getSession(), "canDeleteCompliteOrder", "deleteCompliteOrdersAllow")) {
                model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";

            } else {
                model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
            }

            col++;
        }

        model += "</tr></table>";

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
}
