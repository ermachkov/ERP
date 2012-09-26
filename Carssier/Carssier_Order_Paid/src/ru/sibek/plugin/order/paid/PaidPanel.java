/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.order.paid;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.Callback;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.core.ui.PopupPanel;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class PaidPanel extends TreeExplorerPanel implements HasRightPanels {

    private DataBase dataBase;
    private CarssierCore core = CarssierCore.getInstance();
    private RemoveOrderPanel removeOrderPanel;
    private MacTableModel macTableModel;
    private FilterPanel filterPanel;

    public PaidPanel(String sessionId, DataBase dataBase) {
        super(sessionId, dataBase, (Tree) CarssierCore.getInstance().getTreeOrdersUnpayed().getObject());
        this.dataBase = dataBase;

        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Дата оформления", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Дата оплаты", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Описание", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Заказчик", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Сумма", Number.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Статус", String.class, false));

        macTableModel = new MacTableModel(getSession(), true, new MacTableSummator(5));
        macTableModel.setCssClass("leftMacTable");
        macTableModel.setId("orderPaidTable");
        macTableModel.setHeader(mth);

        macTableModel.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    Order order = (Order) macTableModel.getRow(macTableModel.getSelectedRow()).getValue();
                    if (evt.getJSONObject().getString("eventType").equals("click")) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("session", getSession());
                        jsonObject.put("eventType", "push");
                        jsonObject.put("action", "showOrder");
                        jsonObject.put("className", Order.class.getName());
                        jsonObject.put("dbid", order.getId());
                        WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                                PaidRightPanel.class.getName(), jsonObject.toString());
                    }

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });
        switchView(ExplorerPanel.PANEL_TABLE);

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
        macTableModel.setNavigatorDateSelectorEnabled(true);
        filterPanel = new FilterPanel(getSession());
        macTableModel.getMacTableNavigator().setFilterPanel(filterPanel);

        dataBase.addDataBaseEventListener(new DataBaseEventAdapter() {
            @Override
            public void allEvent(DataBaseEvent evt) {
                try {
                    if (evt.getClassName().equals(Order.class.getName())) {
                        String panelName = MainFrame.getInstance().getSelectedOperationButton(PaidPanel.this.getSession());
                        if (!panelName.equals("Оплачено")) {
                            return;
                        }
                        
                        macTableModel.getMacTableNavigator().resetPage();
                        JSMediator.showLockPanel(PaidPanel.this.getSession());
                        JSMediator.setWorkPanel(PaidPanel.this.getSession(), refreshAndGetHTMLModel());
                        JSMediator.hideLockPanel(PaidPanel.this.getSession());
                        if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                            setMacTableFilterVisible(true);
                        }
                    }
                } catch (Exception e) {
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
        ArrayList<Order> orders = core.getOrdersPaid();

        for (Order order : orders) {

            if (filterPanel.isFindByDatePay()) {
                if (order.getPaidDate() == null) {
                    continue;
                }

                if (order.getPaidDate().getTime() > macTableModel.getMacTableNavigator().getDateEnd().getTime()) {
                    continue;
                }

                if (order.getPaidDate().getTime() < macTableModel.getMacTableNavigator().getDateStart().getTime()) {
                    continue;
                }

            } else {
                if (order.getDate() == null) {
                    continue;
                }

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

            if (!filterPanel.getDescription().equals("")) {
                if (order.getDescription().toLowerCase().indexOf(filterPanel.getDescription().toLowerCase()) == -1) {
                    continue;
                }
            }

            if (filterPanel.getPaidType() > 0) {
                if (order.getPaidType() != filterPanel.getPaidType()) {
                    continue;
                }
            }

            MacTableRow macTableRow = new MacTableRow();
            ArrayList<MacTableCell> r = new ArrayList<>();
            r.add(new MacTableCell(getSession(), DateTime.getFormatedDate("dd.MM.yyyy HH:mm", order.getDate()), false));
            r.add(new MacTableCell(getSession(), DateTime.getFormatedDate("dd.MM.yyyy HH:mm", order.getPaidDate()), false));
            r.add(new MacTableCell(getSession(), "<img src='img/icons/order_payment24.png' "
                    + "style='vertical-align: middle;'/>&nbsp;"
                    + "№ " + order.getId() + ", " + order.getDescription(),
                    false));

            String parnterName = order.getCustomer() == null ? "" : order.getCustomer().getShortName();
            r.add(new MacTableCell(getSession(), parnterName, false));
            r.add(new MacTableCell(getSession(), order.getTotalWithTotalDiscount(), false));

            String status = "";
            if (order.getWorkStatus() == Order.WORK_INWORK) {
                status = "<img src='img/subbuttons/move_to_work.png'>";
            }

            if (order.getWorkStatus() == Order.WORK_DEFERRED) {
                status = "<img src='img/subbuttons/move_to_deferred.png'>";
            }

            if (order.getWorkStatus() == Order.WORK_COMPLETE) {
                status = "<img src='img/subbuttons/move_to_complite.png'>";
            }

            if (order.getWorkStatus() == Order.WORK_CHUNKED_RETURN) {
                status = "<img src='img/subbuttons/delete.png'><img src='img/subbuttons/money_out.png'>";
            }

            if (order.getPaidStatus() == Order.RETURN_ALL_PAID) {
                status = "<img src='img/subbuttons/money_out.png'>";
            }

            if (order.getPaidStatus() == Order.WAIT_PAY_BANK) {
                status += "<img src='img/subbuttons/unpaid.png'>";
            }

            r.add(new MacTableCell(getSession(), "<div align='center'>" + status + "</div>", false));
            macTableRow.setRowData(r);
            macTableRow.setValue(order);
            list.add(macTableRow);
        }


        macTableModel.setData(list);
        setNavigatorPanel();
        htmlModel = macTableModel.getModel();
        return htmlModel;
    }

    private void removeOrder() {
        try {
            Result result = core.getRulesItemByKey(getSession(), "canDeleteUnpaidOrder");
            if (result.isError()) {
                JSMediator.alert(getSession(), result.getReason());
            } else {
                RuleItem ruleItem = (RuleItem) result.getObject();
                for (SelectorRuleItem item : (LinkedList<SelectorRuleItem>) ruleItem.getValue()) {
                    if (item.isSelected()) {
                        if (item.getKey().equals("onlyDefferedOrders")) {
                            Result r = core.getOrderWorkStatus(macTableModel.getSelectedRow());
                            if (r.isError()) {
                                JSMediator.alert(getSession(), r.getReason());

                            } else {
                                if ((int) r.getObject() == Order.WORK_DEFERRED) {
                                    ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                                        @Override
                                        public void pressed(int button) {
                                            if (button == ConfirmPanel.YES) {
                                                Result r = core.removeOrderDeffered(macTableModel.getSelectedRow());
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
                                            "<span style='color:red;'>Удалить заказ безвозвратно?</span>");
                                    confirmPanel.showPanel();

                                } else {
                                    PopupPanel popupPanel = new PopupPanel(getSession());
                                    popupPanel.setTitle("<span style='color:red;'>Предупреждение!<span style='color:red;'>");
                                    popupPanel.setPanel("У вас нет "
                                            + "права на удаление заказа который "
                                            + "находиться в работе");
                                    popupPanel.showPanel("getUICore().showPopupPanel");
                                }
                            }
                        }

                        if (item.getKey().equals("anyOrders")) {
                            Result r = core.getOrderWorkStatus(macTableModel.getSelectedRow());
                            if (r.isError()) {
                                JSMediator.alert(getSession(), r.getReason());
                            } else {
                                if ((int) r.getObject() == Order.WORK_DEFERRED) {
                                    ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                                        @Override
                                        public void pressed(int button) {
                                            if (button == ConfirmPanel.YES) {
                                                Result r = core.removeOrderDeffered(macTableModel.getSelectedRow());
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
                                            "<span style='color:red;'>"
                                            + "Удалить заказ безвозвратно?"
                                            + "</span>");
                                    confirmPanel.showPanel();

                                } else if ((int) r.getObject() == Order.WORK_INWORK
                                        || (int) r.getObject() == Order.WORK_COMPLETE) {

                                    RadioButton rbNothing = new RadioButton(
                                            getSession(), "select", "Ничего не делать, оставить все как есть", true);
                                    RadioButton rbAllPay = new RadioButton(
                                            getSession(), "select", "Клиент заплатит за все", false);
                                    RadioButton rbChunkPay = new RadioButton(
                                            getSession(), "select", "Клиент оплатит частично", false);
                                    RadioButton rbNoPay = new RadioButton(
                                            getSession(), "select", "Клиент не оплатит ничего", false);
                                    RadioButtonGroup radioButtonGroup =
                                            new RadioButtonGroup(rbNothing, rbAllPay, rbChunkPay, rbNoPay);

                                    String status = "имеет статус <b>«В работе»</b>";
                                    if ((int) r.getObject() == Order.WORK_COMPLETE) {
                                        status = "имеет статус <b>«Выполнен»</b>";
                                    }

                                    PopupPanel popupPanel = new PopupPanel(getSession());
                                    popupPanel.setTitle("Вопрос");
                                    popupPanel.setPanel("Вы "
                                            + "действительно желаете удалить заказ, "
                                            + "который " + status + "?<br/>"
                                            + "<b>Предупреждение!</b><br/>"
                                            + "1. Не все товары можно вернуть на склад.<br/>"
                                            + "2. Вам будет необходимо определить, "
                                            + "будет ли клиент оплачивать товары, которые "
                                            + "можно вернуть на склад.<br/>"
                                            + "3. Вам также будет необходимо определить, "
                                            + "за какие из уже оказанных услуг с клиента "
                                            + "необходимо получить оплату.<br/>"
                                            + "4. Все не оплаченные, но использованные "
                                            + "товары/услуги будут списаны в убытки<br/>"
                                            + "<hr/>"
                                            + "<b>Для продолжения процедуры удаления выберите "
                                            + "один из следующих вариантов:</b><br>"
                                            + rbNothing.getModel() + "<br/>"
                                            + rbAllPay.getModel() + "<br/>"
                                            + rbChunkPay.getModel() + "<br/>"
                                            + rbNoPay.getModel() + "<br/>");
                                    popupPanel.showPanel(600, -1);

                                    r = core.getOrder(macTableModel.getSelectedRow());
                                    if (r.isError()) {
                                        return;
                                    }

                                    if (rbNothing.isChecked()) {
                                        return;

                                    } else if (rbAllPay.isChecked()) {
                                        WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                                                PaidRightPanel.class.getName(),
                                                "{eventType:push, session:" + getSession() + ", action:pay, "
                                                + "orderId:" + macTableModel.getSelectedRow() + "}");

                                    } else if (rbChunkPay.isChecked()) {
                                        removeOrderPanel = new RemoveOrderPanel(getSession(), (Order) r.getObject(), dataBase) {
                                            @Override
                                            public void close(LinkedHashSet<GoodsOrderRow> goodsSet,
                                                    LinkedHashSet<ServiceOrderRow> serviceSet) {
                                                Result r = core.removeOrderInWorkChunk(
                                                        macTableModel.getSelectedRow(), goodsSet, serviceSet);
                                                try {
                                                    if (r.isError()) {
                                                        JSMediator.alert(getSession(), r.getReason());
                                                    } else {
                                                        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                                    }

                                                } catch (Exception e) {
                                                    Logger.getGlobal().log(Level.WARNING, null, e);
                                                }
                                            }
                                        };

                                        JSMediator.showRemoveOrderPanel(getSession(), removeOrderPanel.getModel());

                                    } else if (rbNoPay.isChecked()) {
                                        r = core.removeOrderInWorkAll(macTableModel.getSelectedRow());
                                        if (r.isError()) {
                                            JSMediator.alert(getSession(), r.getReason());

                                        } else {
                                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                        }

                                    }
                                } else if ((int) r.getObject() == Order.WORK_CHUNKED_RETURN) {
                                    PopupPanel popupPanel = new PopupPanel(getSession());
                                    popupPanel.setTitle("Предупреждение!");
                                    String panel = "<span style='color:red;'>"
                                            + "Этот заказ уже помечен как "
                                            + "удаляемый с частичной оплатой реализованных "
                                            + "клиенту товаров/услуг. "
                                            + "Вам осталось лишь оплатить этот заказ."
                                            + "</span>";
                                    popupPanel.setPanel(panel);
                                    popupPanel.showPanel();
                                }
                            }
                        }
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
        MenuItem itemPay = new MenuItem(getSession(), "img/subbuttons/coins.png", "Оплатить");
        itemPay.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                        PaidRightPanel.class.getName(),
                        "{eventType:push, session:" + getSession() + ", action:showOrder, "
                        + "dbid:" + leaf.getContainer().getId() + "}");
            }
        });
        model += itemPay.getModel();

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

    private boolean isCanDelete() {
        boolean isShow = true;
        Result result = core.getRulesItemByKey(getSession(), "canDeletePaidOrder");
        if (result.isError()) {
            JSMediator.alert(getSession(), result.getReason());

        } else {
            RuleItem ruleItem = (RuleItem) result.getObject();
            for (SelectorRuleItem item : (LinkedList<SelectorRuleItem>) ruleItem.getValue()) {
                if (item.isSelected()) {
                    if (item.getKey().equals("deletePaidOrdersDeny")) {
                        isShow = false;
                        break;
                    }
                }
            }
        }

        return isShow;
    }

    @Override
    public String getIdentificator() {
        return PaidPanel.class.getName();
    }

    @Override
    public void moveDown(TreeFolder treeFolder) {
        currentTreeFolder = treeFolder;
        setNavigatorPanel();
        JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());
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
                super.switchView(ExplorerPanel.PANEL_TABLE);
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.hideNavigationPanel(getSession());
            }

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Оплачено");
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
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
}
